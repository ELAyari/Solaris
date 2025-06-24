package com.esprit.controllers;

import com.esprit.tests.MainprogGUI;
import com.esprit.utils.ActivityLogger;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Blog Admin view
 */
public class BlogAdminController implements Initializable {
    
    private MainprogGUI mainApp;
    
    public void setMainApp(MainprogGUI mainApp) {
        this.mainApp = mainApp;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ActivityLogger.logInfo("BlogAdminController", "Initializing Blog Admin View");
    }
}
