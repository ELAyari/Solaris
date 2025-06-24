package com.esprit.controllers;

import javafx.fxml.FXML;
import com.esprit.tests.MainprogGUI;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import com.esprit.utils.NotificationManager;
import com.esprit.utils.ProfilePhotoUtils;
import com.esprit.utils.UserSession;
import javafx.scene.shape.Circle;

/**
 * Simple layout controller that hosts the sidebar and loads different
 * fragments into the center content pane.
 */
public class AdminLayoutController {

    @FXML
    private StackPane contentPane;

    @FXML
    private Circle sidebarAvatar;

    // Sidebar buttons for applying active styles
    @FXML private Button btnDashboard;
    @FXML private Button btnManageUsers;
    @FXML private Button btnProducts;
    @FXML private Button btnEvents;
    @FXML private Button btnBlog;
    @FXML private Button btnBinTrucks;
    @FXML private Button btnReports;
    @FXML private Button btnClaims;
    @FXML private Button btnSettings;
    @FXML private Button btnProfile;
    @FXML private Button btnLogout;

    private void loadFragment(String fxml) {
        MainprogGUI app = MainprogGUI.getInstance();
        if (app != null) {
            app.showFragment(fxml);
        }
    }

    @FXML
    private void initialize() {
        // Initialize sidebar avatar
        if (sidebarAvatar != null) {
            ProfilePhotoUtils.loadProfilePhoto(sidebarAvatar);
        }

        // Delay initial fragment load until the scene is fully ready
        javafx.application.Platform.runLater(() -> {
            loadFragment("/layouts/admin_home.fxml");
            refreshSidebarAvatar();
        });
    }

    /**
     * Refreshes the sidebar avatar with the current user's profile photo
     */
    public void refreshSidebarAvatar() {
        if (sidebarAvatar != null) {
            ProfilePhotoUtils.loadProfilePhoto(sidebarAvatar);
        }
    }

    @FXML
    private void showDashboard() {
        styleActiveButton(btnDashboard);
        loadFragment("/layouts/admin_home.fxml");
    }

    @FXML
    private void showManageUsers() {
        styleActiveButton(btnManageUsers);
        loadFragment("/manage_user_dashboard.fxml");
    }

    @FXML
    private void showProducts() {
        styleActiveButton(btnProducts);
        loadFragment("/MainDashboardView.fxml");
    }

    @FXML
    private void showEvents() {
        styleActiveButton(btnEvents);
        loadFragment("/EventsView.fxml");
    }

    @FXML
    private void showBlog() {
        styleActiveButton(btnBlog);
        loadFragment("/BlogDashboardView.fxml");
    }

    @FXML
    private void showBinTrucks() {
        styleActiveButton(btnBinTrucks);
        loadFragment("/views/MainDashboard.fxml");
    }

    @FXML
    private void showReports() {
        styleActiveButton(btnReports);
        loadFragment("/Reports.fxml");
    }

    @FXML
    private void showClaims() {
        styleActiveButton(btnClaims);
        loadFragment("/Claims.fxml");
    }

    @FXML
    private void showSettings() {
        styleActiveButton(btnSettings);
        // Placeholder for settings view
    }

    @FXML
    private void showProfile() {
        styleActiveButton(btnProfile);
        MainprogGUI app = MainprogGUI.getInstance();
        if (app != null) {
            app.loadContentFragment("/user_profile.fxml",
                    (UserProfileController controller) -> {
                        controller.setMainApp(app);
                        controller.refreshUserProfile();
                    });
        } else {
            loadFragment("/user_profile.fxml");
        }
    }

    @FXML
    private void handleLogout() {
        // Ask for confirmation before logging out
        Stage stage = (Stage) contentPane.getScene().getWindow();
        NotificationManager.showConfirmation(
                stage,
                "Confirm Logout",
                "Are you sure you want to logout?",
                () -> {
                    MainprogGUI app = MainprogGUI.getInstance();
                    if (app != null) {
                        app.logout();
                    } else {
                        // fallback: just close the window
                        stage.close();
                    }
                },
                null
        );
    }

    /**
     * Highlights the active sidebar button and removes the style from others.
     */
    private void styleActiveButton(Button activeButton) {
        for (Button btn : new Button[]{
                btnDashboard, btnManageUsers, btnProducts, btnEvents,
                btnBlog, btnBinTrucks, btnReports, btnClaims,
                btnSettings, btnProfile, btnLogout
        }) {
            if (btn != null) {
                btn.getStyleClass().remove("selected");
            }
        }
        if (activeButton != null) {
            activeButton.getStyleClass().add("selected");
        }
    }
}
