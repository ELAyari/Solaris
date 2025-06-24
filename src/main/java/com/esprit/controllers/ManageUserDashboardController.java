package com.esprit.controllers;

import com.esprit.models.User;
import com.esprit.Services.UserService;
import com.esprit.utils.AvatarHelper;
import com.esprit.utils.ProfilePhotoUtils;
import com.esprit.utils.UserSession;
import javafx.scene.shape.Circle;
import com.esprit.tests.MainprogGUI;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ManageUserDashboardController implements Initializable {

    private MainprogGUI mainApp;
    private UserService userService;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colNom;
    @FXML private TableColumn<User, String> colPrenom;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colAddress;
    @FXML private TableColumn<User, String> colPhoneNumber;
    @FXML private TableColumn<User, String> colStatus;

    @FXML private TextField tfSearch;
    @FXML private Button btnNotificationBell;
    @FXML private Label lblNotificationCount;
    @FXML private Label lblUsername;
    @FXML private Circle sidebarAvatar;

    @FXML private Button btnDashboard;
    @FXML private Button btnManageUsers;
    @FXML private Button btnProducts;
    @FXML private Button btnEvents;
    @FXML private Button btnBlog;
    @FXML private Button btnReports;
    @FXML private Button btnClaims;
    @FXML private Button btnSettings;
    @FXML private Button btnProfile;
    @FXML private Button btnLogout;

    @FXML private Button btnAddUserManageDashboard;
    @FXML private Button btnModifyUser;
    @FXML private Button btnDeleteUser;    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Load user profile photo for sidebar avatar
            loadUserProfilePhoto();
            userService = new UserService() {
                @Override
                public void add(User user) {
                    // Not needed for this controller
                }

                @Override
                public void delete(int id) {

                }
            };
            setupTableColumns();
            loadUsers();
            setupSearch();
            setupButtons();
        } catch (SQLException e) {
            showError("Database Error", "Failed to initialize database connection: " + e.getMessage());
        }
        
        // Register sidebar avatar with GlobalAvatarManager for automatic synchronization
        if (sidebarAvatar != null) {
            AvatarHelper.setupAvatar(sidebarAvatar);
        }
    }

    private void setupTableColumns() {
        if (colNom == null) {
            showError("Initialization Error", "Table columns are not properly initialized.");
            return;
        }
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colPhoneNumber.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadUsers() {
        try {
            List<User> users = userService.get();
            userTable.getItems().setAll(users);
        } catch (Exception e) {
            showError("Error", "Failed to load users: " + e.getMessage());
        }
    }

    private void setupSearch() {
        tfSearch.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                loadUsers();
            } else {
                searchUsers(newValue);
            }
        });
    }

    private void setupButtons() {
        btnModifyUser.setDisable(true);
        btnDeleteUser.setDisable(true);

        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean disable = newSelection == null;
            btnModifyUser.setDisable(disable);
            btnDeleteUser.setDisable(disable);
        });
    }

    private void searchUsers(String keyword) {
        try {
            var users = userService.search(keyword);
            userTable.setItems(FXCollections.observableArrayList(users));

            if (users.isEmpty()) {
                Label placeholder = new Label("No matching users found");
                userTable.setPlaceholder(placeholder);
            }
        } catch (Exception e) {
            System.err.println("Error searching users: " + e.getMessage());
            e.printStackTrace();
            showError("Search Error", "Failed to search users: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddUser() {
        if (mainApp != null) {
            // Don't use loadScene, use showAdminAddUserScene which is already set up for fragments
            mainApp.showAdminAddUserScene();
        }
    }

    @FXML
    private void handleModifyUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null && mainApp != null) {
            System.out.println("Opening Modify User window for user: " + selectedUser.getNom());
            mainApp.showAdminModifyUserScene(selectedUser);
            loadUsers(); // Refresh the table after modification
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Ban User");
            alert.setHeaderText("Confirm Ban");
            alert.setContentText("Are you sure you want to ban " + selectedUser.getNom() + "?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        boolean success = userService.banUser(selectedUser);
                        if (success) {
                            loadUsers(); // Refresh the table after banning
                            showInfo("User Banned", selectedUser.getNom() + " has been banned successfully.");
                        } else {
                            showError("Ban Error", "Could not ban user. Please try again.");
                        }
                    } catch (Exception e) {
                        showError("Ban Error", "Could not ban user: " + e.getMessage());
                    }
                }
            });
        }
    }

    @FXML
    private void handleDashboard() {
        if (mainApp != null) {
            mainApp.showAdminDashboardScene();
        }
    }

    @FXML
    private void handleProducts() {
        showInfo("Products", "Products management coming soon!");
    }

    @FXML
    private void handleEvents() {
        showInfo("Events", "Events management coming soon!");
    }

    @FXML
    private void handleBlog() {
        showInfo("Blog", "Blog management coming soon!");
    }

    @FXML
    private void handleReports() {
        if (mainApp != null) {
            mainApp.loadScene("/Reports.fxml", ReportsController.class,
                    controller -> controller.setMainApp(mainApp));
        } else {
            showInfo("Reports", "Reports feature coming soon!");
        }
    }

    @FXML
    private void handleSettings() {
        showInfo("Settings", "Settings panel coming soon!");
    }

    @FXML
    private void handleLogout() {
        if (mainApp != null) {
            mainApp.showLoginScene();
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setMainApp(MainprogGUI mainApp) {
        this.mainApp = mainApp;
        System.out.println("MainApp reference set in ManageUserDashboardController");
    }

    public void refreshView() {
        loadUsers();
    }

    @FXML
    public void handleManageUsers(ActionEvent actionEvent) {
        // We're already on the manage users page, just refresh the view
        refreshView();
    }

    public void handleClaims(ActionEvent actionEvent) {
    }

    public void handleProfile(ActionEvent actionEvent) {
        if (mainApp != null) {
            mainApp.showProfileScene();
        }
    }

    /**
     * Loads the current user's profile photo and sets it in the sidebar avatar
     * Uses the centralized ProfilePhotoUtils for consistent profile photo handling
     */
    private void loadUserProfilePhoto() {
        try {
            // Get current user from UserSession
            User currentUser = UserSession.getInstance().getUser();
            if (currentUser == null) {
                System.err.println("No current user found in UserSession.");
                setDefaultAvatar();
                return;
            }

            // Update username label if available
            if (lblUsername != null) {
                lblUsername.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            }

            // Use ProfilePhotoUtils to load and set the profile photo
            if (sidebarAvatar != null) {
                ProfilePhotoUtils.loadProfilePhoto(sidebarAvatar);
            } else {
                System.err.println("Sidebar avatar Circle is null");
            }
        } catch (Exception e) {
            System.err.println("Error loading user profile photo: " + e.getMessage());
            e.printStackTrace();
            setDefaultAvatar();
        }
    }

    /**
     * Sets a default avatar based on user role or a generic image
     * Uses the centralized ProfilePhotoUtils for consistent avatar handling
     */
    private void setDefaultAvatar() {
        try {
            if (sidebarAvatar != null) {
                // Use the centralized ProfilePhotoUtils to set the default avatar
                ProfilePhotoUtils.setDefaultAvatar(sidebarAvatar, null);
            } else {
                System.err.println("Sidebar avatar Circle is null");
            }
        } catch (Exception e) {
            System.err.println("Error setting default avatar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearSearch() {
        tfSearch.clear();
        loadUsers(); // Reset to show all users
    }
}
