package com.esprit.Services;

import com.esprit.models.SolarisStation;

import java.util.*;

public class SolarisChargingService {

    private List<SolarisStation> solarisStations;

    public SolarisChargingService() {
        this.solarisStations = new ArrayList<>();
    }

    public void addStation(SolarisStation station) {
        solarisStations.add(station);
    }

    public List<SolarisStation> findStationsAlongRoute(List<String> route) {
        List<SolarisStation> stationsAlongRoute = new ArrayList<>();
        for (String point : route) {
            for (SolarisStation station : solarisStations) {
                if (isWithinRange(point, station)) {
                    stationsAlongRoute.add(station);
                }
            }
        }
        return stationsAlongRoute;
    }

    private boolean isWithinRange(String point, SolarisStation station) {
        // Logic to determine if a point is within the station's energy range
        return false; // Placeholder logic
    }

    public double predictSolarEnergy(String stationId, Date time) {
        // Logic to predict available solar energy based on time and space weather
        return 0.0; // Placeholder logic
    }

    public double calculateCO2Savings(List<SolarisStation> stations) {
        // Example calculation: 1 station = 5 kg CO2 saved
        return stations.size() * 5.0;
    }
} 