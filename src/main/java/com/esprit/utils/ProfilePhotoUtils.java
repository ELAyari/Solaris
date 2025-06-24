package com.esprit.utils;

import com.esprit.models.User;
import com.esprit.models.UserProfile;
import com.esprit.models.UserRole;
import com.esprit.Services.UserProfileService;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Utility class for handling profile photo loading and display across the application
 */
public class ProfilePhotoUtils {

    /**
     * Loads the current user's profile photo from UserSession and sets it in the provided avatar circle
     * @param avatarCircle The Circle element to set the profile photo in
     * @return true if photo was loaded successfully, false otherwise
     */
    public static boolean loadProfilePhoto(Circle avatarCircle) {
        if (avatarCircle == null) {
            ActivityLogger.logWarning("ProfilePhotoUtils", "Cannot load profile photo: Avatar circle is null");
            return false;
        }
        
        try {
            // Get current user from UserSession
            User currentUser = UserSession.getInstance().getUser();
            if (currentUser == null) {
                ActivityLogger.logWarning("ProfilePhotoUtils", "Cannot load profile photo: No user in session");
                setDefaultAvatar(avatarCircle, null);
                return false;
            }
            
            // Get user profile to access photo URL
            UserProfileService userProfileService = new UserProfileService();
            UserProfile userProfile = userProfileService.getUserProfileByUserId(currentUser.getId());
            
            if (userProfile != null && userProfile.getProfilePhotoUrl() != null && !userProfile.getProfilePhotoUrl().isEmpty()) {
                // Load the profile photo
                String photoUrl = userProfile.getProfilePhotoUrl();
                ActivityLogger.logInfo("ProfilePhotoUtils", "Loading profile photo from URL: " + photoUrl);
                
                // Validate and fix the URL
                String validUrl = validateAndFixImageUrl(photoUrl);
                if (validUrl == null) {
                    ActivityLogger.logWarning("ProfilePhotoUtils", "Invalid image URL: " + photoUrl + ". Using default avatar.");
                    setDefaultAvatar(avatarCircle, currentUser);
                    return false;
                }
                
                // Check if URL is valid and accessible
                try {
                    // Immediate attempt to load and set the image
                    Image avatarImage = new Image(validUrl, 
                                                avatarCircle.getRadius() * 2, 
                                                avatarCircle.getRadius() * 2, 
                                                true, true);
                    
                    // Set the image immediately if it's already loaded
                    if (!avatarImage.isError() && !avatarImage.isBackgroundLoading()) {
                        avatarCircle.setFill(new ImagePattern(avatarImage));
                        addGlowEffect(avatarCircle, Color.valueOf("#0088ff"));
                        ActivityLogger.logInfo("ProfilePhotoUtils", "Image loaded immediately: " + validUrl);
                    } else {
                        // For background loading images, set up listeners
                        avatarImage.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                            if (newProgress.doubleValue() >= 1.0) {
                                if (!avatarImage.isError()) {
                                    avatarCircle.setFill(new ImagePattern(avatarImage));
                                    addGlowEffect(avatarCircle, Color.valueOf("#0088ff"));
                                    ActivityLogger.logInfo("ProfilePhotoUtils", "Image loaded asynchronously: " + validUrl);
                                }
                            }
                        });
                        
                        // Handle errors loading the image
                        avatarImage.errorProperty().addListener((obs, oldError, newError) -> {
                            if (newError) {
                                ActivityLogger.logError("ProfilePhotoUtils", 
                                        "Error loading profile image: " + validUrl,
                                        new Exception("Image loading error"));
                                setDefaultAvatar(avatarCircle, currentUser);
                            }
                        });
                    }
                    
                    return true;
                } catch (Exception e) {
                    ActivityLogger.logError("ProfilePhotoUtils", "Error loading image from URL: " + validUrl, e);
                    setDefaultAvatar(avatarCircle, currentUser);
                    return false;
                }
            } else {
                ActivityLogger.logInfo("ProfilePhotoUtils", "No profile photo URL found or it's empty");
                setDefaultAvatar(avatarCircle, currentUser);
                return false;
            }
        } catch (SQLException e) {
            ActivityLogger.logError("ProfilePhotoUtils", "Database error loading profile photo", e);
            setDefaultAvatar(avatarCircle, null);
            return false;
        } catch (Exception e) {
            ActivityLogger.logError("ProfilePhotoUtils", "Error loading profile photo", e);
            setDefaultAvatar(avatarCircle, null);
            return false;
        }
    }
    
    /**
     * Validate and fix image URL to ensure it's properly formatted
     */
    private static String validateAndFixImageUrl(String url) {
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
            ActivityLogger.logWarning("ProfilePhotoUtils", "Rejected hardcoded path: " + url + ". Using default avatar.");
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
            ActivityLogger.logWarning("ProfilePhotoUtils", "Image file does not exist: " + url);
            return null;
        } catch (Exception e) {
            ActivityLogger.logWarning("ProfilePhotoUtils", "Error validating image URL: " + url + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Sets a default avatar based on user role or a generic image
     * @param avatarCircle The Circle element to set the default avatar in
     * @param user The user to create an avatar for (can be null for generic avatar)
     */
    public static void setDefaultAvatar(Circle avatarCircle, User user) {
        if (avatarCircle == null) return;
        
        try {
            if (user != null) {
                // Set color based on user role
                String roleColor = getUserRoleColor(user.getRole());
                Color avatarColor = Color.web(roleColor);
                avatarCircle.setFill(avatarColor);
                
                // Add glow effect
                addGlowEffect(avatarCircle, Color.valueOf("#0088ff"));
            } else {
                // First try to load default avatar image from resources
                String defaultAvatarPath = "/images/default_avatar.png";
                try {
                    Image defaultAvatar = new Image(ProfilePhotoUtils.class.getResourceAsStream(defaultAvatarPath));
                    avatarCircle.setFill(new ImagePattern(defaultAvatar));
                } catch (Exception ex) {
                    // If default avatar can't be loaded, use a color fill
                    avatarCircle.setFill(Color.web("#95A5A6"));
                }
            }
        } catch (Exception e) {
            ActivityLogger.logError("ProfilePhotoUtils", "Error setting default avatar", e);
            // Fallback to gray if all else fails
            avatarCircle.setFill(Color.web("#95A5A6"));
        }
    }
    
    /**
     * Returns a color code based on user role for avatar background
     */
    private static String getUserRoleColor(UserRole role) {
        if (role == null) return "#7f8c8d"; // Default gray

        switch (role) {
            case ADMIN: return "#e74c3c"; // Red
            case CLIENT: return "#3498db"; // Blue
            case EVENT_PLANNER: return "#9b59b6"; // Purple
            case PRODUCT_OWNER: return "#f39c12"; // Orange
            case TRUCK_DRIVER: return "#1abc9c"; // Teal
            case BLOGER_ADMIN: return "#2ecc71"; // Green
            default: return "#7f8c8d"; // Gray
        }
    }
    
    /**
     * Adds a glow effect to the avatar circle
     */
    private static void addGlowEffect(Circle avatarCircle, Color glowColor) {
        DropShadow glow = new DropShadow();
        glow.setColor(glowColor);
        glow.setRadius(15);
        glow.setSpread(0.2);
        avatarCircle.setEffect(glow);
    }

    /**
     * Uploads and saves a profile photo for a user
     * @param file The image file selected by the user
     * @param user The user uploading the profile photo
     * @return The URL path to the saved image file
     * @throws IOException If there's an error saving the file
     */
    public static String uploadProfilePhoto(java.io.File file, User user) throws java.io.IOException {
        if (file == null || user == null || user.getEmail() == null) {
            ActivityLogger.logWarning("ProfilePhotoUtils", "Cannot upload profile photo: Invalid parameters");
            return null;
        }

        try {
            // Use a more flexible path that works on any system
            String projectRoot = System.getProperty("user.dir");
            String photoDirPath = projectRoot + "/src/main/resources/images/profile_photo";
            java.io.File photoDir = new java.io.File(photoDirPath);

            // Debug info about the source file
            ActivityLogger.logInfo("ProfilePhotoUtils", "Source file: " + file.getAbsolutePath() +
                    ", exists: " + file.exists() +
                    ", can read: " + file.canRead() +
                    ", size: " + file.length());

            // Create directory if it doesn't exist
            if (!photoDir.exists()) {
                boolean created = photoDir.mkdirs();
                if (!created) {
                    ActivityLogger.logError("ProfilePhotoUtils", "Failed to create directory: " + photoDirPath);
                    throw new java.io.IOException("Failed to create profile photo directory");
                }
                ActivityLogger.logInfo("ProfilePhotoUtils", "Created directory: " + photoDirPath);
            }

            // Sanitize email for filename (replace characters that might be invalid in a filename)
            String sanitizedEmail = user.getEmail().replaceAll("[^a-zA-Z0-9.-]", "_");

            // Define target file with email as filename
            String fileExtension = getFileExtension(file.getName());
            java.io.File targetFile = new java.io.File(photoDir, sanitizedEmail + fileExtension);

            ActivityLogger.logInfo("ProfilePhotoUtils", "Target file: " + targetFile.getAbsolutePath() +
                    ", directory exists: " + photoDir.exists() +
                    ", can write to directory: " + photoDir.canWrite());

            // Copy the file using FileUtils which handles many edge cases
            try {
                // First make sure the file is not locked
                if (file.exists() && !file.canRead()) {
                    ActivityLogger.logWarning("ProfilePhotoUtils", "Source file is not readable: " + file.getAbsolutePath());
                    return null;
                }

                // Copy using standard Java I/O with explicit flush and close
                ActivityLogger.logInfo("ProfilePhotoUtils", "Starting file copy operation...");
                java.io.FileInputStream inputStream = new java.io.FileInputStream(file);
                java.io.FileOutputStream outputStream = new java.io.FileOutputStream(targetFile);

                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesRead = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();

                ActivityLogger.logInfo("ProfilePhotoUtils", "File copied successfully. Bytes copied: " + totalBytesRead);

                if (!targetFile.exists()) {
                    ActivityLogger.logWarning("ProfilePhotoUtils", "Target file does not exist after copy: " + targetFile.getAbsolutePath());
                } else {
                    ActivityLogger.logInfo("ProfilePhotoUtils", "Target file exists after copy, size: " + targetFile.length());
                }
            } catch (Exception e) {
                ActivityLogger.logError("ProfilePhotoUtils", "Error copying file: " + e.getMessage(), e);

                // Try direct copy with java.nio.file.Files.copy as an alternative
                try {
                    java.nio.file.Files.copy(
                        file.toPath(),
                        targetFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                    );
                    ActivityLogger.logInfo("ProfilePhotoUtils", "File copied successfully using NIO");
                } catch (Exception ex) {
                    ActivityLogger.logError("ProfilePhotoUtils", "Both file copy methods failed: " + ex.getMessage(), ex);
                    throw ex;
                }
            }

            // Create a URL that will be accessible in JavaFX
            String imageUrl = "file:///" + targetFile.getAbsolutePath().replace("\\", "/");
            ActivityLogger.logInfo("ProfilePhotoUtils", "Final image URL: " + imageUrl);

            return imageUrl;
        } catch (Exception e) {
            ActivityLogger.logError("ProfilePhotoUtils", "Error uploading profile photo: " + e.getMessage(), e);
            throw new java.io.IOException("Failed to upload profile photo: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts file extension from filename
     * @param filename The name of the file
     * @return The file extension including the dot (e.g., ".jpg")
     */
    private static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty() || !filename.contains(".")) {
            return ".jpg"; // Default extension
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
