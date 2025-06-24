package com.esprit.controllers;

import com.esprit.models.User;
import com.esprit.Services.ConcreteUserService;
import com.esprit.Services.UserService;
import com.esprit.utils.AnimationUtils;
import com.esprit.utils.NotificationManager;
import com.esprit.utils.NotificationManager.NotificationType;
import com.esprit.utils.UserSession;
import com.esprit.utils.RememberMeUtil;
import com.esprit.tests.MainprogGUI;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.SQLException;

public class LoginController {
    private MainprogGUI mainApp;

    @FXML
    private TextField tfEmail;

    @FXML
    private PasswordField tfPassword;

    @FXML
    private CheckBox cbRememberMe;

    @FXML
    private Label lblErrorMessage;

    @FXML
    private ProgressIndicator progressIndicator;
    
    @FXML
    private VBox loginFormContainer;
    
    @FXML
    private ImageView logoImage;
    
    @FXML
    private Button btnLogin;
    
    @FXML
    private Button btnForgetPassword;
    
    @FXML
    private Button btnRegister;

    public void setMainApp(MainprogGUI mainApp) {
        this.mainApp = mainApp;
        loadRememberedUser();
        applyAnimations();
    }
    
    /**
     * Apply animations to the login form elements with optimized performance
     */
    @FXML
    private void applyAnimations() {
        // Set initial states - start with opacity 0
        if (logoImage != null) logoImage.setOpacity(0);
        if (loginFormContainer != null) {
            loginFormContainer.setOpacity(0);
            loginFormContainer.setTranslateY(30);
        }
        
        // Animate logo first
        if (logoImage != null) {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(800), logoImage);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(600), logoImage);
            scaleIn.setFromX(0.8);
            scaleIn.setFromY(0.8);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            
            fadeIn.play();
            scaleIn.play();
            
