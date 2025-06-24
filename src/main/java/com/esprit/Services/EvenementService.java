package com.esprit.Services;

import com.esprit.models.Evenement;
import com.esprit.models.User;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementService implements IService<Evenement>, IEventService {
    private final Connection connection;

    public EvenementService() {
        connection = DataSource.getInstance().getConnection();
    }

    @Override
    public void add(Evenement evenement) {
        String sql = """
        INSERT INTO Evenements
          (titre, description, date, adresse, organisateur,
           posterImageUrl, reservationLimit, priceCents)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, evenement.getTitre());
            ps.setString(2, evenement.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(evenement.getDate()));
            ps.setString(4, evenement.getAdresse());
            ps.setInt(5, evenement.getOrganisateur().getId());
            ps.setString(6, evenement.getPosterImageUrl());
            ps.setInt(7, evenement.getReservationLimit());
            ps.setLong(8, evenement.getPriceCents());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    evenement.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur add(): " + e.getMessage());
        }
    }

    @Override
    public void update(Evenement evenement) {
        String sql = """
        UPDATE Evenements
        SET titre             = ?,
            description      = ?,
            date         = ?,
            adresse          = ?,
            organisateur        = ?,
            posterImageUrl  = ?,
            reservationLimit= ?,
            priceCents      = ?
        WHERE id = ?
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, evenement.getTitre());
            ps.setString(2, evenement.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(evenement.getDate()));
            ps.setString(4, evenement.getAdresse());
            ps.setInt(5, evenement.getOrganisateur().getId());
            ps.setString(6, evenement.getPosterImageUrl());
            ps.setInt(7, evenement.getReservationLimit());
            ps.setLong(8, evenement.getPriceCents());
            ps.setInt(9, evenement.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur update(): " + e.getMessage());
        }
    }

    @Override
    public void delete(Evenement evenement) {

    }

    @Override
    public void delete(int evenementId) {
        String deleteParticipants = "DELETE FROM Evenement_participants WHERE Evenement_id=?";
        String deleteEvent        = "DELETE FROM Evenements WHERE id=?";
        try (PreparedStatement ps1 = connection.prepareStatement(deleteParticipants);
             PreparedStatement ps2 = connection.prepareStatement(deleteEvent)) {

            ps1.setInt(1, evenementId);
            ps1.executeUpdate();

            ps2.setInt(1, evenementId);
            ps2.executeUpdate();

        } catch (SQLException ex) {
            System.err.println("delete() error: " + ex.getMessage());
        }
    }

    @Override
    public List<Evenement> get() {
        return getAll();
    }

    public List<Evenement> getAll() {
        List<Evenement> list = new ArrayList<>();
        String query = "SELECT * FROM Evenements";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setTitre(rs.getString("titre"));
                e.setDescription(rs.getString("description"));
                e.setAdresse(rs.getString("adresse"));
                Timestamp ts = rs.getTimestamp("date");
                if (ts != null) {
                    e.setDate(ts.toLocalDateTime());
                }
                User organizer = new User();
                organizer.setId(rs.getInt("organisateur"));
                e.setOrganisateur(organizer);

                e.setPosterImageUrl(rs.getString("posterImageUrl"));
                e.setReservationLimit(rs.getInt("reservationLimit"));
                e.setPriceCents(rs.getLong("priceCents"));       // ← new

                list.add(e);
            }
        } catch (SQLException e) {
            System.err.println("getAll() error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Evenement getEvent(int id) {
        String sql = "SELECT * FROM Evenements WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Evenement e = new Evenement();
                    e.setId(id);
                    e.setTitre(rs.getString("titre"));
                    e.setDescription(rs.getString("description"));
                    e.setAdresse(rs.getString("adresse"));
                    Timestamp ts = rs.getTimestamp("date");
                    if (ts != null) {
                        e.setDate(ts.toLocalDateTime());
                    }
                    User organizer = new User();
                    organizer.setId(rs.getInt("organisateur"));
                    e.setOrganisateur(organizer);

                    e.setPosterImageUrl(rs.getString("posterImageUrl"));
                    e.setReservationLimit(rs.getInt("reservationLimit"));
                    e.setPriceCents(rs.getLong("priceCents"));   // ← new

                    return e;
                }
            }
        } catch (SQLException ex) {
            System.err.println("getEvent() error: " + ex.getMessage());
        }
        return null;
    }
}