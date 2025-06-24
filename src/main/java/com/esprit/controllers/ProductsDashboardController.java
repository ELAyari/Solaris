package com.esprit.controllers;

import com.esprit.tests.MainprogGUI;
import com.esprit.utils.ActivityLogger;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Products Dashboard view
 */
public class ProductsDashboardController implements Initializable {
    
    private MainprogGUI mainApp;
    
    public void setMainApp(MainprogGUI mainApp) {
        this.mainApp = mainApp;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ActivityLogger.logInfo("ProductsDashboardController", "Initializing Products Dashboard");
    }
}
