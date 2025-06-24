package com.esprit.Services;

import com.esprit.models.Bin;
import com.esprit.models.TruckZone;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for generating simple reports about bins and their status
 */
public class BinReportService {
    private final BinService binService;
    private final BasicRouteOptimizationService routeService;
    
    public BinReportService(BinService binService, BasicRouteOptimizationService routeService) {
        this.binService = binService;
        this.routeService = routeService;
    }
    
    /**
     * Generates a simple CSV report of all bins
     * @param filePath Path to save the CSV file
     * @return true if successful, false otherwise
     */
    public boolean generateBinStatusReport(String filePath) {
        List<Bin> bins = binService.get();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write header
            writer.println("ID,Location,Type,Fill Level,Status,Is Solar,Energy Status,Battery Level,Last Maintenance");
            
            // Write bin data
            for (Bin bin : bins) {
                StringBuilder line = new StringBuilder();
                line.append(bin.getId()).append(",");
                line.append(escapeCsv(bin.getLocation())).append(",");
                line.append(escapeCsv(bin.getTypeDechet())).append(",");
                line.append(bin.getNiveauRemplissage()).append(",");
                line.append(escapeCsv(bin.getStatut())).append(",");
                line.append(bin.isSolarPowered()).append(",");
                line.append(escapeCsv(bin.getEnergyStatus())).append(",");
                line.append(bin.getBatteryLevel()).append(",");
                
                if (bin.getLastMaintenanceDate() != null) {
                    line.append(bin.getLastMaintenanceDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                } else {
                    line.append("Never");
                }
                
                writer.println(line);
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error generating bin status report: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generates a maintenance report showing bins that need attention
     * @param filePath Path to save the report
     * @return true if successful, false otherwise
     */
    public boolean generateMaintenanceReport(String filePath) {
        List<Bin> binsNeedingMaintenance = binService.getBinsNeedingMaintenance();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write header
            writer.println("Maintenance Report - Generated on " + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println();
            writer.println("Total bins needing maintenance: " + binsNeedingMaintenance.size());
            writer.println();
            writer.println("ID,Location,Reason,Battery Level,Last Maintenance,Next Scheduled");
            
            // Write bin data
            for (Bin bin : binsNeedingMaintenance) {
                StringBuilder line = new StringBuilder();
                line.append(bin.getId()).append(",");
                line.append(escapeCsv(bin.getLocation())).append(",");
                
                // Determine reason for maintenance
                String reason;
                if (bin.isSolarPowered() && bin.getBatteryLevel() < 20.0) {
                    reason = "Low Battery";
                } else if (bin.getNextScheduledMaintenance() != null && 
                           bin.getNextScheduledMaintenance().isBefore(LocalDate.now())) {
                    reason = "Maintenance Overdue";
                } else {
                    reason = "Unknown";
                }
                
                line.append(escapeCsv(reason)).append(",");
                line.append(bin.getBatteryLevel()).append(",");
                
                if (bin.getLastMaintenanceDate() != null) {
                    line.append(bin.getLastMaintenanceDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                } else {
                    line.append("Never");
                }
                line.append(",");
                
                if (bin.getNextScheduledMaintenance() != null) {
                    line.append(bin.getNextScheduledMaintenance().format(DateTimeFormatter.ISO_LOCAL_DATE));
                } else {
                    line.append("Not Scheduled");
                }
                
                writer.println(line);
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error generating maintenance report: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generates a collection efficiency report by zone
     * @param filePath Path to save the report
     * @param startingLatitude Starting point latitude for routes
     * @param startingLongitude Starting point longitude for routes
     * @return true if successful, false otherwise
     */
    public boolean generateCollectionEfficiencyReport(String filePath, 
                                                    double startingLatitude, 
                                                    double startingLongitude) {
        List<Bin> allBins = binService.get();
        Map<TruckZone, List<Bin>> zoneRoutes = new HashMap<>();
        
        // Generate optimized routes for each zone
        for (TruckZone zone : TruckZone.values()) {
            if (zone != TruckZone.UNKNOWN) {
                List<Bin> route = routeService.createOptimizedRoute(
                        allBins, startingLatitude, startingLongitude, zone);
                zoneRoutes.put(zone, route);
            }
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write header
            writer.println("Collection Efficiency Report - Generated on " + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println();
            writer.println("Zone,Bins to Collect,Route Distance (km),Efficiency (bins/km)");
            
            // Write zone data
            double totalDistance = 0;
            int totalBins = 0;
            
            for (Map.Entry<TruckZone, List<Bin>> entry : zoneRoutes.entrySet()) {
                TruckZone zone = entry.getKey();
                List<Bin> route = entry.getValue();
                
                double distance = routeService.calculateRouteDistance(
                        route, startingLatitude, startingLongitude);
                double efficiency = routeService.calculateEfficiencyScore(
                        route, startingLatitude, startingLongitude);
                
                writer.println(zone.getDisplayName() + "," + 
                               route.size() + "," + 
                               String.format("%.2f", distance) + "," + 
                               String.format("%.2f", efficiency));
                
                totalDistance += distance;
                totalBins += route.size();
            }
            
            // Write summary
            writer.println();
            writer.println("SUMMARY");
            writer.println("Total bins to collect," + totalBins);
            writer.println("Total distance (km)," + String.format("%.2f", totalDistance));
            if (totalDistance > 0) {
                writer.println("Overall efficiency (bins/km)," + 
                        String.format("%.2f", (double) totalBins / totalDistance));
            } else {
                writer.println("Overall efficiency (bins/km),N/A");
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error generating collection efficiency report: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper method to escape special characters in CSV
     * @param value String to escape
     * @return Escaped string
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");
        if (!needsQuotes) {
            return value;
        }
        
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
