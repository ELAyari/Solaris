package com.esprit.controllers;

import com.esprit.tests.MainprogGUI;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Base controller class that provides common functionality for all controllers.
 * Handles navigation, error messages, and other shared functions.
 */
public abstract class BaseBinController {

    protected Stage primaryStage;

    /**
     * Set the primary stage reference for navigation
     * @param primaryStage The main application stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Show an error alert with the specified title and content
     * @param title Alert title
     * @param content Alert message content
     */
    protected void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Show an informational alert with the specified title and content
     * @param title Alert title
     * @param content Alert message content
     */
    protected void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Show a warning alert with the specified title and content
     * @param title Alert title
     * @param content Alert message content
     */
    protected void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Generic method to handle navigation to any view
     * @param fxmlPath Path to the FXML file
     * @param title Window title to set
     * @throws IOException If the FXML file cannot be loaded
     */
    protected void navigateTo(String fxmlPath, String title) throws IOException {
        if (primaryStage == null) {
            throw new IOException("Primary stage is null. Cannot navigate. The primary stage must be set before navigation.");
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Get controller and set stage reference if it extends BaseBinController
        Object controller = loader.getController();
        if (controller instanceof BaseBinController) {
            ((BaseBinController) controller).setPrimaryStage(primaryStage);
        }

        // Special handling for specific controller types (if this is a DashboardController)
        if (controller instanceof TruckFormController && this instanceof DashboardController) {
            ((TruckFormController) controller).setDashboardController((DashboardController) this);
        } else if (controller instanceof BinFormController && this instanceof DashboardController) {
            ((BinFormController) controller).setDashboardController((DashboardController) this);
        }

        Scene scene = new Scene(root);
        // Apply the shared stylesheets via MainprogGUI's public static method
        MainprogGUI.applyStylesheets(scene);

        primaryStage.setScene(scene);
        if (title != null && !title.isEmpty()) {
            primaryStage.setTitle(title);
        }
    }

    /**
     * Navigate to the dashboard view
     */
    protected void navigateToDashboard() {
        MainprogGUI app = MainprogGUI.getInstance();
        if (app != null) {
            app.showFragment("/views/MainDashboard.fxml");
        } else {
            try {
                navigateTo("/layouts/admin_layout.fxml", "Smart Waste Management - Dashboard");
            } catch (IOException e) {
                showError("Navigation Error", "Failed to navigate to dashboard: " + e.getMessage());
            }
        }
    }

    /**
     * Navigate to the bin management view
     */
    protected void navigateToBinManager() {
        try {
            navigateTo("/views/Bin-form.fxml", "Bin Management");
        } catch (IOException e) {
            showError("Navigation Error", "Failed to navigate to bin management: " + e.getMessage());
        }
    }

    /**
     * Navigate to the solar bin management view
     */
    protected void navigateToSolarBinManager() {
        try {
            navigateTo("/views/SolarBinManager.fxml", "Solar Bin Management");
        } catch (IOException e) {
            showError("Navigation Error", "Failed to navigate to solar bin management: " + e.getMessage());
        }
    }

    /**
     * Navigate to the truck management view
     */
    protected void navigateToTruckManager() {
        try {
            navigateTo("/views/Truck-form.fxml", "Truck Management");
        } catch (IOException e) {
            showError("Navigation Error", "Failed to navigate to truck management: " + e.getMessage());
        }
    }

    /**
     * Validate that a string is not empty
     * @param value The string to validate
     * @param fieldName Name of the field (for error messages)
     * @return true if valid, false otherwise
     */
    protected boolean validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            showWarning("Validation Error", fieldName + " cannot be empty");
            return false;
        }
        return true;
    }

    /**
     * Validate that a numeric value is within a specific range
     * @param value The value to validate
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @param fieldName Name of the field (for error messages)
     * @return true if valid, false otherwise
     */
    protected boolean validateNumericRange(double value, double min, double max, String fieldName) {
        if (value < min || value > max) {
            showWarning("Validation Error",
                    fieldName + " must be between " + min + " and " + max);
            return false;
        }
        return true;
    }

    /**
     * Try to parse a string to a double, showing an error if it fails
     * @param text The text to parse
     * @param fieldName Name of the field (for error messages)
     * @return The parsed double value, or null if parsing failed
     */
    protected Double tryParseDouble(String text, String fieldName) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            showWarning("Input Error", fieldName + " must be a valid number");
            return null;
        }
    }
}
