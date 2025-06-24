package com.esprit.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSwitcher {

    /**
     * Switches the scene to the given FXML file on the same stage.
     * @param currentNode any node from the current scene to get the stage
     * @param fxmlPath the path to the new FXML file
     * @param width width of the new scene
     * @param height height of the new scene
     * @param windowTitle optional window title to set on the stage; if null or empty, title is not changed
     * @throws IOException if loading FXML fails
     */
    public static void switchScene(Node currentNode, String fxmlPath, double width, double height, String windowTitle) throws IOException {
        Stage stage = (Stage) currentNode.getScene().getWindow();
        Parent root = FXMLLoader.load(SceneSwitcher.class.getResource(fxmlPath));
        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        if (windowTitle != null && !windowTitle.isEmpty()) {
            stage.setTitle(windowTitle);
        }
        stage.show();
    }

    /**
     * Overloaded method without windowTitle parameter for backward compatibility.
     */
    public static void switchScene(Node currentNode, String fxmlPath, double width, double height) throws IOException {
        switchScene(currentNode, fxmlPath, width, height, null);
    }
    
    /**
     * Switches to the BlogView while preserving window size and position.
     * 
     * @param scene The current scene
     * @throws IOException if loading FXML fails
     */
    public static void switchToBlogView(Scene scene) throws IOException {
        Stage stage = (Stage) scene.getWindow();
        Parent root = FXMLLoader.load(SceneSwitcher.class.getResource("/BlogView.fxml"));
        
        // Create a new scene with the same dimensions as the current scene
        Scene newScene = new Scene(root, scene.getWidth(), scene.getHeight());
        stage.setScene(newScene);
        stage.show();
    }
}
