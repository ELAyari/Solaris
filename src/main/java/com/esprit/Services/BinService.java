package com.esprit.Services;

import com.esprit.models.Bin;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BinService implements IService<Bin> {

    private Connection connection;

    public BinService() {
        connection = DataSource.getInstance().getConnection();
    }

    @Override
    public void add(Bin smartBin) {
        String req = "INSERT INTO bin(location, typeDechet, niveauRemplissage, statut, latitude, longitude, "
                + "isSolarPowered, energyStatus, batteryLevel, solarPanelOutput, lastMaintenanceDate, "
                + "nextScheduledMaintenance, maintenanceNotes) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setString(1, smartBin.getLocation());
            pst.setString(2, smartBin.getTypeDechet());
            pst.setDouble(3, smartBin.getNiveauRemplissage());
            pst.setString(4, smartBin.getStatut());
            pst.setDouble(5, smartBin.getLatitude());
            pst.setDouble(6, smartBin.getLongitude());
            pst.setBoolean(7, smartBin.isSolarPowered());
            pst.setString(8, smartBin.getEnergyStatus());
            pst.setDouble(9, smartBin.getBatteryLevel());
            pst.setDouble(10, smartBin.getSolarPanelOutput());
            
            // Handle date fields (may be null)
            if (smartBin.getLastMaintenanceDate() != null) {
                pst.setDate(11, java.sql.Date.valueOf(smartBin.getLastMaintenanceDate()));
            } else {
                pst.setNull(11, java.sql.Types.DATE);
            }
            
            if (smartBin.getNextScheduledMaintenance() != null) {
                pst.setDate(12, java.sql.Date.valueOf(smartBin.getNextScheduledMaintenance()));
            } else {
                pst.setNull(12, java.sql.Types.DATE);
            }
            
            pst.setString(13, smartBin.getMaintenanceNotes());
            
            pst.executeUpdate();
            System.out.println("Poubelle ajoutée !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Bin smartBin) {
        String req = "UPDATE bin SET location=?, typeDechet=?, niveauRemplissage=?, statut=?, latitude=?, longitude=?, "
                + "isSolarPowered=?, energyStatus=?, batteryLevel=?, solarPanelOutput=?, "
                + "lastMaintenanceDate=?, nextScheduledMaintenance=?, maintenanceNotes=? "
                + "WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setString(1, smartBin.getLocation());
            pst.setString(2, smartBin.getTypeDechet());
            pst.setDouble(3, smartBin.getNiveauRemplissage());
            pst.setString(4, smartBin.getStatut());
            pst.setDouble(5, smartBin.getLatitude());
            pst.setDouble(6, smartBin.getLongitude());
            pst.setBoolean(7, smartBin.isSolarPowered());
            pst.setString(8, smartBin.getEnergyStatus());
            pst.setDouble(9, smartBin.getBatteryLevel());
            pst.setDouble(10, smartBin.getSolarPanelOutput());
            
            // Handle date fields (may be null)
            if (smartBin.getLastMaintenanceDate() != null) {
                pst.setDate(11, java.sql.Date.valueOf(smartBin.getLastMaintenanceDate()));
            } else {
                pst.setNull(11, java.sql.Types.DATE);
            }
            
            if (smartBin.getNextScheduledMaintenance() != null) {
                pst.setDate(12, java.sql.Date.valueOf(smartBin.getNextScheduledMaintenance()));
            } else {
                pst.setNull(12, java.sql.Types.DATE);
            }
            
            pst.setString(13, smartBin.getMaintenanceNotes());
            pst.setInt(14, smartBin.getId());
            
            pst.executeUpdate();
            System.out.println("Poubelle modifiée !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Bin> get() {
        List<Bin> smartBins = new ArrayList<>();
        String req = "SELECT * FROM bin";
        try (PreparedStatement pst = connection.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Bin bin = new Bin(
                        rs.getInt("id"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getString("location"),
                        rs.getString("typeDechet"),
                        rs.getDouble("niveauRemplissage"),
                        rs.getString("statut")
                );
                
                // Add solar energy data
                try {
                    bin.setSolarPowered(rs.getBoolean("isSolarPowered"));
                    bin.setEnergyStatus(rs.getString("energyStatus"));
                    bin.setBatteryLevel(rs.getDouble("batteryLevel"));
                    bin.setSolarPanelOutput(rs.getDouble("solarPanelOutput"));
                    
                    // Get maintenance dates
                    java.sql.Date lastMaintenanceSQL = rs.getDate("lastMaintenanceDate");
                    if (lastMaintenanceSQL != null) {
                        bin.setLastMaintenanceDate(lastMaintenanceSQL.toLocalDate());
                    }
                    
                    java.sql.Date nextMaintenanceSQL = rs.getDate("nextScheduledMaintenance");
                    if (nextMaintenanceSQL != null) {
                        bin.setNextScheduledMaintenance(nextMaintenanceSQL.toLocalDate());
                    }
                    
                    bin.setMaintenanceNotes(rs.getString("maintenanceNotes"));
                } catch (SQLException ex) {
                    // If columns don't exist yet, just continue with default values
                    System.out.println("Solar or maintenance columns may not exist yet: " + ex.getMessage());
                }
                
                smartBins.add(bin);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return smartBins;
    }


    @Override
    public void delete(Bin smartBin) {
        String req = "DELETE FROM bin WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, smartBin.getId());
            pst.executeUpdate();
            System.out.println("SmartBin supprimée !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(int id) {

    }

    public Bin getById(int id) {
        String query = "SELECT * FROM bin WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Bin bin = new Bin(
                        rs.getInt("id"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getString("location"),
                        rs.getString("typeDechet"),
                        rs.getDouble("niveauRemplissage"),
                        rs.getString("statut")
                );
                
                // Add solar energy data
                try {
                    bin.setSolarPowered(rs.getBoolean("isSolarPowered"));
                    bin.setEnergyStatus(rs.getString("energyStatus"));
                    bin.setBatteryLevel(rs.getDouble("batteryLevel"));
                    bin.setSolarPanelOutput(rs.getDouble("solarPanelOutput"));
                    
                    // Get maintenance dates
                    java.sql.Date lastMaintenanceSQL = rs.getDate("lastMaintenanceDate");
                    if (lastMaintenanceSQL != null) {
                        bin.setLastMaintenanceDate(lastMaintenanceSQL.toLocalDate());
                    }
                    
                    java.sql.Date nextMaintenanceSQL = rs.getDate("nextScheduledMaintenance");
                    if (nextMaintenanceSQL != null) {
                        bin.setNextScheduledMaintenance(nextMaintenanceSQL.toLocalDate());
                    }
                    
                    bin.setMaintenanceNotes(rs.getString("maintenanceNotes"));
                } catch (SQLException ex) {
                    // If columns don't exist yet, just continue with default values
                    System.out.println("Solar or maintenance columns may not exist yet: " + ex.getMessage());
                }
                
                return bin;
            }
        } catch (SQLException e) {
            System.err.println("Erreur getById: " + e.getMessage());
        }
        return null;
    }

    private NominatimService nominatimService = new NominatimService();

    public void addBin(Bin bin) {
        try {
            // Géocodage automatique si l'adresse est fournie
            if (bin.getLocation() != null && !bin.getLocation().isEmpty()) {
                double[] coords = nominatimService.getCoordinates(bin.getLocation());
                bin.setLatitude(coords[0]);
                bin.setLongitude(coords[1]);
            }
            // Logique de sauvegarde en base...
            add(bin);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'ajout de la poubelle", e);
        }
    }
    
    /**
     * Gets a list of bins that are solar powered
     * @return List of solar powered bins
     */
    public List<Bin> getSolarPoweredBins() {
        List<Bin> solarBins = new ArrayList<>();
        String req = "SELECT * FROM bin WHERE isSolarPowered = true";
        try (PreparedStatement pst = connection.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Bin bin = new Bin(
                        rs.getInt("id"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getString("location"),
                        rs.getString("typeDechet"),
                        rs.getDouble("niveauRemplissage"),
                        rs.getString("statut")
                );
                
                // Add solar energy data
                try {
                    bin.setSolarPowered(rs.getBoolean("isSolarPowered"));
                    bin.setEnergyStatus(rs.getString("energyStatus"));
                    bin.setBatteryLevel(rs.getDouble("batteryLevel"));
                    bin.setSolarPanelOutput(rs.getDouble("solarPanelOutput"));
                    
                    // Get maintenance dates
                    java.sql.Date lastMaintenanceSQL = rs.getDate("lastMaintenanceDate");
                    if (lastMaintenanceSQL != null) {
                        bin.setLastMaintenanceDate(lastMaintenanceSQL.toLocalDate());
                    }
                    
                    java.sql.Date nextMaintenanceSQL = rs.getDate("nextScheduledMaintenance");
                    if (nextMaintenanceSQL != null) {
                        bin.setNextScheduledMaintenance(nextMaintenanceSQL.toLocalDate());
                    }
                    
                    bin.setMaintenanceNotes(rs.getString("maintenanceNotes"));
                } catch (SQLException ex) {
                    // If columns don't exist yet, just continue with default values
                    System.out.println("Solar or maintenance columns may not exist yet: " + ex.getMessage());
                }
                
                solarBins.add(bin);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return solarBins;
    }
    
    /**
     * Updates the battery level of a bin and automatically sets the energy status
     * @param binId The ID of the bin to update
     * @param batteryLevel The new battery level (0-100)
     * @return true if update was successful, false otherwise
     */
    public boolean updateBinBatteryLevel(int binId, double batteryLevel) {
        Bin bin = getById(binId);
        if (bin == null || !bin.isSolarPowered()) {
            return false;
        }
        
        bin.setBatteryLevel(batteryLevel); // This will automatically update energy status
        update(bin);
        return true;
    }
    
    /**
     * Gets bins that need maintenance (battery level below 20% or maintenance overdue)
     * @return List of bins needing maintenance
     */
    public List<Bin> getBinsNeedingMaintenance() {
        List<Bin> allBins = get();
        List<Bin> needMaintenance = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        
        for (Bin bin : allBins) {
            boolean needsMaintenance = false;
            
            // Check if solar bin with low battery
            if (bin.isSolarPowered() && bin.getBatteryLevel() < 20.0) {
                needsMaintenance = true;
            }
            
            // Check if maintenance is overdue
            if (bin.getNextScheduledMaintenance() != null && 
                bin.getNextScheduledMaintenance().isBefore(today)) {
                needsMaintenance = true;
            }
            
            if (needsMaintenance) {
                needMaintenance.add(bin);
            }
        }
        
        return needMaintenance;
    }
}
