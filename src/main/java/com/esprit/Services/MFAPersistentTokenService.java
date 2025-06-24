package com.esprit.Services;

import com.esprit.models.MFAPersistentToken;
import com.esprit.utils.DataSource;

import java.sql.*;

public class MFAPersistentTokenService {
    private final Connection connection;

    public MFAPersistentTokenService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    public void addToken(MFAPersistentToken token) throws SQLException {
        String sql = "INSERT INTO mfa_persistent_tokens (user_id, device_identifier, token, expiry_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, token.getUserId());
            ps.setString(2, token.getDeviceIdentifier());
            ps.setString(3, token.getToken());
            ps.setTimestamp(4, new Timestamp(token.getExpiryDate().getTime()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    token.setId(rs.getInt(1));
                }
            }
        }
    }

    public MFAPersistentToken getTokenByUserAndDevice(int userId, String deviceIdentifier) throws SQLException {
        String sql = "SELECT * FROM mfa_persistent_tokens WHERE user_id = ? AND device_identifier = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, deviceIdentifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MFAPersistentToken token = new MFAPersistentToken();
                    token.setId(rs.getInt("id"));
                    token.setUserId(rs.getInt("user_id"));
                    token.setDeviceIdentifier(rs.getString("device_identifier"));
                    token.setToken(rs.getString("token"));
                    token.setExpiryDate(rs.getTimestamp("expiry_date"));
                    return token;
                }
            }
        }
        return null;
    }

    public void deleteToken(int tokenId) throws SQLException {
        String sql = "DELETE FROM mfa_persistent_tokens WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tokenId);
            ps.executeUpdate();
        }
    }

    public void deleteExpiredTokens() throws SQLException {
        String sql = "DELETE FROM mfa_persistent_tokens WHERE expiry_date < NOW()";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }
}
