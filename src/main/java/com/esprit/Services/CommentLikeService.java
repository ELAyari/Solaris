package com.esprit.Services;

import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class for managing comment likes (voting system).
 */
public class CommentLikeService {

    private Connection conn;

    public CommentLikeService() {
        conn = DataSource.getInstance().getConnection();
    }

    /**
     * Like a comment
     * @param commentId The comment ID
     * @param userId The user ID
     * @return true if successful, false otherwise
     */
    public boolean likeComment(int commentId, int userId) {
        // First check if user has already liked this comment
        if (hasUserLikedComment(commentId, userId)) {
            return false;
        }

        // Add the like to the likes table
        String insertSql = "INSERT INTO comment_likes (comment_id, user_id) VALUES (?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(insertSql)) {
            pst.setInt(1, commentId);
            pst.setInt(2, userId);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Unlike a comment
     * @param commentId The comment ID
     * @param userId The user ID
     * @return true if successful, false otherwise
     */
    public boolean unlikeComment(int commentId, int userId) {
        // First check if user has liked this comment
        if (!hasUserLikedComment(commentId, userId)) {
            return false;
        }

        // Remove the like from the likes table
        String deleteSql = "DELETE FROM comment_likes WHERE comment_id = ? AND user_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(deleteSql)) {
            pst.setInt(1, commentId);
            pst.setInt(2, userId);
            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if a user has liked a comment
     * @param commentId The comment ID
     * @param userId The user ID
     * @return true if the user has liked the comment, false otherwise
     */
    public boolean hasUserLikedComment(int commentId, int userId) {
        String sql = "SELECT COUNT(*) FROM comment_likes WHERE comment_id = ? AND user_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, commentId);
            pst.setInt(2, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get the number of likes for a comment
     * @param commentId The comment ID
     * @return The number of likes
     */
    public int getLikeCount(int commentId) {
        String sql = "SELECT COUNT(*) FROM comment_likes WHERE comment_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, commentId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Get like counts for multiple comments
     * @param commentIds Array of comment IDs
     * @return Map of comment ID to like count
     */
    public Map<Integer, Integer> getLikeCounts(int[] commentIds) {
        Map<Integer, Integer> likeCounts = new HashMap<>();
        
        if (commentIds.length == 0) {
            return likeCounts;
        }
        
        // Build the SQL query with placeholders for all comment IDs
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT comment_id, COUNT(*) as like_count FROM comment_likes WHERE comment_id IN (");
        
        for (int i = 0; i < commentIds.length; i++) {
            sqlBuilder.append("?");
            if (i < commentIds.length - 1) {
                sqlBuilder.append(",");
            }
        }
        
        sqlBuilder.append(") GROUP BY comment_id");
        
        try (PreparedStatement pst = conn.prepareStatement(sqlBuilder.toString())) {
            // Set all the comment IDs as parameters
            for (int i = 0; i < commentIds.length; i++) {
                pst.setInt(i + 1, commentIds[i]);
            }
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int commentId = rs.getInt("comment_id");
                    int likeCount = rs.getInt("like_count");
                    likeCounts.put(commentId, likeCount);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Fill in zeros for any comments that have no likes
        for (int commentId : commentIds) {
            if (!likeCounts.containsKey(commentId)) {
                likeCounts.put(commentId, 0);
            }
        }
        
        return likeCounts;
    }
}
