package com.esprit.tests;

import com.esprit.controllers.*;
import com.esprit.models.User;
import com.esprit.utils.ActivityLogger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class MainprogGUI extends Application {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(MainprogGUI.class.getName());
    private static MainprogGUI instance;

    public MainprogGUI() {
        instance = this;
    }

    public static MainprogGUI getInstance() {
        return instance;
    }

    private Stage primaryStage;
    private AdminDashboardController adminDashboardController;
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;

    // Scene cache for better performance
    private Map<String, Scene> sceneCache = new HashMap<>();

    // Navigation history for back navigation
    private Stack<NavigationHistoryEntry> navigationHistory = new Stack<>();

    private static final List<String> APP_STYLESHEETS = new ArrayList<>() {{
        add("/styles/profile.css");
        add("/styles/styles.css");
        add("/styles/sidebar.css");
        add("/styles/header.css");
        add("/styles/content.css");
        add("/styles/common.css");
        add("/styles/admin_dashboard_fixed.css");
        add("/styles/dashboard_cards.css");
    }};

    public void showBlogAdminScene() {
        loadScene("/blog_admin.fxml", BlogAdminController.class,
            controller -> controller.setMainApp(this));
    }

    public void showSettingsScene() {
        loadScene("/settings.fxml", SettingsController.class,
            controller -> controller.setMainApp(this));
    }

    public void showProductsDashboardScene() {
        loadScene("/products_dashboard.fxml", ProductsDashboardController.class,
            controller -> controller.setMainApp(this));
    }

    public void showMainDashboardScene() {
        loadScene("/views/MainDashboard.fxml", DashboardController.class,
                controller -> {
                    controller.setPrimaryStage(primaryStage);
                    controller.refreshData();
                    controller.setMainApp(this);
                });
    }

    public void showSolarBinManagerScene() {
        loadScene("/views/SolarBinManager.fxml", SolarBinManagerController.class,
                controller -> {
                    controller.setPrimaryStage(primaryStage);
                    controller.setMainApp(this);

                });
    }

    public void showProfileScene() {
        try {
            // Check if we're in the admin dashboard with content pane
            if (primaryStage.getScene() != null &&
                    primaryStage.getScene().getRoot() instanceof Parent &&
                    ((Parent)primaryStage.getScene().getRoot()).lookup("#contentPane") instanceof Pane) {

                // We're in admin dashboard, load as fragment
                Pane contentPane = (Pane)((Parent)primaryStage.getScene().getRoot()).lookup("#contentPane");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/user_profile.fxml"));
                Parent fragment = loader.load();

                UserProfileController controller = loader.getController();
                controller.setMainApp(this);

                contentPane.getChildren().setAll(fragment);

            } else {
                // Load as full scene
                loadScene("/user_profile.fxml", UserProfileController.class,
                        controller -> {
                            controller.setMainApp(this);
                            controller.refreshUserProfile();
                        });
            }
        } catch (Exception e) {
            handleException("Failed to load profile screen", e);
        }
    }

    public <T> void loadContentFragment(String fxmlPath, ControllerInitializer<T> initializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent contentRoot = loader.load();

            // Get the controller and initialize it with proper type casting
            T controller = (T) loader.getController();
            if (initializer != null) {
                initializer.initialize(controller);
            }

            // Find the content pane in the current scene
            Scene currentScene = primaryStage.getScene();

            // First check if we're already in admin dashboard
            if (currentScene == null) {
                LOGGER.warning("No current scene available, loading admin dashboard first");
                showAdminDashboardScene();

                // Now try again with the new scene
                currentScene = primaryStage.getScene();
                if (currentScene == null) {
                    LOGGER.severe("Still no current scene after loading admin dashboard");
                    return;
                }
            }

            StackPane contentPane = (StackPane) currentScene.lookup("#contentPane");

            if (contentPane != null) {
                // Clear current content and add new content
                contentPane.getChildren().clear();
                contentPane.getChildren().add(contentRoot);
                
                // Restore active tab state for client dashboard
                restoreClientDashboardTabState();
            } else {
                LOGGER.warning("Content pane not found in current scene");
                showAdminDashboardScene();
                contentPane = (StackPane) primaryStage.getScene().lookup("#contentPane");
                if (contentPane != null) {
                    contentPane.getChildren().clear();
                    contentPane.getChildren().add(contentRoot);
                    
                    // Restore active tab state for client dashboard
                    restoreClientDashboardTabState();
                } else {
                    LOGGER.severe("Content pane still not found after loading admin dashboard");
                }
            }
        } catch (IOException e) {
            handleException("Loading content fragment", e);
        } catch (ClassCastException e) {
            handleException("Controller type mismatch", e);
        }
    }

    /**
     * Restore the active tab state in the client dashboard
     */
    private void restoreClientDashboardTabState() {
        try {
            Scene currentScene = primaryStage.getScene();
            if (currentScene != null && currentScene.getRoot() != null) {
                // Look for ClientDashboardController in the scene
                Parent root = currentScene.getRoot();
                if (root.getUserData() instanceof com.esprit.controllers.ClientDashboardController) {
                    com.esprit.controllers.ClientDashboardController dashboardController = 
                        (com.esprit.controllers.ClientDashboardController) root.getUserData();
                    dashboardController.restoreActiveTab();
                } else {
                    // Try to find the controller through scene lookup
                    Object controller = currentScene.getUserData();
                    if (controller instanceof com.esprit.controllers.ClientDashboardController) {
                        com.esprit.controllers.ClientDashboardController dashboardController = 
                            (com.esprit.controllers.ClientDashboardController) controller;
                        dashboardController.restoreActiveTab();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Could not restore client dashboard tab state: " + e.getMessage());
        }
    }

    /**
     * Convenience method to load a fragment without initialization.
     */
    public void showFragment(String fxmlPath) {
        loadContentFragment(fxmlPath, controller -> {});
    }

    public void showReportsScene() {
        // Check if we're in the admin dashboard with content pane
        if (primaryStage.getScene() != null &&
                primaryStage.getScene().getRoot() instanceof Parent &&
                ((Parent)primaryStage.getScene().getRoot()).lookup("#contentPane") instanceof Pane) {

            // We're in admin dashboard, load as fragment
            loadContentFragment("/Reports.fxml",
                    (ReportsController controller) -> controller.setMainApp(this));
        } else {
            // Load as full scene
            loadScene("/Reports.fxml", ReportsController.class,
                    controller -> controller.setMainApp(this));
        }
    }

    public void showClaimsScene() {
        // Check if we're in the admin dashboard with content pane
        if (primaryStage.getScene() != null &&
                primaryStage.getScene().getRoot() instanceof Parent &&
                ((Parent)primaryStage.getScene().getRoot()).lookup("#contentPane") instanceof Pane) {

            // We're in admin dashboard, load as fragment
            loadContentFragment("/Claims.fxml",
                    (ClaimsController controller) -> controller.setMainApp(this));
        } else {
            // Load as full scene
            loadScene("/Claims.fxml", ClaimsController.class,
                    controller -> controller.setMainApp(this));
        }
    }

    public void showReportsDashboardScene() {

    }

    public void showClaimsDashboardScene() {
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void showSMSVerificationScene(User user) {
        System.out.println("Loading SMS Verification Scene");
        loadScene("/sms_verification.fxml", com.esprit.controllers.SMSVerificationController.class,
                controller -> {
                    controller.setMainApp(this);
                    controller.setUser(user);
                    try {
                        controller.setUserService(new com.esprit.Services.UserService() {
                            @Override
                            public void add(com.esprit.models.User user) {
                                // no-op
                            }

                            @Override
                            public void delete(int id) {
                                // no-op
                            }
                        });
                    } catch (java.sql.SQLException e) {
                        showError("Error initializing user service", e);
                    }
                });
    }

    /**
     * Class to store navigation history entries
     */
    private static class NavigationHistoryEntry {
        private final Scene scene;
        private final String fxmlPath;

        public NavigationHistoryEntry(Scene scene, String fxmlPath) {
            this.scene = scene;
            this.fxmlPath = fxmlPath;
        }

        public Scene getScene() {
            return scene;
        }

        public String getFxmlPath() {
            return fxmlPath;
        }
    }

    private User currentUser;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Solaris Desktop");

        // Set application icon
        try {
            String iconPath = "src/main/resources/images/logo.jpg";
            java.io.File iconFile = new java.io.File(iconPath);
            if (iconFile.exists()) {
                Image icon = new Image(iconFile.toURI().toString());
                this.primaryStage.getIcons().add(icon);
            } else {
                 System.err.println("Warning: Application icon not found at path: " + iconPath);
                 // As a fallback, try loading from resources stream, which may work in packaged apps
                 try {
                    Image icon = new Image(getClass().getResourceAsStream("/images/logo.png"));
                    this.primaryStage.getIcons().add(icon);
                 } catch(Exception e) {
                    System.err.println("Warning: Fallback icon logo.png also not found.");
                 }
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load application icon.");
            e.printStackTrace();
        }

        showLoginScene();
    }

    public User getUser() {
        return currentUser;
    }

    public void setUser(User user) {
        this.currentUser = user;
    }

    private <T> void loadModal(String fxmlPath, String title, Class<T> controllerClass, ControllerInitializer<T> initializer) {
        try {
            LOGGER.info("Loading modal FXML from path: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainprogGUI.class.getResource(fxmlPath));

            if (loader.getLocation() == null) {
                throw new IOException("Cannot find FXML file: " + fxmlPath);
            }

            Parent root = loader.load();
            LOGGER.info("Modal FXML loaded successfully");

            Stage modalStage = new Stage();
            modalStage.setTitle(title);
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(primaryStage);

            T controller = initializeController(loader, controllerClass, initializer);

            Scene scene = new Scene(root);
            applyStylesheets(scene);

            modalStage.setScene(scene);
            modalStage.showAndWait();
        } catch (IOException e) {
            handleException("loading modal " + fxmlPath, e);
        } catch (Exception e) {
            handleException("creating modal dialog", e);
        }
    }

    public <T> void loadScene(String fxmlPath, Class<T> controllerClass, ControllerInitializer<T> initializer) {
        try {
            // Check if scene is in cache
            if (sceneCache.containsKey(fxmlPath)) {
                LOGGER.info("Using cached scene for: " + fxmlPath);
                Scene cachedScene = sceneCache.get(fxmlPath);

                // Get the associated loader from user data
                FXMLLoader loader = (FXMLLoader) cachedScene.getUserData();
                if (loader != null) {
                    // Re-initialize controller with current state
                    T controller = controllerClass.cast(loader.getController());
                    initializer.initialize(controller);
                }

                // Record in navigation history before changing scene
                if (primaryStage.getScene() != null) {
                    pushToNavigationHistory(primaryStage.getScene(), fxmlPath);
                }

                primaryStage.setScene(cachedScene);
                primaryStage.show();
                return;
            }

            // Not in cache, load from FXML
            LOGGER.info("Loading FXML from path: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainprogGUI.class.getResource(fxmlPath));

            if (loader.getLocation() == null) {
                throw new IOException("Cannot find FXML file: " + fxmlPath);
            }

            Parent root = loader.load();
            LOGGER.info("FXML loaded successfully: " + fxmlPath);

            T controller = initializeController(loader, controllerClass, initializer);

            Scene scene = new Scene(root, WIDTH, HEIGHT);
            applyStylesheets(scene);

            // Store loader in scene user data for future controller access
            scene.setUserData(loader);

            // Add to cache for future use
            sceneCache.put(fxmlPath, scene);

            // Record in navigation history before changing scene
            if (primaryStage.getScene() != null) {
                pushToNavigationHistory(primaryStage.getScene(), fxmlPath);
            }

            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            handleException("loading scene " + fxmlPath, e);
        } catch (Exception e) {
            handleException("setting up scene " + fxmlPath, e);
        }
    }

    public void showLoginScene() {
        loadScene("/login_page.fxml", LoginController.class,
            controller -> controller.setMainApp(this));
    }

    public void showEmailVerificationScene(com.esprit.models.User user) {
        loadScene("/email_verification.fxml", com.esprit.controllers.EmailVerificationController.class,
            controller -> {
                controller.setMainApp(this);
                controller.setUser(user);
                try {
                    controller.setUserService(new com.esprit.Services.UserService() {
                        @Override
                        public void add(com.esprit.models.User user) {
                            // no-op
                        }

                        @Override
                        public void delete(int id) {

                        }
                    });
                } catch (SQLException e) {
                    showError("Error initializing user service", e);
                }
            });
    }

    public void showRegisterScene() {
        loadScene("/Registration_Page.fxml", RegistrationController.class,
            controller -> controller.setMainApp(this));
    }

    public void showAdminDashboardScene() {
        loadScene("/layouts/admin_layout.fxml", AdminLayoutController.class,
            controller -> {
                adminDashboardController = null;
                // Store the controller in the scene's userData for access by other components
                if (primaryStage != null && primaryStage.getScene() != null) {
                    FXMLLoader loader = (FXMLLoader) primaryStage.getScene().getUserData();
                    if (loader != null) {
                        ActivityLogger.logInfo("MainprogGUI", "Stored AdminLayoutController in scene userData");
                    }
                }
            });
    }

    public void showManageUserDashboardScene() {
        try {
            // Check if we're in the admin dashboard with content pane
            if (primaryStage.getScene() != null &&
                    primaryStage.getScene().getRoot() instanceof Parent &&
                    ((Parent)primaryStage.getScene().getRoot()).lookup("#contentPane") instanceof Pane) {

                // We're in admin dashboard, load as fragment
                Pane contentPane = (Pane)((Parent)primaryStage.getScene().getRoot()).lookup("#contentPane");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/manage_user_dashboard.fxml"));
                Parent fragment = loader.load();

                ManageUserDashboardController controller = loader.getController();
                controller.setMainApp(this);

                contentPane.getChildren().setAll(fragment);

            } else {
                // Load as full scene
                loadScene("/manage_user_dashboard.fxml", ManageUserDashboardController.class,
                        controller -> controller.setMainApp(this));
            }
        } catch (Exception e) {
            handleException("Failed to load user management dashboard", e);
        }
    }

    public void showAdminModifyUserScene(com.esprit.models.User user) {
        System.out.println("Showing Modify User Scene for user: " + user.getNom());

        // Check if we're in the admin dashboard with content pane
        if (primaryStage.getScene() != null &&
                primaryStage.getScene().getRoot() instanceof Parent &&
                ((Parent)primaryStage.getScene().getRoot()).lookup("#contentPane") instanceof Pane) {

            // We're in admin dashboard, load as fragment
            loadContentFragment("/AdminModifyUser.fxml",
                    (AdminModifyController controller) -> {
                        controller.setMainApp(this);
                        controller.setUser(user);
                    });
        } else {
            // Continue using modal approach as fallback
            loadModal("/AdminModifyUser.fxml", "Modify User", AdminModifyController.class,
                    controller -> {
                        controller.setMainApp(this);
                        controller.setUser(user);
                    });
        }
    }

    public void showEventPlannerDashboardScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventsView.fxml"));
            Parent root = loader.load();

            // Pass the current user into the controller
            EventController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            // Show scene
            Scene scene = new Scene(root, WIDTH, HEIGHT);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Liste des Événements");
            primaryStage.show();
        } catch (Exception e) {
            handleException("Erreur de chargement de la vue des événements", e);
        }
    }

    public void showAdminAddUserScene() {
        // Use loadContentFragment directly - we're already in a dashboard with content pane
        loadContentFragment("/AdminAddUser.fxml",
                (AdminAddUserController controller) -> controller.setMainApp(this));
    }

    /**
     * Handles user logout by clearing current user session and returning to login screen
     */
    public void logout() {
        LOGGER.info("User logout requested");

        // Clear the current user
        this.currentUser = null;

        // Clear any user-specific system properties
        System.clearProperty("currentUser");

        // Clear scene cache to ensure fresh login
        sceneCache.clear();

        // Clear navigation history
        navigationHistory.clear();

        // Return to login screen
        showLoginScene();

        LOGGER.info("User logged out successfully");
    }

    /**
     * Handles exceptions in a centralized way with proper logging and user feedback
     */
    public void handleException(String operation, Exception e) {
        LOGGER.log(Level.SEVERE, "Error during " + operation, e);

        // Categorize errors for better user feedback
        String userMessage;
        if (e instanceof IOException) {
            userMessage = "Could not load required files. Please check your installation.";
        } else if (e instanceof SQLException) {
            userMessage = "Database error occurred. Please try again later.";
        } else {
            userMessage = "An unexpected error occurred.";
        }

        showEnhancedError(operation + " failed", userMessage, e);
    }

    /**
     * Shows an enhanced error dialog with technical details in an expandable section
     */
    private void showEnhancedError(String title, String userMessage, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Application Error");
        alert.setHeaderText(title);
        alert.setContentText(userMessage);

        // Add technical details in expandable section
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("Technical details:");
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    /**
     * Backward compatibility method for showError
     * @param message Error message or title
     * @param e Exception that occurred
     */
    private void showError(String message, Exception e) {
        LOGGER.log(Level.SEVERE, message, e);
        handleException(message, e);
    }

    /**
     * Applies standard stylesheets to a scene
     */
    public static void applyStylesheets(Scene scene) {
        String[] stylesheets = {
            "/styles/common.css",
            "/styles/admin_dashboard_fixed.css",
            "/styles/dashboard_cards.css",
            "/styles/blog-dashboard.css"
        };

        for (String stylesheet : stylesheets) {
            try {
                String cssUrl = MainprogGUI.class.getResource(stylesheet).toExternalForm();
                if (!scene.getStylesheets().contains(cssUrl)) {
                    scene.getStylesheets().add(cssUrl);
                }
            } catch (Exception e) {
                LOGGER.warning("Could not load stylesheet: " + stylesheet);
            }
        }
    }

    /**
     * Initializes a controller with the provided initializer
     */
    private <T> T initializeController(FXMLLoader loader, Class<T> controllerClass, ControllerInitializer<T> initializer) {
        T controller = controllerClass.cast(loader.getController());
        LOGGER.info("Controller loaded: " + controller.getClass().getSimpleName());
        initializer.initialize(controller);
        return controller;
    }

    /**
     * Adds an entry to the navigation history
     */
    private void pushToNavigationHistory(Scene scene, String fxmlPath) {
        // Limit history size to prevent memory issues
        if (navigationHistory.size() >= 10) {
            navigationHistory.remove(0);
        }
        navigationHistory.push(new NavigationHistoryEntry(scene, fxmlPath));
        LOGGER.info("Added to navigation history: " + fxmlPath);
    }

    /**
     * Navigates back to the previous scene if available
     */
    public void navigateBack() {
        if (navigationHistory.size() > 0) {
            NavigationHistoryEntry entry = navigationHistory.pop();
            LOGGER.info("Navigating back to: " + entry.getFxmlPath());
            primaryStage.setScene(entry.getScene());
            primaryStage.show();
        } else {
            LOGGER.info("No navigation history available");
        }
    }

    /**
     * Clears the scene cache to force reload of scenes
     */
    public void clearSceneCache() {
        LOGGER.info("Clearing scene cache");
        sceneCache.clear();
    }

    /**
     * Navigates to the appropriate dashboard based on user role
     */
    public void navigateToRoleDashboard(User user) {
        if (user == null || user.getRole() == null) {
            LOGGER.warning("User or role is null, defaulting to admin dashboard");
            showAdminDashboardScene();
            return;
        }

        LOGGER.info("Navigating to dashboard for role: " + user.getRole());

        switch (user.getRole()) {
            case ADMIN:
                showAdminDashboardScene();
                break;
            case BLOGER_ADMIN:
                showBlogerAdminDashboardScene();
                break;
            case CLIENT:
                showClientDashboardScene();
                break;
            case EVENT_PLANNER:
                showEventPlannerDashboardScene();
                break;
            case PRODUCT_OWNER:
                showProductOwnerDashboardScene();
                break;
            case TRUCK_DRIVER:
                showTruckDriverDashboardScene();
                break;
            default:
                LOGGER.warning("Unknown role: " + user.getRole() + ", defaulting to admin dashboard");
                showAdminDashboardScene();
        }
    }

    public void showForgetPasswordScene() {
        loadScene("/ForgetPassword.fxml", ForgetPasswordController.class,
                controller -> controller.setMainApp(this));
    }

    // Reports and Claims scene methods are already defined above
    // User Stats Dashboard has been consolidated into the main Admin Dashboard

    public void showUpdatePasswordScene(String email) {
        loadScene("/UpdatePassword.fxml", com.esprit.controllers.UpdatePasswordController.class,
            controller -> {
                controller.setMainApp(this);
                controller.setUserEmail(email);
            });
    }

    public void loadSceneInPrimaryStage(String fxmlPath, String title) {
        try {
            System.out.println("Loading FXML from path: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainprogGUI.class.getResource(fxmlPath));

            if (loader.getLocation() == null) {
                throw new IOException("Cannot find FXML file: " + fxmlPath);
            }

            System.out.println("FXML URL: " + loader.getLocation());

            Parent root = loader.load();
            System.out.println("FXML loaded successfully");

            // Set the MainApp reference in the controller if it has setMainApp method
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    java.lang.reflect.Method setMainApp = controller.getClass().getMethod("setMainApp", MainprogGUI.class);
                    setMainApp.invoke(controller, this);
                } catch (NoSuchMethodException e) {
                    // Controller doesn't have setMainApp method, that's okay
                    System.out.println("Note: Controller doesn't have setMainApp method");
                } catch (Exception e) {
                    System.err.println("Error setting MainApp in controller: " + e.getMessage());
                }
            }

            Scene scene = new Scene(root, WIDTH, HEIGHT);

            // Add stylesheets
            String[] stylesheets = {
                "/styles/common.css",
                "/styles/admin_dashboard_fixed.css",
                "/styles/dashboard_cards.css",
                "/styles/blog-dashboard.css"
            };

            for (String stylesheet : stylesheets) {
                String cssUrl = MainprogGUI.class.getResource(stylesheet).toExternalForm();
                if (!scene.getStylesheets().contains(cssUrl)) {
                    scene.getStylesheets().add(cssUrl);
                }
            }

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
            showError("Could not load " + fxmlPath, e);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading scene", e);
        }
    }

    public void showLimitedAccessScene() {
        // Implementation can be added later if needed
    }

    // Profile Scene method already declared above

    public void showMFAVerificationScene(User user, String email) {
        loadScene("/email_verification.fxml", com.esprit.controllers.EmailVerificationController.class,
            controller -> {
                controller.setMainApp(this);
                controller.setUser(user);
                try {
                    controller.setUserService(new com.esprit.Services.UserService() {
                        @Override
                        public void add(com.esprit.models.User user) {
                            // no-op
                        }

                        @Override
                        public void delete(int id) {

                        }
                    });
                } catch (java.sql.SQLException e) {
                    showError("Error initializing user service", e);
                }
            });
    }

    public void showMFAChoiceScene(User user) {
        try {
            System.out.println("Loading MFA Choice Scene");
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainprogGUI.class.getResource("/mfa_choice.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Cannot find FXML file: /mfa_choice.fxml");
            }
            Parent root = loader.load();
            System.out.println("FXML loaded successfully for MFA Choice Scene");

            Object controllerObj = loader.getController();
            System.out.println("Controller loaded: " + controllerObj.getClass().getSimpleName());
            if (!(controllerObj instanceof com.esprit.controllers.MFAChoiceController)) {
                throw new ClassCastException("Controller is not an instance of MFAChoiceController");
            }
            com.esprit.controllers.MFAChoiceController controller = (com.esprit.controllers.MFAChoiceController) controllerObj;
            controller.setMainApp(this);
            controller.setUser(user);

            Scene scene = new Scene(root, WIDTH, HEIGHT);

            String[] stylesheets = {
                "/styles/common.css",
                "/styles/admin_dashboard_fixed.css",
                "/styles/dashboard_cards.css"
            };

            for (String stylesheet : stylesheets) {
                String cssUrl = MainprogGUI.class.getResource(stylesheet).toExternalForm();
                if (!scene.getStylesheets().contains(cssUrl)) {
                    scene.getStylesheets().add(cssUrl);
                }
            }

            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading MFA Choice Scene");
            e.printStackTrace();
            showError("Could not load MFA Choice Scene", e);
        } catch (ClassCastException e) {
            System.err.println("Controller class cast error in MFA Choice Scene");
            e.printStackTrace();
            showError("Controller class cast error", e);
        } catch (Exception e) {
            System.err.println("Unexpected error loading MFA Choice Scene");
            e.printStackTrace();
            showError("Unexpected error loading MFA Choice Scene", e);
        }
    }

    /**
     * TODO: Show BlogerAdmin dashboard scene
     * Implementation needed by blog feature team
     */
    public void showBlogerAdminDashboardScene() {
        // TODO: Implement BlogerAdmin dashboard scene
        // Example implementation:
        // loadScene("/bloger_admin_dashboard.fxml", BlogerAdminDashboardController.class,
        //     controller -> controller.setMainApp(this));

        // Temporary fallback
        LOGGER.info("BlogerAdmin dashboard not implemented yet, using admin dashboard");
        showAdminDashboardScene();
    }    /**
     * Show Client dashboard scene
     * Implementation for CLIENT role users
     */
    public void showClientDashboardScene() {
        try {
            LOGGER.info("Loading client dashboard scene");
            loadScene("/client_dashboard.fxml", ClientDashboardController.class,
                controller -> controller.setMainApp(this));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load client dashboard scene", e);
            showError("Failed to load client dashboard", e);
            // Fallback to admin dashboard if client dashboard fails to load
            showAdminDashboardScene();
        }
    }

    // EventPlanner dashboard scene method already defined above

    /**
     * TODO: Show ProductOwner dashboard scene
     * Implementation needed by product feature team
     */
    public void showProductOwnerDashboardScene() {
        // TODO: Implement ProductOwner dashboard scene
        // Example implementation:
        // loadScene("/product_owner_dashboard.fxml", ProductOwnerDashboardController.class,
        //     controller -> controller.setMainApp(this));

        // Temporary fallback
        LOGGER.info("ProductOwner dashboard not implemented yet, using admin dashboard");
        showAdminDashboardScene();
    }

    /**
     * TODO: Show TruckDriver dashboard scene
     * Implementation needed by logistics feature team
     */
    public void showTruckDriverDashboardScene() {
        // TODO: Implement TruckDriver dashboard scene
        // Example implementation:
        // loadScene("/truck_driver_dashboard.fxml", TruckDriverDashboardController.class,
        //     controller -> controller.setMainApp(this));

        // Temporary fallback
        LOGGER.info("TruckDriver dashboard not implemented yet, using admin dashboard");
        showAdminDashboardScene();
    }

    @FunctionalInterface
    public interface ControllerInitializer<T> {
        void initialize(T controller);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
