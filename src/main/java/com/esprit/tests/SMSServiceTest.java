package com.esprit.tests;

import com.esprit.models.User;
import com.esprit.Services.SMSVerificationService;

import java.sql.SQLException;
import java.util.Date;

public class SMSServiceTest {
    public static void main(String[] args) {
        SMSVerificationService smsService = new SMSVerificationService();

        // Create a dummy user for testing
        User testUser = new User();
        testUser.setId(88); // Set a valid user ID for testing
        testUser.setNom("TestUser");
        testUser.setPrenom("Test");
        testUser.setPhoneNumber("+21623803535"); // Replace with your test phone number

        try {
            // Create a verification token
            var token = smsService.createVerificationToken(testUser);
            System.out.println("Verification token created: " + token.getToken());

            // Validate the token
            boolean valid = smsService.validateVerificationToken(token.getToken());
            System.out.println("Token validation result: " + valid);

            // Validate an invalid token
            boolean invalid = smsService.validateVerificationToken("invalid-token");
            System.out.println("Invalid token validation result: " + invalid);

            // Simulate expired token validation (manually expire token)
            token.setExpiryDate(new Date(System.currentTimeMillis() - 1000)); // expired 1 second ago
            // Normally, you would update the token expiry in DB, but for test, just print message
            System.out.println("Simulated expired token test (manual DB update needed)");

            // Send verification SMS
            smsService.sendVerificationSMS(testUser, token.getToken());
            System.out.println("Test SMS sent successfully.");
        } catch (SQLException e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
