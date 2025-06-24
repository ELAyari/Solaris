package com.esprit.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Utility class for showing modern toast notifications and alerts
 */
public class NotificationManager {

    public static int getUnreadNotificationCount() {
        // This is a placeholder implementation
        // In a real application, this would query a database or service
        // to get the actual count of unread notifications
        
        // For demo purposes, return a random number between 0 and 5
        return (int) (Math.random() * 5);
    }

    public static void showInfo(String notifications, String noNewNotifications) {
        
    }

    public enum NotificationType {
        SUCCESS, WARNING, ERROR, INFO
    }

    /**
     * Shows a toast notification in the application
     * @param owner The owner window
     * @param message The notification message
     * @param type The notification type
     * @param durationMillis Duration to show notification in milliseconds
     */
    public static void showToast(Window owner, String message, NotificationType type, int durationMillis) {
        Platform.runLater(() -> {
            Popup popup = new Popup();
            popup.setAutoFix(true);
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);
            
            // Create notification container
            HBox toast = new HBox(10);
            toast.setAlignment(Pos.CENTER_LEFT);
            toast.setPrefWidth(350);
            toast.setPrefHeight(70);
            toast.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 10 15;");
            
            // Add drop shadow effect
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.rgb(0, 0, 0, 0.2));
            shadow.setRadius(10);
            shadow.setOffsetY(3);
            toast.setEffect(shadow);
            
            // Set icon based on notification type
            String iconPath;
            String colorStyle;
            
            switch (type) {
                case SUCCESS:
                    iconPath = "/images/success_icon.png";
                    colorStyle = "-fx-border-color: #27ae60; -fx-border-width: 0 0 0 5; -fx-border-radius: 0 0 0 8;";
                    break;
                case WARNING:
                    iconPath = "/images/warning_icon.png";
                    colorStyle = "-fx-border-color: #f1c40f; -fx-border-width: 0 0 0 5; -fx-border-radius: 0 0 0 8;";
                    break;
                case ERROR:
                    iconPath = "/images/error_icon.png";
                    colorStyle = "-fx-border-color: #e74c3c; -fx-border-width: 0 0 0 5; -fx-border-radius: 0 0 0 8;";
                    break;
                default: // INFO
                    iconPath = "/images/info_icon.png";
                    colorStyle = "-fx-border-color: #3498db; -fx-border-width: 0 0 0 5; -fx-border-radius: 0 0 0 8;";
                    break;
            }
            
            toast.setStyle(toast.getStyle() + colorStyle);
            
            try {
                ImageView iconView = new ImageView(new Image(NotificationManager.class.getResourceAsStream(iconPath)));
                iconView.setFitHeight(25);
                iconView.setFitWidth(25);
                toast.getChildren().add(iconView);
            } catch (Exception e) {
                // Fallback if image not found
                Region iconRegion = new Region();
                iconRegion.setPrefSize(25, 25);
                
                switch (type) {
                    case SUCCESS:
                        iconRegion.setStyle("-fx-background-color: #27ae60");
                        break;
                    case WARNING:
                        iconRegion.setStyle("-fx-background-color: #f1c40f");
                        break;
                    case ERROR:
                        iconRegion.setStyle("-fx-background-color: #e74c3c");
                        break;
                    default: // INFO
                        iconRegion.setStyle("-fx-background-color: #3498db");
                        break;
                }
                
                toast.getChildren().add(iconRegion);
            }
            
            // Add message
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
            HBox.setHgrow(messageLabel, Priority.ALWAYS);
            toast.getChildren().add(messageLabel);
            
            popup.getContent().add(toast);
            
            // Position popup at the top right of the window
            double screenWidth = Screen.getPrimary().getBounds().getWidth();
            double offsetX = 20;
            double offsetY = 70; // Below the top bar
            
            popup.setX(screenWidth - toast.getPrefWidth() - offsetX);
            popup.setY(offsetY);
            
            popup.show(owner);
            
            // Animate entry
            toast.setOpacity(0);
            toast.setTranslateY(-20);
            
            Timeline fadeInTimeline = new Timeline();
            KeyFrame fadeInKey1 = new KeyFrame(Duration.millis(0),
                    new KeyValue(toast.opacityProperty(), 0),
                    new KeyValue(toast.translateYProperty(), -20));
            KeyFrame fadeInKey2 = new KeyFrame(Duration.millis(300),
                    new KeyValue(toast.opacityProperty(), 1),
                    new KeyValue(toast.translateYProperty(), 0));
            fadeInTimeline.getKeyFrames().addAll(fadeInKey1, fadeInKey2);
            fadeInTimeline.play();
            
            // Hide toast after duration
            Timeline fadeOutTimeline = new Timeline();
            KeyFrame fadeOutKey1 = new KeyFrame(Duration.millis(durationMillis),
                    new KeyValue(toast.opacityProperty(), 1),
                    new KeyValue(toast.translateYProperty(), 0));
            KeyFrame fadeOutKey2 = new KeyFrame(Duration.millis(durationMillis + 300),
                    new KeyValue(toast.opacityProperty(), 0),
                    new KeyValue(toast.translateYProperty(), -20));
            fadeOutTimeline.getKeyFrames().addAll(fadeOutKey1, fadeOutKey2);
            fadeOutTimeline.setOnFinished(event -> popup.hide());
            fadeOutTimeline.play();
        });
    }
    
    /**
     * Simplified showToast method that finds the active window automatically
     * @param message The notification message
     * @param type The notification type
     */
    public static void showToast(String message, NotificationType type) {
        // Get the focused window to use as owner
        Window owner = null;
        for (Window window : Stage.getWindows()) {
            if (window.isFocused()) {
                owner = window;
                break;
            }
        }
        
        // If no focused window found, use the first available window
        if (owner == null && !Stage.getWindows().isEmpty()) {
            owner = Stage.getWindows().get(0);
        }
        
        // Use a default duration of 3 seconds
        if (owner != null) {
            showToast(owner, message, type, 3000);
        }
    }
    
    /**
     * Shows a notification with the given parameters
     * This method is used for consistent notifications across the application
     * 
     * @param title The title of the notification
     * @param message The message to display
     * @param type The notification type
     * @param duration The duration to show the notification
     */
    public static void showNotification(String title, String message, NotificationType type, Duration duration) {
        // Find the focused window to use as owner
        Window owner = null;
        for (Window window : Stage.getWindows()) {
            if (window.isFocused()) {
                owner = window;
                break;
            }
        }
        
        // If no focused window found, use the first available window
        if (owner == null && !Stage.getWindows().isEmpty()) {
            owner = Stage.getWindows().get(0);
        }
        
        // Fall back to creating a new stage if needed
        if (owner == null) {
            owner = new Stage();
        }
        
        // Convert Duration to milliseconds
        int durationMillis = (int) duration.toMillis();
        
        // Call the toast notification method
        showToast(owner, message, type, durationMillis);
    }
    
    /**
     * Shows a simple confirmation dialog with default settings
     */
    public static void showConfirmation() {
        // Get the focused window to use as owner
        Stage ownerStage = null;
        for (Window window : Stage.getWindows()) {
            if (window.isFocused() && window instanceof Stage) {
                ownerStage = (Stage) window;
                break;
            }
        }
        
        // If no focused window found, use the first available window
        if (ownerStage == null) {
            for (Window window : Stage.getWindows()) {
                if (window instanceof Stage) {
                    ownerStage = (Stage) window;
                    break;
                }
            }
        }
        
        // Show default confirmation if we found an owner
        if (ownerStage != null) {
            // Using final variable to reference in lambda
            final Stage finalOwner = ownerStage;
            showConfirmation(finalOwner, "Confirmation", "You have new notifications. Would you like to view them?",
                    () -> showToast(finalOwner, "Viewing notifications", NotificationType.INFO, 3000),
                    null);
        }
    }
    
    /**
     * Shows a confirmation popup with customized styling
     * @param owner The owner stage
     * @param title Title of the confirmation
     * @param message Message to display
     * @param onConfirm Runnable to execute when confirmed
     * @param onCancel Runnable to execute when canceled (can be null)
     */
    public static void showConfirmation(Stage owner, String title, String message, Runnable onConfirm, Runnable onCancel) {
        Platform.runLater(() -> {
            // Create a semi-transparent background
            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
            
            // Create confirmation dialog
            StackPane dialog = new StackPane();
            dialog.setMaxWidth(400);
            dialog.setMaxHeight(200);
            dialog.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 20;");
            
            // Add title
            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            
            // Add message
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
            
            // Create buttons
            HBox buttonBar = new HBox(10);
            buttonBar.setAlignment(Pos.CENTER_RIGHT);
            
            javafx.scene.control.Button cancelButton = new javafx.scene.control.Button("Cancel");
            cancelButton.getStyleClass().add("button-secondary");
            cancelButton.setPrefWidth(100);
            
            javafx.scene.control.Button confirmButton = new javafx.scene.control.Button("Confirm");
            confirmButton.getStyleClass().add("button-primary");
            confirmButton.setPrefWidth(100);
            
            buttonBar.getChildren().addAll(cancelButton, confirmButton);
            
            // Add all components to dialog
            javafx.scene.layout.VBox dialogContent = new javafx.scene.layout.VBox(15);
            dialogContent.getChildren().addAll(titleLabel, messageLabel, new Region(), buttonBar);
            dialog.getChildren().add(dialogContent);
            
            overlay.getChildren().add(dialog);
            
            // Create a new scene with the overlay
            Scene overlayScene = new Scene(overlay, owner.getWidth(), owner.getHeight());
            overlayScene.setFill(Color.TRANSPARENT);
            
            // Create a new stage for the confirmation
            Stage dialogStage = new Stage();
            dialogStage.initOwner(owner);
            dialogStage.setScene(overlayScene);
            
            // Set up button actions
            cancelButton.setOnAction(e -> {
                dialogStage.close();
                if (onCancel != null) {
                    onCancel.run();
                }
            });
            
            confirmButton.setOnAction(e -> {
                dialogStage.close();
                onConfirm.run();
            });
            
            // Show dialog with animation
            dialog.setScaleX(0.8);
            dialog.setScaleY(0.8);
            dialog.setOpacity(0);
            
            dialogStage.show();
            
            Timeline timeline = new Timeline();
            KeyFrame key1 = new KeyFrame(Duration.millis(0),
                    new KeyValue(dialog.opacityProperty(), 0),
                    new KeyValue(dialog.scaleXProperty(), 0.8),
                    new KeyValue(dialog.scaleYProperty(), 0.8));
            KeyFrame key2 = new KeyFrame(Duration.millis(250),
                    new KeyValue(dialog.opacityProperty(), 1),
                    new KeyValue(dialog.scaleXProperty(), 1),
                    new KeyValue(dialog.scaleYProperty(), 1));
            timeline.getKeyFrames().addAll(key1, key2);
            timeline.play();
        });
    }
}
