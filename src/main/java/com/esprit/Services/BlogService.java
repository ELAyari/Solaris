package com.esprit.Services;

import com.esprit.models.Blog;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlogService implements IService<Blog> {

    private Connection conn;

    public BlogService() {
        conn = DataSource.getInstance().getConnection();
    }

    // Existing methods...

    /**
     * Get blogs authored by the current logged-in user
     * @return List of blogs by current user
     */
    public List<Blog> getBlogsByCurrentUser() {
        try {
            int currentUserId = com.esprit.utils.UserSession.getInstance().getUser().getId();
            return getAllBlogs().stream()
                    .filter(blog -> blog.getAuteurId() == currentUserId)
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public void ajouter(Blog blog) {
        String sql = "INSERT INTO blog (titre, resume, contenu, image, datePublication, auteurId, views, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, blog.getTitre());
            pst.setString(2, blog.getResume());
            pst.setString(3, blog.getContenu());
            pst.setString(4, blog.getImage());
            pst.setTimestamp(5, Timestamp.valueOf(blog.getDatePublication()));
            pst.setInt(6, blog.getAuteurId());
            pst.setInt(7, blog.getViews());
            pst.setString(8, blog.getStatus());
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void modifier(Blog blog) {
        String sql = "UPDATE blog SET titre=?, resume=?, contenu=?, image=?, datePublication=?, auteurId=?, views=?, status=? WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, blog.getTitre());
            pst.setString(2, blog.getResume());
            pst.setString(3, blog.getContenu());
            pst.setString(4, blog.getImage());
            pst.setTimestamp(5, Timestamp.valueOf(blog.getDatePublication()));
            pst.setInt(6, blog.getAuteurId());
            pst.setInt(7, blog.getViews());
            pst.setString(8, blog.getStatus());
            pst.setInt(9, blog.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void supprimer(Blog blog) {
        String sql = "DELETE FROM blog WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, blog.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<Blog> recuperer() {
        List<Blog> blogs = new ArrayList<>();
        String sql = "SELECT * FROM blog";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Blog blog = new Blog();
                blog.setId(rs.getInt("id"));
                blog.setTitre(rs.getString("titre"));
                blog.setResume(rs.getString("resume"));
                blog.setContenu(rs.getString("contenu"));
                blog.setImage(rs.getString("image"));
                Timestamp timestamp = rs.getTimestamp("datePublication");
                if (timestamp != null) {
                    blog.setDatePublication(timestamp.toLocalDateTime());
                } else {
                    blog.setDatePublication(null);
                }
                blog.setAuteurId(rs.getInt("auteurId"));
                blog.setViews(rs.getInt("views"));
                blog.setStatus(rs.getString("status"));
                blog.setLikes(rs.getInt("like_count")); // fetch like_count column
                blogs.add(blog);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return blogs;
    }

    public List<Blog> getAllBlogs() {
        return recuperer();
    }

    public void incrementViewCount(int blogId) {
        String sql = "UPDATE blog SET views = views + 1 WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, blogId);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean likeBlog(int blogId, int userId) {
        // First check if user has already liked this blog
        if (hasUserLikedBlog(blogId, userId)) {
            return false;
        }

        // Add the like to the likes table
        String insertSql = "INSERT INTO blog_likes (blog_id, user_id) VALUES (?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(insertSql)) {
            pst.setInt(1, blogId);
            pst.setInt(2, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Update the blog's like count
        String updateSql = "UPDATE blog SET like_count = like_count + 1 WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(updateSql)) {
            pst.setInt(1, blogId);
            int affectedRows = pst.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unlikeBlog(int blogId, int userId) {
        // First check if user has liked this blog
        if (!hasUserLikedBlog(blogId, userId)) {
            return false;
        }

        // Remove the like from the likes table
        String deleteSql = "DELETE FROM blog_likes WHERE blog_id = ? AND user_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(deleteSql)) {
            pst.setInt(1, blogId);
            pst.setInt(2, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Update the blog's like count
        String updateSql = "UPDATE blog SET like_count = like_count - 1 WHERE id = ? AND like_count > 0";
        try (PreparedStatement pst = conn.prepareStatement(updateSql)) {
            pst.setInt(1, blogId);
            int affectedRows = pst.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasUserLikedBlog(int blogId, int userId) {
        String sql = "SELECT COUNT(*) FROM blog_likes WHERE blog_id = ? AND user_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, blogId);
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

    public boolean addComment(int blogId, String comment) {
        // TODO: Implement comment persistence if comments table exists
        // For now, return true as dummy implementation
        return true;
    }

    public List<String> getComments(int blogId) {
        // TODO: Implement fetching comments from database if comments table exists
        // For now, return dummy comments
        List<String> dummyComments = new java.util.ArrayList<>();
        dummyComments.add("Great post!");
        dummyComments.add("Thanks for sharing.");
        return dummyComments;
    }

    @Override
    public List<Blog> get() {
        return List.of();
    }

    @Override
    public void add(Blog blog) {

    }

    @Override
    public void update(Blog blog) {
        modifier(blog);
    }

    @Override
    public void delete(Blog blog) {

    }

    @Override
    public void delete(int id) {

    }
}
