package com.esprit.Services;

import com.esprit.models.Bin;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service specifically for managing solar-powered bins and their energy status
 */
public class SolarBinService {
    private final BinService binService;

    public SolarBinService(BinService binService) {
        this.binService = binService;
    }

    /**
     * Converts a regular bin to a solar-powered bin
     *
     * @param binId               ID of the bin to convert
     * @param initialBatteryLevel Initial battery level (0-100)
     * @param solarPanelOutput    Solar panel output in watts
     * @return true if successful, false otherwise
     */
    public boolean convertToSolarBin(int binId, double initialBatteryLevel, double solarPanelOutput) {
        Bin bin = binService.getById(binId);
        if (bin == null) {
            return false;
        }

        bin.setSolarPowered(true);
        bin.setBatteryLevel(initialBatteryLevel);
        bin.setSolarPanelOutput(solarPanelOutput);
        bin.setLastMaintenanceDate(LocalDate.now());
        bin.setNextScheduledMaintenance(LocalDate.now().plusMonths(3)); // Schedule maintenance in 3 months
        bin.setMaintenanceNotes("Converted to solar power on " + LocalDate.now());

        binService.update(bin);
        return true;
    }

    /**
     * Creates a new solar-powered bin
     *
     * @param latitude         Latitude coordinates
     * @param longitude        Longitude coordinates
     * @param location         Location description
     * @param typeDechet       Waste type
     * @param batteryLevel     Initial battery level
     * @param solarPanelOutput Solar panel output in watts
     * @return The created bin or null if failed
     */
    public Bin createSolarBin(double latitude, double longitude, String location,
                              String typeDechet, double batteryLevel, double solarPanelOutput) {
        Bin bin = new Bin(latitude, longitude, location, typeDechet, 0.0, "ACTIVE");
        bin.setSolarPowered(true);
        bin.setBatteryLevel(batteryLevel);
        bin.setSolarPanelOutput(solarPanelOutput);
        bin.setLastMaintenanceDate(LocalDate.now());
        bin.setNextScheduledMaintenance(LocalDate.now().plusMonths(3));

        binService.add(bin);
        return bin;
    }

    /**
     * Simulates battery drain based on fill level
     * Bins with higher fill levels will consume more energy for compaction
     *
     * @return Number of bins updated
     */
    public int simulateBatteryDrain() {
        List<Bin> solarBins = binService.getSolarPoweredBins();
        int updatedCount = 0;

        for (Bin bin : solarBins) {
            // Higher fill level means more power consumption
            double drainAmount = calculateDrainAmount(bin.getNiveauRemplissage());
            double newLevel = Math.max(0, bin.getBatteryLevel() - drainAmount);

            if (newLevel != bin.getBatteryLevel()) {
                bin.setBatteryLevel(newLevel);
                binService.update(bin);
                updatedCount++;
            }
        }

        return updatedCount;
    }

    /**
     * Simulates solar charging based on bin energy parameters
     *
     * @return Number of bins updated
     */
    public int simulateSolarCharging() {
        List<Bin> solarBins = binService.getSolarPoweredBins();
        int updatedCount = 0;

        // Simulate a sunny day with good charging conditions
        double chargingEfficiency = 0.8; // 80% efficiency

        for (Bin bin : solarBins) {
            // Calculate charge amount based on panel output
            double chargeAmount = bin.getSolarPanelOutput() * 0.1 * chargingEfficiency;
            double newLevel = Math.min(100, bin.getBatteryLevel() + chargeAmount);

            if (newLevel != bin.getBatteryLevel()) {
                bin.setBatteryLevel(newLevel);
                binService.update(bin);
                updatedCount++;
            }
        }

        return updatedCount;
    }

    /**
     * Calculates how much battery to drain based on fill level
     *
     * @param fillLevel Fill level of the bin (0-100)
     * @return Amount to drain from battery
     */
    private double calculateDrainAmount(double fillLevel) {
        // Base drain is 0.5% per day
        double baseDrain = 0.5;

        // Additional drain based on fill level
        // Higher fill levels trigger compaction which uses more energy
        if (fillLevel > 80) {
            return baseDrain + 2.0; // Heavy compaction
        } else if (fillLevel > 50) {
            return baseDrain + 1.0; // Medium compaction
        } else {
            return baseDrain; // No compaction needed
        }
    }

