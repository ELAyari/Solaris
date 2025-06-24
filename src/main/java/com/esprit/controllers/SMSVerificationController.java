package com.esprit.controllers;

import com.esprit.models.User;
import com.esprit.Services.SMSVerificationService;
import com.esprit.Services.UserService;
import com.esprit.tests.MainprogGUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import com.esprit.Services.MFAPersistentTokenService;
import com.esprit.utils.DeviceIdentifierUtil;
import com.esprit.utils.TokenUtil;
import com.esprit.utils.NotificationManager;
import com.esprit.utils.NotificationManager.NotificationType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

import java.sql.SQLException;
import java.util.prefs.Preferences;

public class SMSVerificationController {
    @FXML
    private TextField tfVerificationCode;

    @FXML
    private CheckBox cbRememberDevice;

    @FXML
    private Label lblMessage;
    
    @FXML
    private Button btnBack;

    private User user;
    private UserService userService;
    private SMSVerificationService smsVerificationService = new SMSVerificationService();
    private MainprogGUI mainApp;

    private Preferences prefs = Preferences.userNodeForPackage(SMSVerificationController.class);
    private static final String PREFS_TOKEN_KEY = "mfaPersistentToken";

    public void setUser(User user) {
        this.user = user;
        
        // Initialize UserService if it's null
        if (this.userService == null) {
            try {
                this.userService = new UserService();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        
        checkStoredToken();
        
        // Ensure we have valid user data for SMS
        if (user != null && (user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty())) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("No phone number associated with this account.");
        }
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setSMSVerificationService(SMSVerificationService smsVerificationService) {
        this.smsVerificationService = smsVerificationService;
    }

    public void setMainApp(MainprogGUI mainApp) {
        this.mainApp = mainApp;
    }

    private void checkStoredToken() {
        String storedToken = prefs.get(PREFS_TOKEN_KEY, null);
        if (storedToken != null) {
            // Optionally, verify token validity with backend or auto-skip MFA
            System.out.println("Found stored persistent token: " + storedToken);
            // For now, just pre-select the checkbox
            cbRememberDevice.setSelected(true);
        }
    }
    
    @FXML
    void handleResendCode(ActionEvent event) {
        if (user == null || user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty()) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("No phone number associated with this account.");
            return;
        }
        
        try {
            // Generate and send verification code
            System.out.println("Resending SMS verification token for user ID: " + user.getId());
            smsVerificationService.createVerificationToken(user);
            
            lblMessage.setStyle("-fx-text-fill: green;");
            lblMessage.setText("Verification code resent to " + user.getPhoneNumber());
            
            // Show toast notification
            if (tfVerificationCode.getScene() != null && tfVerificationCode.getScene().getWindow() != null) {
                NotificationManager.showToast(
                    tfVerificationCode.getScene().getWindow(),
                    "Verification code resent to " + user.getPhoneNumber(),
                    NotificationType.INFO,
                    3000
                );
            }
        } catch (Exception e) {
            System.err.println("Error sending SMS verification: " + e.getMessage());
            e.printStackTrace();
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Failed to send SMS: " + e.getMessage());
        }
    }
    
    @FXML
    void handleBack(ActionEvent event) {
        if (mainApp != null && user != null) {
            mainApp.showMFAChoiceScene(user);
        }
    }

    @FXML
    void handleVerify(ActionEvent event) {
        String code = tfVerificationCode.getText();
        if (code == null || code.length() != 6 || !code.matches("\\d{6}")) {
            lblMessage.setText("Please enter a valid 6-digit code.");
            return;
        }

        try {
            boolean verified = smsVerificationService.validateVerificationToken(code);
            if (verified) {
                lblMessage.setStyle("-fx-text-fill: green;");
                lblMessage.setText("Verification successful! Redirecting...");
                user.setStatus(com.esprit.models.UserStatus.VERIFIED);
                try {
                    if (user.getRole() == null) {
                        user.setRole(com.esprit.models.UserRole.UNKNOWN);
                    }
                    userService.update(user);
                    System.out.println("User status updated to VERIFIED.");

                    if (cbRememberDevice != null && cbRememberDevice.isSelected()) {
                        String deviceIdentifier = DeviceIdentifierUtil.getDeviceIdentifier();
                        String persistentToken = TokenUtil.generateToken();
                        java.util.Date expiryDate = new java.util.Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);

                        MFAPersistentTokenService persistentTokenService = new MFAPersistentTokenService();
                        com.esprit.models.MFAPersistentToken mfaToken = new com.esprit.models.MFAPersistentToken(
                            user.getId(), deviceIdentifier, persistentToken, expiryDate);
                        persistentTokenService.addToken(mfaToken);

                        // Store persistent token in Java Preferences
                        prefs.put(PREFS_TOKEN_KEY, persistentToken);
                    } else {
                        // Remove stored token if checkbox not selected
                        prefs.remove(PREFS_TOKEN_KEY);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to update user status: " + e.getMessage());
                    e.printStackTrace();
                    lblMessage.setStyle("-fx-text-fill: red;");
                    lblMessage.setText("Failed to update user status. Please try again.");
                    return;
                }
                if (mainApp != null) {
                    System.out.println("Navigating to Admin Dashboard.");
                    mainApp.showAdminDashboardScene();
                } else {
                    System.err.println("mainApp is null, cannot navigate.");
                    lblMessage.setStyle("-fx-text-fill: red;");
                    lblMessage.setText("Internal error: Navigation failed.");
                }
            } else {
                lblMessage.setStyle("-fx-text-fill: red;");
                lblMessage.setText("Invalid or expired verification code. Please try again.");
            }
        } catch (Exception e) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Verification failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
