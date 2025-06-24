package com.esprit.tests;

import com.esprit.models.UserStatus;
import com.esprit.Services.EmailVerificationService;

import java.sql.SQLException;
import java.util.Date;

public class EmailServiceTest {
    public static void main(String[] args) {
        EmailVerificationService emailService = new EmailVerificationService();

        // Create a dummy user for testing
        com.esprit.models.User testUser = new com.esprit.models.User();
        testUser.setId(1); // Set a valid user ID for testing
        testUser.setNom("TestUser");
        testUser.setPrenom("Test");
        testUser.setEmail("wael2chenoufi@gmail.com"); // Replace with your test email address
        testUser.setStatus(UserStatus.NON_CONFIRMED);

        try {
            // Create a verification token
            var token = emailService.createVerificationToken(testUser);
            System.out.println("Verification token created: " + token.getToken());

            // Validate the token
            boolean valid = emailService.validateVerificationToken(token.getToken());
            System.out.println("Token validation result: " + valid);

            // Validate an invalid token
            boolean invalid = emailService.validateVerificationToken("invalid-token");
            System.out.println("Invalid token validation result: " + invalid);

            // Simulate expired token validation (manually expire token)
            token.setExpiryDate(new Date(System.currentTimeMillis() - 1000)); // expired 1 second ago
            // Normally, you would update the token expiry in DB, but for test, just print message
            System.out.println("Simulated expired token test (manual DB update needed)");

            // Send verification email
            emailService.sendVerificationEmail(testUser, token.getToken());
            System.out.println("Test email sent successfully.");
        } catch (SQLException | java.io.IOException e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
