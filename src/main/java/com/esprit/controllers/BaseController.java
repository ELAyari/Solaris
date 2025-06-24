package com.esprit.controllers;

import com.esprit.Services.UserService;
import com.esprit.models.User;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Base controller class that provides common functionality for all controllers.
 * This includes user management, scene switching, and other shared utilities.
 * 
 * @param <T> The type of the main data model this controller manages
 */
public abstract class BaseController<T> implements Initializable {
    
    protected User currentUser;
    protected UserService userService;

    @Override
    public abstract void initialize(URL location, ResourceBundle resources);
    
    /**
     * Initializes the UserService using the UserServiceProvider.
     * This ensures consistent user service access across all controllers.
     */
    protected void initializeUserService() {
        try {
            // Direct instantiation of UserService
            this.userService = new UserService();
        } catch (Exception e) {
            System.err.println("Error initializing UserService in BaseController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Sets the current user for this controller.
     * This method should be called after controller initialization.
     * 
     * @param user The current user
     */
    public void setCurrentUser(User user) {
        // Update the singleton UserSession
        com.esprit.utils.UserSession.getInstance().setUser(user);

        // Use UserSession as the source of truth for currentUser
        this.currentUser = com.esprit.utils.UserSession.getInstance().getUser();

        if (this.currentUser == null) {
            // Try to get the user again from UserSession
            this.currentUser = com.esprit.utils.UserSession.getInstance().getUser();
        }

        if (this.currentUser != null) {
            System.out.println(getClass().getSimpleName() + ": Current user set to " + this.currentUser.getNom() + " " + this.currentUser.getPrenom() + " (ID: " + this.currentUser.getId() + ")");
        } else {
            System.out.println(getClass().getSimpleName() + ": No user logged in");
        }
    }
    
    /**
     * Gets the current user.
     * 
     * @return The current user or null if not set
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Switches the scene to the specified FXML view.
     * 
     * @param stage The stage whose scene root will be switched
     * @param fxmlPath The path to the FXML file
     * @throws IOException If loading the FXML fails
     */
    protected void switchScene(Stage stage, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        stage.getScene().setRoot(root);
    }

    /**
     * Loads an FXML view, switches the scene root to the loaded view, and returns the controller instance.
     * @param stage The stage whose scene root will be switched.
     * @param fxmlPath The path to the FXML file.
     * @param <C> The type of the controller.
     * @return The controller instance of the loaded FXML.
     * @throws IOException If loading the FXML fails.
     */
    /**
     * Loads an FXML view, switches the scene root to the loaded view, and returns the controller instance.
     * Also passes the current user to the new controller if it extends BaseController.
     * 
     * @param stage The stage whose scene root will be switched
     * @param fxmlPath The path to the FXML file
     * @param <C> The type of the controller
     * @return The controller instance of the loaded FXML
     * @throws IOException If loading the FXML fails
     */
    protected <C> C loadViewAndSwitchScene(Stage stage, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        stage.getScene().setRoot(root);
        
        C controller = loader.getController();
        
        // If the controller extends BaseController, pass the current user
        if (controller instanceof BaseController && this.currentUser != null) {
            ((BaseController<?>) controller).setCurrentUser(this.currentUser);
        }
        
        return controller;
    }


}
