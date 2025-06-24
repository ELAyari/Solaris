package com.esprit.utils;

import javafx.scene.shape.Circle;

/**
 * Helper class for easy avatar management across controllers.
 * This provides a simple interface for controllers to register their avatar circles
 * with the GlobalAvatarManager.
 */
public class AvatarHelper {
    
    /**
     * Set up an avatar circle to be automatically managed by the GlobalAvatarManager.
     * This will register the circle and apply the current user's profile photo.
     * 
     * @param avatarCircle The Circle element to use as an avatar
     */
    public static void setupAvatar(Circle avatarCircle) {
        if (avatarCircle == null) {
            ActivityLogger.logWarning("AvatarHelper", "Cannot setup avatar: Circle is null");
            return;
        }
        
        // Register with the global manager only once the circle is attached to a scene
        if (avatarCircle.getScene() != null) {
            GlobalAvatarManager.getInstance().registerAvatar(avatarCircle);
        } else {
            avatarCircle.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    GlobalAvatarManager.getInstance().registerAvatar(avatarCircle);
                }
            });
        }
    }
    
    /**
     * Unregister an avatar circle from the GlobalAvatarManager.
     * Call this when a controller is being destroyed or when the avatar is no longer needed.
     * 
     * @param avatarCircle The Circle element to unregister
     */
    public static void releaseAvatar(Circle avatarCircle) {
        if (avatarCircle != null) {
            GlobalAvatarManager.getInstance().unregisterAvatar(avatarCircle);
        }
    }
    
    /**
     * Refresh all avatars across the application.
     * Call this after changing a user's profile photo to update all instances.
     */
    public static void refreshAllAvatars() {
        GlobalAvatarManager.getInstance().refreshAllAvatars();
        ActivityLogger.logInfo("AvatarHelper", "Refreshed all avatars across the application");
    }
}
