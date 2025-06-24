package com.esprit.Services;

import com.esprit.models.Bin;
import com.esprit.models.EnergyEfficiencyMetrics;
import com.esprit.models.Truck;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for calculating and tracking energy efficiency metrics
 */
public class EnergyEfficiencyService {
    private final TruckService truckService;
    private final BinService binService;
    
    // In-memory cache of recent calculations for each truck
    private final Map<Integer, List<EnergyEfficiencyMetrics>> truckMetricsHistory;
    
    public EnergyEfficiencyService(TruckService truckService, BinService binService) {
        this.truckService = truckService;
        this.binService = binService;
        this.truckMetricsHistory = new HashMap<>();
    }
    
    /**
     * Calculate energy efficiency metrics for a truck based on a route
     * @param truck The truck to calculate metrics for
     * @param routeDistance The route distance in km
     * @param binsToService List of bins to service on the route
     * @return Energy efficiency metrics
     */
    public EnergyEfficiencyMetrics calculateMetrics(Truck truck, double routeDistance, List<Bin> binsToService) {
        // Calculate total waste collected from bins
        double totalWaste = binsToService.stream()
                .mapToDouble(Bin::getNiveauRemplissage)
                .sum();
        
        // Create metrics
        EnergyEfficiencyMetrics metrics = new EnergyEfficiencyMetrics(
                truck, routeDistance, binsToService.size(), totalWaste);
        
        // Save to history
        saveMetricsToHistory(truck.getId(), metrics);
        
        return metrics;
    }
    
    /**
     * Calculate metrics for a truck based on a simulated route to solar bins
     * @param truck The truck
     * @param solarBins List of solar bins to service
     * @return Energy efficiency metrics
     */
    public EnergyEfficiencyMetrics calculateMetricsForSolarBins(Truck truck, List<Bin> solarBins) {
        // Calculate a simple route distance (this is simplified - in reality you'd use a routing algorithm)
        double routeDistance = estimateRouteDistance(truck, solarBins);
        
        return calculateMetrics(truck, routeDistance, solarBins);
    }
    
    /**
     * Estimate route distance based on truck position and bins (simplified)
     */
    private double estimateRouteDistance(Truck truck, List<Bin> bins) {
        if (bins.isEmpty()) {
            return 0.0;
        }
        
        double totalDistance = 0.0;
        double lastLat = truck.getCurrentLatitude();
        double lastLng = truck.getCurrentLongitude();
        
        for (Bin bin : bins) {
            double distance = calculateDistance(lastLat, lastLng, bin.getLatitude(), bin.getLongitude());
            totalDistance += distance;
            
            // Update last position
            lastLat = bin.getLatitude();
            lastLng = bin.getLongitude();
        }
        
        // Add return distance to starting point (if needed)
        // totalDistance += calculateDistance(lastLat, lastLng, truck.getCurrentLatitude(), truck.getCurrentLongitude());
        
        return totalDistance;
    }
    
    /**
     * Calculate the distance between two coordinates using the Haversine formula
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
    }
    
    /**
     * Save metrics to the truck's history
     */
    private void saveMetricsToHistory(int truckId, EnergyEfficiencyMetrics metrics) {
        truckMetricsHistory.computeIfAbsent(truckId, k -> new ArrayList<>())
                .add(metrics);
        
        // Keep only last 10 entries
        List<EnergyEfficiencyMetrics> history = truckMetricsHistory.get(truckId);
        if (history.size() > 10) {
            history = history.subList(history.size() - 10, history.size());
            truckMetricsHistory.put(truckId, history);
        }
    }
    
    /**
     * Get metrics history for a truck
     */
    public List<EnergyEfficiencyMetrics> getMetricsHistory(int truckId) {
        return truckMetricsHistory.getOrDefault(truckId, new ArrayList<>());
    }
    
    /**
     * Get the latest metrics for a truck if available
     */
    public EnergyEfficiencyMetrics getLatestMetrics(int truckId) {
        List<EnergyEfficiencyMetrics> history = truckMetricsHistory.get(truckId);
        if (history != null && !history.isEmpty()) {
            return history.get(history.size() - 1);
        }
        return null;
    }
    
    /**
     * Calculate the potential savings if the truck was upgraded to electric
     * @param truck The current truck
     * @param routeDistance The route distance
     * @return Map with savings metrics
     */
    public Map<String, Double> calculatePotentialSavings(Truck truck, double routeDistance) {
        Map<String, Double> savings = new HashMap<>();
        
        // Only calculate for non-electric trucks
        if (!"Electric".equals(truck.getFuelType())) {
            // Current emissions and consumption
            double currentConsumption = routeDistance * truck.getFuelEfficiency() / 100.0;
            double currentEmissions = routeDistance * truck.getCo2EmissionRate();
            
            // Electric equivalent (assuming 15 kWh/100km)
            double electricConsumption = routeDistance * 15.0 / 100.0;
            double electricEmissions = 0.0; // Zero direct emissions
            
            // Calculate savings
            savings.put("fuelSavings", currentConsumption); // Liters of fuel saved
            savings.put("emissionSavings", currentEmissions); // kg CO2 saved
            savings.put("electricConsumption", electricConsumption); // kWh required
            
            // Estimate cost savings (assuming $1.5/L for diesel and $0.15/kWh for electricity)
            double fuelCost = currentConsumption * 1.5;
            double electricityCost = electricConsumption * 0.15;
            savings.put("costSavings", fuelCost - electricityCost);
        }
        
        return savings;
    }
}
