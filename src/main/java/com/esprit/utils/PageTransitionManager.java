package com.esprit.utils;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Utility class for managing page transitions when navigating between screens
 */
public class PageTransitionManager {

    public enum TransitionType {
        FADE, 
        SLIDE_LEFT, 
        SLIDE_RIGHT,
        SLIDE_UP, 
        SLIDE_DOWN, 
        ZOOM_IN, 
        ZOOM_OUT
    }

    /**
     * Navigate to a new screen with a smooth transition
     * @param currentNode The current node to transition from
     * @param fxmlPath Path to the FXML for the new screen
     * @param transitionType Type of transition to apply
     * @param durationMillis Duration of the transition in milliseconds
     */
    public static void navigate(Node currentNode, String fxmlPath, TransitionType transitionType, int durationMillis) {
        try {
            // Load the new scene
            FXMLLoader loader = new FXMLLoader(PageTransitionManager.class.getResource(fxmlPath));
            Parent newRoot = loader.load();
            
            Scene currentScene = currentNode.getScene();
            Stage stage = (Stage) currentScene.getWindow();
            
            // Create a container for the transition
            StackPane container = new StackPane();
            Scene transitionScene = new Scene(container, currentScene.getWidth(), currentScene.getHeight());
            transitionScene.getStylesheets().addAll(currentScene.getStylesheets());

            // Add both scenes to the container
            container.getChildren().add(newRoot);
            
            // Set up initial state for the new root based on transition type
            setupInitialState(newRoot, transitionType);
            
            // Show the transition scene
            stage.setScene(transitionScene);
            
            // Apply the transition animation
            ParallelTransition transition = createTransition(newRoot, transitionType, durationMillis);
            transition.play();
            
        } catch (IOException e) {
            System.err.println("Error navigating to " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Change content in a BorderPane content area with animation
     * @param borderPane The BorderPane to update
     * @param contentArea The content area to update (e.g., "center", "right")
     * @param newContent The new content to display
     * @param transitionType Type of transition to apply
     * @param durationMillis Duration of the transition in milliseconds
     */
    public static void changeContent(BorderPane borderPane, String contentArea, Node newContent, 
                                     TransitionType transitionType, int durationMillis) {
        // Get the current content
        Node currentContent = null;
        
        switch (contentArea.toLowerCase()) {
            case "center":
                currentContent = borderPane.getCenter();
                break;
            case "top":
                currentContent = borderPane.getTop();
                break;
            case "right":
                currentContent = borderPane.getRight();
                break;
            case "bottom":
                currentContent = borderPane.getBottom();
                break;
            case "left":
                currentContent = borderPane.getLeft();
                break;
        }
        
        // If there's current content, animate it out
        if (currentContent != null) {
            // Create exit transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(durationMillis / 2), currentContent);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            
            fadeOut.setOnFinished(e -> {
                // Remove current content and set new content
                setContentInBorderPane(borderPane, contentArea, newContent);
                
                // Set up initial state for new content
                setupInitialState(newContent, transitionType);
                
                // Animate new content in
                ParallelTransition entryTransition = createTransition(newContent, transitionType, durationMillis / 2);
                entryTransition.play();
            });
            
            fadeOut.play();
        } else {
            // No current content, just add the new content with entry animation
            setContentInBorderPane(borderPane, contentArea, newContent);
            
            // Set up initial state
            setupInitialState(newContent, transitionType);
            
            // Animate it in
            ParallelTransition entryTransition = createTransition(newContent, transitionType, durationMillis);
            entryTransition.play();
        }
    }
    
    /**
     * Helper method to set content in the specified BorderPane area
     */
    private static void setContentInBorderPane(BorderPane borderPane, String contentArea, Node content) {
        switch (contentArea.toLowerCase()) {
            case "center":
                borderPane.setCenter(content);
                break;
            case "top":
                borderPane.setTop(content);
                break;
            case "right":
                borderPane.setRight(content);
                break;
            case "bottom":
                borderPane.setBottom(content);
                break;
            case "left":
                borderPane.setLeft(content);
                break;
        }
    }
    
    /**
     * Sets up the initial state of a node based on transition type
     */
    private static void setupInitialState(Node node, TransitionType transitionType) {
        switch (transitionType) {
            case FADE:
                node.setOpacity(0);
                break;
            case SLIDE_LEFT:
                node.setTranslateX(node.getScene().getWidth());
                break;
            case SLIDE_RIGHT:
                node.setTranslateX(-node.getScene().getWidth());
                break;
            case SLIDE_UP:
                node.setTranslateY(node.getScene().getHeight());
                break;
            case SLIDE_DOWN:
                node.setTranslateY(-node.getScene().getHeight());
                break;
            case ZOOM_IN:
                node.setScaleX(0.5);
                node.setScaleY(0.5);
                node.setOpacity(0);
                break;
            case ZOOM_OUT:
                node.setScaleX(1.5);
                node.setScaleY(1.5);
                node.setOpacity(0);
                break;
        }
    }
    
    /**
     * Creates a transition animation based on transition type
     */
    private static ParallelTransition createTransition(Node node, TransitionType transitionType, int durationMillis) {
        ParallelTransition transition = new ParallelTransition();
        Duration duration = Duration.millis(durationMillis);
        
        switch (transitionType) {
            case FADE:
                FadeTransition fadeIn = new FadeTransition(duration, node);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                transition.getChildren().add(fadeIn);
                break;
                
            case SLIDE_LEFT:
                TranslateTransition slideLeft = new TranslateTransition(duration, node);
                slideLeft.setFromX(node.getScene().getWidth());
                slideLeft.setToX(0);
                
                FadeTransition fadeSlideLeft = new FadeTransition(duration, node);
                fadeSlideLeft.setFromValue(0.7);
                fadeSlideLeft.setToValue(1.0);
                
                transition.getChildren().addAll(slideLeft, fadeSlideLeft);
                break;
                
            case SLIDE_RIGHT:
                TranslateTransition slideRight = new TranslateTransition(duration, node);
                slideRight.setFromX(-node.getScene().getWidth());
                slideRight.setToX(0);
                
                FadeTransition fadeSlideRight = new FadeTransition(duration, node);
                fadeSlideRight.setFromValue(0.7);
                fadeSlideRight.setToValue(1.0);
                
                transition.getChildren().addAll(slideRight, fadeSlideRight);
                break;
                
            case SLIDE_UP:
                TranslateTransition slideUp = new TranslateTransition(duration, node);
                slideUp.setFromY(node.getScene().getHeight());
                slideUp.setToY(0);
                
                FadeTransition fadeSlideUp = new FadeTransition(duration, node);
                fadeSlideUp.setFromValue(0.7);
                fadeSlideUp.setToValue(1.0);
                
                transition.getChildren().addAll(slideUp, fadeSlideUp);
                break;
                
            case SLIDE_DOWN:
                TranslateTransition slideDown = new TranslateTransition(duration, node);
                slideDown.setFromY(-node.getScene().getHeight());
                slideDown.setToY(0);
                
                FadeTransition fadeSlideDown = new FadeTransition(duration, node);
                fadeSlideDown.setFromValue(0.7);
                fadeSlideDown.setToValue(1.0);
                
                transition.getChildren().addAll(slideDown, fadeSlideDown);
                break;
                
            case ZOOM_IN:
                ScaleTransition scaleIn = new ScaleTransition(duration, node);
                scaleIn.setFromX(0.5);
                scaleIn.setFromY(0.5);
                scaleIn.setToX(1.0);
                scaleIn.setToY(1.0);
                
                FadeTransition fadeScaleIn = new FadeTransition(duration, node);
                fadeScaleIn.setFromValue(0);
                fadeScaleIn.setToValue(1.0);
                
                transition.getChildren().addAll(scaleIn, fadeScaleIn);
                break;
                
            case ZOOM_OUT:
                ScaleTransition scaleOut = new ScaleTransition(duration, node);
                scaleOut.setFromX(1.5);
                scaleOut.setFromY(1.5);
                scaleOut.setToX(1.0);
                scaleOut.setToY(1.0);
                
                FadeTransition fadeScaleOut = new FadeTransition(duration, node);
                fadeScaleOut.setFromValue(0);
                fadeScaleOut.setToValue(1.0);
                
                transition.getChildren().addAll(scaleOut, fadeScaleOut);
                break;
        }
        
        return transition;
    }
    
    /**
     * Applies staggered animation to child elements of a container
     * @param container The parent container
     * @param delayMillis Delay between each child animation in milliseconds
     */
    public static void applyStaggeredAnimation(Pane container, int delayMillis) {
        int index = 0;
        for (Node child : container.getChildren()) {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), child);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.millis(index * delayMillis));
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), child);
            slideIn.setFromY(20);
            slideIn.setToY(0);
            slideIn.setDelay(Duration.millis(index * delayMillis));
            
            ParallelTransition parallelTransition = new ParallelTransition(fadeIn, slideIn);
            parallelTransition.play();
            
            index++;
        }
    }
}
