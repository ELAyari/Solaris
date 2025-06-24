package com.esprit.Services;

import com.esprit.models.SMSVerificationToken;
import com.esprit.models.User;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.sql.*;
import java.util.Date;
import java.security.SecureRandom;

public class SMSVerificationService {
    private final Connection connection;
    private final String ACCOUNT_SID = "ACa69e3a3ddf7a3659f305b482412234fd"; // TODO: Replace with actual Twilio Account SID
    private final String AUTH_TOKEN = "5cf22346b705255fdeb10083a4e945dd";   // TODO: Replace with actual Twilio Auth Token
    private final String FROM_PHONE_NUMBER = "+16168832876";       // TODO: Replace with your Twilio phone number

    public SMSVerificationService() {
        this.connection = com.esprit.utils.DataSource.getInstance().getConnection();
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public SMSVerificationToken createVerificationToken(User user) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        if (user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("User does not have a phone number");
        }
        
        // Log phone number for debugging
        System.out.println("Creating verification token for user ID: " + user.getId());
        System.out.println("Phone number: " + user.getPhoneNumber());
        
        // Generate 6-digit random numeric code
        SecureRandom random = new SecureRandom();
        int codeInt = 100000 + random.nextInt(900000);
        String code = String.valueOf(codeInt);
        Date expiryDate = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000); // 24 hours
        
        System.out.println("Generated verification code: " + code);

        // Remove existing tokens for this user
        String deleteSql = "DELETE FROM sms_verification_tokens WHERE user_id = ?";
        try (PreparedStatement deletePs = connection.prepareStatement(deleteSql)) {
            deletePs.setInt(1, user.getId());
            int rowsDeleted = deletePs.executeUpdate();
            System.out.println("Deleted " + rowsDeleted + " existing token(s) for user ID: " + user.getId());
        }

        // Insert new token
        String insertSql = "INSERT INTO sms_verification_tokens (user_id, token, expiry_date) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, user.getId());
            ps.setString(2, code);
            ps.setTimestamp(3, new Timestamp(expiryDate.getTime()));

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating verification code failed, no rows affected.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    SMSVerificationToken verificationToken = new SMSVerificationToken(user.getId(), code, expiryDate);
                    verificationToken.setId(rs.getInt(1));
                    
                    try {
                        // Send SMS with the code
                        sendVerificationSMS(user, code);
                        System.out.println("SMS verification sent successfully");
                    } catch (Exception e) {
                        System.err.println("Failed to send SMS, but token was created: " + e.getMessage());
                        e.printStackTrace();
                        // Don't rethrow the exception so the token is still created
                        // We can try resending later
                    }
                    
                    return verificationToken;
                } else {
                    throw new SQLException("Creating verification code failed, no ID obtained.");
                }
            }
        }
    }
    
    public void sendVerificationSMS(User user, String code) {
        try {
            String toPhoneNumber = user.getPhoneNumber();
            
            // Ensure phone number is in correct E.164 format (+[country code][phone number])
            if (!toPhoneNumber.startsWith("+")) {
                // If phone number doesn't start with +, assume it's a local number and add +216 (Tunisia)
                toPhoneNumber = "+216" + toPhoneNumber;
                System.out.println("Formatted phone number to E.164 format: " + toPhoneNumber);
            }
            
            String messageBody = "Dear " + user.getPrenom() + ", your SMS verification code is: " + code;
            
            System.out.println("Attempting to send SMS to: " + toPhoneNumber);
            System.out.println("Message body: " + messageBody);
            System.out.println("Using Twilio phone number: " + FROM_PHONE_NUMBER);
            System.out.println("Using Twilio account SID: " + ACCOUNT_SID.substring(0, 6) + "...");

            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(FROM_PHONE_NUMBER),
                    messageBody
            ).create();

            System.out.println("SMS sent successfully with SID: " + message.getSid());
            System.out.println("SMS Status: " + message.getStatus());
        } catch (com.twilio.exception.ApiException e) {
            System.err.println("Twilio API Error: " + e.getMessage());
            System.err.println("Error Code: " + e.getCode());
            System.err.println("More info: " + e.getMoreInfo());
            throw new RuntimeException("Failed to send SMS: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Unexpected error sending SMS: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send SMS: " + e.getMessage(), e);
        }
    }
    
    public boolean validateVerificationToken(String token) throws SQLException {
        String sql = "SELECT user_id, expiry_date FROM sms_verification_tokens WHERE token = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    Timestamp expiryDate = rs.getTimestamp("expiry_date");

                    if (expiryDate.before(new Timestamp(System.currentTimeMillis()))) {
                        deleteToken(token);
                        return false; // Token expired
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void deleteToken(String token) throws SQLException {
        String sql = "DELETE FROM sms_verification_tokens WHERE token = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.executeUpdate();
        }
    }
    
    /**
     * Debug method to test SMS sending functionality
     * @param phoneNumber The phone number to send test SMS to
     * @return True if SMS was sent successfully, false otherwise
     */
    public boolean testSMSSending(String phoneNumber) {
        try {
            System.out.println("===== SMS VERIFICATION DEBUG =====");
            System.out.println("Testing SMS sending to: " + phoneNumber);
            
            // Format phone number if needed
            if (!phoneNumber.startsWith("+")) {
                phoneNumber = "+216" + phoneNumber;
                System.out.println("Formatted phone number to: " + phoneNumber);
            }
            
            // Print Twilio config
            System.out.println("Twilio Account SID: " + ACCOUNT_SID.substring(0, 6) + "...");
            System.out.println("Twilio Auth Token: " + AUTH_TOKEN.substring(0, 3) + "...");
            System.out.println("Twilio Phone Number: " + FROM_PHONE_NUMBER);
            
            // Check if Twilio is initialized
            try {
                // Send a test message
                Message message = Message.creator(
                        new PhoneNumber(phoneNumber),
                        new PhoneNumber(FROM_PHONE_NUMBER),
                        "This is a test message from your app."
                ).create();
                
                System.out.println("Test SMS sent successfully!");
                System.out.println("Message SID: " + message.getSid());
                System.out.println("Message Status: " + message.getStatus());
                System.out.println("================================");
                return true;
            } catch (Exception e) {
                System.err.println("Error during Twilio SMS test:");
                System.err.println(e.getMessage());
                e.printStackTrace();
                System.out.println("================================");
                return false;
            }
        } catch (Exception e) {
            System.err.println("General error in testSMSSending:");
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.out.println("================================");
            return false;
        }
    }
}
