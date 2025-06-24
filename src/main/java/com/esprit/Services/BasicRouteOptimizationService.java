package com.esprit.Services;

import com.esprit.models.Bin;
import com.esprit.models.TruckZone;

import java.util.*;

/**
 * A simplified route optimization service that prioritizes bin collection
 * based on fill level and location using a basic greedy algorithm.
 */
public class BasicRouteOptimizationService {
    
    /**
     * Creates an optimized collection route for a given truck zone
     * @param bins List of bins to collect
     * @param startingLatitude Starting latitude for the collection route
     * @param startingLongitude Starting longitude for the collection route
     * @param zone Zone for collection (optional, can be null)
     * @return Ordered list of bins for collection
     */
    public List<Bin> createOptimizedRoute(List<Bin> bins, double startingLatitude, 
                                         double startingLongitude, TruckZone zone) {
        
        // Filter bins by zone if specified
        List<Bin> zoneBins = new ArrayList<>();
        if (zone != null) {
            for (Bin bin : bins) {
                if (zone.isLocationInZone(bin.getLatitude(), bin.getLongitude())) {
                    zoneBins.add(bin);
                }
            }
        } else {
            zoneBins.addAll(bins);
        }
        
        // Sort bins by fill level (descending) to prioritize fuller bins
        zoneBins.sort((bin1, bin2) -> Double.compare(bin2.getNiveauRemplissage(), bin1.getNiveauRemplissage()));
        
        // Find the top 5 fullest bins that must be collected first
        List<Bin> priorityBins = new ArrayList<>();
        for (int i = 0; i < Math.min(5, zoneBins.size()); i++) {
            if (zoneBins.get(i).getNiveauRemplissage() > 80.0) {
                priorityBins.add(zoneBins.get(i));
            }
        }
        
        // Remove priority bins from main list
        zoneBins.removeAll(priorityBins);
        
        // Create final route starting with priority bins
        List<Bin> optimizedRoute = new ArrayList<>(priorityBins);
        
        // Starting point coordinates
        double currentLat = startingLatitude;
        double currentLon = startingLongitude;
        
        // Build the rest of the route using nearest neighbor algorithm
        while (!zoneBins.isEmpty()) {
            Bin closest = findClosestBin(zoneBins, currentLat, currentLon);
            optimizedRoute.add(closest);
            zoneBins.remove(closest);
            
            // Update current position
            currentLat = closest.getLatitude();
            currentLon = closest.getLongitude();
        }
        
        return optimizedRoute;
    }
    
    /**
     * Finds the closest bin to the current location
     * @param bins List of bins to check
     * @param currentLat Current latitude
     * @param currentLon Current longitude
     * @return The closest bin
     */
    private Bin findClosestBin(List<Bin> bins, double currentLat, double currentLon) {
        Bin closest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Bin bin : bins) {
            double distance = calculateDistance(currentLat, currentLon, bin.getLatitude(), bin.getLongitude());
            if (distance < minDistance) {
                minDistance = distance;
                closest = bin;
            }
        }
        
        return closest;
    }
    
    /**
     * Calculates the distance between two points using the Haversine formula
     * @param lat1 First point latitude
     * @param lon1 First point longitude
     * @param lat2 Second point latitude
     * @param lon2 Second point longitude
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometers
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    
    /**
     * Estimates the total distance of a route in kilometers
     * @param route List of bins in the route
     * @param startingLatitude Starting latitude
     * @param startingLongitude Starting longitude
     * @return Total route distance in kilometers
     */
    public double calculateRouteDistance(List<Bin> route, double startingLatitude, double startingLongitude) {
        if (route.isEmpty()) {
            return 0.0;
        }
        
        double totalDistance = 0.0;
        double currentLat = startingLatitude;
        double currentLon = startingLongitude;
        
        for (Bin bin : route) {
            totalDistance += calculateDistance(currentLat, currentLon, bin.getLatitude(), bin.getLongitude());
            currentLat = bin.getLatitude();
            currentLon = bin.getLongitude();
        }
        
        // Add return distance to starting point
        totalDistance += calculateDistance(currentLat, currentLon, startingLatitude, startingLongitude);
        
        return totalDistance;
    }
    
    /**
     * Calculates an efficiency score for the route (bins collected per km traveled)
     * @param route The collection route
     * @param startingLatitude Starting latitude
     * @param startingLongitude Starting longitude
     * @return Efficiency score (bins/km)
     */
    public double calculateEfficiencyScore(List<Bin> route, double startingLatitude, double startingLongitude) {
        double distance = calculateRouteDistance(route, startingLatitude, startingLongitude);
        if (distance == 0.0) {
            return 0.0;
        }
        return route.size() / distance;
    }
}
