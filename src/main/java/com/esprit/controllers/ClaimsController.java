package com.esprit.controllers;

import com.esprit.models.Reclamation;
import com.esprit.models.User;
import com.esprit.Services.ReclamationService;
import com.esprit.tests.MainprogGUI;
import com.esprit.utils.*;
import com.esprit.utils.NotificationManager.NotificationType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.Date;

public class ClaimsController {

    @FXML private TableView<Reclamation> claimsTable;
    @FXML private TableColumn<Reclamation, Integer> colId;
    @FXML private TableColumn<Reclamation, String> colUserId;
    @FXML private TableColumn<Reclamation, String> colReport;
    @FXML private TableColumn<Reclamation, String> colStatus;
    @FXML private TableColumn<Reclamation, String> colCreatedAt;
    @FXML private Button btnRefresh;

    // Sidebar components
    @FXML private Circle sidebarAvatar;
    @FXML private Label lblUsername;
    
    // Sidebar buttons
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

    private MainprogGUI mainApp;
    private final ReclamationService reclamationService;
    private final ObservableList<Reclamation> reclamations;

    public ClaimsController() {
        this.reclamationService = new ReclamationService();
        this.reclamations = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Set initial opacity for animated elements
        claimsTable.setOpacity(0);
        btnRefresh.setOpacity(0);
        
        // Setup components        setupTableColumns();
        setupTableContextMenu();
        loadReclamations();
        loadUserProfilePhoto(); // Load user profile photo for sidebar avatar
        
        // Register sidebar avatar with GlobalAvatarManager for automatic synchronization
        if (sidebarAvatar != null) {
            AvatarHelper.setupAvatar(sidebarAvatar);
        }
        
        btnRefresh.setOnAction(this::handleRefresh);
        
        // Change ID column header to "Ticket Number"
        colId.setText("Ticket Number");
        
        // Apply modern styles and effects to buttons
        btnRefresh.getStyleClass().add("modern-button");
        
        // Apply ripple effect to buttons
        AnimationUtils.addRippleEffect(btnRefresh);
        AnimationUtils.addRippleEffect(btnDashboard);
        AnimationUtils.addRippleEffect(btnManageUsers);
        AnimationUtils.addRippleEffect(btnProducts);
        AnimationUtils.addRippleEffect(btnEvents);
        AnimationUtils.addRippleEffect(btnBlog);
        AnimationUtils.addRippleEffect(btnReports);
        AnimationUtils.addRippleEffect(btnClaims);
        AnimationUtils.addRippleEffect(btnSettings);
        AnimationUtils.addRippleEffect(btnProfile);
        AnimationUtils.addRippleEffect(btnLogout);
        
        // Set Claims as active by default (since we're on Claims screen)
        styleActiveButton(btnClaims);
        
        // Animate elements with staggered timing
        AnimationUtils.fadeIn(claimsTable, 800, 300);
        AnimationUtils.fadeIn(btnRefresh, 800, 500);
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserId.setCellValueFactory(cellData -> {
            String fullName = cellData.getValue().getNom() + " " + cellData.getValue().getPrenom();
            return new javafx.beans.property.SimpleStringProperty(fullName);
        });
        colReport.setCellValueFactory(new PropertyValueFactory<>("report"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Format date in a readable format
        colCreatedAt.setCellValueFactory(cellData -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(cellData.getValue().getCreatedAt());
            return new javafx.beans.property.SimpleStringProperty(formattedDate);
        });

        // Add tooltip for report column to show full text
        colReport.setCellFactory(column -> {
            TableCell<Reclamation, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setTooltip(null);
                    } else {
                        setText(item);
                        Tooltip tooltip = new Tooltip(item);
                        tooltip.setWrapText(true);
                        tooltip.setMaxWidth(300);
                        setTooltip(tooltip);
                    }
                }
            };
            return cell;
        });
    }

    private void setupTableContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem updateStatusItem = new MenuItem("Update Status");
        updateStatusItem.setOnAction(e -> {
            Reclamation selectedClaim = claimsTable.getSelectionModel().getSelectedItem();
            if (selectedClaim != null) {
                showStatusUpdateDialog(selectedClaim);
            }
        });

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            Reclamation selectedClaim = claimsTable.getSelectionModel().getSelectedItem();
            if (selectedClaim != null) {
                deleteClaim(selectedClaim);
            }
        });

        contextMenu.getItems().addAll(updateStatusItem, deleteItem);
        claimsTable.setContextMenu(contextMenu);
    }

    private void showStatusUpdateDialog(Reclamation claim) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Update Status");
        dialog.setHeaderText("Update status for claim #" + claim.getId());

        // Apply modern styles to dialog
        dialog.getDialogPane().getStyleClass().add("modern-dialog");
        
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        // Style the buttons
        Button updateButton = (Button) dialog.getDialogPane().lookupButton(updateButtonType);
        updateButton.getStyleClass().add("modern-button");
        AnimationUtils.addRippleEffect(updateButton);
        
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.getStyleClass().add("modern-button-secondary");
        AnimationUtils.addRippleEffect(cancelButton);

        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Pending", "In Progress", "Resolved", "Rejected");
        statusComboBox.setValue(claim.getStatus());
        statusComboBox.getStyleClass().add("modern-combo-box");

        dialog.getDialogPane().setContent(statusComboBox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                return statusComboBox.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(status -> {
            try {
                claim.setStatus(status);
                reclamationService.updateReclamation(claim);
                loadReclamations();
                
                // Apply animation to table row after update
                int index = reclamations.indexOf(claim);
                if (index >= 0 && index < claimsTable.getItems().size()) {
                    TableRow<Reclamation> row = (TableRow<Reclamation>) claimsTable.lookupAll(".table-row-cell").toArray()[index];
                    if (row != null) {
                        AnimationUtils.pulse(row);
                    }
                }
                
                showInfo("Success", "Claim status updated successfully");
            } catch (SQLException e) {
                showErrorAlert("Error", "Failed to update claim status: " + e.getMessage());
            }
        });
    }

    private void deleteClaim(Reclamation claim) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Claim");
        alert.setHeaderText("Delete Claim #" + claim.getId());
        alert.setContentText("Are you sure you want to delete this claim?");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    reclamationService.deleteReclamation(claim.getId());
                    loadReclamations();
                } catch (SQLException e) {
                    showErrorAlert("Delete Error", "Failed to delete claim: " + e.getMessage());
                }
            }
        });
    }

    private void loadReclamations() {
        try {
            List<Reclamation> list = reclamationService.getAllReclamations();
            reclamations.setAll(list);
            claimsTable.setItems(reclamations);
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load claims from the database: " + e.getMessage());
            e.printStackTrace();
            // Populate table with sample data so the view isn't empty
            if (reclamations.isEmpty()) {
                Reclamation sample = new Reclamation(1, 0, "Sample claim", "Pending", new java.util.Date());
                sample.setNom("John");
                sample.setPrenom("Doe");
                reclamations.add(sample);
                claimsTable.setItems(reclamations);
            }
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadReclamations();
    }

    private void showErrorAlert(String title, String content) {
        // Use modern notification instead of traditional alert
        NotificationManager.showNotification(
            title,
            content,
            NotificationType.ERROR,
            Duration.seconds(5)
        );
    }

    private void showInfo(String title, String content) {
        // Use modern notification instead of traditional alert
        NotificationManager.showNotification(
            title,
            content,
            NotificationType.SUCCESS,
            Duration.seconds(5)
        );
    }
    
    /**
     * Highlights the active button with a green background and resets other buttons
     * @param activeButton The button to highlight as active
     */
    private void styleActiveButton(Button activeButton) {
        // Reset all buttons to default style
        for (Button btn : new Button[] {
                btnDashboard, btnManageUsers, btnProducts, btnEvents,
                btnBlog, btnReports, btnClaims, btnSettings, btnProfile
        }) {
            if (btn != null) {
                // Remove active style
                btn.getStyleClass().remove("sidebar-button-active");
                // Apply default style if not present
                if (!btn.getStyleClass().contains("sidebar-button")) {
                    btn.getStyleClass().add("sidebar-button");
                }
            }
        }

        // Apply active style to the current button
        if (activeButton != null) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }

    // Sidebar Navigation Methods
    @FXML
    private void handleDashboard(ActionEvent event) {
        styleActiveButton(btnDashboard);
        AnimationUtils.buttonPress(btnDashboard);
        if (mainApp != null) {
            mainApp.showAdminDashboardScene();
        }
    }

    @FXML
    private void handleManageUsers(ActionEvent event) {
        styleActiveButton(btnManageUsers);
        AnimationUtils.buttonPress(btnManageUsers);
        if (mainApp != null) {
            mainApp.showManageUserDashboardScene();
        }
    }

    @FXML
    private void handleProducts(ActionEvent event) {
        styleActiveButton(btnProducts);
        AnimationUtils.buttonPress(btnProducts);
        if (mainApp != null) {
            mainApp.showProductsDashboardScene();
        } else {
            showInfo("Products", "Products management coming soon!");
        }
    }

    @FXML
    private void handleEvents(ActionEvent event) {
        styleActiveButton(btnEvents);
        AnimationUtils.buttonPress(btnEvents);
        if (mainApp != null) {
            mainApp.showEventPlannerDashboardScene();
        } else {
            showInfo("Events", "Events management coming soon!");
        }
    }

    @FXML
    private void handleBlog(ActionEvent event) {
        styleActiveButton(btnBlog);
        AnimationUtils.buttonPress(btnBlog);
        if (mainApp != null) {
            mainApp.showBlogAdminScene();
        } else {
            showInfo("Blog", "Blog management coming soon!");
        }
    }

    @FXML
    private void handleReports(ActionEvent event) {
        styleActiveButton(btnReports);
        AnimationUtils.buttonPress(btnReports);
        if (mainApp != null) {
            mainApp.showReportsScene();
        }
    }

    @FXML
    private void handleClaims(ActionEvent event) {
        // Already on Claims page
        styleActiveButton(btnClaims);
        AnimationUtils.buttonPress(btnClaims);
    }

    @FXML
    private void handleSettings(ActionEvent event) {
        styleActiveButton(btnSettings);
        AnimationUtils.buttonPress(btnSettings);
        if (mainApp != null) {
            mainApp.showSettingsScene();
        } else {
            showInfo("Settings", "Settings panel coming soon!");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        AnimationUtils.buttonPress(btnLogout);
        if (mainApp != null) {
            NotificationManager.showConfirmation(
                    (Stage) btnLogout.getScene().getWindow(),
                    "Confirm Logout",
                    "Are you sure you want to logout?",
                    () -> mainApp.logout(),
                    null
            );
        }
    }

    public void setMainApp(MainprogGUI mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleProfile(ActionEvent event) {
        styleActiveButton(btnProfile);
        AnimationUtils.buttonPress(btnProfile);
        if (mainApp != null) {
            mainApp.showProfileScene();
        }
    }
    
    /**
     * Loads the current user's profile photo and sets it in the sidebar avatar
     */
    private void loadUserProfilePhoto() {
        try {
            // Get current user from UserSession
            User currentUser = UserSession.getInstance().getUser();
            if (currentUser == null) {
                ActivityLogger.logWarning("ClaimsController", "Cannot load profile photo: No user in session");
                return;
            }
            
            // Update username label if available
            if (lblUsername != null) {
                lblUsername.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            }
            
            // Use the centralized ProfilePhotoUtils to load the profile photo
            if (sidebarAvatar != null) {
                ProfilePhotoUtils.loadProfilePhoto(sidebarAvatar);
            }
        } catch (Exception e) {
            ActivityLogger.logError("ClaimsController", "Error loading user profile photo", e);
        }
    }
}
