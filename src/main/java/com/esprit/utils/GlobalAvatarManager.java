package com.esprit.utils;

import com.esprit.models.User;
import com.esprit.models.UserProfile;
import com.esprit.Services.UserProfileService;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Global manager for avatar circles across the application.
 * Uses observer pattern to update all registered avatar circles when profile photo changes.
 */
public class GlobalAvatarManager {
    private static final Logger LOGGER = Logger.getLogger(GlobalAvatarManager.class.getName());
    private static GlobalAvatarManager instance;
    private final CopyOnWriteArrayList<Circle> registeredAvatars = new CopyOnWriteArrayList<>();
    private Image currentProfileImage = null;
    private boolean isLoadingImage = false;
    
    // Default avatar path
    private static final String DEFAULT_AVATAR_PATH = "/images/default_avatar.png";
    
    private GlobalAvatarManager() {
        // Private constructor for singleton
    }
    
    /**
     * Get the singleton instance
     */
    public static synchronized GlobalAvatarManager getInstance() {
        if (instance == null) {
            instance = new GlobalAvatarManager();
        }
        return instance;
    }
    
    /**
     * Register an avatar circle to be managed by this class
     * @param avatarCircle The circle to register
     */
    public void registerAvatar(Circle avatarCircle) {
        if (avatarCircle == null) return;
        
        if (!registeredAvatars.contains(avatarCircle)) {
            registeredAvatars.add(avatarCircle);
            LOGGER.info("Avatar circle registered. Total registered: " + registeredAvatars.size());
            
            // Apply current image if available
            if (currentProfileImage != null) {
                Platform.runLater(() -> applyImageToCircle(avatarCircle, currentProfileImage));
            } else {
                // Load image if not already loading
                if (!isLoadingImage) {
                    loadProfileImage();
                } else {
                    // Apply default avatar while waiting for image to load
                    applyDefaultAvatar(avatarCircle);
                }
            }
        }
    }
    
    /**
     * Unregister an avatar circle
     * @param avatarCircle The circle to unregister
     */
    public void unregisterAvatar(Circle avatarCircle) {
        if (avatarCircle != null) {
            registeredAvatars.remove(avatarCircle);
            LOGGER.info("Avatar circle unregistered. Remaining: " + registeredAvatars.size());
        }
    }
    
    /**
     * Refresh all registered avatars with the current user's profile photo
     */
    public void refreshAllAvatars() {
        LOGGER.info("Refreshing all avatars. Count: " + registeredAvatars.size());
        loadProfileImage();
    }
    
