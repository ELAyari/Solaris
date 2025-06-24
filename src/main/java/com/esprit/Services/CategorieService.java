package com.esprit.Services;

import com.esprit.models.Categorie;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService implements IService<Categorie> {

    private final Connection connection;

    public CategorieService() {
        connection = DataSource.getInstance().getConnection();
    }

    
    public void ajouter(Categorie categorie) {
        String query = "INSERT INTO Categorie(nom, description) VALUES(?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, categorie.getNom());
            stmt.setString(2, categorie.getDescription());
            stmt.executeUpdate();
            
            // Get the auto-generated ID
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                categorie.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    
    public void modifier(Categorie categorie) {
        String query = "UPDATE Categorie SET nom = ?, description = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, categorie.getNom());
            stmt.setString(2, categorie.getDescription());
            stmt.setInt(3, categorie.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    
    public void supprimer(Categorie categorie) {
        String query = "DELETE FROM Categorie WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, categorie.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    
    public List<Categorie> recuperer() {
        List<Categorie> categories = new ArrayList<>();
        String query = "SELECT * FROM Categorie";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                categories.add(new Categorie(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return categories;
    }
    
    public Categorie recupererParId(int id) {
        String query = "SELECT * FROM Categorie WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Categorie(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving category by ID: " + e.getMessage());
        }
        return null;
    }


    @Override
    public List<Categorie> get() {
        // Reuse the existing recuperer() method
        return recuperer();
    }


    @Override
    public void add(Categorie t) {
        // Reuse the existing ajouter() method
        ajouter(t);
    }


    @Override
    public void update(Categorie t) {
        // TODO Auto-generated method stub
    }


    @Override
    public void delete(Categorie t) {
        // TODO Auto-generated method stub
  }


    @Override
    public void delete(int id) {
        // TODO Auto-generated method stub
   }
}
