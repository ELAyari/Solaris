package com.esprit.Services;

import com.esprit.models.Panier;
import com.esprit.models.PanierProduit;
import com.esprit.models.Produit;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PanierService {

    private final Connection connection;

    public PanierService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    public double getTotalAmount(int utilisateurId) {
        Panier panier = getPanierByUserId(utilisateurId);
        if (panier != null) {
            return panier.getTotal();
        }
        return 0.0;
    }

    public Panier getPanierByUserId(int utilisateurId) {
        Panier panier = null;
        try {
            String panierQuery = "SELECT * FROM panier WHERE utilisateurId = ?";
            PreparedStatement panierStmt = connection.prepareStatement(panierQuery);
            panierStmt.setInt(1, utilisateurId);
            ResultSet panierRs = panierStmt.executeQuery();

            if (panierRs.next()) {
                int panierId = panierRs.getInt("id");
                double total = panierRs.getDouble("total");

                List<PanierProduit> produits = getPanierProduits(panierId);

                panier = new Panier(panierId, utilisateurId, total, produits);
            } else {
                // Create new panier if not exists
                String insertPanier = "INSERT INTO panier (utilisateurId, total) VALUES (?, 0)";
                PreparedStatement insertStmt = connection.prepareStatement(insertPanier, Statement.RETURN_GENERATED_KEYS);
                insertStmt.setInt(1, utilisateurId);
                insertStmt.executeUpdate();
                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int newPanierId = generatedKeys.getInt(1);
                    panier = new Panier(newPanierId, utilisateurId, 0.0, new ArrayList<>());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return panier;
    }

    public void clearPanier(int utilisateurId) {
        try {
            Panier panier = getPanierByUserId(utilisateurId);
            if (panier == null) {
                return;
            }
            String delete = "DELETE FROM panierproduit WHERE panierId = ?";
            PreparedStatement stmt = connection.prepareStatement(delete);
            stmt.setInt(1, panier.getId());
            stmt.executeUpdate();

            recalculatePanierTotal(panier.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<PanierProduit> getPanierProduits(int panierId) {
        List<PanierProduit> produits = new ArrayList<>();
        try {
            String query = "SELECT pp.id, pp.quantite, pp.sousTotal, p.id as produitId, p.nom, p.description, p.prix, p.quantiteStock, p.image, p.recyclable, p.categorieId " +
                    "FROM panierproduit pp " +
                    "JOIN produit p ON pp.produitId = p.id " +
                    "WHERE pp.panierId = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, panierId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Produit produit = new Produit();
                produit.setId(rs.getInt("produitId"));
                produit.setNom(rs.getString("nom"));
                produit.setDescription(rs.getString("description"));
                produit.setPrix(rs.getDouble("prix"));
                produit.setQuantiteStock(rs.getInt("quantiteStock"));
                produit.setImage(rs.getString("image"));
                produit.setRecyclable(rs.getBoolean("recyclable"));
                produit.setCategorieId(rs.getInt("categorieId"));

                PanierProduit pp = new PanierProduit();
                pp.setId(rs.getInt("id"));
                pp.setProduit(produit);
                pp.setQuantite(rs.getInt("quantite"));
                // sousTotal is calculated in setter

                produits.add(pp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return produits;
    }

    public void addProductToPanier(int utilisateurId, Produit produit, int quantite) {
        try {
            Panier panier = getPanierByUserId(utilisateurId);
            if (panier == null) {
                return;
            }
            // Check if product already in panier
            PanierProduit existing = panier.getProduits().stream()
                    .filter(pp -> pp.getProduit().getId() == produit.getId())
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                updateProductQuantity(utilisateurId, produit.getId(), existing.getQuantite() + quantite);
            } else {
                String insert = "INSERT INTO panierproduit (quantite, sousTotal, panierId, produitId) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = connection.prepareStatement(insert);
                stmt.setInt(1, quantite);
                stmt.setDouble(2, quantite * produit.getPrix());
                stmt.setInt(3, panier.getId());
                stmt.setInt(4, produit.getId());
                stmt.executeUpdate();
            }
            recalculatePanierTotal(panier.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateProductQuantity(int utilisateurId, int produitId, int newQuantity) {
        try {
            Panier panier = getPanierByUserId(utilisateurId);
            if (panier == null) {
                return;
            }
            if (newQuantity <= 0) {
                removeProductFromPanier(utilisateurId, produitId);
                return;
            }
            String update = "UPDATE panierproduit SET quantite = ?, sousTotal = ? WHERE panierId = ? AND produitId = ?";
            PreparedStatement stmt = connection.prepareStatement(update);
            stmt.setInt(1, newQuantity);
            // Get product price
            double prix = getProductPrice(produitId);
            stmt.setDouble(2, newQuantity * prix);
            stmt.setInt(3, panier.getId());
            stmt.setInt(4, produitId);
            stmt.executeUpdate();

            recalculatePanierTotal(panier.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeProductFromPanier(int utilisateurId, int produitId) {
        try {
            Panier panier = getPanierByUserId(utilisateurId);
            if (panier == null) {
                return;
            }
            String delete = "DELETE FROM panierproduit WHERE panierId = ? AND produitId = ?";
            PreparedStatement stmt = connection.prepareStatement(delete);
            stmt.setInt(1, panier.getId());
            stmt.setInt(2, produitId);
            stmt.executeUpdate();

            recalculatePanierTotal(panier.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void recalculatePanierTotal(int panierId) {
        try {
            String sumQuery = "SELECT SUM(sousTotal) as total FROM panierproduit WHERE panierId = ?";
            PreparedStatement stmt = connection.prepareStatement(sumQuery);
            stmt.setInt(1, panierId);
            ResultSet rs = stmt.executeQuery();
            double total = 0;
            if (rs.next()) {
                total = rs.getDouble("total");
            }
            String updatePanier = "UPDATE panier SET total = ? WHERE id = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updatePanier);
            updateStmt.setDouble(1, total);
            updateStmt.setInt(2, panierId);
            updateStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private double getProductPrice(int produitId) {
        double prix = 0;
        try {
            String query = "SELECT prix FROM produit WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, produitId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                prix = rs.getDouble("prix");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prix;
    }

    public int getPanierItemCount(int utilisateurId) {
        int count = 0;
        try {
            Panier panier = getPanierByUserId(utilisateurId);
            if (panier != null) {
                String query = "SELECT SUM(quantite) as total FROM panierproduit WHERE panierId = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, panier.getId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    count = rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public int getCountForUser(int userId) {
        try {
            String query = "SELECT COUNT(*) FROM panier_produit pp " +
                    "JOIN panier p ON pp.panierId = p.id " +
                    "WHERE p.utilisateurId = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error getting cart count: " + e.getMessage());
            return 0;
        }
    }
}
