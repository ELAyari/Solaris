package com.esprit.Services;

import com.esprit.models.Truck;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TruckService implements IService<Truck> {

    private Connection connection;

    public TruckService() {
        connection = DataSource.getInstance().getConnection();
    }

    @Override
    public void add(Truck truck) {
        String req = "INSERT INTO truck(current_location, destinationLatitude1, destinationLongitude1, " +
                "destinationLatitude2, destinationLongitude2, " +
                "capaciteMax, niveauRemplissageActuel, section, statut, " +
                "fuel_efficiency, fuel_level, co2_emission_rate, fuel_type, " +
                "battery_level, energy_consumption, supported_waste_types, " +
                "last_maintenance_date, next_scheduled_maintenance, maintenance_notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";  // 19 parameters total

        try (PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            // Basic truck properties
            pst.setString(1, truck.getCurrentLocation());
            pst.setDouble(2, truck.getDestinationLatitude1());
            pst.setDouble(3, truck.getDestinationLongitude1());
            pst.setDouble(4, truck.getDestinationLatitude2());
            pst.setDouble(5, truck.getDestinationLongitude2());
            pst.setDouble(6, truck.getCapaciteMax());
            pst.setDouble(7, truck.getNiveauRemplissageActuel());
            pst.setString(8, truck.getSection());
            pst.setString(9, truck.getStatut());
            
            // Energy-related properties
            pst.setDouble(10, truck.getFuelEfficiency());
            pst.setDouble(11, truck.getFuelLevel());
            pst.setDouble(12, truck.getCo2EmissionRate());
            pst.setString(13, truck.getFuelType());
            pst.setDouble(14, truck.getBatteryLevel());
            pst.setDouble(15, truck.getEnergyConsumption());
            
            // Waste management properties
            // Convert waste types array to comma-separated string
            String wasteTypes = String.join(",", truck.getSupportedWasteTypes());
            pst.setString(16, wasteTypes);
            
            // Maintenance info
            pst.setDate(17, truck.getLastMaintenanceDate() != null ? 
                    java.sql.Date.valueOf(truck.getLastMaintenanceDate()) : null);
            pst.setDate(18, truck.getNextScheduledMaintenance() != null ? 
                    java.sql.Date.valueOf(truck.getNextScheduledMaintenance()) : null);
            pst.setString(19, truck.getMaintenanceNotes());
            
            pst.executeUpdate();
            System.out.println("Camion ajouté !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du camion: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void update(Truck truck) {
        String req = "UPDATE truck SET current_location=?, destinationLatitude1=?, destinationLongitude1=?, " +
                "destinationLatitude2=?, destinationLongitude2=?, " +
                "capaciteMax=?, niveauRemplissageActuel=?, section=?, statut=?, " +
                "fuel_efficiency=?, fuel_level=?, co2_emission_rate=?, fuel_type=?, " +
                "battery_level=?, energy_consumption=?, supported_waste_types=?, " +
                "last_maintenance_date=?, next_scheduled_maintenance=?, maintenance_notes=? " +
                "WHERE id=?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            // Basic truck properties
            pst.setString(1, truck.getCurrentLocation());
            pst.setDouble(2, truck.getDestinationLatitude1());
            pst.setDouble(3, truck.getDestinationLongitude1());
            pst.setDouble(4, truck.getDestinationLatitude2());
            pst.setDouble(5, truck.getDestinationLongitude2());
            pst.setDouble(6, truck.getCapaciteMax());
            pst.setDouble(7, truck.getNiveauRemplissageActuel());
            pst.setString(8, truck.getSection());
            pst.setString(9, truck.getStatut());
            
            // Energy-related properties
            pst.setDouble(10, truck.getFuelEfficiency());
            pst.setDouble(11, truck.getFuelLevel());
            pst.setDouble(12, truck.getCo2EmissionRate());
            pst.setString(13, truck.getFuelType());
            pst.setDouble(14, truck.getBatteryLevel());
            pst.setDouble(15, truck.getEnergyConsumption());
            
            // Waste management properties
            String wasteTypes = String.join(",", truck.getSupportedWasteTypes());
            pst.setString(16, wasteTypes);
            
            // Maintenance info
            pst.setDate(17, truck.getLastMaintenanceDate() != null ? 
                    java.sql.Date.valueOf(truck.getLastMaintenanceDate()) : null);
            pst.setDate(18, truck.getNextScheduledMaintenance() != null ? 
                    java.sql.Date.valueOf(truck.getNextScheduledMaintenance()) : null);
            pst.setString(19, truck.getMaintenanceNotes());
            
            // ID for WHERE clause
            pst.setInt(20, truck.getId());
            
            pst.executeUpdate();
            System.out.println("Camion modifié !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du camion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Truck> get() {
        List<Truck> trucks = new ArrayList<>();
        String req = "SELECT * FROM truck";
        try (PreparedStatement pst = connection.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {
            System.out.println("Executing truck query: " + req);
            while (rs.next()) {
                try {
                    // Create truck with basic properties
                    Truck truck = new Truck(
                            rs.getInt("id"),
                            rs.getString("current_location"),
                            rs.getDouble("capaciteMax"),
                            rs.getDouble("niveauRemplissageActuel"),
                            rs.getDouble("destinationLatitude1"),
                            rs.getDouble("destinationLongitude1"),
                            rs.getDouble("destinationLatitude2"),
                            rs.getDouble("destinationLongitude2"),
                            rs.getString("section"),
                            rs.getString("statut")
                    );
                    
                    // Set energy-related properties if they exist in the database
                    try {
                        truck.setFuelEfficiency(rs.getDouble("fuel_efficiency"));
                        truck.setFuelLevel(rs.getDouble("fuel_level"));
                        truck.setCo2EmissionRate(rs.getDouble("co2_emission_rate"));
                        truck.setFuelType(rs.getString("fuel_type"));
                        truck.setBatteryLevel(rs.getDouble("battery_level"));
                        truck.setEnergyConsumption(rs.getDouble("energy_consumption"));
                        
                        // Handle waste types (stored as comma-separated string)
                        String wasteTypesStr = rs.getString("supported_waste_types");
                        if (wasteTypesStr != null && !wasteTypesStr.isEmpty()) {
                            truck.setSupportedWasteTypes(wasteTypesStr.split(","));
                        }
                        
                        // Handle maintenance dates
                        Date lastMaintDate = rs.getDate("last_maintenance_date");
                        if (lastMaintDate != null) {
                            truck.setLastMaintenanceDate(lastMaintDate.toLocalDate());
                        }
                        
                        Date nextMaintDate = rs.getDate("next_scheduled_maintenance");
                        if (nextMaintDate != null) {
                            truck.setNextScheduledMaintenance(nextMaintDate.toLocalDate());
                        }
                        
                        truck.setMaintenanceNotes(rs.getString("maintenance_notes"));
                    } catch (SQLException e) {
                        // These columns might not exist in older database versions, so catch and ignore
                        System.out.println("Note: Some energy or maintenance columns may not exist: " + e.getMessage());
                    }
                    
                    trucks.add(truck);
                    System.out.println("Retrieved truck: " + truck);
                } catch (Exception e) {
                    System.err.println("Error creating truck from database row: " + e.getMessage());
                    e.printStackTrace();
                    // Continue to next truck instead of breaking the entire load
                    continue;
                }
            }
            System.out.println("Total trucks retrieved: " + trucks.size());
        } catch (SQLException e) {
            System.err.println("Error fetching trucks: " + e.getMessage());
            e.printStackTrace();
        }
        return trucks;
    }


    @Override
    public void delete(Truck truck) {
        String req = "DELETE FROM truck WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, truck.getId());
            pst.executeUpdate();
            System.out.println("Camion supprimé !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du camion: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {

    }


    // Méthode supplémentaire pour trouver un camion par son ID
    public Truck getById(int id) {
        String req = "SELECT * FROM truck WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    // Create truck with basic properties
                    Truck truck = new Truck(
                            rs.getInt("id"),
                            rs.getString("current_location"),
                            rs.getDouble("capaciteMax"),
                            rs.getDouble("niveauRemplissageActuel"),
                            rs.getDouble("destinationLatitude1"),
                            rs.getDouble("destinationLongitude1"),
                            rs.getDouble("destinationLatitude2"),
                            rs.getDouble("destinationLongitude2"),
                            rs.getString("section"),
                            rs.getString("statut")
                    );
                    
                    // Set energy-related properties if they exist in the database
                    try {
                        truck.setFuelEfficiency(rs.getDouble("fuel_efficiency"));
                        truck.setFuelLevel(rs.getDouble("fuel_level"));
                        truck.setCo2EmissionRate(rs.getDouble("co2_emission_rate"));
                        truck.setFuelType(rs.getString("fuel_type"));
                        truck.setBatteryLevel(rs.getDouble("battery_level"));
                        truck.setEnergyConsumption(rs.getDouble("energy_consumption"));
                        
                        // Handle waste types (stored as comma-separated string)
                        String wasteTypesStr = rs.getString("supported_waste_types");
                        if (wasteTypesStr != null && !wasteTypesStr.isEmpty()) {
                            truck.setSupportedWasteTypes(wasteTypesStr.split(","));
                        }
                        
                        // Handle maintenance dates
                        Date lastMaintDate = rs.getDate("last_maintenance_date");
                        if (lastMaintDate != null) {
                            truck.setLastMaintenanceDate(lastMaintDate.toLocalDate());
                        }
                        
                        Date nextMaintDate = rs.getDate("next_scheduled_maintenance");
                        if (nextMaintDate != null) {
                            truck.setNextScheduledMaintenance(nextMaintDate.toLocalDate());
                        }
                        
                        truck.setMaintenanceNotes(rs.getString("maintenance_notes"));
                    } catch (SQLException e) {
                        // These columns might not exist in older database versions, so catch and ignore
                        System.out.println("Note: Some energy or maintenance columns may not exist: " + e.getMessage());
                    }
                    
                    return truck;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche du camion: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}