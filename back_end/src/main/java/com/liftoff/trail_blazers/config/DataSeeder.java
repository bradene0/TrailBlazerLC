package com.liftoff.trail_blazers.config;

import com.liftoff.trail_blazers.data.FaunaRepository;
import com.liftoff.trail_blazers.data.GeolocationsRepository;
import com.liftoff.trail_blazers.data.PlantsRepository;
import com.liftoff.trail_blazers.model.Fauna;
import com.liftoff.trail_blazers.model.Geolocations;
import com.liftoff.trail_blazers.model.Plants;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final FaunaRepository faunaRepository;
    private final PlantsRepository plantsRepository;
    private final GeolocationsRepository geolocationsRepository;

    @Value("${data.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${data.seed.refresh:false}")
    private boolean refreshData;

    @Value("${data.seed.base-path:../databases}")
    private String seedBasePath;

    public DataSeeder(FaunaRepository faunaRepository,
                      PlantsRepository plantsRepository,
                      GeolocationsRepository geolocationsRepository) {
        this.faunaRepository = faunaRepository;
        this.plantsRepository = plantsRepository;
        this.geolocationsRepository = geolocationsRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!seedEnabled) {
            log.info("Data seeding disabled (data.seed.enabled=false)");
            return;
        }

        Path basePath = Paths.get(seedBasePath).toAbsolutePath().normalize();
        log.info("Seeding database from CSV files under: {}", basePath);

        int faunaInserted = 0;
        int plantsInserted = 0;
        int parksInserted = 0;

        try {
            faunaInserted = seedFauna(basePath.resolve("animal_information/animals_mo_state_parks.csv"));
        } catch (Exception e) {
            log.error("Failed to seed fauna; continuing startup.", e);
        }

        try {
            plantsInserted = seedPlants(basePath.resolve("plant_information/plants_mo_state_parks.csv"));
        } catch (Exception e) {
            log.error("Failed to seed plants; continuing startup.", e);
        }

        try {
            parksInserted = seedParks(basePath.resolve("park_locations/MO_State_Park.csv"));
        } catch (Exception e) {
            log.error("Failed to seed parks; continuing startup.", e);
        }

        log.info("Seeded data - fauna: {}, plants: {}, parks: {}", faunaInserted, plantsInserted, parksInserted);
    }

    private int seedFauna(Path csvPath) throws IOException {
        if (!shouldSeed(faunaRepository.count())) {
            log.info("Fauna already present; skipping fauna seeding.");
            return 0;
        }

        deleteExistingIfNeeded(faunaRepository);

        List<Fauna> faunaBatch = new ArrayList<>();
        for (CSVRecord record : parseCsv(csvPath)) {
            Fauna fauna = new Fauna();
            fauna.setScientificName(valueOrNull(record, "Scientific Name"));
            fauna.setCommonName(valueOrNull(record, "Common Name"));
            fauna.setCurrentDistribution(valueOrNull(record, "CurrentDistribution"));
            fauna.setFamily(valueOrNull(record, "Family"));
            fauna.setStatus(valueOrNull(record, "Federal Listing Status"));
            fauna.setImage(valueOrNull(record, "image"));
            fauna.setPhotoCredit(valueOrNull(record, "photo_credit"));
            faunaBatch.add(fauna);
        }

        faunaRepository.saveAll(faunaBatch);
        return faunaBatch.size();
    }

    private int seedPlants(Path csvPath) throws IOException {
        if (!shouldSeed(plantsRepository.count())) {
            log.info("Plants already present; skipping plant seeding.");
            return 0;
        }

        deleteExistingIfNeeded(plantsRepository);

        List<Plants> plantBatch = new ArrayList<>();
        for (CSVRecord record : parseCsv(csvPath)) {
            Plants plant = new Plants();
            plant.setCommonName(valueOrNull(record, "common_name"));
            plant.setCurrentDistribution(valueOrNull(record, "current_distribution"));
            plant.setFamily(valueOrNull(record, "family"));
            plant.setFederalListingStatus(valueOrNull(record, "federal_listing_status"));
            plant.setScientificName(valueOrNull(record, "scientific_name"));
            plant.setImage(valueOrNull(record, "image"));
            plant.setPhotoCredit(valueOrNull(record, "photo_credit"));
            plantBatch.add(plant);
        }

        plantsRepository.saveAll(plantBatch);
        return plantBatch.size();
    }

    private int seedParks(Path csvPath) throws IOException {
        if (!shouldSeed(geolocationsRepository.count())) {
            log.info("Geolocations already present; skipping park seeding.");
            return 0;
        }

        deleteExistingIfNeeded(geolocationsRepository);

        List<Geolocations> parks = new ArrayList<>();
        for (CSVRecord record : parseCsv(csvPath)) {
            Geolocations geolocation = new Geolocations();
            geolocation.setName(valueOrNull(record, "name"));
            geolocation.setLatitude(parseDouble(record, "latitude"));
            geolocation.setLongitude(parseDouble(record, "longitude"));
            geolocation.setPark_type(valueOrNull(record, "PARK_TYPE"));
            geolocation.setUrl(valueOrNull(record, "URL"));
            geolocation.setShort_name(valueOrNull(record, "short_name"));
            parks.add(geolocation);
        }

        geolocationsRepository.saveAll(parks);
        return parks.size();
    }

    private Iterable<CSVRecord> parseCsv(Path csvPath) throws IOException {
        if (!Files.exists(csvPath)) {
            throw new IOException("CSV file not found: " + csvPath);
        }

        try (Reader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {
            return parser.getRecords();
        }
    }

    private boolean shouldSeed(long existingCount) {
        return refreshData || existingCount == 0;
    }

    private void deleteExistingIfNeeded(JpaRepository<?, ?> repository) {
        if (refreshData && repository.count() > 0) {
            repository.deleteAllInBatch();
        }
    }

    private String valueOrNull(CSVRecord record, String columnName) {
        String value = record.get(columnName);
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    private double parseDouble(CSVRecord record, String columnName) {
        String value = valueOrNull(record, columnName);
        if (value == null) {
            throw new IllegalArgumentException("Missing numeric value for column: " + columnName);
        }
        return Double.parseDouble(value);
    }
}
