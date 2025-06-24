package com.esprit.Services;

import com.esprit.models.CommentReply;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing comment replies (nested comments).
 */
public class CommentReplyService implements IService<CommentReply> {

    private Connection conn;

    public CommentReplyService() {
        conn = DataSource.getInstance().getConnection();
    }


    public void ajouter(CommentReply reply) {
        String sql = "INSERT INTO comment_replies (contenu, date_reply, auteur_id, comment_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, reply.getContenu());
            pst.setTimestamp(2, Timestamp.valueOf(reply.getDateReply()));
            pst.setInt(3, reply.getAuteurId());
            pst.setInt(4, reply.getCommentId());
            pst.executeUpdate();
            
            // Get the generated ID
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reply.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void modifier(CommentReply reply) {
        String sql = "UPDATE comment_replies SET contenu=?, date_reply=?, auteur_id=?, comment_id=? WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, reply.getContenu());
            pst.setTimestamp(2, Timestamp.valueOf(reply.getDateReply()));
            pst.setInt(3, reply.getAuteurId());
            pst.setInt(4, reply.getCommentId());
            pst.setInt(5, reply.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimer(CommentReply reply) {
        String sql = "DELETE FROM comment_replies WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, reply.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<CommentReply> recuperer() {
        List<CommentReply> replies = new ArrayList<>();
        String sql = "SELECT * FROM comment_replies";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                CommentReply reply = new CommentReply();
                reply.setId(rs.getInt("id"));
                reply.setContenu(rs.getString("contenu"));
                reply.setDateReply(rs.getTimestamp("date_reply").toLocalDateTime());
                reply.setAuteurId(rs.getInt("auteur_id"));
                reply.setCommentId(rs.getInt("comment_id"));
                replies.add(reply);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return replies;
    }

    /**
     * Get all replies for a specific comment
     * @param commentId The parent comment ID
     * @return List of replies
     */
    public List<CommentReply> getRepliesByCommentId(int commentId) {
        List<CommentReply> replies = new ArrayList<>();
        String sql = "SELECT r.*, CONCAT(u.nom, ' ', u.prenom) as author_name FROM comment_replies r " +
                     "LEFT JOIN users u ON r.auteur_id = u.id " +
                     "WHERE r.comment_id = ? " +
                     "ORDER BY r.date_reply ASC";
                     
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, commentId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    CommentReply reply = new CommentReply();
                    reply.setId(rs.getInt("id"));
                    reply.setContenu(rs.getString("contenu"));
                    reply.setDateReply(rs.getTimestamp("date_reply").toLocalDateTime());
                    reply.setAuteurId(rs.getInt("auteur_id"));
                    reply.setCommentId(rs.getInt("comment_id"));
                    reply.setAuteurName(rs.getString("author_name"));
                    replies.add(reply);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return replies;
    }
    
    /**
     * Count the number of replies for a specific comment
     * @param commentId The parent comment ID
     * @return Number of replies
     */
    public int getReplyCountByCommentId(int commentId) {
        String sql = "SELECT COUNT(*) FROM comment_replies WHERE comment_id = ?";
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
    public List<CommentReply> get() {
        return List.of();
    }

    @Override
    public void add(CommentReply commentReply) {

    }

    @Override
    public void update(CommentReply commentReply) {

    }

    @Override
    public void delete(CommentReply commentReply) {

    }

    @Override
    public void delete(int id) {

    }
}