    /**
     * Refreshes all registered avatars by updating them with the latest profile photo
     * This is used when the profile photo changes to propagate the update across the app
     */
    public void refreshAvatars() {
        LOGGER.info("Refreshing all registered avatars: " + registeredAvatars.size() + " avatars");

        // First load the current user's profile photo
        try {
            User currentUser = UserSession.getInstance().getUser();
            if (currentUser == null) {
                LOGGER.warning("Cannot refresh avatars: No user in session");
                return;
            }

            UserProfileService userProfileService = new UserProfileService();
            UserProfile userProfile = userProfileService.getUserProfileByUserId(currentUser.getId());

            if (userProfile != null && userProfile.getProfilePhotoUrl() != null &&
                !userProfile.getProfilePhotoUrl().isEmpty()) {

                // Load new image
                String url = userProfile.getProfilePhotoUrl();
                
                // Validate and fix the URL
                String validUrl = validateAndFixImageUrl(url);
                if (validUrl == null) {
                    LOGGER.warning("Invalid image URL during refresh: " + url + ". Using default avatars.");
                    updateAvatarsWithDefaultImage(currentUser);
                    return;
                }

                // Create a new image for all avatars to use
                final Image newProfileImage = new Image(validUrl, 150, 150, true, true);
                currentProfileImage = newProfileImage;

                // Update all registered avatars with this new image
                Platform.runLater(() -> {
                    for (Circle avatar : registeredAvatars) {
                        if (avatar != null) {
                            avatar.setFill(new ImagePattern(newProfileImage));
                        }
                    }
                });

                LOGGER.info("Successfully refreshed all avatars with new profile photo");
            } else {
                LOGGER.info("No profile photo URL found, using default avatars");
                // Set default avatars
                updateAvatarsWithDefaultImage(currentUser);
            }
        } catch (Exception e) {
            LOGGER.severe("Error refreshing avatars: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates all avatars with the default image or color based on user role
     * @param user The current user
     */
    private void updateAvatarsWithDefaultImage(User user) {
        Platform.runLater(() -> {
            for (Circle avatar : registeredAvatars) {
                if (avatar != null) {
                    ProfilePhotoUtils.setDefaultAvatar(avatar, user);
                }
            }
        });
    }

    /**
     * Load the current user's profile image and apply to all registered avatars
     */
    private void loadProfileImage() {
        isLoadingImage = true;
        
        try {
            User currentUser = UserSession.getInstance().getUser();
            if (currentUser == null) {
                LOGGER.warning("No user in session. Using default avatar.");
                applyDefaultAvatarToAll();
                isLoadingImage = false;
                return;
            }
            
            // Get user profile to access photo URL
            UserProfileService userProfileService = new UserProfileService();
            UserProfile userProfile = userProfileService.getUserProfileByUserId(currentUser.getId());
            
            if (userProfile != null && userProfile.getProfilePhotoUrl() != null && !userProfile.getProfilePhotoUrl().isEmpty()) {
                // Load the profile photo
                String photoUrl = userProfile.getProfilePhotoUrl();
                LOGGER.info("Loading profile photo from URL: " + photoUrl);
                
                // Validate and fix the URL
                String validUrl = validateAndFixImageUrl(photoUrl);
                if (validUrl == null) {
                    LOGGER.warning("Invalid image URL: " + photoUrl + ". Using default avatar.");
                    applyDefaultAvatarToAll();
                    isLoadingImage = false;
                    return;
                }
                
                Image avatarImage = new Image(validUrl, 150, 150, true, true, true);
                
                avatarImage.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                    if (newProgress.doubleValue() >= 1.0) {
                        if (!avatarImage.isError()) {
                            // Store the loaded image
                            currentProfileImage = avatarImage;
                            
                            // Apply to all registered avatars
                            applyImageToAllAvatars(avatarImage);
                        } else {
                            LOGGER.warning("Error loading profile image. Using default avatar.");
                            applyDefaultAvatarToAll();
                        }
                        isLoadingImage = false;
                    }
                });
                
                // Handle errors loading the image
                avatarImage.errorProperty().addListener((obs, oldError, newError) -> {
                    if (newError) {
                        LOGGER.warning("Error loading profile image: " + validUrl);
                        applyDefaultAvatarToAll();
                        isLoadingImage = false;
                    }
                });
            } else {
                LOGGER.info("No profile photo URL found or it's empty. Using default avatar.");
                applyDefaultAvatarToAll();
                isLoadingImage = false;
            }
        } catch (Exception e) {
            LOGGER.severe("Error loading profile photo: " + e.getMessage());
            e.printStackTrace();
            applyDefaultAvatarToAll();
            isLoadingImage = false;
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
            LOGGER.warning("Rejected hardcoded path: " + url + ". Using default avatar.");
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
            LOGGER.warning("Image file does not exist: " + url);
            return null;
        } catch (Exception e) {
            LOGGER.warning("Error validating image URL: " + url + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Apply the given image to all registered avatar circles
     */
    private void applyImageToAllAvatars(Image image) {
        Platform.runLater(() -> {
            for (Circle avatarCircle : registeredAvatars) {
                applyImageToCircle(avatarCircle, image);
            }
        });
    }
    
    /**
     * Apply the given image to a specific avatar circle
     */
    private void applyImageToCircle(Circle avatarCircle, Image image) {
        if (avatarCircle != null && image != null) {
            avatarCircle.setFill(new ImagePattern(image));
            addGlowEffect(avatarCircle, Color.valueOf("#0088ff"));
        }
    }
    
    /**
     * Apply default avatar to all registered circles
     */
    private void applyDefaultAvatarToAll() {
        Platform.runLater(() -> {
            for (Circle avatarCircle : registeredAvatars) {
                applyDefaultAvatar(avatarCircle);
            }
        });
    }
    
    /**
     * Apply default avatar to a specific circle
     */
    private void applyDefaultAvatar(Circle avatarCircle) {
        if (avatarCircle == null) return;
        
        try {
            // Try to load default avatar image from resources
            Image defaultAvatar = new Image(getClass().getResourceAsStream(DEFAULT_AVATAR_PATH));
            if (defaultAvatar != null && !defaultAvatar.isError()) {
                avatarCircle.setFill(new ImagePattern(defaultAvatar));
            } else {
                // If default avatar can't be loaded, use a color fill
                User currentUser = UserSession.getInstance().getUser();
                if (currentUser != null) {
                    // Set color based on user role
                    String roleColor = getUserRoleColor(currentUser.getRole());
                    Color avatarColor = Color.web(roleColor);
                    avatarCircle.setFill(avatarColor);
                } else {
                    // Use a default color if no user is available
                    avatarCircle.setFill(Color.valueOf("#95A5A6"));
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error loading default avatar: " + e.getMessage());
            // If default avatar can't be loaded, use a color fill
            User currentUser = UserSession.getInstance().getUser();
            if (currentUser != null) {
                // Set color based on user role
                String roleColor = getUserRoleColor(currentUser.getRole());
                Color avatarColor = Color.web(roleColor);
                avatarCircle.setFill(avatarColor);
            } else {
                // Use a default color if no user is available
                avatarCircle.setFill(Color.valueOf("#95A5A6"));
            }
        }
    }
    
    /**
     * Add a subtle glow effect to the avatar
     */
    private void addGlowEffect(Circle avatarCircle, Color color) {
        javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
        glow.setColor(color);
        glow.setRadius(10);
        glow.setSpread(0.1);
        avatarCircle.setEffect(glow);
    }
    
    /**
     * Get a color based on user role
     */
    private String getUserRoleColor(com.esprit.models.UserRole role) {
        if (role == null) return "#95A5A6"; // Default gray
        
        switch (role) {
            case ADMIN:
                return "#e74c3c"; // Red
            case BLOGER_ADMIN:
                return "#3498db"; // Blue
            case CLIENT:
                return "#2ecc71"; // Green
            case EVENT_PLANNER:
                return "#9b59b6"; // Purple
            case PRODUCT_OWNER:
                return "#f39c12"; // Orange
            case TRUCK_DRIVER:
                return "#1abc9c"; // Turquoise
            default:
                return "#95A5A6"; // Gray
        }
    }
}