            // Add subtle pulsing effect to logo
            scaleIn.setOnFinished(e -> AnimationUtils.pulse(logoImage));
        }
        
        // Animate login form with slight delay
        if (loginFormContainer != null) {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), loginFormContainer);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.millis(300));
            
            TranslateTransition slideUp = new TranslateTransition(Duration.millis(600), loginFormContainer);
            slideUp.setFromY(30);
            slideUp.setToY(0);
            slideUp.setDelay(Duration.millis(300));
            
            fadeIn.play();
            slideUp.play();
            
            // After login form appears, animate the input fields and buttons sequentially
            fadeIn.setOnFinished(e -> animateLoginFormElements());
        }
    }
    
    /**
     * Animate the individual elements within the login form
     */
    private void animateLoginFormElements() {
        if (loginFormContainer == null) return;
        
        // Get all nodes in the form container
        int delay = 0;
        for (Node node : loginFormContainer.getChildren()) {
            // Skip nodes that are already visible (like labels)
            if (node instanceof Label) continue;
            
            // Set initial opacity
            node.setOpacity(0);
            
            // Create fade-in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), node);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.millis(delay));
            
            // Create slide animation
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), node);
            slideIn.setFromX(-20);
            slideIn.setToX(0);
            slideIn.setDelay(Duration.millis(delay));
            
            // Play animations
            fadeIn.play();
            slideIn.play();
            
            // Increase delay for next element
            delay += 100;
        }
        
        // Apply special effects to buttons
        if (btnLogin != null) {
            btnLogin.getStyleClass().add("button-modern");
            btnLogin.getStyleClass().add("button-press");
        }
        
        if (btnForgetPassword != null) {
            btnForgetPassword.getStyleClass().add("link-button");
        }
        
        if (btnRegister != null) {
            btnRegister.getStyleClass().add("button-modern-outlined");
            btnRegister.getStyleClass().add("button-press");
        }
    }

    @FXML
    private void handleEmail(ActionEvent event) {
        if (mainApp != null && mainApp.getUser() != null) {
            mainApp.showMFAVerificationScene(mainApp.getUser(), mainApp.getUser().getEmail());
        } else {
            showAlert("Error", "User information is missing.");
        }
    }

    @FXML
    private void handleSMS(ActionEvent event) {
        if (mainApp != null && mainApp.getUser() != null) {
            mainApp.showMFAVerificationScene(mainApp.getUser(), null);
        } else {
            showAlert("Error", "User information is missing.");
        }
    }

    private void loadRememberedUser() {
        String savedEmail = RememberMeUtil.getSavedEmail();
        String savedPassword = RememberMeUtil.getSavedPassword();
        if (savedEmail != null) {
            tfEmail.setText(savedEmail);
            cbRememberMe.setSelected(true);
        }
        if (savedPassword != null) {
            tfPassword.setText(savedPassword);
        }
    }

    @FXML
    void handleForgetPassword(ActionEvent event) {
        System.out.println("handleForgetPassword called");
        if (mainApp != null) {
            System.out.println("mainApp is not null, navigating to ForgetPasswordScene");
            mainApp.showForgetPasswordScene();
        } else {
            System.out.println("mainApp is null in handleForgetPassword");
            showAlert("Error", "Application reference missing");
        }
    }

    /**
     * Handles the login process including authentication, MFA verification, and role-based routing.
     * Integrates with MFA persistent tokens for seamless returning user experience.
     */
    @FXML
    void handleLogin(ActionEvent event) {
        lblErrorMessage.setVisible(false);
        progressIndicator.setVisible(true);

        // Button press animation
        AnimationUtils.pulse(btnLogin);

        // Basic validation
        if (tfEmail.getText().isEmpty() || tfPassword.getText().isEmpty()) {
            progressIndicator.setVisible(false);
            lblErrorMessage.setText("Please fill in all fields");
            lblErrorMessage.setVisible(true);
            return;
        }
        
        // Basic email format validation
        if (!tfEmail.getText().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            progressIndicator.setVisible(false);
            lblErrorMessage.setText("Please enter a valid email address");
            lblErrorMessage.setVisible(true);
            return;
        }

        try {
            UserService userService = new UserService() {
                @Override
                public void add(User user) {
                    // Implementation not needed for login
                }

                @Override
                public void delete(int id) {
                    // Implementation not needed for login
                }
            };

            // Log authentication attempt (without password)
            System.out.println("Login attempt for email: " + tfEmail.getText());

            User user = userService.authenticate(tfEmail.getText(), tfPassword.getText());
            if (user != null) {
                System.out.println("Login successful!");
                if (mainApp != null) {
                    System.setProperty("currentUser", user.getEmail());
                    // Initialize UserSession with logged-in user
                    UserSession.getInstance().setUser(user);
                    System.out.println("Logged in user ID = " + UserSession.getInstance().getUser().getId());
                    
                    // Handle remember me functionality
                    if (cbRememberMe.isSelected()) {
                        RememberMeUtil.saveEmail(user.getEmail());
                        RememberMeUtil.savePassword(tfPassword.getText());
                    } else {
                        RememberMeUtil.clearAll();
                    }

                    // Check for saved device token to skip MFA
                    String deviceIdentifier = com.esprit.utils.DeviceIdentifierUtil.getDeviceIdentifier();
                    com.esprit.Services.MFAPersistentTokenService persistentTokenService = new com.esprit.Services.MFAPersistentTokenService();
                    com.esprit.models.MFAPersistentToken persistentToken = persistentTokenService.getTokenByUserAndDevice(UserSession.getInstance().getUser().getId(), deviceIdentifier);

                    boolean skipMFA = false; // Default to requiring MFA
                    if (persistentToken != null) {
                        // Check if the persistent token is still valid
                        if (persistentToken.getExpiryDate().after(new java.util.Date())) {
                            skipMFA = true; // Valid token, skip MFA
                            System.out.println("Valid MFA token found - skipping MFA verification");
                        } else {
                            System.out.println("MFA token expired - requiring verification");
                        }
                    } else {
                        System.out.println("No MFA token found - requiring verification");
                    }

                    if (skipMFA) {
                        // Skip MFA and proceed based on user status
                        switch (user.getStatus()) {
                            case NON_CONFIRMED:
                                // For users with valid MFA tokens, automatically promote to VERIFIED
                                // instead of sending to email verification
                                if (persistentToken != null && persistentToken.getExpiryDate().after(new java.util.Date())) {
                                    System.out.println("User has valid MFA token - skipping email verification for user ID: " + user.getId());
                                    
                                    // Update user status to VERIFIED and assign CLIENT role if needed
                                    user.setStatus(com.esprit.models.UserStatus.VERIFIED);
                                    if (user.getRole() == null) {
                                        user.setRole(com.esprit.models.UserRole.CLIENT);
                                    }
                                    userService.update(user);
                                    
                                    // Update session with modified user
                                    UserSession.getInstance().setUser(user);
                                    
                                    // Route to appropriate dashboard based on user's role
                                    if (user.getRole() == com.esprit.models.UserRole.CLIENT) {
                                        mainApp.showClientDashboardScene();
                                    } else {
                                        // Default to admin dashboard for other roles
                                        mainApp.showAdminDashboardScene();
                                    }
                                } else {
                                    // No valid token, proceed with normal email verification
                                    mainApp.showEmailVerificationScene(user);
                                }
                                break;
                            case VERIFIED:
                                mainApp.showLimitedAccessScene();
                                break;
                            case CONFIRMED:
                                // Route to different scenes based on user role
                                if (user.getRole() != null) {
                                    switch (user.getRole()) {
                                        case ADMIN:
                                            mainApp.showAdminDashboardScene();
                                            break;
                                        case BLOGER_ADMIN:
                                            // Try to use reflection to call the method if it exists
                                            try {
                                                java.lang.reflect.Method method = mainApp.getClass().getMethod("showBlogerAdminDashboardScene");
                                                method.invoke(mainApp);
                                                System.out.println("Routing to Bloger Admin Dashboard");
                                            } catch (Exception e) {
                                                System.out.println("Bloger Admin Dashboard not implemented, using fallback");
                                                mainApp.showAdminDashboardScene(); // Fallback
                                            }
                                            break;
                                        case CLIENT:
                                            // Try to use reflection to call the method if it exists
                                            try {
                                                java.lang.reflect.Method method = mainApp.getClass().getMethod("showClientDashboardScene");
                                                method.invoke(mainApp);
                                                System.out.println("Routing to Client Dashboard");
                                            } catch (Exception e) {
                                                System.out.println("Client Dashboard not implemented, using fallback");
                                                mainApp.showAdminDashboardScene(); // Fallback
                                            }
                                            break;
                                        case EVENT_PLANNER:
                                            // Try to use reflection to call the method if it exists
                                            try {
                                                java.lang.reflect.Method method = mainApp.getClass().getMethod("showEventPlannerDashboardScene");
                                                method.invoke(mainApp);
                                                System.out.println("Routing to Event Planner Dashboard");
                                            } catch (Exception e) {
                                                System.out.println("Event Planner Dashboard not implemented, using fallback");
                                                mainApp.showAdminDashboardScene(); // Fallback
                                            }
                                            break;
                                        case PRODUCT_OWNER:
                                            // Try to use reflection to call the method if it exists
                                            try {
                                                java.lang.reflect.Method method = mainApp.getClass().getMethod("showProductOwnerDashboardScene");
                                                method.invoke(mainApp);
                                                System.out.println("Routing to Product Owner Dashboard");
                                            } catch (Exception e) {
                                                System.out.println("Product Owner Dashboard not implemented, using fallback");
                                                mainApp.showAdminDashboardScene(); // Fallback
                                            }
                                            break;
                                        case TRUCK_DRIVER:
                                            // Try to use reflection to call the method if it exists
                                            try {
                                                java.lang.reflect.Method method = mainApp.getClass().getMethod("showTruckDriverDashboardScene");
                                                method.invoke(mainApp);
                                                System.out.println("Routing to Truck Driver Dashboard");
                                            } catch (Exception e) {
                                                System.out.println("Truck Driver Dashboard not implemented, using fallback");
                                                mainApp.showAdminDashboardScene(); // Fallback
                                            }
                                            break;
                                        case UNKNOWN:
                                        default:
                                            // Default to admin dashboard for now
                                            System.out.println("Unknown role or role not set, using default dashboard");
                                            mainApp.showAdminDashboardScene();
                                            break;
                                    }
                                } else {
                                    // Role not set, use default dashboard
                                    System.out.println("User role is null, using default dashboard");
                                    mainApp.showAdminDashboardScene();
                                }
                                break;
                            case BANNED:
                                progressIndicator.setVisible(false);
                                lblErrorMessage.setText("Your account has been banned.");
                                lblErrorMessage.setVisible(true);
                                NotificationManager.showToast(
                                        btnLogin.getScene().getWindow(),
                                        "Your account has been banned.",
                                        NotificationType.ERROR,
                                        4000
                                );
                                System.out.println("Banned user attempted login: " + user.getEmail());
                                break;
                            default:
                                progressIndicator.setVisible(false);
                                lblErrorMessage.setText("Your account status does not allow login.");
                                lblErrorMessage.setVisible(true);
                                break;
                        }
                    } else {
                        // Show MFA choice scene for SMS or Email verification
                        mainApp.showMFAChoiceScene(user);
                    }
                } else {
                    // MainApp reference is null
                    progressIndicator.setVisible(false);
                    lblErrorMessage.setText("System error: Application reference is null");
                    lblErrorMessage.setVisible(true);
                    System.err.println("MainApp reference is null during login");
                }
            } else {
                progressIndicator.setVisible(false);
                lblErrorMessage.setText("Invalid email or password");
                lblErrorMessage.setVisible(true);

                // Shake the login form to indicate error
                if (loginFormContainer != null) {
                    TranslateTransition shakeLeft = new TranslateTransition(Duration.millis(50), loginFormContainer);
                    shakeLeft.setFromX(0);
                    shakeLeft.setToX(-10);

                    TranslateTransition shakeRight = new TranslateTransition(Duration.millis(50), loginFormContainer);
                    shakeRight.setFromX(-10);
                    shakeRight.setToX(10);

                    TranslateTransition shakeReset = new TranslateTransition(Duration.millis(50), loginFormContainer);
                    shakeReset.setFromX(10);
                    shakeReset.setToX(0);

                    shakeLeft.play();
                    shakeLeft.setOnFinished(e -> shakeRight.play());
                    shakeRight.setOnFinished(e -> shakeReset.play());
                }

                NotificationManager.showToast(
                        btnLogin.getScene().getWindow(),
                        "Invalid email or password",
                        NotificationType.ERROR,
                        3000
                );
                
                System.out.println("Failed login attempt for email: " + tfEmail.getText());
            }
        } catch (SQLException e) {
            progressIndicator.setVisible(false);
            lblErrorMessage.setText("Unable to connect to the database. Please try again later.");
            lblErrorMessage.setVisible(true);
            System.err.println("Database error during login: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            progressIndicator.setVisible(false);
            lblErrorMessage.setText("An unexpected error occurred. Please try again later.");
            lblErrorMessage.setVisible(true);
            System.err.println("Unexpected error during login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean promptForVerificationCode(User user) {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Email Verification");
        dialog.setHeaderText("Enter the 6-digit verification code sent to your email");
        dialog.setContentText("Verification Code:");
        
        java.util.Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String code = result.get();
            try {
                // Use concrete implementation instead of anonymous class
                UserService userService = new ConcreteUserService();
                boolean verified = userService.verifyUserCode(user.getId(), code);
                
                if (verified) {
                    showAlert("Success", "Your email has been verified. You can now access the application.");
                    return true;
                } else {
                    showAlert("Error", "Invalid verification code.");
                    return false;
                }
            } catch (SQLException e) {
                System.err.println("Database error during verification: " + e.getMessage());
                showAlert("Database Error", "Unable to verify code. Please try again later.");
                return false;
            } catch (Exception e) {
                System.err.println("Unexpected error during verification: " + e.getMessage());
                showAlert("Error", "An unexpected error occurred. Please try again later.");
                return false;
            }
        } else {
            return false;
        }
    }

    @FXML
    void handleRegister(ActionEvent event) {
        if (mainApp != null) {
            mainApp.showRegisterScene();
        } else {
            showAlert("Error", "Application reference missing");
        }
    }

    private void showAlert(String title, String content) {
        if (btnLogin != null && btnLogin.getScene() != null) {
            // Use modern notification instead of Alert dialog
            NotificationType type = title.contains("Error") ? NotificationType.ERROR : 
                                title.contains("Warning") ? NotificationType.WARNING :
                                title.contains("Success") ? NotificationType.SUCCESS : NotificationType.INFO;
            
            NotificationManager.showToast(
                btnLogin.getScene().getWindow(),
                content,
                type,
                4000
            );
        } else {
            // Fallback to traditional alert if scene is not available
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        }
    }
}