    /**
     * Schedules maintenance for all solar bins with low battery
     *
     * @return Map of bin IDs to scheduled maintenance dates
     */
    public Map<Integer, LocalDate> scheduleMaintenance() {
        List<Bin> lowBatteryBins = new ArrayList<>();

        for (Bin bin : binService.getSolarPoweredBins()) {
            if (bin.getBatteryLevel() < 20.0) {
                // Schedule maintenance within the next week for low battery bins
                LocalDate maintenanceDate = LocalDate.now().plusDays(7);
                bin.setNextScheduledMaintenance(maintenanceDate);
                bin.setMaintenanceNotes("Scheduled maintenance due to low battery (" +
                        bin.getBatteryLevel() + "%)");
                binService.update(bin);
                lowBatteryBins.add(bin);
            }
        }

        // Return map of bin IDs to scheduled maintenance dates
        return lowBatteryBins.stream()
                .collect(Collectors.toMap(Bin::getId, Bin::getNextScheduledMaintenance));
    }

    /**
     * Calculate energy efficiency for each bin (fill level % per energy unit consumed)
     *
     * @return Map of bin IDs to energy efficiency scores
     */
    public Map<Integer, Double> calculateEnergyEfficiency() {
        List<Bin> solarBins = binService.getSolarPoweredBins();

        return solarBins.stream()
                .collect(Collectors.toMap(
                        Bin::getId,
                        bin -> (bin.getBatteryLevel() > 0) ?
                                bin.getNiveauRemplissage() / (100 - bin.getBatteryLevel()) : 0.0
                ));
    }

    /**
     * Perform maintenance on a bin, resetting maintenance dates and optionally charging the battery
     *
     * @param binId            ID of the bin to maintain
     * @param chargeBattery    Whether to fully charge the battery
     * @param maintenanceNotes Notes about the maintenance performed
     * @return true if successful, false otherwise
     */
    public boolean performMaintenance(int binId, boolean chargeBattery, String maintenanceNotes) {
        Bin bin = binService.getById(binId);
        if (bin == null || !bin.isSolarPowered()) {
            return false;
        }

        // Update maintenance dates
        bin.setLastMaintenanceDate(LocalDate.now());
        bin.setNextScheduledMaintenance(LocalDate.now().plusMonths(3));

        // Update notes
        String notes = "Maintenance performed on " + LocalDate.now() + ": " + maintenanceNotes;
        bin.setMaintenanceNotes(notes);

        // Charge battery if requested
        if (chargeBattery) {
            bin.setBatteryLevel(100.0);
        }

        binService.update(bin);
        return true;
    }

    /**
     * Updates the battery level of a bin and automatically sets the energy status
     * 
     * @param binId The ID of the bin to update
     * @param batteryLevel The new battery level (0-100)
     * @return true if update was successful, false otherwise
     */
    public boolean updateBinBatteryLevel(int binId, double batteryLevel) {
        Bin bin = binService.getById(binId);
        if (bin == null || !bin.isSolarPowered()) {
            return false;
        }
        
        bin.setBatteryLevel(batteryLevel); // This will automatically update energy status
        binService.update(bin);
        return true;
    }
    
    /**
     * Creates a new solar bin by address only (coordinates will be looked up)
     * 
     * @param address           The address/location name to geocode
     * @param typeDechet        Waste type
     * @param fillLevel         Initial fill level
     * @param batteryLevel      Initial battery level
     * @param solarPanelOutput  Solar panel output in watts
     * @param nominatimService  The service to use for geocoding
     * @return The created bin or null if geocoding failed
     * @throws Exception If geocoding fails
     */
    public Bin createSolarBinByAddress(String address, String typeDechet, double fillLevel, 
                                      double batteryLevel, double solarPanelOutput,
                                      NominatimService nominatimService) throws Exception {
        // Geocode the address to get coordinates
        double[] coordinates = nominatimService.getCoordinates(address);
        if (coordinates == null || coordinates.length != 2) {
            throw new Exception("Could not geocode address: " + address);
        }
        
        // Create the bin with the coordinates
        Bin bin = createSolarBin(coordinates[0], coordinates[1], address, typeDechet, 
                               batteryLevel, solarPanelOutput);
        
        // Set the fill level (not set by createSolarBin)
        bin.setNiveauRemplissage(fillLevel);
        binService.update(bin);
        
        return bin;
    }
}
