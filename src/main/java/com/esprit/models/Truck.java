package com.esprit.models;

import java.time.LocalDate;

public class Truck {
    private int id;
    private String currentLocation;
    private double currentLatitude;
    private double currentLongitude;
    private double destinationLatitude1;
    private double destinationLongitude1;
    private double destinationLatitude2;
    private double destinationLongitude2;
    private double capaciteMax;
    private double niveauRemplissageActuel;
    private TruckZone section;
    private String statut;
    
    // New energy-related properties
    private double fuelEfficiency; // Liters per 100 km
    private double fuelLevel; // Current fuel level in percentage
    private double co2EmissionRate; // kg/km
    private String fuelType; // Diesel, Electric, Hybrid
    private double batteryLevel; // For electric/hybrid trucks (percentage)
    private double energyConsumption; // kWh/100km for electric trucks
    
    // Truck brand information
    private String brand; // E.g., Solaris, Volvo, Mercedes-Benz, etc.
    
    // Waste management properties
    private String[] supportedWasteTypes; // Types of waste this truck can handle
    private LocalDate lastMaintenanceDate;
    private LocalDate nextScheduledMaintenance;
    private String maintenanceNotes;

    public Truck() {
    }

    public Truck(int id, String currentLocation, double capaciteMax, double niveauRemplissageActuel,
                 double destinationLatitude1, double destinationLongitude1,
                 double destinationLatitude2, double destinationLongitude2,
                 String section, String statut) {

        this.id = id;
        this.currentLocation = currentLocation;
        this.capaciteMax = capaciteMax;
        this.niveauRemplissageActuel = niveauRemplissageActuel;
        this.destinationLatitude1 = destinationLatitude1;
        this.destinationLongitude1 = destinationLongitude1;
        this.destinationLatitude2 = destinationLatitude2;
        this.destinationLongitude2 = destinationLongitude2;
        this.section = TruckZone.fromDisplayName(section);
        this.statut = statut;
        
        // Default values for energy properties
        this.fuelEfficiency = 30.0; // Default value - 30L/100km
        this.fuelLevel = 100.0; // Default to full tank
        this.co2EmissionRate = 2.5; // Default value - 2.5kg/km
        this.fuelType = "Diesel"; // Default fuel type
        this.batteryLevel = 0.0; // Not applicable for diesel trucks
        this.energyConsumption = 0.0; // Not applicable for diesel trucks
        this.supportedWasteTypes = new String[]{"Mixed"}; // Default waste type
        this.lastMaintenanceDate = LocalDate.now().minusMonths(1); // Default to 1 month ago
        this.nextScheduledMaintenance = LocalDate.now().plusMonths(2); // Default to 2 months from now
    }

    public Truck(String currentLocation, double capaciteMax, double niveauRemplissageActuel,
                 double destinationLatitude1, double destinationLongitude1,
                 double destinationLatitude2, double destinationLongitude2,
                 String section, String statut) {

        this.currentLocation = currentLocation;
        this.capaciteMax = capaciteMax;
        this.niveauRemplissageActuel = niveauRemplissageActuel;
        this.destinationLatitude1 = destinationLatitude1;
        this.destinationLongitude1 = destinationLongitude1;
        this.destinationLatitude2 = destinationLatitude2;
        this.destinationLongitude2 = destinationLongitude2;
        this.section = TruckZone.fromDisplayName(section);
        this.statut = statut;
        
        // Default values for energy properties
        this.fuelEfficiency = 30.0; // Default value - 30L/100km
        this.fuelLevel = 100.0; // Default to full tank
        this.co2EmissionRate = 2.5; // Default value - 2.5kg/km
        this.fuelType = "Diesel"; // Default fuel type
        this.batteryLevel = 0.0; // Not applicable for diesel trucks
        this.energyConsumption = 0.0; // Not applicable for diesel trucks
        this.supportedWasteTypes = new String[]{"Mixed"}; // Default waste type
        this.lastMaintenanceDate = LocalDate.now().minusMonths(1); // Default to 1 month ago
        this.nextScheduledMaintenance = LocalDate.now().plusMonths(2); // Default to 2 months from now
    }

