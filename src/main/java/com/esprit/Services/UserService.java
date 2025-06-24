package com.esprit.Services;

import com.esprit.models.User;
import com.esprit.models.UserRole;
import com.esprit.models.UserStatus;
import com.esprit.utils.ActivityLogger;
import com.esprit.utils.DatabaseConnection;
import com.esprit.utils.UserSession;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {
    private final Connection connection;
    private final EmailVerificationService emailService;



    public UserService() throws SQLException {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        this.emailService = new EmailVerificationService();
    }

    @Override
    public void add(User user) {

    }

    @Override
    public void update(User user) {
        // Delegate to the concrete updateUser method so that callers using the
        // IService interface still persist changes. This was previously empty,
        // which meant updates like banning a user had no effect.
        if (user != null) {
            updateUser(user);
        }
    }

    @Override
    public void delete(int id) {

    }


    @Deprecated
    public boolean verifyUserCode(int userId, String code) throws SQLException {
        throw new UnsupportedOperationException("verifyUserCode is deprecated. Use EmailVerificationService.validateVerificationToken instead.");
    }

    @Override
    public List<User> get() {
        List<User> users = new ArrayList<>();
        String req = "SELECT * FROM users";

        try {
            // Test connection
            if (connection == null || connection.isClosed()) {
                System.err.println("Database connection is null or closed!");
                return users;
            }

            System.out.println("Database connection is valid, executing query: " + req);

            try (PreparedStatement pst = connection.prepareStatement(req)) {
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        try {
                            User user = mapResultSetToUser(rs);
                            // Debug output for each user
                            System.out.println("Mapped user - ID: " + user.getId() +
                                    ", Name: " + user.getNom() + " " + user.getPrenom() +
                                    ", Email: " + user.getEmail() +
                                    ", Role: " + user.getRole() +
                                    ", Status: " + user.getStatus());
                            users.add(user);
                        } catch (SQLException e) {
                            System.err.println("Error mapping user from ResultSet: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }

            System.out.println("Query completed. Found " + users.size() + " users");
            if (users.isEmpty()) {
                System.out.println("Warning: No users found in database!");
            }

        } catch (SQLException e) {
            System.err.println("Database error occurred!");
            System.err.println("Error Message: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }
        return users;
    }

    public List<User> search(String keyword) {
        List<User> users = new ArrayList<>();
        String req = "SELECT * FROM users WHERE nom LIKE ? OR prenom LIKE ? OR email LIKE ?";

        try (PreparedStatement ps = connection.prepareStatement(req)) {
            String searchTerm = "%" + keyword + "%";
            ps.setString(1, searchTerm);
            ps.setString(2, searchTerm);
            ps.setString(3, searchTerm);

            System.out.println("Executing search with keyword: " + keyword);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        users.add(mapResultSetToUser(rs));
                    } catch (SQLException e) {
                        System.err.println("Error mapping user from search ResultSet: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            System.out.println("Found " + users.size() + " users matching search");

        } catch (SQLException e) {
            System.err.println("Error searching users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    public void addUserForRegistration(User user) {
        String sql = "INSERT INTO users (nom, prenom, birth_date, email, password, adresse, telephone, role, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            if (emailExists(user.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }

            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getNom());
                ps.setString(2, user.getPrenom());
                ps.setDate(3, new java.sql.Date(user.getBirth_date().getTime()));
                ps.setString(4, user.getEmail());
                ps.setString(5, hashedPassword);
                ps.setString(6, user.getAddress());
                ps.setString(7, user.getPhoneNumber());
                ps.setString(8, user.getRole() != null ? user.getRole().toString() : "USER"); // default role USER
                ps.setString(9, UserStatus.NON_CONFIRMED.getValue()); // status non confirmed

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }

                System.out.println("User registered successfully with ID: " + user.getId());
            }

            // Create and send verification token using EmailVerificationService
            EmailVerificationService emailService = new EmailVerificationService();
            emailService.createVerificationToken(user);

        } catch (SQLException e) {
            System.err.println("Error in addUserForRegistration(): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to register user", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addUserByAdmin(User user) {
        String sql = "INSERT INTO users (nom, prenom, birth_date, email, password, adresse, telephone, role, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            if (emailExists(user.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }

            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getNom());
                ps.setString(2, user.getPrenom());
                ps.setDate(3, new java.sql.Date(user.getBirth_date().getTime()));
                ps.setString(4, user.getEmail());
                ps.setString(5, hashedPassword);
                ps.setString(6, user.getAddress());
                ps.setString(7, user.getPhoneNumber());
                ps.setString(8, user.getRole() != null ? user.getRole().toString() : null);
                ps.setString(9, user.getStatus() != null ? user.getStatus().getValue() : UserStatus.NON_CONFIRMED.getValue());

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }

                System.out.println("User added by admin successfully with ID: " + user.getId());
            }
        } catch (SQLException e) {
            System.err.println("Error in addUserByAdmin(): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to add user by admin", e);
        }
    }


    public boolean updateUser(User u) {
        final String sql =
                "UPDATE users SET " +
                        " nom       = ?, " +
                        " prenom    = ?, " +
                        " email     = ?, " +
                        " adresse   = ?, " +
                        " telephone = ?, " +
                        " role      = ?, " +
                        " status    = ?, " +
                        " birth_date= ? " +
                        "WHERE id  = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString   (1,  u.getNom());
            ps.setString   (2,  u.getPrenom());
            ps.setString   (3,  u.getEmail());
            ps.setString   (4,  u.getAddress());
            ps.setString   (5,  u.getPhoneNumber());
            ps.setString   (6,  u.getRole()   != null ? u.getRole().toString()   : null);
            ps.setString   (7,  u.getStatus() != null ? u.getStatus().getValue() : null);
            ps.setDate     (8,  u.getBirth_date() != null
                    ? new java.sql.Date(u.getBirth_date().getTime())
                    : null);
            ps.setInt      (9,  u.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                ActivityLogger.logWarning("UserService",
                        "No rows affected when updating user " + u.getId());
            }
            return rows > 0;
        } catch (SQLException ex) {
            ActivityLogger.logError("UserService",
                    "Database error updating user " + u.getId(), ex);
            return false;
        }
    }


    /**
     * Bans a user by changing their status to BANNED
     * @param user The user to ban
     * @return true if successful, false otherwise
     */
    public boolean banUser(User user) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, UserStatus.BANNED.getValue());
            ps.setInt(2, user.getId());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Banning user failed, no rows affected.");
            }

            // Update the user object status to reflect the change
            user.setStatus(UserStatus.BANNED);

            System.out.println("User banned successfully: ID=" + user.getId());
            ActivityLogger.logInfo("UserService", "User banned: ID=" + user.getId());
            return true;
        } catch (SQLException e) {
            System.err.println("Error in banUser(): " + e.getMessage());
            e.printStackTrace();
            ActivityLogger.logError("UserService", "Failed to ban user: ID=" + user.getId(), e);
            return false;
        }
    }

    @Override
    public void delete(User user) {
        String sql = "DELETE FROM users WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, user.getId());
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting user failed, no rows affected.");
            }
            System.out.println("User deleted successfully: ID=" + user.getId());
        } catch (SQLException e) {
            System.err.println("Error in delete(): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by email: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public User authenticate(String email, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (BCrypt.checkpw(password, storedHash)) {
                        return mapResultSetToUser(rs);
                    }
                }
            }
        }
        return null;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        Timestamp ts = rs.getTimestamp("birth_date");
        if (ts != null) {
            user.setBirth_date(ts);
        }
        user.setEmail(rs.getString("email"));
        user.setAddress(rs.getString("adresse")); // Note: DB column is "adresse"
        user.setPhoneNumber(rs.getString("telephone")); // Note: DB column is "telephone"
        user.setPassword(rs.getString("password"));
        user.setDescription(rs.getString("description"));

        String roleStr = rs.getString("role");
        if (roleStr != null) {
            try {
                user.setRole(UserRole.fromString(roleStr));
            } catch (IllegalArgumentException e) {
                System.err.println("Unknown role: " + roleStr + ". Setting role to null.");
                user.setRole(null);
            }
        } else {
            user.setRole(null);
        }

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                user.setStatusFromString(statusStr);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid status value in database: " + statusStr);
                user.setStatus(UserStatus.NON_CONFIRMED); // Default status
            }
        } else {
            user.setStatus(UserStatus.NON_CONFIRMED); // Default status
        }

        return user;
    }

    public int getNewUsers() {
        String sql = "SELECT COUNT(*) FROM users WHERE status = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, UserStatus.NON_CONFIRMED.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting non-confirmed users count: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public List<User> getRecentUsers() {
        List<User> nonConfirmedUsers = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE status = ? ORDER BY birth_date DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, UserStatus.NON_CONFIRMED.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    nonConfirmedUsers.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting non-confirmed users: " + e.getMessage());
            e.printStackTrace();
        }
        return nonConfirmedUsers;
    }

    public int getActiveUsers() {
        String sql = "SELECT COUNT(*) FROM users WHERE status = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, UserStatus.CONFIRMED.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting active users count: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total users count: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalPosts() {
        String sql = "SELECT COUNT(*) FROM posts";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total posts count: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalComments() {
        String sql = "SELECT COUNT(*) FROM comments";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total comments count: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalLikes() {
        String sql = "SELECT COUNT(*) FROM likes";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total likes count: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public List<User> getRecentUsers(int limit) {
        String sql = "SELECT * FROM users ORDER BY created_at DESC LIMIT ?";
        List<User> recentUsers = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    recentUsers.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting recent users: " + e.getMessage());
            e.printStackTrace();
        }
        return recentUsers;
    }

    public List<String> getRecentPosts(int limit) {
        String sql = "SELECT title, created_at FROM posts ORDER BY created_at DESC LIMIT ?";
        List<String> recentPosts = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    recentPosts.add(rs.getString("title") + " (" + rs.getTimestamp("created_at") + ")");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting recent posts: " + e.getMessage());
            e.printStackTrace();
        }
        return recentPosts;
    }

    public void Rigstration(User user) {
        // This method appears incomplete and unused, consider removing it
    }

    public int getBannedUsers() {
        String sql = "SELECT COUNT(*) FROM users WHERE status = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, UserStatus.BANNED.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting banned users count: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public User getById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));

                    // address & phone
                    user.setAddress(rs.getString("adresse"));
                    user.setPhoneNumber(rs.getString("telephone"));

                    // password (hashed)
                    user.setPassword(rs.getString("password"));

                    // birth_date → LocalDateTime
                    Timestamp ts = rs.getTimestamp("birth_date");
                    if (ts != null) {
                        user.setBirth_date(ts);
                    }

                    // role as enum
                    String roleStr = rs.getString("role");
                    if (roleStr != null) {
                        user.setRole(UserRole.valueOf(roleStr));
                    }

                    // status as enum (using your fromString helper)
                    String statusStr = rs.getString("status");
                    if (statusStr != null) {
                        user.setStatusFromString(statusStr);
                    }

                    // description
                    user.setDescription(rs.getString("description"));

                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in getById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public User getCurrentUser() {
        return UserSession.getInstance().getUser();
    }
}

