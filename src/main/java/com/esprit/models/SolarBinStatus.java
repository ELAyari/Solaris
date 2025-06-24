package com.esprit.models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for displaying solar bin status in the truck interface
 */
public class SolarBinStatus {
    private final StringProperty id;
    private final StringProperty location;
    private final DoubleProperty fillLevel;
    private final DoubleProperty batteryLevel;
    private final StringProperty energyStatus;
    private final DoubleProperty distance;
    private final StringProperty priorityStatus;
    
    public SolarBinStatus(Bin bin, double distanceFromTruck) {
        this.id = new SimpleStringProperty(String.valueOf(bin.getId()));
        this.location = new SimpleStringProperty(bin.getLocation());
        this.fillLevel = new SimpleDoubleProperty(bin.getNiveauRemplissage());
        this.batteryLevel = new SimpleDoubleProperty(bin.getBatteryLevel());
        this.energyStatus = new SimpleStringProperty(bin.getEnergyStatus());
        this.distance = new SimpleDoubleProperty(distanceFromTruck);
        
        // Calculate priority based on fill level and battery status
        this.priorityStatus = new SimpleStringProperty(calculatePriority(bin));
    }
    
    private String calculatePriority(Bin bin) {
        // High priority if bin is full or battery is low
        if (bin.getNiveauRemplissage() > 80 || bin.getBatteryLevel() < 20) {
            return "HIGH";
        }
        // Medium priority if bin is getting full or battery is medium
        else if (bin.getNiveauRemplissage() > 60 || bin.getBatteryLevel() < 40) {
            return "MEDIUM";
        }
        // Low priority otherwise
        else {
            return "LOW";
        }
    }
    
    // Getters for JavaFX properties
    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }
    
    public String getLocation() { return location.get(); }
    public StringProperty locationProperty() { return location; }
    
    public double getFillLevel() { return fillLevel.get(); }
    public DoubleProperty fillLevelProperty() { return fillLevel; }
    
    public double getBatteryLevel() { return batteryLevel.get(); }
    public DoubleProperty batteryLevelProperty() { return batteryLevel; }
    
    public String getEnergyStatus() { return energyStatus.get(); }
    public StringProperty energyStatusProperty() { return energyStatus; }
    
    public double getDistance() { return distance.get(); }
    public DoubleProperty distanceProperty() { return distance; }
    
    public String getPriorityStatus() { return priorityStatus.get(); }
    public StringProperty priorityStatusProperty() { return priorityStatus; }
}
