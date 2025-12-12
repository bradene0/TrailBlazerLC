package com.liftoff.trail_blazers.config;

import com.liftoff.trail_blazers.data.FaunaRepository;
import com.liftoff.trail_blazers.data.GeolocationsRepository;
import com.liftoff.trail_blazers.data.PlantsRepository;
import com.liftoff.trail_blazers.model.Fauna;
import com.liftoff.trail_blazers.model.Geolocations;
import com.liftoff.trail_blazers.model.Plants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        int faunaInserted = seedFauna(basePath.resolve("animal_information/animals_mo_state_parks.csv"));
        int plantsInserted = seedPlants(basePath.resolve("plant_information/plants_mo_state_parks.csv"));
        int parksInserted = seedParks(basePath.resolve("park_locations/MO_State_Park.csv"));

        log.info("Seeded data - fauna: {}, plants: {}, parks: {}", faunaInserted, plantsInserted, parksInserted);
    }

    private int seedFauna(Path csvPath) throws IOException {
        if (!shouldSeed(faunaRepository.count())) {
            log.info("Fauna already present; skipping fauna seeding.");
            return 0;
        }

        deleteExistingIfNeeded(faunaRepository);

        List<Fauna> faunaBatch = new ArrayList<>();
        for (Map<String, String> record : parseCsv(csvPath)) {
            Fauna fauna = new Fauna();
            fauna.setScientificName(valueOrNull(record.get("Scientific Name")));
            fauna.setCommonName(valueOrNull(record.get("Common Name")));
            fauna.setCurrentDistribution(valueOrNull(record.get("CurrentDistribution")));
            fauna.setFamily(valueOrNull(record.get("Family")));
            fauna.setStatus(valueOrNull(record.get("Federal Listing Status")));
            fauna.setImage(valueOrNull(record.get("image")));
            fauna.setPhotoCredit(valueOrNull(record.get("photo_credit")));
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
        for (Map<String, String> record : parseCsv(csvPath)) {
            Plants plant = new Plants();
            plant.setCommonName(valueOrNull(record.get("common_name")));
            plant.setCurrentDistribution(valueOrNull(record.get("current_distribution")));
            plant.setFamily(valueOrNull(record.get("family")));
            plant.setFederalListingStatus(valueOrNull(record.get("federal_listing_status")));
            plant.setScientificName(valueOrNull(record.get("scientific_name")));
            plant.setImage(valueOrNull(record.get("image")));
            plant.setPhotoCredit(valueOrNull(record.get("photo_credit")));
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
        for (Map<String, String> record : parseCsv(csvPath)) {
            Geolocations geolocation = new Geolocations();
            geolocation.setName(valueOrNull(record.get("name")));

            Double latitude = parseDouble(record.get("latitude"), "latitude", record);
            Double longitude = parseDouble(record.get("longitude"), "longitude", record);
            if (latitude == null || longitude == null) {
                log.warn("Skipping park row due to missing coordinates: {}", record);
                continue;
            }

            geolocation.setLatitude(latitude);
            geolocation.setLongitude(longitude);
            geolocation.setPark_type(valueOrNull(record.get("PARK_TYPE")));
            geolocation.setUrl(valueOrNull(record.get("URL")));
            geolocation.setShort_name(valueOrNull(record.get("short_name")));
            parks.add(geolocation);
        }

        geolocationsRepository.saveAll(parks);
        return parks.size();
    }

    private boolean shouldSeed(long existingCount) {
        return refreshData || existingCount == 0;
    }

    private void deleteExistingIfNeeded(JpaRepository<?, ?> repository) {
        if (refreshData && repository.count() > 0) {
            repository.deleteAllInBatch();
        }
    }

    private List<Map<String, String>> parseCsv(Path csvPath) throws IOException {
        if (!Files.exists(csvPath)) {
            throw new IOException("CSV file not found: " + csvPath);
        }

        List<Map<String, String>> rows = new ArrayList<>();
        try (var reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return rows;
            }

            List<String> headers = parseLine(headerLine);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                List<String> values = parseLine(line);
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    String header = headers.get(i);
                    String value = i < values.size() ? values.get(i) : "";
                    row.put(header, value);
                }
                rows.add(row);
            }
        }

        return rows;
    }

    private List<String> parseLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // skip escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        values.add(current.toString());
        return values;
    }

    private String valueOrNull(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    private Double parseDouble(String value, String columnName, Map<String, String> record) {
        String trimmed = valueOrNull(value);
        if (trimmed == null) {
            return null;
        }

        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid numeric value for column: " + columnName + " (row=" + record + ")", ex);
        }
    }
}
