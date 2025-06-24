package com.esprit.Services;

import com.esprit.models.CommandeProduit;
import com.esprit.utils.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommandeProduitService implements IService<CommandeProduit> {

    private Connection connection;

    public CommandeProduitService() {
        connection = DataSource.getInstance().getConnection();
    }

    
    public void ajouter(CommandeProduit commandeProduit) {
        String query = "INSERT INTO commandeproduit (commandeId, produitId, quantite, sousTotal) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, commandeProduit.getCommandeId());
            pst.setInt(2, commandeProduit.getProduitId());
            pst.setInt(3, commandeProduit.getQuantite());
            pst.setDouble(4, commandeProduit.getSousTotal());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    
    public void modifier(CommandeProduit commandeProduit) {
        String query = "UPDATE commandeproduit SET quantite = ?, sousTotal = ? WHERE commandeId = ? AND produitId = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, commandeProduit.getQuantite());
            pst.setDouble(2, commandeProduit.getSousTotal());
            pst.setInt(3, commandeProduit.getCommandeId());
            pst.setInt(4, commandeProduit.getProduitId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public void supprimer(CommandeProduit commandeProduit) {
        String query = "DELETE FROM commandeproduit WHERE commandeId = ? AND produitId = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, commandeProduit.getCommandeId());
            pst.setInt(2, commandeProduit.getProduitId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    
    public List<CommandeProduit> recuperer() {
        List<CommandeProduit> commandeProduits = new ArrayList<>();
        String query = "SELECT * FROM commandeproduit";
        try (PreparedStatement pst = connection.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                CommandeProduit cp = new CommandeProduit(
                        rs.getInt("id"),
                        rs.getInt("commandeId"),
                        rs.getInt("produitId"),
                        rs.getInt("quantite"),
                        rs.getDouble("sousTotal")
                );
                commandeProduits.add(cp);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return commandeProduits;
    }

    public CommandeProduit trouverParIds(int commandeId, int produitId) {
        String query = "SELECT * FROM commandeproduit WHERE commandeId = ? AND produitId = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, commandeId);
            pst.setInt(2, produitId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new CommandeProduit(
                            rs.getInt("id"),
                            rs.getInt("commandeId"),
                            rs.getInt("produitId"),
                            rs.getInt("quantite"),
                            rs.getDouble("sousTotal")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<CommandeProduit> getOrderItemsByCommandeId(int commandeId) {
        List<CommandeProduit> items = new ArrayList<>();
        String query = "SELECT * FROM commandeproduit WHERE commandeId = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, commandeId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    CommandeProduit cp = new CommandeProduit(
                            rs.getInt("id"),
                            rs.getInt("commandeId"),
                            rs.getInt("produitId"),
                            rs.getInt("quantite"),
                            rs.getDouble("sousTotal")
                    );
                    items.add(cp);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching order items: " + e.getMessage());
        }
        return items;
    }

    // Alias for findByCommandeId to match usage in PDFInvoiceService
    public List<CommandeProduit> findByCommandeId(int commandeId) {
        List<CommandeProduit> items = getOrderItemsByCommandeId(commandeId);
        ProduitService produitService = new ProduitService();
        for (CommandeProduit cp : items) {
            cp.setProduit(produitService.getProduitById(cp.getProduitId()));
        }
        return items;
    }

    @Override
    public List<CommandeProduit> get() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public void add(CommandeProduit t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }

    @Override
    public void update(CommandeProduit t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(CommandeProduit t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public void delete(int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
}
