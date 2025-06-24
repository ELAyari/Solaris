package com.esprit.Services;

import com.esprit.models.Wishlist;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WishlistService implements IService<Wishlist> {

    private final Connection connection;

    public WishlistService() {
        connection = DataSource.getInstance().getConnection();
    }

    
    public void ajouter(Wishlist wishlist) {
        String query = "INSERT INTO wishlist (user_id, product_id, date_added) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, wishlist.getUser().getId());
            stmt.setInt(2, wishlist.getProduit().getId());
            stmt.setTimestamp(3, Timestamp.valueOf(wishlist.getDateAdded()));
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                wishlist.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    
    public void modifier(Wishlist wishlist) {
        // Typically not needed for wishlist, can be left empty or implemented if needed
    }

    
    public void supprimer(Wishlist wishlist) {
        String query = "DELETE FROM wishlist WHERE user_id = ? AND product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, wishlist.getUser().getId());
            stmt.setInt(2, wishlist.getProduit().getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    
    public List<Wishlist> recuperer() {
        List<Wishlist> wishlists = new ArrayList<>();
        String query = "SELECT * FROM wishlist";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Wishlist wishlist = new Wishlist();
                wishlist.setId(rs.getInt("id"));

                int userId = rs.getInt("user_id");
                int produitId = rs.getInt("product_id");

                // Fetch User and Produit objects
                com.esprit.Services.UserService userService = new com.esprit.Services.UserService();
                com.esprit.Services.ProduitService produitService = new com.esprit.Services.ProduitService();

                // Use appropriate methods to fetch User and Produit by ID
                // Assuming UserService and ProduitService have methods findById(int id)
                wishlist.setUser(userService.getById(userId));
                wishlist.setProduit(produitService.getProduitById(produitId));

                wishlists.add(wishlist);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return wishlists;
    }
public int getWishlistCountForUser(int userId) {
        String query = "SELECT COUNT(*) FROM wishlist WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching wishlist count for user " + userId + ": " + e.getMessage());
        }
        return 0;
    }
    
    public void addProductToWishlist(int userId, int productId) throws SQLException {
        // Check if the product is already in the wishlist
        String checkQuery = "SELECT COUNT(*) FROM wishlist WHERE user_id = ? AND product_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, productId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // Product already in wishlist
                return;
            }
        }
        
        // Add the product to the wishlist
        String insertQuery = "INSERT INTO wishlist (user_id, product_id, date_added) VALUES (?, ?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
            insertStmt.setInt(1, userId);
            insertStmt.setInt(2, productId);
            insertStmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            insertStmt.executeUpdate();
        }
    }


    @Override
    public List<Wishlist> get() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }


    @Override
    public void add(Wishlist t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }


    @Override
    public void update(Wishlist t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }


    @Override
    public void delete(Wishlist t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }


    @Override
    public void delete(int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
}
