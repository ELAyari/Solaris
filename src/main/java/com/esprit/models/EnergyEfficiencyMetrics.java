package com.esprit.models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model for tracking energy efficiency metrics for trucks servicing solar bins
 */
public class EnergyEfficiencyMetrics {
    // Basic energy metrics
    private final DoubleProperty energyConsumption; // kWh or liters
    private final DoubleProperty co2Emissions; // kg CO2
    private final DoubleProperty energySavings; // kWh or liters saved
    private final DoubleProperty co2Savings; // kg CO2 saved
    private final DoubleProperty efficiency; // percentage
    
    // Route metrics
    private final DoubleProperty distance; // km
    private final DoubleProperty solarBinsServiced; // count
    private final DoubleProperty totalWasteCollected; // kg
    
    // Efficiency indicators
    private final StringProperty efficiencyRating; // A, B, C, D, E
    private final StringProperty optimizationTip;
    
    /**
     * Creates new energy efficiency metrics
     * @param truck The truck to calculate metrics for
     * @param routeDistance The route distance in km
     * @param binsServiced Number of bins serviced
     * @param wasteCollected Total waste collected in kg
     */
    public EnergyEfficiencyMetrics(Truck truck, double routeDistance, int binsServiced, double wasteCollected) {
        this.distance = new SimpleDoubleProperty(routeDistance);
        this.solarBinsServiced = new SimpleDoubleProperty(binsServiced);
        this.totalWasteCollected = new SimpleDoubleProperty(wasteCollected);
        
        // Calculate consumption based on truck type
        boolean isElectric = "Electric".equals(truck.getFuelType());
        double consumption;
        
        if (isElectric) {
            // Electric trucks: energy consumption in kWh
            consumption = routeDistance * truck.getEnergyConsumption() / 100.0;
            this.co2Emissions = new SimpleDoubleProperty(0.0); // No direct emissions
        } else {
            // Conventional trucks: fuel consumption in liters
            consumption = routeDistance * truck.getFuelEfficiency() / 100.0;
            this.co2Emissions = new SimpleDoubleProperty(routeDistance * truck.getCo2EmissionRate());
        }
        
        this.energyConsumption = new SimpleDoubleProperty(consumption);
        
        // Calculate savings (compared to conventional truck if electric)
        if (isElectric) {
            // Calculate what a diesel truck would consume
            double conventionalConsumption = routeDistance * 30.0 / 100.0; // Assuming 30L/100km for conventional
            double conventionalEmissions = routeDistance * 2.5; // Assuming 2.5kg CO2/km for conventional
            
            this.energySavings = new SimpleDoubleProperty(conventionalConsumption);
            this.co2Savings = new SimpleDoubleProperty(conventionalEmissions);
        } else {
            this.energySavings = new SimpleDoubleProperty(0.0);
            this.co2Savings = new SimpleDoubleProperty(0.0);
        }
        
        // Calculate efficiency (waste collected per energy unit)
        double efficiencyValue = (consumption > 0) ? wasteCollected / consumption : 0;
        this.efficiency = new SimpleDoubleProperty(efficiencyValue);
        
        // Determine rating based on efficiency
        String rating;
        String tip;
        
        if (isElectric) {
            if (efficiencyValue > 50) rating = "A";
            else if (efficiencyValue > 40) rating = "B";
            else if (efficiencyValue > 30) rating = "C";
            else if (efficiencyValue > 20) rating = "D";
            else rating = "E";
            
            // Tips for electric trucks
            if ("A".equals(rating) || "B".equals(rating)) {
                tip = "Excellent efficiency! Consider servicing more bins per route.";
            } else if ("C".equals(rating)) {
                tip = "Consider optimizing your route to service more bins with less distance.";
            } else {
                tip = "Efficiency is low. Try reducing distance between bins and optimize charging.";
            }
        } else {
            if (efficiencyValue > 40) rating = "B";
            else if (efficiencyValue > 30) rating = "C";
            else if (efficiencyValue > 20) rating = "D";
            else rating = "E";
            
            // Tips for conventional trucks
            if ("B".equals(rating)) {
                tip = "Good efficiency for a conventional truck. Consider upgrading to electric.";
            } else if ("C".equals(rating)) {
                tip = "Average efficiency. Plan routes with fewer stops and less idling.";
            } else {
                tip = "Efficiency is low. Consider maintenance check and route optimization.";
            }
        }
        
        this.efficiencyRating = new SimpleStringProperty(rating);
        this.optimizationTip = new SimpleStringProperty(tip);
    }
    
    // Getters for all properties
    public double getEnergyConsumption() { return energyConsumption.get(); }
    public DoubleProperty energyConsumptionProperty() { return energyConsumption; }
    
    public double getCo2Emissions() { return co2Emissions.get(); }
    public DoubleProperty co2EmissionsProperty() { return co2Emissions; }
    
    public double getEnergySavings() { return energySavings.get(); }
    public DoubleProperty energySavingsProperty() { return energySavings; }
    
    public double getCo2Savings() { return co2Savings.get(); }
    public DoubleProperty co2SavingsProperty() { return co2Savings; }
    
    public double getEfficiency() { return efficiency.get(); }
    public DoubleProperty efficiencyProperty() { return efficiency; }
    
    public double getDistance() { return distance.get(); }
    public DoubleProperty distanceProperty() { return distance; }
    
    public double getSolarBinsServiced() { return solarBinsServiced.get(); }
    public DoubleProperty solarBinsServicedProperty() { return solarBinsServiced; }
    
    public double getTotalWasteCollected() { return totalWasteCollected.get(); }
    public DoubleProperty totalWasteCollectedProperty() { return totalWasteCollected; }
    
    public String getEfficiencyRating() { return efficiencyRating.get(); }
    public StringProperty efficiencyRatingProperty() { return efficiencyRating; }
    
    public String getOptimizationTip() { return optimizationTip.get(); }
    public StringProperty optimizationTipProperty() { return optimizationTip; }
}
