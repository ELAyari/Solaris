package com.esprit.Services;

import com.esprit.models.Commande;
import com.esprit.utils.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CommandeService implements IService<Commande> {

    private final Connection connection;

    public CommandeService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    
    public void ajouter(Commande commande) {
        // Set estimated delivery date to 4 days after order date if not set
        if (commande.getEstimatedDeliveryDate() == null && commande.getDate() != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(commande.getDate());
            cal.add(java.util.Calendar.DATE, 4);
            commande.setEstimatedDeliveryDate(cal.getTime());
        }

        String query = "INSERT INTO Commande (dateCommande, statut, total, clientId, shipmentAddress, estimatedDeliveryDate) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setTimestamp(1, new java.sql.Timestamp(commande.getDate().getTime()));
            preparedStatement.setString(2, commande.getStatut());
            preparedStatement.setDouble(3, 0.0); // initial total 0
            preparedStatement.setInt(4, commande.getClientId());
            preparedStatement.setString(5, commande.getShipmentAddress());
            if (commande.getEstimatedDeliveryDate() != null) {
                preparedStatement.setTimestamp(6, new java.sql.Timestamp(commande.getEstimatedDeliveryDate().getTime()));
            } else {
                preparedStatement.setTimestamp(6, null);
            }

            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    commande.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de la commande : " + e.getMessage());
        }
    }

    public void updateTotalForCommande(int commandeId) {
        String sumQuery = "SELECT SUM(sousTotal) AS totalSum FROM commandeproduit WHERE commandeId = ?";
        String updateQuery = "UPDATE Commande SET total = ? WHERE id = ?";
        try (PreparedStatement sumStmt = connection.prepareStatement(sumQuery);
             PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
            sumStmt.setInt(1, commandeId);
            try (ResultSet rs = sumStmt.executeQuery()) {
                if (rs.next()) {
                    double totalSum = rs.getDouble("totalSum");
                    updateStmt.setDouble(1, totalSum);
                    updateStmt.setInt(2, commandeId);
                    updateStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour du total de la commande : " + e.getMessage());
        }
    }

    
    public void modifier(Commande commande) {
        String query = "UPDATE Commande SET dateCommande = ?, statut = ?, clientId = ?, shipmentAddress = ?, estimatedDeliveryDate = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setTimestamp(1, new java.sql.Timestamp(commande.getDate().getTime()));
            preparedStatement.setString(2, commande.getStatut());
            preparedStatement.setInt(3, commande.getClientId());
            preparedStatement.setString(4, commande.getShipmentAddress());
            if (commande.getEstimatedDeliveryDate() != null) {
                preparedStatement.setTimestamp(5, new java.sql.Timestamp(commande.getEstimatedDeliveryDate().getTime()));
            } else {
                preparedStatement.setTimestamp(5, null);
            }
            preparedStatement.setInt(6, commande.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification de la commande : " + e.getMessage());
        }
    }

    
    public void supprimer(Commande commande) {
        String query = "DELETE FROM Commande WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, commande.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de la commande : " + e.getMessage());
        }
    }

    
    public List<Commande> recuperer() {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT * FROM Commande";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Commande commande = new Commande();
                commande.setId(resultSet.getInt("id"));
                commande.setDate(resultSet.getTimestamp("dateCommande"));
                commande.setStatut(resultSet.getString("statut"));
                commande.setClientId(resultSet.getInt("clientId"));
                commande.setShipmentAddress(resultSet.getString("shipmentAddress"));
                commande.setEstimatedDeliveryDate(resultSet.getTimestamp("estimatedDeliveryDate"));
                commande.setTotal(resultSet.getDouble("total")); // Set the total field
                commandes.add(commande);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des commandes : " + e.getMessage());
        }
        return commandes;
    }

    public Commande findById(int id) {
        Commande commande = null;
        String query = "SELECT * FROM commande WHERE id = ?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                commande = new Commande();
                commande.setId(rs.getInt("id"));
                commande.setDate(rs.getTimestamp("dateCommande"));
                commande.setStatut(rs.getString("statut"));
                commande.setClientId(rs.getInt("clientId"));
                commande.setShipmentAddress(rs.getString("shipmentAddress"));
                commande.setEstimatedDeliveryDate(rs.getTimestamp("estimatedDeliveryDate"));
                commande.setTotal(rs.getDouble("total")); // Set total field
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return commande;
    }
    
    // Alias method for getCommandeById to match the method name used in ProductOwnerDashboardController
    public Commande getCommandeById(int id) {
        return findById(id);
    }


    @Override
    public List<Commande> get() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }


    @Override
    public void add(Commande t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }


    @Override
    public void update(Commande t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }


    @Override
    public void delete(Commande t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }


    @Override
    public void delete(int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
}
