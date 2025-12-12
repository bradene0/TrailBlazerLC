package com.liftoff.trail_blazers.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Plants extends AbstractEntity {

    private String scientificName;
    private String commonName;
    @Column(length = 2048)
    private String currentDistribution;
    private String family;
    private String federalListingStatus;
    private String image;
    private String photoCredit;

    @ManyToMany(mappedBy = "plants")
    @JsonBackReference
    private List<Trips> trips = new ArrayList<>();

    public Plants(){}

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getCurrentDistribution() {
        return currentDistribution;
    }

    public void setCurrentDistribution(String currentDistribution) {
        this.currentDistribution = currentDistribution;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getFederalListingStatus() {
        return federalListingStatus;
    }

    public void setFederalListingStatus(String federalListingStatus) {
        this.federalListingStatus = federalListingStatus;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPhotoCredit(){
        return photoCredit;
    }

    public void setPhotoCredit(String photoCredit) {
        this.photoCredit = photoCredit;
    }

    public List<Trips> getTrips() {
        return trips;
    }

    @Override
    public String toString() {
        return commonName;
    }
}
