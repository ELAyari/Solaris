package com.esprit.controllers;

import com.esprit.models.User;
import com.esprit.tests.MainprogGUI;
import com.esprit.utils.UserSession;
import com.esprit.Services.SMSVerificationService;
import com.esprit.Services.EmailVerificationService;
import com.esprit.utils.NotificationManager;
import com.esprit.utils.NotificationManager.NotificationType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.sql.SQLException;
import java.io.IOException;

public class MFAChoiceController {

    private MainprogGUI mainApp;
    private User user;

    @FXML
    private Button btnEmail;

    @FXML
    private Button btnSMS;

    public void setMainApp(MainprogGUI mainApp) {
        this.mainApp = mainApp;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    private void handleEmail(ActionEvent event) {
        try {
            // Get user from UserSession if not already set
            if (user == null) {
                user = UserSession.getInstance().getUser();
                if (user == null) {
                    showAlert("Error", "User information is missing.");
                    return;
                }
            }
            
            // Check if user has an email
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                showAlert("No Email Address", "You don't have an email address associated with your account.");
                return;
            }
            
            System.out.println("User email: " + user.getEmail());
            
            // Create Email verification service
            EmailVerificationService emailService = new EmailVerificationService();
            
            // Generate and send verification code
            System.out.println("Generating email verification token for user ID: " + user.getId());
            try {
                emailService.createVerificationToken(user);
                
                // Show success notification
                NotificationManager.showToast(
                    btnEmail.getScene().getWindow(),
                    "Verification code sent to " + user.getEmail(),
                    NotificationType.SUCCESS,
                    3000
                );
                
                // Navigate to email verification scene
                if (mainApp != null) {
                    mainApp.showMFAVerificationScene(user, user.getEmail());
                }
            } catch (SQLException e) {
                System.err.println("Database error creating verification token: " + e.getMessage());
                e.printStackTrace();
                showAlert("Database Error", "Failed to create verification token: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Email sending error: " + e.getMessage());
                e.printStackTrace();
                showAlert("Email Error", "Failed to send email verification: " + e.getMessage());
            } catch (RuntimeException e) {
                System.err.println("Email verification error: " + e.getMessage());
                e.printStackTrace();
                showAlert("Email Error", "Failed to send email verification: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error in email verification flow: " + e.getMessage());
            e.printStackTrace();
            showAlert("Email Verification Error", "Failed during email verification setup: " + e.getMessage());
        }
    }

    @FXML
    private void handleSMS(ActionEvent event) {
        try {
            // Get user from UserSession if not already set
            if (user == null) {
                user = UserSession.getInstance().getUser();
                if (user == null) {
                    showAlert("Error", "User information is missing.");
                    return;
                }
            }
            
            // Check if user has a phone number
            if (user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty()) {
                showAlert("No Phone Number", "You don't have a phone number associated with your account. Please use email verification instead.");
                return;
            }
            
            System.out.println("User phone number: " + user.getPhoneNumber());
            
            // Create SMS verification service
            SMSVerificationService smsService = new SMSVerificationService();
            
            // Generate and send verification code
            System.out.println("Generating SMS verification token for user ID: " + user.getId());
            try {
                smsService.createVerificationToken(user);
                
                // Show success notification
                NotificationManager.showToast(
                    btnSMS.getScene().getWindow(),
                    "Verification code sent to " + user.getPhoneNumber(),
                    NotificationType.SUCCESS,
                    3000
                );
                
                // Navigate to SMS verification scene
                if (mainApp != null) {
                    mainApp.showSMSVerificationScene(user);
                }
            } catch (SQLException e) {
                System.err.println("Database error creating verification token: " + e.getMessage());
                e.printStackTrace();
                showAlert("Database Error", "Failed to create verification token: " + e.getMessage());
            } catch (RuntimeException e) {
                System.err.println("SMS sending error: " + e.getMessage());
                e.printStackTrace();
                showAlert("SMS Error", "Failed to send SMS verification: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error in SMS verification flow: " + e.getMessage());
            e.printStackTrace();
            showAlert("SMS Verification Error", "Failed during SMS verification setup: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
