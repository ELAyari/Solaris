package com.esprit.controllers;

import com.esprit.tests.MainprogGUI;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class LimitedAccessController {

    private MainprogGUI mainApp;

    @FXML
    private Label lblUsername;

    @FXML
    private Button btnLogout;

    public void setMainApp(MainprogGUI mainApp) {
        this.mainApp = mainApp;
        // Set the username label from system property or other source
        String currentUser = System.getProperty("currentUser");
        lblUsername.setText(currentUser != null ? currentUser : "User");
    }

    @FXML
    private void handleLogout() {
        if (mainApp != null) {
            System.clearProperty("currentUser");
            mainApp.showLoginScene();
        }
    }
}
