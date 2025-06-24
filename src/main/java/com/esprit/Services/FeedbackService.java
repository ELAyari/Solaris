package com.esprit.Services;

import com.esprit.models.Feedback;
import com.esprit.models.User;
import com.esprit.models.Evenement;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FeedbackService {

    private final Connection connection;

    public FeedbackService() {
        this.connection = DataSource.getInstance().getConnection();
        // Test the connection
        try {
            if (connection != null && !connection.isClosed()) {
                System.out.println("Database connection is valid for FeedbackService");
            } else {
                System.err.println("Warning: Database connection is closed or null in FeedbackService");
            }
        } catch (SQLException e) {
            System.err.println("Error checking database connection: " + e.getMessage());
        }
    }

    public void add(Feedback feedback) {
        String sql = """
            INSERT INTO feedbacks (commentaire, note, date, utilisateur_id, evenement_id)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, feedback.getCommentaire());
            ps.setInt(2, feedback.getNote());
            ps.setTimestamp(3, Timestamp.valueOf(feedback.getDate()));
            ps.setInt(4, feedback.getUtilisateur().getId());
            ps.setInt(5, feedback.getEvenement().getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur add Feedback: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Feedback> getByEvent(int evenementId) {
        List<Feedback> list = new ArrayList<>();
        String sql = "SELECT * FROM feedbacks WHERE evenement_id=?";
        System.out.println("Executing query: " + sql + " with eventId: " + evenementId);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, evenementId);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("Query executed successfully");
                int count = 0;
                while (rs.next()) {
                    count++;
                    Feedback f = new Feedback();
                    f.setId(rs.getInt("id"));
                    f.setCommentaire(rs.getString("commentaire"));
                    f.setNote(rs.getInt("note"));

                    Timestamp ts = rs.getTimestamp("date");
                    if (ts != null) {
                        f.setDate(ts.toLocalDateTime());
                    } else {
                        f.setDate(LocalDateTime.now());
                    }

                    // Load user info with UserService
                    UserService userService = new UserService() {
                        @Override
                        public void add(User user) {

                        }

                        @Override
                        public void delete(int id) {

                        }
                    };
                    int userId = rs.getInt("utilisateur_id");
                    System.out.println("Loading user with ID: " + userId);
                    User user = userService.getById(userId);
                    if (user == null) {
                        System.out.println("User not found for ID: " + userId);
                        user = new User();
                        user.setId(userId);
                        user.setNom("Unknown");
                    }
                    f.setUtilisateur(user);

                    // Set event
                    Evenement evt = new Evenement();
                    evt.setId(evenementId);
                    f.setEvenement(evt);

                    list.add(f);
                }
                System.out.println("Found " + count + " feedback records for event ID: " + evenementId);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getByEvent: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public void delete(int id) {
        String sql = "DELETE FROM feedbacks WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur delete Feedback: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void update(Feedback feedback) {
        String sql = """
            UPDATE feedbacks
               SET commentaire = ?,
                   note= ?,
                   date= ?
             WHERE id = ?
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, feedback.getCommentaire());
            ps.setInt(2, feedback.getNote());
            ps.setTimestamp(3, Timestamp.valueOf(feedback.getDate()));
            ps.setInt(4, feedback.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur update Feedback: " + e.getMessage());
            e.printStackTrace();
        }
    }
}