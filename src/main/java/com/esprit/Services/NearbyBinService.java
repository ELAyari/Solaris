package com.esprit.Services;

import com.esprit.models.Bin;
import com.esprit.models.SolarBinStatus;
import com.esprit.models.Truck;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for finding nearby bins for trucks, with specialized functions for solar bins
 */
public class NearbyBinService {
    private final BinService binService;

    public NearbyBinService(BinService binService) {
        this.binService = binService;
    }

    /**
     * Finds nearby solar bins for a truck
     * @param truck The truck to find nearby bins for
     * @param maxDistance Maximum distance in kilometers to search for bins
     * @return List of SolarBinStatus objects sorted by priority and distance
     */
    public List<SolarBinStatus> findNearbySolarBins(Truck truck, double maxDistance) {
        List<Bin> solarBins = binService.getSolarPoweredBins();
        List<SolarBinStatus> nearbySolarBins = new ArrayList<>();
        
        // Get current truck coordinates
        double truckLat = truck.getCurrentLatitude();
        double truckLng = truck.getCurrentLongitude();
        
        // Find solar bins within the specified distance
        for (Bin bin : solarBins) {
            double distance = calculateDistance(
                truckLat, truckLng, bin.getLatitude(), bin.getLongitude());
            
            if (distance <= maxDistance) {
                nearbySolarBins.add(new SolarBinStatus(bin, distance));
            }
        }
        
        // Sort by priority (HIGH, MEDIUM, LOW) and then by distance
        return nearbySolarBins.stream()
            .sorted(Comparator
                .comparing(SolarBinStatus::getPriorityStatus, 
                    (p1, p2) -> {
                        // Custom comparator for priority order
                        if (p1.equals(p2)) return 0;
                        if (p1.equals("HIGH")) return -1;
                        if (p2.equals("HIGH")) return 1;
                        if (p1.equals("MEDIUM")) return -1;
                        return 1;
                    })
                .thenComparing(SolarBinStatus::getDistance))
            .collect(Collectors.toList());
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
     * Find bins with critical status (high fill level or low battery)
     * @param truck The truck to find nearby bins for
     * @param maxDistance Maximum distance in kilometers
     * @return List of critical bins
     */
    public List<SolarBinStatus> findCriticalSolarBins(Truck truck, double maxDistance) {
        return findNearbySolarBins(truck, maxDistance).stream()
            .filter(bin -> "HIGH".equals(bin.getPriorityStatus()))
            .collect(Collectors.toList());
    }
}
