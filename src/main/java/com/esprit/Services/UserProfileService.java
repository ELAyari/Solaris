package com.esprit.Services;

import com.esprit.models.UserProfile;
import com.esprit.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserProfileService {

    private final Connection connection;

    public UserProfileService() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    public UserProfile getUserProfileByUserId(int userId) {
        String sql = "SELECT * FROM user_profiles WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserProfile profile = new UserProfile();
                    profile.setId(rs.getInt("id"));
                    profile.setUserId(rs.getInt("user_id"));
                    profile.setProfilePhotoUrl(rs.getString("profile_photo_url"));
                    profile.setDescription(rs.getString("description"));
                    profile.setCreatedAt(rs.getTimestamp("created_at"));
                    profile.setUpdatedAt(rs.getTimestamp("updated_at"));
                    return profile;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user profile for userId " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateProfilePhoto(int userId, String photoUrl) {
        String sql = "UPDATE user_profiles SET profile_photo_url = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, photoUrl);
            ps.setInt(2, userId);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating profile photo for userId " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserProfile(int userId, String photoUrl, String description) {
        String sql = "UPDATE user_profiles SET profile_photo_url = ?, description = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, photoUrl);
            ps.setString(2, description);
            ps.setInt(3, userId);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user profile for userId " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertUserProfile(int userId, String photoUrl, String description) {
        String sql = "INSERT INTO user_profiles (user_id, profile_photo_url, description, created_at, updated_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, photoUrl);
            ps.setString(3, description);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting user profile for userId " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean userProfileExists(int userId) {
        String sql = "SELECT COUNT(*) FROM user_profiles WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking existence of user profile for userId " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Save or update a user profile including the profile photo URL
     * This method will update an existing profile or create a new one if it doesn't exist
     *
     * @param userProfile The user profile to save or update
     * @return true if successful, false otherwise
     */
    public boolean saveOrUpdateUserProfile(UserProfile userProfile) {
        if (userProfile == null) {
            return false;
        }

        try {
            // Check if profile already exists
            boolean exists = userProfileExists(userProfile.getUserId());
            if (exists) {
                // Update existing profile
                String sql = "UPDATE user_profiles SET profile_photo_url = ?, description = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, userProfile.getProfilePhotoUrl());
                    ps.setString(2, userProfile.getDescription());
                    ps.setInt(3, userProfile.getUserId());
                    int rows = ps.executeUpdate();
                    return rows > 0;
                }
            } else {
                // Create new profile
                String sql = "INSERT INTO user_profiles (user_id, profile_photo_url, description, created_at, updated_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, userProfile.getUserId());
                    ps.setString(2, userProfile.getProfilePhotoUrl());
                    ps.setString(3, userProfile.getDescription());
                    int rows = ps.executeUpdate();
                    return rows > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving/updating user profile for userId " + userProfile.getUserId() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
