package com.esprit.Services;

import com.esprit.utils.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for tracking and retrieving blog analytics data
 */
public class BlogAnalyticsService {
    
    private Connection conn;
    
    public BlogAnalyticsService() {
        conn = DataSource.getInstance().getConnection();
    }
    
    /**
     * Record a blog view
     * @param blogId The blog ID
     */
    public void recordView(int blogId) {
        // Update the blog's view count in the blog table
        String updateBlogSql = "UPDATE blog SET views = views + 1 WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(updateBlogSql)) {
            pst.setInt(1, blogId);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Record the view in the analytics log
        String logSql = "INSERT INTO analytics_log (blog_id, action_type, action_date) VALUES (?, 'VIEW', NOW())";
        try (PreparedStatement pst = conn.prepareStatement(logSql)) {
            pst.setInt(1, blogId);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Record a blog like
     * @param blogId The blog ID
     */
    public void recordLike(int blogId) {
        // Update the blog's like count in the blog table
        String updateBlogSql = "UPDATE blog SET likes = likes + 1 WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(updateBlogSql)) {
            pst.setInt(1, blogId);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Record the like in the analytics log
        String logSql = "INSERT INTO analytics_log (blog_id, action_type, action_date) VALUES (?, 'LIKE', NOW())";
        try (PreparedStatement pst = conn.prepareStatement(logSql)) {
            pst.setInt(1, blogId);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Record a blog unlike (removing a like)
     * @param blogId The blog ID
     */
    public void recordUnlike(int blogId) {
        // Update the blog's like count in the blog table
        String updateBlogSql = "UPDATE blog SET likes = GREATEST(likes - 1, 0) WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(updateBlogSql)) {
            pst.setInt(1, blogId);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Record the unlike in the analytics log
        String logSql = "INSERT INTO analytics_log (blog_id, action_type, action_date) VALUES (?, 'UNLIKE', NOW())";
        try (PreparedStatement pst = conn.prepareStatement(logSql)) {
            pst.setInt(1, blogId);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Record a blog comment
     * @param blogId The blog ID
     */
    public void recordComment(int blogId) {
        // Record the comment in the analytics log
        String logSql = "INSERT INTO analytics_log (blog_id, action_type, action_date) VALUES (?, 'COMMENT', NOW())";
        try (PreparedStatement pst = conn.prepareStatement(logSql)) {
            pst.setInt(1, blogId);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Record a blog share
     * @param blogId The blog ID
     * @param platform The platform where the blog was shared (e.g., "facebook", "twitter")
     */
    public void recordShare(int blogId, String platform) {
        // Record the share in the analytics log
        String logSql = "INSERT INTO analytics_log (blog_id, action_type, action_details, action_date) VALUES (?, 'SHARE', ?, NOW())";
        try (PreparedStatement pst = conn.prepareStatement(logSql)) {
            pst.setInt(1, blogId);
            pst.setString(2, platform);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get view count for a blog
     * @param blogId The blog ID
     * @return The view count
     */
    public int getViewCount(int blogId) {
        String sql = "SELECT views FROM blog WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, blogId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("views");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Get like count for a blog
     * @param blogId The blog ID
     * @return The like count
     */
    public int getLikeCount(int blogId) {
        String sql = "SELECT likes FROM blog WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, blogId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("likes");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Get comment count for a blog
     * @param blogId The blog ID
     * @return The comment count
     */
    public int getCommentCount(int blogId) {
        String sql = "SELECT COUNT(*) FROM commentaire WHERE blog_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, blogId);
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
     * Get share count for a blog
     * @param blogId The blog ID
     * @return The share count
     */
    public int getShareCount(int blogId) {
        String sql = "SELECT COUNT(*) FROM analytics_log WHERE blog_id = ? AND action_type = 'SHARE'";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, blogId);
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
     * Get daily analytics for a blog
     * @param blogId The blog ID
     * @return Map containing daily analytics data
     */
    public Map<String, Integer> getDailyAnalytics(int blogId) {
        Map<String, Integer> analytics = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        // Get daily views
        String viewsSql = "SELECT COUNT(*) FROM analytics_log WHERE blog_id = ? AND action_type = 'VIEW' AND DATE(action_date) = ?";
        try (PreparedStatement pst = conn.prepareStatement(viewsSql)) {
            pst.setInt(1, blogId);
            pst.setString(2, today.toString());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    analytics.put("views", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            analytics.put("views", 0);
        }
        
        // Get daily likes
        String likesSql = "SELECT COUNT(*) FROM analytics_log WHERE blog_id = ? AND action_type = 'LIKE' AND DATE(action_date) = ?";
        try (PreparedStatement pst = conn.prepareStatement(likesSql)) {
            pst.setInt(1, blogId);
            pst.setString(2, today.toString());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    analytics.put("likes", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            analytics.put("likes", 0);
        }
        
        // Get daily comments
        String commentsSql = "SELECT COUNT(*) FROM analytics_log WHERE blog_id = ? AND action_type = 'COMMENT' AND DATE(action_date) = ?";
        try (PreparedStatement pst = conn.prepareStatement(commentsSql)) {
            pst.setInt(1, blogId);
            pst.setString(2, today.toString());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    analytics.put("comments", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            analytics.put("comments", 0);
        }
        
        // Get daily shares
        String sharesSql = "SELECT COUNT(*) FROM analytics_log WHERE blog_id = ? AND action_type = 'SHARE' AND DATE(action_date) = ?";
        try (PreparedStatement pst = conn.prepareStatement(sharesSql)) {
            pst.setInt(1, blogId);
            pst.setString(2, today.toString());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    analytics.put("shares", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            analytics.put("shares", 0);
        }
        
        return analytics;
    }
    
    /**
     * Get weekly analytics for a blog
     * @param blogId The blog ID
     * @return Map containing weekly analytics data
     */
    public Map<String, Integer> getWeeklyAnalytics(int blogId) {
        Map<String, Integer> analytics = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        
        // Get weekly views
        String viewsSql = "SELECT COUNT(*) FROM analytics_log WHERE blog_id = ? AND action_type = 'VIEW' AND DATE(action_date) BETWEEN ? AND ?";
        try (PreparedStatement pst = conn.prepareStatement(viewsSql)) {
            pst.setInt(1, blogId);
            pst.setString(2, weekAgo.toString());
            pst.setString(3, today.toString());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    analytics.put("views", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            analytics.put("views", 0);
        }
        
        // Get weekly likes
        String likesSql = "SELECT COUNT(*) FROM analytics_log WHERE blog_id = ? AND action_type = 'LIKE' AND DATE(action_date) BETWEEN ? AND ?";
        try (PreparedStatement pst = conn.prepareStatement(likesSql)) {
            pst.setInt(1, blogId);
            pst.setString(2, weekAgo.toString());
            pst.setString(3, today.toString());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    analytics.put("likes", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            analytics.put("likes", 0);
        }
        
        // Get weekly comments
        String commentsSql = "SELECT COUNT(*) FROM analytics_log WHERE blog_id = ? AND action_type = 'COMMENT' AND DATE(action_date) BETWEEN ? AND ?";
        try (PreparedStatement pst = conn.prepareStatement(commentsSql)) {
            pst.setInt(1, blogId);
            pst.setString(2, weekAgo.toString());
            pst.setString(3, today.toString());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    analytics.put("comments", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            analytics.put("comments", 0);
        }
        
        // Get weekly shares
        String sharesSql = "SELECT COUNT(*) FROM analytics_log WHERE blog_id = ? AND action_type = 'SHARE' AND DATE(action_date) BETWEEN ? AND ?";
        try (PreparedStatement pst = conn.prepareStatement(sharesSql)) {
            pst.setInt(1, blogId);
            pst.setString(2, weekAgo.toString());
            pst.setString(3, today.toString());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    analytics.put("shares", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            analytics.put("shares", 0);
        }
        
        return analytics;
    }
    
    /**
     * Get monthly analytics for a blog
     * @param blogId The blog ID
     * @return Map containing monthly analytics data
     */
    public Map<String, Integer> getMonthlyAnalytics(int blogId) {
        Map<String, Integer> analytics = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate monthAgo = today.minusMonths(1);
        
        // Get monthly views
        String viewsSql = "SELECT COUNT(*) FROM analytics_log WHERE blog_id = ? AND action_type = 'VIEW' AND DATE(action_date) BETWEEN ? AND ?";
        try (PreparedStatement pst = conn.prepareStatement(viewsSql)) {
            pst.setInt(1, blogId);
            pst.setString(2, monthAgo.toString());
            pst.setString(3, today.toString());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    analytics.put("views", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            analytics.put("views", 0);
        }
        
        // Get monthly likes
        String likesSql = "SELECT COUNT(*) FROM analytics_log WHERE blog_id = ? AND action_type = 'LIKE' AND DATE(action_date) BETWEEN ? AND ?";
        try (PreparedStatement pst = conn.prepareStatement(likesSql)) {
            pst.setInt(1, blogId);
            pst.setString(2, monthAgo.toString());
            pst.setString(3, today.toString());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    analytics.put("likes", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            analytics.put("likes", 0);
        }
        
        // Get monthly comments
        String commentsSql = "SELECT COUNT(*) FROM analytics_log WHERE blog_id = ? AND action_type = 'COMMENT' AND DATE(action_date) BETWEEN ? AND ?";
        try (PreparedStatement pst = conn.prepareStatement(commentsSql)) {
            pst.setInt(1, blogId);
            pst.setString(2, monthAgo.toString());
            pst.setString(3, today.toString());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    analytics.put("comments", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            analytics.put("comments", 0);
        }
        
        // Get monthly shares
        String sharesSql = "SELECT COUNT(*) FROM analytics_log WHERE blog_id = ? AND action_type = 'SHARE' AND DATE(action_date) BETWEEN ? AND ?";
        try (PreparedStatement pst = conn.prepareStatement(sharesSql)) {
            pst.setInt(1, blogId);
            pst.setString(2, monthAgo.toString());
            pst.setString(3, today.toString());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    analytics.put("shares", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            analytics.put("shares", 0);
        }
        
        return analytics;
    }
    
    /**
     * Get analytics data for all blogs
     * @return Map of blog IDs to their analytics data
     */
    public Map<Integer, Map<String, Integer>> getAllBlogsAnalytics() {
        Map<Integer, Map<String, Integer>> allAnalytics = new HashMap<>();
        
        // Get all blog IDs
        String blogsSql = "SELECT id FROM blog";
        try (PreparedStatement pst = conn.prepareStatement(blogsSql);
             ResultSet rs = pst.executeQuery()) {
            
            while (rs.next()) {
                int blogId = rs.getInt("id");
                Map<String, Integer> blogAnalytics = new HashMap<>();
                
                // Get total views, likes, comments, and shares for each blog
                blogAnalytics.put("views", getViewCount(blogId));
                blogAnalytics.put("likes", getLikeCount(blogId));
                blogAnalytics.put("comments", getCommentCount(blogId));
                blogAnalytics.put("shares", getShareCount(blogId));
                
                allAnalytics.put(blogId, blogAnalytics);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return allAnalytics;
    }
}
