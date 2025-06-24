package com.esprit.Services;

import com.esprit.models.Produit;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService implements IService<Produit> {

    private final Connection connection;

    public ProduitService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    
    public void ajouter(Produit produit) {
        String query = "INSERT INTO produit (nom, description, prix, quantiteStock, image, recyclable, categorieId, companyName, ownerId, companyLogo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, produit.getNom());
            preparedStatement.setString(2, produit.getDescription());
            preparedStatement.setDouble(3, produit.getPrix());
            preparedStatement.setInt(4, produit.getQuantiteStock());
            preparedStatement.setString(5, produit.getImage());
            preparedStatement.setBoolean(6, produit.isRecyclable());
            preparedStatement.setInt(7, produit.getCategorieId());
            preparedStatement.setString(8, produit.getCompanyName());
            preparedStatement.setInt(9, produit.getOwnerId());
            preparedStatement.setString(10, produit.getCompanyLogo());

            preparedStatement.executeUpdate();

            // Get the generated ID
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    produit.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du produit : " + e.getMessage());
        }
    }

    
    public void modifier(Produit produit) {
        String query = "UPDATE produit SET nom = ?, description = ?, prix = ?, quantiteStock = ?, image = ?, recyclable = ?, categorieId = ?, companyName = ?, ownerId = ?, companyLogo = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, produit.getNom());
            preparedStatement.setString(2, produit.getDescription());
            preparedStatement.setDouble(3, produit.getPrix());
            preparedStatement.setInt(4, produit.getQuantiteStock());
            preparedStatement.setString(5, produit.getImage());
            preparedStatement.setBoolean(6, produit.isRecyclable());
            preparedStatement.setInt(7, produit.getCategorieId());
            preparedStatement.setString(8, produit.getCompanyName());
            preparedStatement.setInt(9, produit.getOwnerId());
            preparedStatement.setString(10, produit.getCompanyLogo());
            preparedStatement.setInt(11, produit.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du produit : " + e.getMessage());
        }
    }

    
    public void supprimer(Produit produit) {   
        String query = "DELETE FROM produit WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, produit.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du produit : " + e.getMessage());
        }
    }

    
    public List<Produit> recuperer() {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Produit produit = new Produit(
                        resultSet.getInt("id"),
                        resultSet.getString("nom"),
                        resultSet.getString("description"),
                        resultSet.getDouble("prix"),
                        resultSet.getInt("quantiteStock"),
                        resultSet.getString("image"),
                        resultSet.getBoolean("recyclable"),
                        resultSet.getInt("categorieId"),
                        resultSet.getString("companyName") != null ? resultSet.getString("companyName") : "",
                        resultSet.getInt("ownerId"),
                        resultSet.getString("companyLogo") != null ? resultSet.getString("companyLogo") : ""
                );
                produits.add(produit);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des produits : " + e.getMessage());
        }
        return produits;
    }

    // New method to get product categories
    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        String query = "SELECT DISTINCT nom FROM categorie";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                categories.add(resultSet.getString("nom"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des catégories : " + e.getMessage());
        }
        return categories;
    }

    // New method to search products
    public Produit getProduitById(int id) {
        String query = "SELECT * FROM produit WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Produit(
                        resultSet.getInt("id"),
                        resultSet.getString("nom"),
                        resultSet.getString("description"),
                        resultSet.getDouble("prix"),
                        resultSet.getInt("quantiteStock"),
                        resultSet.getString("image"),
                        resultSet.getBoolean("recyclable"),
                        resultSet.getInt("categorieId"),
                        resultSet.getString("companyName") != null ? resultSet.getString("companyName") : "",
                        resultSet.getInt("ownerId"),
                        resultSet.getString("companyLogo") != null ? resultSet.getString("companyLogo") : ""
                );
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du produit : " + e.getMessage());
        }
        return null;
    }

    public List<Produit> searchProducts(String searchText) {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit WHERE nom LIKE ? OR description LIKE ? OR companyName LIKE ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, "%" + searchText + "%");
            preparedStatement.setString(2, "%" + searchText + "%");
            preparedStatement.setString(3, "%" + searchText + "%");

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Produit produit = new Produit(
                        resultSet.getInt("id"),
                        resultSet.getString("nom"),
                        resultSet.getString("description"),
                        resultSet.getDouble("prix"),
                        resultSet.getInt("quantiteStock"),
                        resultSet.getString("image"),
                        resultSet.getBoolean("recyclable"),
                        resultSet.getInt("categorieId"),
                        resultSet.getString("companyName") != null ? resultSet.getString("companyName") : "",
                        resultSet.getInt("ownerId"),
                        resultSet.getString("companyLogo") != null ? resultSet.getString("companyLogo") : ""
                );
                produits.add(produit);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche de produits : " + e.getMessage());
        }
        return produits;
    }

    public boolean productExists(String productName) {
        String query = "SELECT COUNT(*) FROM produit WHERE nom = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, productName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la vérification de l'existence du produit : " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Retrieve products by owner ID
     * @param ownerId The ID of the product owner
     * @return List of products owned by the specified owner
     */
    public List<Produit> recupererParOwnerId(int ownerId) {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit WHERE ownerId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, ownerId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Produit produit = new Produit(
                        resultSet.getInt("id"),
                        resultSet.getString("nom"),
                        resultSet.getString("description"),
                        resultSet.getDouble("prix"),
                        resultSet.getInt("quantiteStock"),
                        resultSet.getString("image"),
                        resultSet.getBoolean("recyclable"),
                        resultSet.getInt("categorieId"),
                        resultSet.getString("companyName") != null ? resultSet.getString("companyName") : "",
                        resultSet.getInt("ownerId"),
                        resultSet.getString("companyLogo") != null ? resultSet.getString("companyLogo") : ""
                );
                produits.add(produit);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des produits par propriétaire : " + e.getMessage());
        }
        return produits;
    }

   
    public String getCategoryNameById(int categoryId) {
        String query = "SELECT nom FROM categorie WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, categoryId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("nom");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du nom de la catégorie : " + e.getMessage());
        }
        return "Unknown";
    }

    public List<Produit> recupererParCategorie(int categorieId) {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit WHERE categorieId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, categorieId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Produit produit = new Produit(
                        resultSet.getInt("id"),
                        resultSet.getString("nom"),
                        resultSet.getString("description"),
                        resultSet.getDouble("prix"),
                        resultSet.getInt("quantiteStock"),
                        resultSet.getString("image"),
                        resultSet.getBoolean("recyclable"),
                        resultSet.getInt("categorieId"),
                        resultSet.getString("companyName") != null ? resultSet.getString("companyName") : "",
                        resultSet.getInt("ownerId"),
                        resultSet.getString("companyLogo") != null ? resultSet.getString("companyLogo") : ""
                );
                produits.add(produit);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des produits par catégorie : " + e.getMessage());
        }
        return produits;
    }
    
    public List<Produit> getProductsByOwnerId(int ownerId) {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit WHERE ownerId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, ownerId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Produit produit = new Produit(
                        resultSet.getInt("id"),
                        resultSet.getString("nom"),
                        resultSet.getString("description"),
                        resultSet.getDouble("prix"),
                        resultSet.getInt("quantiteStock"),
                        resultSet.getString("image"),
                        resultSet.getBoolean("recyclable"),
                        resultSet.getInt("categorieId"),
                        resultSet.getString("companyName") != null ? resultSet.getString("companyName") : "",
                        resultSet.getInt("ownerId"),
                        resultSet.getString("companyLogo") != null ? resultSet.getString("companyLogo") : ""
                );
                produits.add(produit);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des produits par propriétaire : " + e.getMessage());
        }
        return produits;
    }


    @Override
    public List<Produit> get() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }


    @Override
    public void add(Produit t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }


    @Override
    public void update(Produit t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }


    @Override
    public void delete(Produit t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }


    @Override
    public void delete(int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
}
