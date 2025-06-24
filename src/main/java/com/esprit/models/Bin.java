package com.esprit.models;

import java.time.LocalDate;

public class Bin {

    private int id;
    private double latitude;     // Coordonnée GPS
    private double longitude;
    private String location;
    private String typeDechet;
    private String statut;
    private double niveauRemplissage;
    
    // Solar energy fields
    private boolean isSolarPowered;
    private String energyStatus; // "Fully Charged", "Medium", "Low"
    private double batteryLevel; // Percentage (0-100)
    private double solarPanelOutput; // in Watts
    
    // Maintenance tracking
    private LocalDate lastMaintenanceDate;
    private LocalDate nextScheduledMaintenance;
    private String maintenanceNotes;

    public Bin() {
        this.isSolarPowered = false;
        this.energyStatus = "N/A";
        this.batteryLevel = 0.0;
        this.solarPanelOutput = 0.0;
    }

    public Bin(int id, double latitude, double longitude, String location, String typeDechet, double niveauRemplissage, String statut) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
        this.typeDechet = typeDechet;
        this.niveauRemplissage = niveauRemplissage;
        this.statut = statut;
        this.isSolarPowered = false;
        this.energyStatus = "N/A";
        this.batteryLevel = 0.0;
        this.solarPanelOutput = 0.0;
    }

    public Bin(double latitude, double longitude, String location, String typeDechet, double niveauRemplissage, String statut) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
        this.typeDechet = typeDechet;
        this.niveauRemplissage = niveauRemplissage;
        this.statut = statut;
        this.isSolarPowered = false;
        this.energyStatus = "N/A";
        this.batteryLevel = 0.0;
        this.solarPanelOutput = 0.0;
    }
    
    public Bin(double latitude, double longitude, String location, String typeDechet, double niveauRemplissage, String statut,
               boolean isSolarPowered, String energyStatus, double batteryLevel, double solarPanelOutput) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
        this.typeDechet = typeDechet;
        this.niveauRemplissage = niveauRemplissage;
        this.statut = statut;
        this.isSolarPowered = isSolarPowered;
        this.energyStatus = energyStatus;
        this.batteryLevel = batteryLevel;
        this.solarPanelOutput = solarPanelOutput;
    }

    public int getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public String getStatut() {
        return statut;
    }

    public String getTypeDechet() {
        return typeDechet;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getNiveauRemplissage() {
        return niveauRemplissage;
    }

    public void setNiveauRemplissage(double niveauRemplissage) {
        this.niveauRemplissage = niveauRemplissage;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public void setTypeDechet(String typeDechet) {
        this.typeDechet = typeDechet;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // Solar energy getters and setters
    public boolean isSolarPowered() {
        return isSolarPowered;
    }

    public void setSolarPowered(boolean solarPowered) {
        isSolarPowered = solarPowered;
    }

    public String getEnergyStatus() {
        return energyStatus;
    }

    public void setEnergyStatus(String energyStatus) {
        this.energyStatus = energyStatus;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = batteryLevel;
        // Update energy status based on battery level
        if (batteryLevel >= 70.0) {
            this.energyStatus = "Fully Charged";
        } else if (batteryLevel >= 30.0) {
            this.energyStatus = "Medium";
        } else {
            this.energyStatus = "Low";
        }
    }

    public double getSolarPanelOutput() {
        return solarPanelOutput;
    }

    public void setSolarPanelOutput(double solarPanelOutput) {
        this.solarPanelOutput = solarPanelOutput;
    }
    
    // Maintenance getters and setters
    public LocalDate getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public LocalDate getNextScheduledMaintenance() {
        return nextScheduledMaintenance;
    }

    public void setNextScheduledMaintenance(LocalDate nextScheduledMaintenance) {
        this.nextScheduledMaintenance = nextScheduledMaintenance;
    }

    public String getMaintenanceNotes() {
        return maintenanceNotes;
    }

    public void setMaintenanceNotes(String maintenanceNotes) {
        this.maintenanceNotes = maintenanceNotes;
    }
    
    @Override
    public String toString() {
        return "Bin{" +
                "id=" + id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", location='" + location + '\'' +
                ", typeDechet='" + typeDechet + '\'' +
                ", niveauRemplissage=" + niveauRemplissage +
                ", statut='" + statut + '\'' +
                ", isSolarPowered=" + isSolarPowered +
                ", energyStatus='" + energyStatus + '\'' +
                ", batteryLevel=" + batteryLevel +
                ", solarPanelOutput=" + solarPanelOutput +
                ", lastMaintenance=" + (lastMaintenanceDate != null ? lastMaintenanceDate : "Not maintained") +
                '}';
    }
}