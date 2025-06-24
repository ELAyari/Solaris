package com.esprit.Services;

import com.esprit.models.SolarisStation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.*;

public class SolarisRouteOptimizationService {
    private final ObservableList<SolarisStation> stations;
    private static final double TRUCK_RANGE = 300.0; // km
    private static final double CHARGING_EFFICIENCY = 0.85; // 85% efficiency

    public SolarisRouteOptimizationService() {
        this.stations = FXCollections.observableArrayList();
    }

    public void addStation(SolarisStation station) {
        stations.add(station);
    }

    public ObservableList<SolarisStation> getStations() {
        return stations;
    }

    public List<SolarisStation> findStationsAlongRoute(double startLat, double startLon, 
                                                     double endLat, double endLon) {
        List<SolarisStation> routeStations = new ArrayList<>();
        double totalDistance = calculateDistance(startLat, startLon, endLat, endLon);
        
        // If distance is within truck range, no charging needed
        if (totalDistance <= TRUCK_RANGE) {
            return routeStations;
        }

        // Find stations within range of the route
        for (SolarisStation station : stations) {
            if (isStationAlongRoute(startLat, startLon, endLat, endLon, 
                                  station.getLatitude(), station.getLongitude())) {
                routeStations.add(station);
            }
        }

        // Sort stations by distance from start point
        routeStations.sort(Comparator.comparingDouble(s -> 
            calculateDistance(startLat, startLon, s.getLatitude(), s.getLongitude())));

        return routeStations;
    }

    public double calculateCO2Savings(List<SolarisStation> usedStations) {
        double totalSolarEnergy = usedStations.stream()
            .mapToDouble(SolarisStation::getCurrentEnergyOutput)
            .sum();
        
        // Assuming 1 kWh of solar energy saves 0.4 kg of CO2
        return totalSolarEnergy * 0.4;
    }

    public double predictSolarEnergy(double latitude, double longitude, Date time) {
        // This is a simplified prediction model
        // In a real implementation, this would use weather data and space weather forecasts
        double baseOutput = 100.0; // kW
        double timeFactor = calculateTimeFactor(time);
        double locationFactor = calculateLocationFactor(latitude, longitude);
        
        return baseOutput * timeFactor * locationFactor * CHARGING_EFFICIENCY;
    }

    private boolean isStationAlongRoute(double startLat, double startLon, 
                                      double endLat, double endLon,
                                      double stationLat, double stationLon) {
        // Calculate distance from station to route line
        double distance = calculateDistanceToLine(startLat, startLon, endLat, endLon, 
                                                stationLat, stationLon);
        return distance <= 5.0; // 5 km threshold
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula for calculating distance between two points
        final int R = 6371; // Earth's radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    private double calculateDistanceToLine(double x1, double y1, double x2, double y2, 
                                         double px, double py) {
        // Calculate distance from point (px,py) to line segment (x1,y1)-(x2,y2)
        double A = px - x1;
        double B = py - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double len_sq = C * C + D * D;
        double param = -1;

        if (len_sq != 0) {
            param = dot / len_sq;
        }

        double xx, yy;

        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }

        double dx = px - xx;
        double dy = py - yy;

        return Math.sqrt(dx * dx + dy * dy);
    }

    private double calculateTimeFactor(Date time) {
        // Simplified time factor based on hour of day
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        
        // Peak solar hours (10 AM to 4 PM)
        if (hour >= 10 && hour <= 16) {
            return 1.0;
        } else if (hour >= 8 && hour <= 18) {
            return 0.7;
        } else {
            return 0.3;
        }
    }

    private double calculateLocationFactor(double latitude, double longitude) {
        // Simplified location factor based on latitude
        // Higher factor for locations closer to the equator
        return 1.0 - Math.abs(latitude) / 90.0;
    }
} 