    public double getCapaciteMax() {
        return capaciteMax;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public double getCurrentLatitude() {
        return currentLatitude;
    }
    
    public void setCurrentLatitude(double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }
    
    public double getCurrentLongitude() {
        return currentLongitude;
    }
    
    public void setCurrentLongitude(double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }
    
    public double getDestinationLatitude1() {
        return destinationLatitude1;
    }

    public double getDestinationLatitude2() {
        return destinationLatitude2;
    }

    public double getDestinationLongitude1() {
        return destinationLongitude1;
    }

    public double getDestinationLongitude2() {
        return destinationLongitude2;
    }

    public int getId() {
        return id;
    }

    public double getNiveauRemplissageActuel() {
        return niveauRemplissageActuel;
    }

    public String getSection() {
        return section.getDisplayName();
    }

    public String getSectionDescription() {
        return section.getDescription();
    }

    public TruckZone getTruckZone() {
        return section;
    }

    public String getStatut() {
        return statut;
    }

    public void setCapaciteMax(double capaciteMax) {
        this.capaciteMax = capaciteMax;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void setDestinationLatitude1(double destinationLatitude1) {
        this.destinationLatitude1 = destinationLatitude1;
    }

    public void setDestinationLatitude2(double destinationLatitude2) {
        this.destinationLatitude2 = destinationLatitude2;
    }

    public void setDestinationLongitude1(double destinationLongitude1) {
        this.destinationLongitude1 = destinationLongitude1;
    }

    public void setDestinationLongitude2(double destinationLongitude2) {
        this.destinationLongitude2 = destinationLongitude2;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNiveauRemplissageActuel(double niveauRemplissageActuel) {
        this.niveauRemplissageActuel = niveauRemplissageActuel;
    }

    public void setSection(String section) {
        this.section = TruckZone.fromDisplayName(section);
    }

    public void setTruckSection(TruckZone section) {
        this.section = section;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
    
    // Getters and setters for energy properties
    public double getFuelEfficiency() {
        return fuelEfficiency;
    }
    
    public void setFuelEfficiency(double fuelEfficiency) {
        this.fuelEfficiency = fuelEfficiency;
    }
    
    public double getFuelLevel() {
        return fuelLevel;
    }
    
    public void setFuelLevel(double fuelLevel) {
        this.fuelLevel = fuelLevel;
    }
    
    public double getCo2EmissionRate() {
        return co2EmissionRate;
    }
    
    public void setCo2EmissionRate(double co2EmissionRate) {
        this.co2EmissionRate = co2EmissionRate;
    }
    
    public String getFuelType() {
        return fuelType;
    }
    
    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
        
        // Update related properties based on fuel type
        if ("Electric".equals(fuelType)) {
            this.co2EmissionRate = 0.0; // Zero direct emissions
            this.fuelEfficiency = 0.0; // Not applicable
        } else if ("Hybrid".equals(fuelType)) {
            this.co2EmissionRate = 1.5; // Lower emissions
        }
    }
    
    public double getBatteryLevel() {
        return batteryLevel;
    }
    
    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
    
    public double getEnergyConsumption() {
        return energyConsumption;
    }
    
    public void setEnergyConsumption(double energyConsumption) {
        this.energyConsumption = energyConsumption;
    }
    
    // Getters and setters for waste management properties
    public String[] getSupportedWasteTypes() {
        return supportedWasteTypes;
    }
    
    public void setSupportedWasteTypes(String[] supportedWasteTypes) {
        this.supportedWasteTypes = supportedWasteTypes;
    }
    
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
    
    /**
     * Get the truck's brand
     * @return The brand name
     */
    public String getBrand() {
        return brand;
    }
    
    /**
     * Set the truck's brand
     * @param brand The brand name to set
     */
    public void setBrand(String brand) {
        this.brand = brand;
        
        // If the brand is Solaris, adjust energy properties for electric vehicle
        if ("Solaris".equals(brand)) {
            setFuelType("Electric");
            this.co2EmissionRate = 0.0;
            this.fuelEfficiency = 0.0;
            if (this.batteryLevel <= 0) {
                this.batteryLevel = 100.0;
            }
            this.energyConsumption = 15.0; // kWh/100km
        }
    }
    
    /**
     * Checks if this truck can handle a specific waste type
     * @param wasteType The waste type to check
     * @return true if the truck can handle this waste type
     */
    public boolean canHandleWasteType(String wasteType) {
        if (wasteType == null || supportedWasteTypes == null) {
            return false;
        }
        for (String type : supportedWasteTypes) {
            if (type.equalsIgnoreCase(wasteType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Calculate the estimated CO2 emissions for a given distance
     * @param distanceKm Distance in kilometers
     * @return Estimated CO2 emissions in kg
     */
    public double calculateCO2Emissions(double distanceKm) {
        return distanceKm * co2EmissionRate;
    }
    
    /**
     * Calculate fuel consumption for a given distance
     * @param distanceKm Distance in kilometers
     * @return Fuel consumption in liters (or kWh for electric vehicles)
     */
    public double calculateFuelConsumption(double distanceKm) {
        if ("Electric".equals(fuelType)) {
            return distanceKm * energyConsumption / 100.0; // kWh
        } else {
            return distanceKm * fuelEfficiency / 100.0; // liters
        }
    }

    @Override
    public String toString() {
        return "Truck{" +
                "id=" + id +
                ", brand='" + brand + '\'' +
                ", currentLocation='" + currentLocation + '\'' +
                ", section='" + section.getDisplayName() + '\'' +
                ", statut='" + statut + '\'' +
                ", capaciteMax=" + capaciteMax +
                ", niveauRemplissageActuel=" + niveauRemplissageActuel +
                ", fuelType='" + fuelType + '\'' +
                ", fuelLevel=" + fuelLevel + "%" +
                ("Electric".equals(fuelType) || "Hybrid".equals(fuelType) ? 
                 ", batteryLevel=" + batteryLevel + "%" : "") +
                ", wasteTypes=" + (supportedWasteTypes != null && supportedWasteTypes.length > 0 ? 
                    supportedWasteTypes[0] : "None") +
                '}';
    }
}




