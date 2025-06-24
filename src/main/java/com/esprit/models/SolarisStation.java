package com.esprit.models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SolarisStation {
    private final StringProperty id;
    private final DoubleProperty latitude;
    private final DoubleProperty longitude;
    private final DoubleProperty energyRange; // in kilometers
    private final DoubleProperty currentEnergyOutput; // in kW
    private final StringProperty status;

    public SolarisStation(String id, double latitude, double longitude, double energyRange) {
        this.id = new SimpleStringProperty(id);
        this.latitude = new SimpleDoubleProperty(latitude);
        this.longitude = new SimpleDoubleProperty(longitude);
        this.energyRange = new SimpleDoubleProperty(energyRange);
        this.currentEnergyOutput = new SimpleDoubleProperty(0.0);
        this.status = new SimpleStringProperty("ACTIVE");
    }

    // Getters and setters
    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }

    public double getLatitude() { return latitude.get(); }
    public DoubleProperty latitudeProperty() { return latitude; }

    public double getLongitude() { return longitude.get(); }
    public DoubleProperty longitudeProperty() { return longitude; }

    public double getEnergyRange() { return energyRange.get(); }
    public DoubleProperty energyRangeProperty() { return energyRange; }

    public double getCurrentEnergyOutput() { return currentEnergyOutput.get(); }
    public DoubleProperty currentEnergyOutputProperty() { return currentEnergyOutput; }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }

    public void setCurrentEnergyOutput(double output) {
        this.currentEnergyOutput.set(output);
    }

    public void setStatus(String status) {
        this.status.set(status);
    }
} 