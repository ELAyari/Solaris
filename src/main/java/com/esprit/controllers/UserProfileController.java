package com.esprit.controllers;

import com.esprit.models.User;
import com.esprit.models.UserProfile;
import com.esprit.models.UserRole;
import com.esprit.Services.UserProfileService;
import com.esprit.Services.UserService;
import com.esprit.tests.MainprogGUI;
import com.esprit.utils.*;
import com.esprit.utils.NotificationManager.NotificationType;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Controller for the user profile screen
 */
public class UserProfileController implements Initializable {
    private MainprogGUI mainApp;


    @FXML private Label lblUsername;
    @FXML private Label lblUserRole;
    @FXML private Circle imgAvatar;
    @FXML private Circle sidebarAvatar;
    @FXML private Button btnChangePhoto;
    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextArea txtAddress;
    @FXML private Button btnSaveProfile;
    @FXML private VBox personalInfoSection;
    @FXML private VBox accountInfoSection;
    @FXML private HBox profileHeader;
    @FXML private TextField txtRole;

    private UserService userService;
    private User currentUser;
    private UserProfile currentUserProfile;

    /**
     * Set the main application reference
     */
    public void setMainApp(MainprogGUI mainApp) {
        this.mainApp = mainApp;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // REMOVE THIS LINE or modify it - it's making all elements invisible
            // prepareAnimatedElements();

            userService = new UserService();

            // Fetch current user from UserSession
            User sessionUser = UserSession.getInstance().getUser();
            if (sessionUser == null) {
                ActivityLogger.logError("UserProfileController", "No user in session", new Exception("User not logged in"));
                showError("User not logged in", "No current user information found.");
                return;
            }

            currentUser = sessionUser;
            ActivityLogger.logInfo("UserProfileController", "Loaded user from session: ID=" + currentUser.getId() + ", Email=" + currentUser.getEmail());

            // Initialize user profile - create if it doesn't exist
            updateUserProfile();

            // Update the username and role in the header
            if (lblUsername != null) {
                lblUsername.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            }

            // Set role label if available
            if (lblUserRole != null && currentUser.getRole() != null) {
                lblUserRole.setText(currentUser.getRole().toString());
            }

            // Load user data into form fields
            populateFormFields();

            // Initialize profile data and avatar
            reloadUserProfile();

            // Register avatar with the global avatar manager
            if (imgAvatar != null) {
                AvatarHelper.setupAvatar(imgAvatar);
            }

            // Set up button handlers
            if (btnSaveProfile != null) {
                btnSaveProfile.setOnAction(event -> saveProfile());
            }
            if (btnChangePhoto != null) {
                btnChangePhoto.setOnAction(event -> changePhoto());
            }

            // Apply styling to elements
            applyVisualEffects();

            // Make sure all elements are visible (in case animations fail)
            ensureElementsAreVisible();

        } catch (SQLException e) {
            showError("Database Error", "Error initializing user profile: " + e.getMessage());
            ActivityLogger.logError("UserProfileController", "Error initializing profile", e);
        }
    }

    /**
     * Ensures all UI elements are visible regardless of animation state
     */
    private void ensureElementsAreVisible() {
        if (profileHeader != null) profileHeader.setOpacity(1);
        if (imgAvatar != null) imgAvatar.setOpacity(1);
        if (btnChangePhoto != null) btnChangePhoto.setOpacity(1);
        if (personalInfoSection != null) personalInfoSection.setOpacity(1);
        if (accountInfoSection != null) accountInfoSection.setOpacity(1);
        if (btnSaveProfile != null) btnSaveProfile.setOpacity(1);
        if (lblUsername != null) lblUsername.setOpacity(1);
        if (lblUserRole != null) lblUserRole.setOpacity(1);
    }

    private void setProfileImage(Image image) {
        if (image == null) return;

        // Create a proper zoomed/cropped ImagePattern
        double width = image.getWidth();
        double height = image.getHeight();
        double size = Math.min(width, height);
        double x = (width - size) / 2;
        double y = (height - size) / 2;

        // Create a cropped pattern that focuses on the center of the image
        ImagePattern pattern = new ImagePattern(
                image,
                x, y, size, size,
                false
        );

        // Apply to avatar circle with a nice glow effect
        imgAvatar.setFill(pattern);

        // Add a subtle glow effect to highlight the avatar
        DropShadow glow = new DropShadow();
        glow.setColor(Color.valueOf("#27ae60"));
        glow.setRadius(15);
        glow.setSpread(0.2);
        imgAvatar.setEffect(glow);

        // Apply same cropped image to sidebar avatar if it exists
        if (sidebarAvatar != null) {
            sidebarAvatar.setFill(pattern);
        }
    }

    private void loadProfileImage(String url) {
        try {
            // Validate and fix the URL
            String validUrl = validateAndFixImageUrl(url);
            if (validUrl == null) {
                ActivityLogger.logWarning("UserProfileController", "Invalid image URL: " + url + ". Using default avatar.");
                setDefaultAvatar(currentUser);
                return;
            }
            
            Image image = new Image(validUrl,
                    imgAvatar.getRadius() * 2,
                    imgAvatar.getRadius() * 2,
                    true, true);
            setProfileImage(image);
        } catch (Exception e) {
            ActivityLogger.logError("UserProfileController", "Error loading profile image", e);
            setDefaultAvatar(currentUser);
        }
    }
    
    /**
     * Validate and fix image URL to ensure it's properly formatted
     */
    private String validateAndFixImageUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        
        url = url.trim();
        
        // Safety check: Reject only actual hardcoded paths that might cause issues
        // Only reject paths that contain specific hardcoded user directories
        if (url.contains("C:/Users/wael/") || url.contains("C:\\Users\\wael\\") || 
            url.contains("/Users/wael/") || url.contains("\\Users\\wael\\") ||
            url.contains("wael.jpeg") || url.contains("wael.jpg") ||
            url.contains("wael.png") || url.contains("wael.gif")) {
            ActivityLogger.logWarning("UserProfileController", "Rejected hardcoded path: " + url + ". Using default avatar.");
            return null;
        }
        
        // If it's already a valid URL, return as is
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file:")) {
            return url;
        }
        
        // If it's a local file path, check if it exists
        try {
            java.io.File file = new java.io.File(url);
            if (file.exists() && file.isFile()) {
                return "file:" + file.getAbsolutePath();
            }
            
            // Try with file: prefix
            if (!url.startsWith("file:")) {
                file = new java.io.File(url);
                if (file.exists() && file.isFile()) {
                    return "file:" + file.getAbsolutePath();
                }
            }
            
            // Try to find the file in the project's profile_photo directory
            String projectRoot = System.getProperty("user.dir");
            String profilePhotoDir = projectRoot + "/src/main/resources/images/profile_photo";
            java.io.File profileDir = new java.io.File(profilePhotoDir);
            
            if (profileDir.exists() && profileDir.isDirectory()) {
                // Extract filename from the URL
                String filename = url;
                if (url.contains("/")) {
                    filename = url.substring(url.lastIndexOf("/") + 1);
                } else if (url.contains("\\")) {
                    filename = url.substring(url.lastIndexOf("\\") + 1);
                }
                
                java.io.File profileFile = new java.io.File(profileDir, filename);
                if (profileFile.exists() && profileFile.isFile()) {
                    return "file:" + profileFile.getAbsolutePath();
                }
            }
            
            // If file doesn't exist, return null to use default avatar
            ActivityLogger.logWarning("UserProfileController", "Image file does not exist: " + url);
            return null;
        } catch (Exception e) {
            ActivityLogger.logWarning("UserProfileController", "Error validating image URL: " + url + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Prepare elements for animations by setting initial opacity
     */
    private void prepareAnimatedElements() {
        // Set initial opacity to 0 for elements we want to animate in
        if (profileHeader != null) profileHeader.setOpacity(0);
        if (imgAvatar != null) imgAvatar.setOpacity(0);
        if (btnChangePhoto != null) btnChangePhoto.setOpacity(0);
        if (personalInfoSection != null) personalInfoSection.setOpacity(0);
        if (accountInfoSection != null) accountInfoSection.setOpacity(0);
        if (btnSaveProfile != null) btnSaveProfile.setOpacity(0);
    }

    /**
     * Apply modern visual effects to UI elements
     */
    private void applyVisualEffects() {
        // Apply shadows to the card sections
        if (personalInfoSection != null) {
            DropShadow cardShadow = new DropShadow();
            cardShadow.setColor(Color.rgb(0, 0, 0, 0.1));
            cardShadow.setRadius(10);
            cardShadow.setOffsetY(4);
            personalInfoSection.setEffect(cardShadow);
        }

        if (accountInfoSection != null) {
            DropShadow cardShadow = new DropShadow();
            cardShadow.setColor(Color.rgb(0, 0, 0, 0.1));
            cardShadow.setRadius(10);
            cardShadow.setOffsetY(4);
            accountInfoSection.setEffect(cardShadow);
        }

        // Apply hover effect to buttons
        if (btnSaveProfile != null) {
            btnSaveProfile.setOnMouseEntered(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(150), btnSaveProfile);
                scale.setToX(1.05);
                scale.setToY(1.05);
                scale.play();

                DropShadow shadow = new DropShadow();
                shadow.setColor(Color.rgb(0, 0, 0, 0.3));
                shadow.setRadius(15);
                shadow.setOffsetY(5);
                btnSaveProfile.setEffect(shadow);
            });

            btnSaveProfile.setOnMouseExited(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(150), btnSaveProfile);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();

                DropShadow shadow = new DropShadow();
                shadow.setColor(Color.rgb(0, 0, 0, 0.2));
                shadow.setRadius(10);
                shadow.setOffsetY(3);
                btnSaveProfile.setEffect(shadow);
            });
        }
    }

    /**
     * Start animation sequence for profile page elements
     */
    private void startAnimationSequence() {
        // Animate sidebar avatar first
        if (sidebarAvatar != null) {
            AnimationUtils.fadeIn(sidebarAvatar, 500, 0);
        }

        // Animate profile header
        if (profileHeader != null) {
            AnimationUtils.fadeIn(profileHeader, 600, 100);
        }

        // Animate main profile avatar with zoom effect
        if (imgAvatar != null) {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(800), imgAvatar);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.millis(300));

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(800), imgAvatar);
            scaleIn.setFromX(0.8);
            scaleIn.setFromY(0.8);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            scaleIn.setDelay(Duration.millis(300));

            fadeIn.play();
            scaleIn.play();
        }

        // Animate change photo button
        if (btnChangePhoto != null) {
            AnimationUtils.fadeIn(btnChangePhoto, 500, 700);
        }

        // Animate personal info section sliding in from left
        if (personalInfoSection != null) {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), personalInfoSection);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.millis(800));

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(600), personalInfoSection);
            slideIn.setFromX(-20);
            slideIn.setToX(0);
            slideIn.setDelay(Duration.millis(800));

            fadeIn.play();
            slideIn.play();
        }

        // Animate account info section sliding in from right
        if (accountInfoSection != null) {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), accountInfoSection);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.millis(1000));

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(600), accountInfoSection);
            slideIn.setFromX(20);
            slideIn.setToX(0);
            slideIn.setDelay(Duration.millis(1000));

            fadeIn.play();
            slideIn.play();
        }

        // Animate save button popping in last
        if (btnSaveProfile != null) {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), btnSaveProfile);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.millis(1200));

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), btnSaveProfile);
            scaleIn.setFromX(0.9);
            scaleIn.setFromY(0.9);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            scaleIn.setDelay(Duration.millis(1200));

            fadeIn.play();
            scaleIn.play();
        }
    }

    /**
     * Populates the form fields with current user data
     */
    private void populateFormFields() {
        if (currentUser == null) return;

        // Set full name (combine first and last name)
        if (txtFullName != null) {
            String fullName = currentUser.getPrenom() + " " + currentUser.getNom();
            txtFullName.setText(fullName.trim());
        }

        // Set other user data with null checks
        if (txtEmail != null) {
            txtEmail.setText(currentUser.getEmail());
        }

        if (txtPhone != null) {
            txtPhone.setText(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "");
        }

        if (txtAddress != null) {
            txtAddress.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "");
        }

        // Set role field if it exists
        if (txtRole != null) {
            txtRole.setText(currentUser.getRole() != null ? currentUser.getRole().toString() : "");
            txtRole.setDisable(true); // Make it read-only
        }

        // Set role label in the hero banner if it exists
        if (lblUserRole != null && currentUser.getRole() != null) {
            lblUserRole.setText(currentUser.getRole().toString());
        }
    }

    /**
     * Sets a default avatar for the user based on their role color
     * Does not rely on external image files
     * @param user The user to create an avatar for
     */
    private void setDefaultAvatar(User user) {
        if (user == null) return;

        // Use the centralized ProfilePhotoUtils for consistent avatar styling
        ProfilePhotoUtils.setDefaultAvatar(imgAvatar, user);
        if (sidebarAvatar != null) {
            ProfilePhotoUtils.setDefaultAvatar(sidebarAvatar, user);
        }

        ActivityLogger.logInfo("UserProfileController", "Created default avatar for user: " + user.getEmail());
    }

    /**
     * Loads the user profile and sets avatar images
     */
    private void reloadUserProfile() {
        try {
            UserProfileService userProfileService = new UserProfileService();
            if (currentUser != null) {
                // Check if profile exists in database
                boolean profileExists = userProfileService.userProfileExists(currentUser.getId());
                
                if (!profileExists) {
                    // Create a new profile if it doesn't exist
                    ActivityLogger.logInfo("UserProfileController", "Profile doesn't exist for user ID: " + currentUser.getId() + ", creating new profile");
                    boolean created = userProfileService.insertUserProfile(
                            currentUser.getId(),
                            "", // default empty photo URL
                            ""  // default empty description
                    );
                    
                    if (!created) {
                        ActivityLogger.logError("UserProfileController", "Failed to create profile for user ID: " + currentUser.getId(), 
                                new Exception("Database insert failed"));
                    }
                }
                
                // Load the profile (whether it existed or was just created)
                currentUserProfile = userProfileService.getUserProfileByUserId(currentUser.getId());
                ActivityLogger.logInfo("UserProfileController", "Loaded profile for user ID: " + currentUser.getId() + 
                        ", profile ID: " + (currentUserProfile != null ? currentUserProfile.getId() : "null"));

                // Clear any existing image patterns
                if (imgAvatar != null) {
                    // Try to load the profile photo directly first
                    if (currentUserProfile != null && currentUserProfile.getProfilePhotoUrl() != null && 
                        !currentUserProfile.getProfilePhotoUrl().isEmpty()) {
                        try {
                            String url = currentUserProfile.getProfilePhotoUrl();
                            
                            // Validate and fix the URL
                            String validUrl = validateAndFixImageUrl(url);
                            if (validUrl == null) {
                                ActivityLogger.logWarning("UserProfileController", "Invalid image URL during reload: " + url + ". Using default avatar.");
                                setDefaultAvatar(currentUser);
                            } else {
                                Image avatarImage = new Image(validUrl,
                                                            imgAvatar.getRadius() * 2,
                                                            imgAvatar.getRadius() * 2,
                                                            true, true);
                                imgAvatar.setFill(new ImagePattern(avatarImage));
                                ActivityLogger.logInfo("UserProfileController", "Directly loaded profile image: " + validUrl);
                            }
                        } catch (Exception e) {
                            // If direct loading fails, fall back to ProfilePhotoUtils
                            ActivityLogger.logError("UserProfileController", "Error directly loading profile image, falling back to utils", e);
                            ProfilePhotoUtils.loadProfilePhoto(imgAvatar);
                        }
                    } else {
                        // Use the centralized ProfilePhotoUtils as fallback
                        ProfilePhotoUtils.loadProfilePhoto(imgAvatar);
                    }
                }
                
                if (sidebarAvatar != null) {
                    // Apply the same image pattern from imgAvatar if it exists
                    if (imgAvatar != null && imgAvatar.getFill() instanceof ImagePattern) {
                        sidebarAvatar.setFill(imgAvatar.getFill());
                    } else {
                        // Otherwise load it independently
                        ProfilePhotoUtils.loadProfilePhoto(sidebarAvatar);
                    }
                }
                
            } else {
                ActivityLogger.logError("UserProfileController", "Current user is null when reloading profile", 
                        new Exception("User not set"));
            }
        } catch (Exception e) {
            ActivityLogger.logError("UserProfileController", "Error reloading user profile", e);
        }
    }

    /**
     * Returns a color code based on user role for avatar background
     */
    private String getUserRoleColor(UserRole role) {
        if (role == null) return "#7f8c8d"; // Default gray

        switch (role) {
            case ADMIN: return "#e74c3c"; // Red
            case CLIENT: return "#3498db"; // Blue
            //case EMPLOYEE: return "#2ecc71"; // Green
            case EVENT_PLANNER: return "#9b59b6"; // Purple
            case PRODUCT_OWNER: return "#f39c12"; // Orange
            case TRUCK_DRIVER: return "#1abc9c"; // Teal
            default: return "#7f8c8d"; // Gray
        }
    }

    private void saveProfile() {
        // Disable save button to prevent multiple submissions
        btnSaveProfile.setDisable(true);
        btnSaveProfile.setText("Saving...");

        try {
            if (!validateFormFields()) {
                btnSaveProfile.setDisable(false);
                btnSaveProfile.setText(" Save Changes");
                return;
            }

            // Get the new values
            String fullName = txtFullName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();
            String address = txtAddress.getText().trim();

            // Update user data
            if (currentUser != null) {
                // Split full name into first and last name (if possible)
                String[] nameParts = fullName.split("\\s+", 2);
                if (nameParts.length >= 2) {
                    currentUser.setPrenom(nameParts[0]);
                    currentUser.setNom(nameParts[1]);
                } else {
                    currentUser.setPrenom(fullName);
                    currentUser.setNom("");
                }

                currentUser.setEmail(email);
                currentUser.setPhoneNumber(phone);
                currentUser.setAddress(address);

                ActivityLogger.logInfo("UserProfileController", "Updating user data for ID: " + currentUser.getId());

                // First update basic user information
                boolean userUpdateSuccess = userService.updateUser(currentUser);
                if (!userUpdateSuccess) {
                    showError("Update Failed", "Failed to update basic user information. Please try again.");
                    return;
                }

                // Now handle the user profile data
                try {
                    UserProfileService userProfileService = new UserProfileService();

                    // Check if profile exists
                    boolean profileExists = userProfileService.userProfileExists(currentUser.getId());
                    boolean profileUpdateSuccess;

                    // Get current profile photo URL if it exists
                    String currentPhotoUrl = "";
                    if (currentUserProfile != null && currentUserProfile.getProfilePhotoUrl() != null) {
                        currentPhotoUrl = currentUserProfile.getProfilePhotoUrl();
                    }

                    // Update or create profile
                    if (profileExists) {
                        // Update existing profile - keep the photo URL but update timestamp
                        profileUpdateSuccess = userProfileService.updateUserProfile(
                            currentUser.getId(),
                            currentPhotoUrl,
                            "" // We could add a description field to the form if needed
                        );
                        ActivityLogger.logInfo("UserProfileController", "Updated existing profile for user ID: " + currentUser.getId());
                    } else {
                        // Create new profile
                        profileUpdateSuccess = userProfileService.insertUserProfile(
                            currentUser.getId(),
                            currentPhotoUrl,
                            ""
                        );
                        ActivityLogger.logInfo("UserProfileController", "Created new profile for user ID: " + currentUser.getId());
                    }

                    if (profileUpdateSuccess) {
                        // Reload the user profile data to get the latest
                        currentUserProfile = userProfileService.getUserProfileByUserId(currentUser.getId());
                        showInfo("Profile Updated", "Your profile has been updated successfully.");
                    } else {
                        showError("Partial Update", "Basic info updated but profile details failed to update.");
                    }
                } catch (SQLException e) {
                    ActivityLogger.logError("UserProfileController", "Database error updating profile", e);
                    showError("Database Error", "Error accessing user profile data: " + e.getMessage());
                }
            } else {
                ActivityLogger.logError("UserProfileController", "No user to update", new Exception("Current user is null"));
                showError("User Error", "No current user found to update.");
            }
        } catch (Exception e) {
            showError("Save Error", "Failed to save profile: " + e.getMessage());
            ActivityLogger.logError("UserProfileController", "Error saving profile", e);
        } finally {
            // Re-enable the save button
            btnSaveProfile.setDisable(false);
            btnSaveProfile.setText(" Save Changes");
        }
    }

    /**
     * Validates all form fields before saving
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateFormFields() {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder("Please correct the following errors:\n");

        // Validate full name
        if (txtFullName.getText().trim().isEmpty()) {
            errorMessage.append("- Full name is required\n");
            txtFullName.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            txtFullName.setStyle("");
        }

        // Validate email
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            errorMessage.append("- Email address is required\n");
            txtEmail.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (!isValidEmail(email)) {
            errorMessage.append("- Please enter a valid email address\n");
            txtEmail.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            txtEmail.setStyle("");
        }

        // Validate phone (optional but must be valid if provided)
        String phone = txtPhone.getText().trim();
        if (!phone.isEmpty() && !isValidPhone(phone)) {
            errorMessage.append("- Please enter a valid phone number\n");
            txtPhone.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            txtPhone.setStyle("");
        }

        if (!isValid) {
            showError("Validation Error", errorMessage.toString());
        }

        return isValid;
    }

    /**
     * Validates email format
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    /**
     * Validates phone number format
     */
    private boolean isValidPhone(String phone) {
        // Accept digits, +, -, (, ), and spaces
        String phoneRegex = "^[\\d\\+\\-\\(\\)\\s]*$";
        return phone.matches(phoneRegex);
    }

    private void updateUserProfile() {
        try {
            UserProfileService userProfileService = new UserProfileService();
            if (currentUser != null) {
                // Check if profile exists
                boolean profileExists = userProfileService.userProfileExists(currentUser.getId());
                
                if (!profileExists) {
                    // Insert new profile if not exists
                    boolean inserted = userProfileService.insertUserProfile(
                            currentUser.getId(),
                            "", // default empty photo URL
                            ""  // default empty description
                    );
                    if (!inserted) {
                        showError("Insert Error", "Failed to insert new user profile.");
                        return;
                    }
                    ActivityLogger.logInfo("UserProfileController", "Created new user profile for ID: " + currentUser.getId());
                } else if (currentUserProfile == null) {
                    // Profile exists in database but not loaded in controller
                    ActivityLogger.logInfo("UserProfileController", "Loading existing profile for user ID: " + currentUser.getId());
                }
                
                // Reload currentUserProfile after insertion or if it was null
                if (!profileExists || currentUserProfile == null) {
                    currentUserProfile = userProfileService.getUserProfileByUserId(currentUser.getId());
                }
            }
        } catch (Exception e) {
            ActivityLogger.logError("UserProfileController", "Error updating user profile", e);
        }
    }

    /**
     * Public method to refresh the user profile when navigating back to this screen
     * Called by MainprogGUI when showing the profile scene
     */
    public void refreshUserProfile() {
        try {
            // Fetch current user from UserSession again
            User sessionUser = UserSession.getInstance().getUser();
            if (sessionUser != null) {
                currentUser = sessionUser;
                ActivityLogger.logInfo("UserProfileController", "Refreshed user from session: ID=" + currentUser.getId());
                
                // Reload user profile data
                updateUserProfile();
                
                // Update UI elements
                lblUsername.setText(currentUser.getNom() + " " + currentUser.getPrenom());
                populateFormFields();
                
                // Force reload profile photo
                UserProfileService userProfileService = new UserProfileService();
                currentUserProfile = userProfileService.getUserProfileByUserId(currentUser.getId());
                
                if (currentUserProfile != null && currentUserProfile.getProfilePhotoUrl() != null && 
                    !currentUserProfile.getProfilePhotoUrl().isEmpty()) {
                    ActivityLogger.logInfo("UserProfileController", "Refreshing profile with photo URL: " + 
                            currentUserProfile.getProfilePhotoUrl());
                    
                    try {
                        // Direct image loading for immediate display
                        String url = currentUserProfile.getProfilePhotoUrl();
                        
                        // Validate and fix the URL
                        String validUrl = validateAndFixImageUrl(url);
                        if (validUrl == null) {
                            ActivityLogger.logWarning("UserProfileController", "Invalid image URL during refresh: " + url + ". Using default avatar.");
                            setDefaultAvatar(currentUser);
                        } else {
                            Image avatarImage = new Image(validUrl,
                                                        imgAvatar.getRadius() * 2,
                                                        imgAvatar.getRadius() * 2,
                                                        true, false); // false for background loading to ensure immediate display
                            
                            // Apply to both avatars
                            imgAvatar.setFill(new ImagePattern(avatarImage));
                            if (sidebarAvatar != null) {
                                sidebarAvatar.setFill(new ImagePattern(avatarImage));
                            }
                            
                            // Add glow effect
                            DropShadow glow = new DropShadow();
                            glow.setColor(Color.valueOf("#0088ff"));
                            glow.setRadius(15);
                            glow.setSpread(0.2);
                            imgAvatar.setEffect(glow);
                            
                            ActivityLogger.logInfo("UserProfileController", "Successfully refreshed profile photo");
                        }
                    } catch (Exception e) {
                        ActivityLogger.logError("UserProfileController", "Error loading profile photo during refresh", e);
                        setDefaultAvatar(currentUser);
                    }
                } else {
                    ActivityLogger.logInfo("UserProfileController", "No profile photo URL to refresh, using default avatar");
                    setDefaultAvatar(currentUser);
                }
            }
        } catch (Exception e) {
            ActivityLogger.logError("UserProfileController", "Error refreshing user profile", e);
        }
    }

    /**
     * Handles the change profile photo button click event
     */
    private void changePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Set initial directory to user home pictures folder if possible
        try {
            File picturesDir = new File(System.getProperty("user.home") + "/Pictures");
            if (picturesDir.exists()) {
                fileChooser.setInitialDirectory(picturesDir);
            }
        } catch (Exception e) {
            // Ignore - will use default directory
        }

        File selectedFile = fileChooser.showOpenDialog(btnChangePhoto.getScene().getWindow());
        if (selectedFile != null) {
            // Validate file size (max 5MB)
            if (selectedFile.length() > 5 * 1024 * 1024) {
                showError("File Too Large", "Please select an image smaller than 5MB.");
                return;
            }

            // Disable button during processing
            btnChangePhoto.setDisable(true);
            if (btnChangePhoto.getGraphic() instanceof HBox) {
                // Find the label in the HBox and update it
                HBox box = (HBox) btnChangePhoto.getGraphic();
                for (Node node : box.getChildren()) {
                    if (node instanceof Label) {
                        ((Label) node).setText("Processing...");
                        break;
                    }
                }
                // IMPORTANT: Don't set text property when using graphic
                btnChangePhoto.setText(null);
            } else {
                // Fallback if graphic isn't configured
                btnChangePhoto.setText("Processing...");
            }

            try {
                // Load the image for preview
                Image image = new Image(selectedFile.toURI().toString(), 150, 150, true, true);

                // Check if image loaded correctly
                if (image.isError()) {
                    showError("Image Error", "Could not load the selected image. Please try another file.");
                    return;
                }

                // Apply the image to both avatars for immediate feedback
                imgAvatar.setFill(new ImagePattern(image));
                if (sidebarAvatar != null) {
                    sidebarAvatar.setFill(new ImagePattern(image));
                }

                // Update profile photo URL in DB with confirmation
                try {
                    UserProfileService userProfileService = new UserProfileService();
                    if (currentUser != null) {
                        // Confirmation dialog
                        Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
                        confirmDialog.setTitle("Confirm Update");
                        confirmDialog.setHeaderText("Update Profile Photo");
                        confirmDialog.setContentText("Do you want to save this as your new profile photo?");

                        var result = confirmDialog.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            btnChangePhoto.setText("Saving...");

                            // First save the file to images/profile_photo directory
                            try {
                                // Define the directory path using project root
                                String projectRoot = System.getProperty("user.dir");
                                String targetDir = projectRoot + "/src/main/resources/images/profile_photo";
                                File photoDirFile = new File(targetDir);
                                if (!photoDirFile.exists()) {
                                    boolean created = photoDirFile.mkdirs();
                                    if (!created) {
                                        ActivityLogger.logError("UserProfileController", "Failed to create directory: " + targetDir);
                                        showError("File Error", "Could not create profile photo directory");
                                        return;
                                    }
                                    ActivityLogger.logInfo("UserProfileController", "Created directory: " + photoDirFile.getAbsolutePath());
                                }

                                // Define the target file with a sanitized email as filename
                                String sanitizedEmail = currentUser.getEmail().replaceAll("[^a-zA-Z0-9.-]", "_");
                                String fileExtension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                                File targetFile = new File(photoDirFile, sanitizedEmail + fileExtension);

                                ActivityLogger.logInfo("UserProfileController", "Copying file from: " + selectedFile.getAbsolutePath() +
                                    " to: " + targetFile.getAbsolutePath());

                                // Copy the file using NIO
                                java.nio.file.Files.copy(
                                    selectedFile.toPath(),
                                    targetFile.toPath(),
                                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                                );

                                // Check if file was copied
                                if (targetFile.exists()) {
                                    ActivityLogger.logInfo("UserProfileController", "File copied successfully, size: " + targetFile.length() + " bytes");

                                    // Use file URL for the database
                                    String photoUrl = targetFile.toURI().toString();
                                    ActivityLogger.logInfo("UserProfileController", "Image URL: " + photoUrl);

                                    // Update the profile with the new photo URL
                                    boolean profileExists = userProfileService.userProfileExists(currentUser.getId());
                                    boolean success;

                                    if (profileExists) {
                                        // Update existing profile photo
                                        success = userProfileService.updateProfilePhoto(currentUser.getId(), photoUrl);
                                        ActivityLogger.logInfo("UserProfileController", "Updated profile photo for existing profile");
                                    } else {
                                        // Create new profile with photo
                                        success = userProfileService.insertUserProfile(
                                                currentUser.getId(),
                                                photoUrl,
                                                "" // Empty description for now
                                        );
                                        ActivityLogger.logInfo("UserProfileController", "Created new profile with photo");
                                    }

                                    if (success) {
                                        // Show success message
                                        showSuccess("Profile Updated", "Your profile photo has been updated successfully.");

                                        // Reload the user profile to get the updated data
                                        currentUserProfile = userProfileService.getUserProfileByUserId(currentUser.getId());

                                        // Update UserSession to ensure the change is reflected across the application
                                        UserSession.getInstance().setUser(currentUser);

                                        // Refresh all avatars across the application
                                        AvatarHelper.refreshAllAvatars();
                                        
                                        // Try to refresh sidebar avatar in admin layout
                                        try {
                                            MainprogGUI app = MainprogGUI.getInstance();
                                            if (app != null && app.getPrimaryStage() != null) {
                                                javafx.scene.Scene scene = app.getPrimaryStage().getScene();
                                                if (scene != null && scene.getRoot() != null) {
                                                    // Try to find controller through FXML loader
                                                    javafx.fxml.FXMLLoader loader = (javafx.fxml.FXMLLoader) scene.getUserData();
                                                    if (loader != null) {
                                                        Object controller = loader.getController();
                                                        if (controller != null) {
                                                            // Check if it's an AdminLayoutController
                                                            if (controller instanceof com.esprit.controllers.AdminLayoutController) {
                                                                com.esprit.controllers.AdminLayoutController adminController = 
                                                                    (com.esprit.controllers.AdminLayoutController) controller;
                                                                adminController.refreshSidebarAvatar();
                                                                ActivityLogger.logInfo("UserProfileController", "Successfully refreshed sidebar avatar");
                                                            } else if (controller instanceof com.esprit.controllers.ClientDashboardController) {
                                                                // For client dashboard, we might need to refresh differently
                                                                ActivityLogger.logInfo("UserProfileController", "Client dashboard detected, skipping sidebar avatar refresh");
                                                            } else {
                                                                ActivityLogger.logInfo("UserProfileController", "Unknown controller type: " + controller.getClass().getSimpleName());
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } catch (Exception e) {
                                            ActivityLogger.logError("UserProfileController", "Error refreshing sidebar avatar", e);
                                        }
                                        
                                        ActivityLogger.logInfo("UserProfileController", "Refreshed all avatars after profile photo update");
                                    } else {
                                        showError("Update Failed", "Failed to update profile photo. Please try again.");
                                        // Revert the avatar to the previous one
                                        reloadUserProfile();
                                    }
                                } else {
                                    ActivityLogger.logWarning("UserProfileController", "File copy appears to have failed - target file doesn't exist");
                                    showError("File Error", "Could not save the profile photo. Please try again.");
                                    reloadUserProfile();
                                }
                            } catch (Exception e) {
                                ActivityLogger.logError("UserProfileController", "Error saving file: " + e.getMessage(), e);
                                showError("File Error", "Could not save the profile photo: " + e.getMessage());
                                reloadUserProfile();
                            }
                        } else {
                            // User canceled, revert to previous avatar
                            reloadUserProfile();
                        }
                    }
                } catch (Exception e) {
                    showError("Database Error", "Failed to update profile photo: " + e.getMessage());
                    ActivityLogger.logError("UserProfileController", "Error updating profile photo", e);
                    reloadUserProfile(); // Revert to previous state
                }

            } catch (Exception e) {
                showError("Error", "Failed to process image: " + e.getMessage());
                ActivityLogger.logError("UserProfileController", "Error processing image", e);
            } finally {
                btnChangePhoto.setDisable(false);
                if (btnChangePhoto.getGraphic() instanceof HBox) {
                    HBox box = (HBox) btnChangePhoto.getGraphic();
                    for (Node node : box.getChildren()) {
                        if (node instanceof Label) {
                            ((Label) node).setText("Change Photo");
                            break;
                        }
                    }
                    btnChangePhoto.setText(null); // IMPORTANT: Keep text null
                } else {
                    btnChangePhoto.setText("Change Photo");
                }
            }
        }
    }

    private void showError(String title, String message) {
        NotificationManager.showNotification(
                title,
                message,
                NotificationType.ERROR,
                Duration.seconds(5)
        );
    }

    private void showInfo(String title, String message) {
        NotificationManager.showNotification(
                title,
                message,
                NotificationType.SUCCESS,
                Duration.seconds(5)
        );
    }

    /**
     * Shows a success notification message
     * @param title The title of the notification
     * @param message The message to display
     */
    private void showSuccess(String title, String message) {
        NotificationManager.showNotification(
                title,
                message,
                NotificationType.SUCCESS,
                Duration.seconds(5)
        );
    }

    /**
     * Shows a confirmation dialog with Yes/No options
     * @return true if user confirmed, false otherwise
     */
    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        return alert.showAndWait().orElse(buttonTypeNo) == buttonTypeYes;
    }

}

