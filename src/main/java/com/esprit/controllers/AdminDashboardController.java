package com.esprit.controllers;

import com.esprit.models.User;
import com.esprit.models.UserProfile;
import com.esprit.models.UserRole;
import com.esprit.models.UserStatus;
import com.esprit.Services.UserProfileService;
import com.esprit.Services.UserService;
import com.esprit.tests.MainprogGUI;
import com.esprit.utils.AnimationUtils;
import com.esprit.utils.AvatarHelper;
import com.esprit.utils.DatabaseConnection;
import com.esprit.utils.NotificationManager;
import com.esprit.utils.NotificationManager.NotificationType;
import com.esprit.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class AdminDashboardController {

    private MainprogGUI mainApp;
    private Connection connection;

    @FXML private Button btnDashboard;
    @FXML private Button btnManageUsers;
    @FXML private Button btnUserStats;
    @FXML private Button btnProducts;
    @FXML private Button btnEvents;
    @FXML private Button btnBlog;
    @FXML private Button btnReports;
    @FXML private Button btnClaims;
    @FXML private Button btnSettings;
    @FXML private Button btnProfile;
    @FXML private Button btnLogout;
    @FXML private Circle sidebarAvatar;
    @FXML private Label lblUsername;
    @FXML private Button btnBinTrucks;


    @FXML private Button btnNotificationBell;
    @FXML private Label lblNotificationCount;

    // Dashboard cards elements
    @FXML private Label lblTotalUsers;
    @FXML private Label lblActiveUsers;
    @FXML private Label lblNewUsers;
    @FXML private Label lblBannedUsers;

    // Additional user stats elements
    @FXML private PieChart userStatusPieChart;
    @FXML private Label lblNewUsersToday;
    @FXML private Label lblActiveLastWeek;
    @FXML private Label lblTotalAdmins;

    // Activity table
    @FXML private TableView<UserActivity> tblRecentActivity;
    @FXML private TableColumn<UserActivity, String> colUser;
    @FXML private TableColumn<UserActivity, String> colAction;
    @FXML private TableColumn<UserActivity, Date> colTimestamp;

    @FXML private StackPane contentPane;
    @FXML private ScrollPane dashboardHomeScroll;


    private UserService userService;
    private Node dashboardHome;
    private Stage primaryStage;

    public void setMainApp(MainprogGUI mainApp) {
        this.mainApp = mainApp;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
        }

        // Set initial opacity for animated elements
        setInitialOpacityForAnimations();

        setupTooltips();
        setupButtonActions();
        setupDashboardStats();
        setupUserStatusChart();
        loadRecentActivity();
        updateNotificationCount();
        loadUserProfilePhoto(); // Load user profile photo
          // Register sidebar avatar with GlobalAvatarManager for automatic synchronization
        if (sidebarAvatar != null) {
            AvatarHelper.setupAvatar(sidebarAvatar);
        }
        
        // Configure role-based UI visibility
        configureRoleBasedUI();
        
        styleActiveButton(btnDashboard); // Set dashboard as active by default
        dashboardHome = contentPane.getChildren().isEmpty()           // NEW
                ? null                                           // NEW
                : contentPane.getChildren().get(0);        // Apply animations to dashboard elements
        animateDashboardElements();
    }

    /**
     * Configures UI visibility based on the current user's role
     * Hides Claims and User Management buttons for CLIENT role users
     */
    private void configureRoleBasedUI() {
        try {
            User currentUser = UserSession.getInstance().getUser();
            if (currentUser == null || currentUser.getRole() == null) {
                // If no user or role, show all elements (fallback)
                return;
            }

            UserRole userRole = currentUser.getRole();
            System.out.println("Configuring UI for role: " + userRole);

            // Hide Claims and User Management buttons for CLIENT role
            if (userRole == UserRole.CLIENT) {
                if (btnClaims != null) {
                    btnClaims.setVisible(false);
                    btnClaims.setManaged(false);
                }
                if (btnManageUsers != null) {
                    btnManageUsers.setVisible(false);
                    btnManageUsers.setManaged(false);
                }
                
                // Optionally hide other admin-specific features for clients
                if (btnUserStats != null) {
                    btnUserStats.setVisible(false);
                    btnUserStats.setManaged(false);
                }
                if (btnReports != null) {
                    btnReports.setVisible(false);
                    btnReports.setManaged(false);
                }
            }
            // For ADMIN and other roles, keep all buttons visible (default behavior)
            
        } catch (Exception e) {
            System.err.println("Error configuring role-based UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sets initial opacity for elements that will be animated
     */
    private void setInitialOpacityForAnimations() {
        // Set header elements for slide-in animation
        lblUsername.setOpacity(0);
        btnNotificationBell.setOpacity(0);

        // Set dashboard cards for fade-in animation
        lblTotalUsers.getParent().setOpacity(0);
        lblActiveUsers.getParent().setOpacity(0);
        lblNewUsers.getParent().setOpacity(0);
        lblBannedUsers.getParent().setOpacity(0);

        // Set chart and stats area for fade-in animation
        userStatusPieChart.setOpacity(0);
        lblNewUsersToday.getParent().getParent().setOpacity(0);

        // Set activity table for fade-in animation
        tblRecentActivity.setOpacity(0);
    }

    /**
     * Applies animations to dashboard elements
     */
    private void animateDashboardElements() {
        // Animate sidebar avatar with fade-in
        AnimationUtils.fadeIn(sidebarAvatar, 800, 300);

        // Animate header elements with slide-in from right
        AnimationUtils.slideInFromRight(lblUsername, 500);
        AnimationUtils.slideInFromRight(btnNotificationBell, 600);

        // Animate dashboard cards with staggered fade-in
        AnimationUtils.fadeIn(lblTotalUsers.getParent(), 500, 400);
        AnimationUtils.fadeIn(lblActiveUsers.getParent(), 500, 500);
        AnimationUtils.fadeIn(lblNewUsers.getParent(), 500, 600);
        AnimationUtils.fadeIn(lblBannedUsers.getParent(), 500, 700);

        // Apply hover effects to dashboard cards
        applyCardHoverEffects();

        // Animate chart and stats
        AnimationUtils.fadeIn(userStatusPieChart, 800, 800);
        AnimationUtils.fadeIn(lblNewUsersToday.getParent().getParent(), 800, 900);

        // Animate table with fade-in
        AnimationUtils.fadeIn(tblRecentActivity, 800, 1000);

        // Animate counter labels for numeric stats
        if (userService != null) {
            AnimationUtils.animateCounter(lblTotalUsers, 0, Integer.parseInt(lblTotalUsers.getText()), 1500);
            AnimationUtils.animateCounter(lblActiveUsers, 0, Integer.parseInt(lblActiveUsers.getText()), 1500);
            AnimationUtils.animateCounter(lblNewUsers, 0, Integer.parseInt(lblNewUsers.getText()), 1500);
            AnimationUtils.animateCounter(lblBannedUsers, 0, Integer.parseInt(lblBannedUsers.getText()), 1500);
        }
    }

    /**
     * Applies hover effects to dashboard cards
     */
    private void applyCardHoverEffects() {
        // Apply hover effect to all dashboard cards
        AnimationUtils.applyCardHoverEffect((javafx.scene.layout.Region) lblTotalUsers.getParent());
        AnimationUtils.applyCardHoverEffect((javafx.scene.layout.Region) lblActiveUsers.getParent());
        AnimationUtils.applyCardHoverEffect((javafx.scene.layout.Region) lblNewUsers.getParent());
        AnimationUtils.applyCardHoverEffect((javafx.scene.layout.Region) lblBannedUsers.getParent());
    }

    /**
     * Loads the current user's profile photo and sets it in the sidebar avatar
     */
    private void loadUserProfilePhoto() {
        try {
            if (userService == null) {
                setupUserService();
            }

            // Get current user email from system property
            String currentUserEmail = System.getProperty("currentUser");
            if (currentUserEmail == null || currentUserEmail.isEmpty()) {
                System.err.println("No current user information found.");
                setDefaultAvatar();
                return;
            }

            // Get user information
            User currentUser = userService.getUserByEmail(currentUserEmail);
            if (currentUser == null) {
                System.err.println("Could not find user with email: " + currentUserEmail);
                setDefaultAvatar();
                return;
            }

            // Update username label if available
            if (lblUsername != null) {
                lblUsername.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            }

            // Get user profile to access photo URL
            UserProfileService userProfileService = new UserProfileService();
            UserProfile userProfile = userProfileService.getUserProfileByUserId(currentUser.getId());

            if (userProfile != null && userProfile.getProfilePhotoUrl() != null && !userProfile.getProfilePhotoUrl().isEmpty()) {
                // Load the profile photo. If the path is local, prefix it with 'file:' so
                // JavaFX can resolve it correctly.
                String url = userProfile.getProfilePhotoUrl();
                
                // Validate and fix the URL
                String validUrl = validateAndFixImageUrl(url);
                if (validUrl == null) {
                    System.err.println("Invalid image URL: " + url + ". Using default avatar.");
                    setDefaultAvatar();
                    return;
                }
                
                System.out.println("Loading profile photo from URL: " + validUrl);
                Image avatarImage = new Image(validUrl, true);

                avatarImage.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                    if (newProgress.doubleValue() >= 1.0) {
                        if (sidebarAvatar != null) {
                            sidebarAvatar.setFill(new ImagePattern(avatarImage));
                        }
                    }
                });

                // Handle errors loading the image
                avatarImage.errorProperty().addListener((obs, oldError, newError) -> {
                    if (newError) {
                        System.err.println("Error loading profile image: " + validUrl);
                        setDefaultAvatar();
                    }
                });
            } else {
                setDefaultAvatar();
            }
        } catch (Exception e) {
            System.err.println("Error loading user profile photo: " + e.getMessage());
            e.printStackTrace();
            setDefaultAvatar();
        }
    }

    /**
     * Validate and fix image URL to ensure it's properly formatted
     */
    private String validateAndFixImageUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        
        url = url.trim();
        
        // Safety check: Reject only actual hardcoded paths that might cause issues
        // Only reject paths that contain specific hardcoded user directories
        if (url.contains("C:/Users/wael/") || url.contains("C:\\Users\\wael\\") || 
            url.contains("/Users/wael/") || url.contains("\\Users\\wael\\") ||
            url.contains("wael.jpeg") || url.contains("wael.jpg") ||
            url.contains("wael.png") || url.contains("wael.gif")) {
            System.err.println("Rejected hardcoded path: " + url + ". Using default avatar.");
            return null;
        }
        
        // If it's already a valid URL, return as is
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file:")) {
            return url;
        }
        
        // If it's a local file path, check if it exists
        try {
            java.io.File file = new java.io.File(url);
            if (file.exists() && file.isFile()) {
                return "file:" + file.getAbsolutePath();
            }
            
            // Try with file: prefix
            if (!url.startsWith("file:")) {
                file = new java.io.File(url);
                if (file.exists() && file.isFile()) {
                    return "file:" + file.getAbsolutePath();
                }
            }
            
            // Try to find the file in the project's profile_photo directory
            String projectRoot = System.getProperty("user.dir");
            String profilePhotoDir = projectRoot + "/src/main/resources/images/profile_photo";
            java.io.File profileDir = new java.io.File(profilePhotoDir);
            
            if (profileDir.exists() && profileDir.isDirectory()) {
                // Extract filename from the URL
                String filename = url;
                if (url.contains("/")) {
                    filename = url.substring(url.lastIndexOf("/") + 1);
                } else if (url.contains("\\")) {
                    filename = url.substring(url.lastIndexOf("\\") + 1);
                }
                
                java.io.File profileFile = new java.io.File(profileDir, filename);
                if (profileFile.exists() && profileFile.isFile()) {
                    return "file:" + profileFile.getAbsolutePath();
                }
            }
            
            // If file doesn't exist, return null to use default avatar
            System.err.println("Image file does not exist: " + url);
            return null;
        } catch (Exception e) {
            System.err.println("Error validating image URL: " + url + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Sets a default avatar based on user role or a generic image
     */
    private void setDefaultAvatar() {
        try {
            // First try to load default avatar image from resources
            String defaultAvatarPath = "/images/default_avatar.png";
            try {
                Image defaultAvatar = new Image(getClass().getResourceAsStream(defaultAvatarPath));
                if (sidebarAvatar != null) {
                    sidebarAvatar.setFill(new ImagePattern(defaultAvatar));
                }
            } catch (Exception ex) {
                // If default avatar can't be loaded, use a color fill
                if (sidebarAvatar != null) {
                    // Use a default color
                    sidebarAvatar.setFill(javafx.scene.paint.Color.valueOf("#95A5A6"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error setting default avatar: " + e.getMessage());
            // Last resort - use a simple color
            if (sidebarAvatar != null) {
                sidebarAvatar.setFill(javafx.scene.paint.Color.valueOf("#95A5A6"));
            }
        }
    }

    private int previousNotificationCount = 0;

    private void updateNotificationCount() {
        if (userService == null) {
            try {
                userService = new UserService() {
                    @Override
                    public void add(User user) {
                    }

                    @Override
                    public void delete(int id) {

                    }
                };
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        int newUserCount = userService.getNewUsers();
        if (lblNotificationCount != null) {
            lblNotificationCount.setText(String.valueOf(newUserCount));
            lblNotificationCount.setVisible(newUserCount > 0);

            // Animate the notification bell if there are new notifications
            if (newUserCount > previousNotificationCount && btnNotificationBell != null) {
                AnimationUtils.bellShake(btnNotificationBell);

                // Show a toast notification if there are new users
                if (newUserCount > 0 && btnNotificationBell.getScene() != null) {
                    NotificationManager.showToast(
                            btnNotificationBell.getScene().getWindow(),
                            "You have " + newUserCount + " new user registration" + (newUserCount > 1 ? "s" : ""),
                            NotificationType.INFO,
                            3000
                    );
                }
            }

            previousNotificationCount = newUserCount;
        }
    }

    private void setupTooltips() {
        btnDashboard.setTooltip(new Tooltip("View dashboard overview"));
        btnManageUsers.setTooltip(new Tooltip("Add, edit, or remove users"));
        btnProducts.setTooltip(new Tooltip("Manage products"));
        btnEvents.setTooltip(new Tooltip("Manage events"));
        btnBlog.setTooltip(new Tooltip("Manage blog posts"));
        btnReports.setTooltip(new Tooltip("View system reports"));
        btnClaims.setTooltip(new Tooltip("View user claims"));
        btnSettings.setTooltip(new Tooltip("Configure system settings"));
        btnLogout.setTooltip(new Tooltip("Logout from the system"));
    }

    private void setupButtonActions() {
        btnDashboard.setOnAction(event -> handleDashboard(event));
        btnManageUsers.setOnAction(event -> handleManageUsers(event));
        btnProducts.setOnAction(event -> handleProducts(event));
        btnEvents.setOnAction(event -> handleEvents(event));
        btnBlog.setOnAction(event -> handleBlog(event));
        btnReports.setOnAction(event -> handleReports(event));
        btnClaims.setOnAction(event -> handleClaims(event));
        btnSettings.setOnAction(event -> handleSettings(event));
        btnProfile.setOnAction(event -> handleProfile(event));
        btnNotificationBell.setOnAction(event -> handleNotificationBellClick());
        btnBinTrucks.setOnAction(event -> handleBinTrucks(event));
    }

    @FXML
    private void handleBinTrucks(ActionEvent event) {
        styleActiveButton(btnBinTrucks);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainDashboard.fxml"));
            Parent fragmentRoot = loader.load();

            // Grab the DashboardController instance:
            DashboardController dashCtrl = loader.getController();

            // ① Give it the Stage so it can navigate out later:
            dashCtrl.setPrimaryStage(primaryStage);

            // ② If DashboardController needs a reference back to MainprogGUI (for mainApp-based navigation):
            dashCtrl.setMainApp(mainApp);

            // ③ Force it to refresh data if you want:
            dashCtrl.refreshData();

            // Finally insert the fragment into the content pane:
            contentPane.getChildren().setAll(fragmentRoot);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Error loading MainDashboard: " + ex.getMessage());
        }
    }

    @FXML
    private void handleProducts(ActionEvent event) {
        styleActiveButton(btnProducts);
    
        User currentUser = UserSession.getInstance().getUser();
        boolean isClient = currentUser != null && UserRole.CLIENT.equals(currentUser.getRole());
    
        String fxml = isClient ? "/ProductCardView.fxml" : "/MainDashboardView.fxml";
        String title = isClient ? "Produits" : "Dashboard";
    
        try {
            Parent fragment = FXMLLoader.load(getClass().getResource(fxml));
            contentPane.getChildren().setAll(fragment);
    
            // Optionally update the window title
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setTitle(title);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    @FXML
    private void handleEvents(ActionEvent event) {
        styleActiveButton(btnEvents);
        try {
            Parent fragment = FXMLLoader.load(
                    getClass().getResource("/EventsView.fxml"));
            contentPane.getChildren().setAll(fragment);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleBlog(ActionEvent event) {
        styleActiveButton(btnBlog);
    
        User currentUser = UserSession.getInstance().getUser();
        boolean isClient = currentUser != null && UserRole.CLIENT.equals(currentUser.getRole());
    
        String fxml = isClient ? "/BlogView.fxml" : "/BlogDashboardView.fxml";
        String title = isClient ? "Blogs" : "Dashboard";
    
        try {
            Parent fragment = FXMLLoader.load(getClass().getResource(fxml));
            contentPane.getChildren().setAll(fragment);
    
            // Optionally update the window title
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setTitle(title);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void setupDashboardStats() {
        if (userService == null) {
            try {
                userService = new UserService() {
                    @Override
                    public void add(User user) {

                    }

                    @Override
                    public void delete(int id) {

                    }
                };
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        int totalUsers = userService.getTotalUsers();
        int activeUsers = userService.getActiveUsers();
        int newUsers = userService.getNewUsers();
        int bannedUsers = userService.getBannedUsers();

        updateDashboardStats(totalUsers, activeUsers, newUsers, bannedUsers);
    }

    private void updateDashboardStats(int total, int active, int newUsers, int bannedUsers) {
        if (lblTotalUsers != null) lblTotalUsers.setText(String.valueOf(total));
        if (lblActiveUsers != null) lblActiveUsers.setText(String.valueOf(active));
        if (lblNewUsers != null) lblNewUsers.setText(String.valueOf(newUsers));
        if (lblBannedUsers != null) lblBannedUsers.setText(String.valueOf(bannedUsers));
    }

    private void styleActiveButton(Button activeButton) {
        // Remove selected style from all buttons
        btnDashboard.getStyleClass().remove("selected");
        btnManageUsers.getStyleClass().remove("selected");
        btnProducts.getStyleClass().remove("selected");
        btnEvents.getStyleClass().remove("selected");
        btnBlog.getStyleClass().remove("selected");
        btnReports.getStyleClass().remove("selected");
        btnClaims.getStyleClass().remove("selected");
        btnSettings.getStyleClass().remove("selected");
        btnProfile.getStyleClass().remove("selected");

        // Add selected style to active button
        activeButton.getStyleClass().add("selected");
    }

    private void setupUserStatusChart() {
        try {
            if (userService == null) {
                setupUserService();
            }

            int confirmed = 0;
            int notConfirmed = 0;
            int banned = 0;
            int admins = 0;

            List<User> users = userService.get();
            for (User user : users) {
                if (user.getStatus() == UserStatus.CONFIRMED) confirmed++;
                else if (user.getStatus() == UserStatus.NON_CONFIRMED) notConfirmed++;
                else if (user.getStatus() == UserStatus.BANNED) banned++;

                if (user.getRole() == UserRole.ADMIN) admins++;
            }

            // Update additional labels
            if (lblTotalAdmins != null) lblTotalAdmins.setText(String.valueOf(admins));
            if (lblNewUsersToday != null) lblNewUsersToday.setText(String.valueOf(userService.getNewUsers()));
            if (lblActiveLastWeek != null) lblActiveLastWeek.setText(String.valueOf(userService.getActiveUsers()));

            // Setup pie chart
            if (userStatusPieChart != null) {
                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                        new PieChart.Data("Confirmed", confirmed),
                        new PieChart.Data("Not Confirmed", notConfirmed),
                        new PieChart.Data("Banned", banned)
                );

                userStatusPieChart.setData(pieChartData);
                userStatusPieChart.setTitle("User Status Distribution");
            }

        } catch (Exception e) {
            System.err.println("Error setting up user status chart: " + e.getMessage());
        }
    }

    private void loadRecentActivity() {
        // Setup the table columns if table exists
        if (tblRecentActivity != null) {
            colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
            colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
            colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

            ObservableList<UserActivity> activities = FXCollections.observableArrayList();

            try {
                // Try to get real activity data if possible
                String query = "SELECT u.email, a.action_type, a.timestamp FROM user_activity a " +
                        "JOIN users u ON a.user_id = u.id " +
                        "ORDER BY a.timestamp DESC LIMIT 10";

                if (connection != null) {
                    PreparedStatement stmt = connection.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        activities.add(new UserActivity(
                                rs.getString("email"),
                                rs.getString("action_type"),
                                rs.getTimestamp("timestamp")
                        ));
                    }
                }

                // If we got no data (perhaps table doesn't exist yet), add sample data
                if (activities.isEmpty()) {
                    activities.add(new UserActivity("user1@example.com", "Login", new Date()));
                    activities.add(new UserActivity("user2@example.com", "Profile Update", new Date()));
                    activities.add(new UserActivity("admin@example.com", "User Ban", new Date()));
                }

                tblRecentActivity.setItems(activities);

            } catch (SQLException e) {
                System.err.println("Could not load activity data: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        styleActiveButton(btnDashboard);
      //  if (dashboardHome != null)
            contentPane.getChildren().setAll(dashboardHomeScroll);
    }

    @FXML
    private void handleManageUsers(ActionEvent event) {
        styleActiveButton(btnManageUsers);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manage_user_dashboard.fxml"));
            Parent fragment = loader.load();

            // Get controller and pass mainApp reference
            ManageUserDashboardController controller = loader.getController();
            controller.setMainApp(mainApp);

            // Load the fragment into the content pane
            contentPane.getChildren().setAll(fragment);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading manage users fragment: " + e.getMessage());
        }
    }

    @FXML
    private void handleReports(ActionEvent event) {
        styleActiveButton(btnReports);
        if (mainApp != null) {
            mainApp.showReportsScene();
        } else {
            System.err.println("Error: mainApp is null in handleReports");
        }
    }

    @FXML
    private void handleClaims(ActionEvent event) {
        styleActiveButton(btnClaims);
        if (mainApp != null) {
            mainApp.loadScene("/Claims.fxml", ClaimsController.class,
                    controller -> controller.setMainApp(mainApp));
        } else {
            System.err.println("Error: mainApp is null in handleClaims");
        }
    }

    @FXML
    private void handleSettings(ActionEvent event) {
        styleActiveButton(btnSettings);
        // TODO: Implement settings functionality
    }

    @FXML
    private void handleProfile(ActionEvent event) {
        styleActiveButton(btnProfile);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user_profile.fxml"));
            Parent fragment = loader.load();

            // Get controller and set mainApp reference
            UserProfileController controller = loader.getController();
            controller.setMainApp(mainApp);

            // Make sure to call this after all initialization
            if (controller != null) {
                try {
                    // Add a small delay to ensure everything is properly initialized
                    java.util.concurrent.CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(100);
                            javafx.application.Platform.runLater(() -> controller.refreshUserProfile());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Load the fragment into the content pane
            contentPane.getChildren().setAll(fragment);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading profile fragment: " + e.getMessage());
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        // Show confirmation dialog with animation
        NotificationManager.showConfirmation(
                (Stage) btnLogout.getScene().getWindow(),
                "Confirm Logout",
                "Are you sure you want to logout?",
                () -> { // OnConfirm action
                    // Clear the current user from system properties
                    System.clearProperty("currentUser");
                    System.clearProperty("userRole");

                    // Navigate back to the login page
                    if (mainApp != null) {
                        mainApp.showLoginScene();
                    } else {
                        System.err.println("Error: mainApp is null in handleLogout");
                    }
                },
                null // OnCancel action (do nothing)
        );
    }

    @FXML
    void handleAddUser(ActionEvent event) {
        if (mainApp != null) {
            mainApp.showAdminAddUserScene();
        } else {
            System.err.println("Error: mainApp is null in handleAddUser");
        }
    }

    public void setUsername(String email) throws SQLException {
        if (lblUsername != null) {
            if (userService == null) {
                userService = new UserService() {
                    @Override
                    public void add(User user) {
                    }

                    @Override
                    public void delete(int id) {

                    }
                };
            }

            try {
                User user = userService.getUserByEmail(email);
                if (user != null && user.getNom() != null && user.getPrenom() != null) {
                    lblUsername.setText(user.getPrenom() + " " + user.getNom());
                } else {
                    lblUsername.setText("User");
                }
            } catch (Exception e) {
                lblUsername.setText("User");
            }
        }
    }

    public void refreshDashboard() {
        setupDashboardStats();
        updateNotificationCount();
        if (dashboardHome != null) {
            contentPane.getChildren().setAll(dashboardHome);
        }
    }

    @FXML
    private void handleNotificationBellClick() {
        handleReports(new ActionEvent());
    }

    private void setupUserService() {
        try {
            userService = new UserService() {
                @Override
                public void add(User user) {
                    // Not needed for this controller
                }

                @Override
                public void delete(int id) {

                }
            };
        } catch (SQLException e) {
            System.err.println("Error initializing user service: " + e.getMessage());
        }
    }

    public void handleGenerateReports(ActionEvent actionEvent) {

    }

    // Inner class for user activity table
    public static class UserActivity {
        private String username;
        private String action;
        private Date timestamp;

        public UserActivity(String username, String action, Date timestamp) {
            this.username = username;
            this.action = action;
            this.timestamp = timestamp;
        }

        public String getUsername() { return username; }
        public String getAction() { return action; }
        public Date getTimestamp() { return timestamp; }
    }
}