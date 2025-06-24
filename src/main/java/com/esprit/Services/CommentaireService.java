package com.esprit.Services;

import com.esprit.models.Commentaire;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService implements IService<Commentaire> {

    private Connection conn;

    public CommentaireService() {
        conn = DataSource.getInstance().getConnection();
    }


    public int ajouter(Commentaire commentaire) {
        String sql = "INSERT INTO commentaire (contenu, date, auteurId, blogId) VALUES (?, ?, ?, ?)";
        int generatedId = -1;

        try (PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, commentaire.getContenu());
            pst.setTimestamp(2, Timestamp.valueOf(commentaire.getDateCommentaire()));
            pst.setInt(3, commentaire.getAuteurId());
            pst.setInt(4, commentaire.getBlogId());
            pst.executeUpdate();
            
            // Get the generated ID
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return generatedId;
    }


    public void modifier(Commentaire commentaire) {
        String sql = "UPDATE commentaire SET contenu=?, date=?, auteurId=?, blogId=? WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, commentaire.getContenu());
            pst.setTimestamp(2, Timestamp.valueOf(commentaire.getDateCommentaire()));
            pst.setInt(3, commentaire.getAuteurId());
            pst.setInt(4, commentaire.getBlogId());
            pst.setInt(5, commentaire.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void supprimer(Commentaire commentaire) {
        String sql = "DELETE FROM commentaire WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, commentaire.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<Commentaire> recuperer() {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT * FROM commentaire";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Commentaire commentaire = new Commentaire();
                commentaire.setId(rs.getInt("id"));
                commentaire.setContenu(rs.getString("contenu"));
                commentaire.setDateCommentaire(rs.getTimestamp("date").toLocalDateTime());
                commentaire.setAuteurId(rs.getInt("auteurId"));
                commentaire.setBlogId(rs.getInt("blogId"));
                commentaires.add(commentaire);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return commentaires;
    }

    public List<Commentaire> recupererByBlogId(int blogId) {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT * FROM commentaire WHERE blogId = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, blogId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Commentaire commentaire = new Commentaire();
                    commentaire.setId(rs.getInt("id"));
                    commentaire.setContenu(rs.getString("contenu"));
                    commentaire.setDateCommentaire(rs.getTimestamp("date").toLocalDateTime());
                    commentaire.setAuteurId(rs.getInt("auteurId"));
                    commentaire.setBlogId(rs.getInt("blogId"));
                    commentaires.add(commentaire);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return commentaires;
    }

    public int getCommentCountByBlogId(int blogId) {
        String sql = "SELECT COUNT(*) FROM commentaire WHERE blogId = ?";
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

    // New methods for comment likes

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

    public boolean likeComment(int commentId, int userId) {
        String sql = "INSERT INTO comment_likes (comment_id, user_id, created_at) VALUES (?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, commentId);
            pst.setInt(2, userId);
            pst.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            int affectedRows = pst.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean unlikeComment(int commentId, int userId) {
        String sql = "DELETE FROM comment_likes WHERE comment_id = ? AND user_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, commentId);
            pst.setInt(2, userId);
            int affectedRows = pst.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getLikeCountForComment(int commentId) {
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

    @Override
    public List<Commentaire> get() {
        return recuperer();
    }

    @Override
    public void add(Commentaire commentaire) {
        ajouter(commentaire);
    }

    @Override
    public void update(Commentaire commentaire) {
        modifier(commentaire);
    }

    @Override
    public void delete(Commentaire commentaire) {
        supprimer(commentaire);
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM commentaire WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
