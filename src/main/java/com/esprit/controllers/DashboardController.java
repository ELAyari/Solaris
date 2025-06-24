package com.esprit.controllers;

import com.esprit.models.Bin;
import com.esprit.models.Truck;
import com.esprit.Services.BinService;
import com.esprit.Services.NominatimService;
import com.esprit.Services.TruckService;
import com.esprit.tests.MainprogGUI;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.util.List;

public class DashboardController extends BaseBinController {
    @FXML private Label lblUsername;
    @FXML private Circle userAvatar;
    @FXML private WebView mapView;
    @FXML private StackPane contentPane;

    // Statistics labels
    @FXML private Label lblTotalBins;
    @FXML private Label lblTotalTrucks;
    @FXML private Label lblActiveTrucks;

    // Table views for data
    @FXML private TableView<Bin> binsTable;
    @FXML private TableView<Truck> trucksTable;

    // Services
    private final BinService binService = new BinService();
    private final TruckService truckService = new TruckService();
    private final NominatimService nominatimService = new NominatimService();

    // Main application reference
    private MainprogGUI mainApp;
    private Stage primaryStage;
    private Parent dashboardContent;


    /**
     * Set the main application reference
     * @param mainApp The main application instance
     */
    public void setMainApp(MainprogGUI mainApp) {
        this.mainApp = mainApp;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        initializeMap();
        loadData();

        // Load data into tables if they exist
        if (binsTable != null) {
            binsTable.getItems().setAll(binService.get());
        }
        if (trucksTable != null) {
            trucksTable.getItems().setAll(truckService.get());
        }
        if (contentPane != null && !contentPane.getChildren().isEmpty()) {
            dashboardContent = (Parent) contentPane.getChildren().get(0);
        }
    }

    /**
     * Method to handle navigation to the dashboard (self-refresh)
     */
    @FXML
    public void showDashboard() {
        refreshData();
    }

    private void initializeMap() {
        try {
            if (mapView == null) {
                System.err.println("Map view is null");
                return;
            }

            java.net.URL mapUrl = getClass().getResource("/views/map.html");
            if (mapUrl == null) {
                throw new RuntimeException("Map HTML file not found");
            }
            mapView.getEngine().load(mapUrl.toExternalForm());

            mapView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    updateMapMarkers();
                }
            });
        } catch (Exception e) {
            System.err.println("Error initializing map: " + e.getMessage());
            e.printStackTrace();
            showError("Map Error", "Failed to load map: " + e.getMessage());
        }
    }

    private void loadData() {
        try {
            // Load bins
            List<Bin> bins = binService.get();
            System.out.println("Loaded " + bins.size() + " bins");

            // Debug bins data
            for (Bin bin : bins) {
                System.out.println("Bin: " + bin.getLocation() +
                        " at (" + bin.getLatitude() + ", " + bin.getLongitude() + ")" +
                        " Type: " + bin.getTypeDechet() +
                        " Fill: " + bin.getNiveauRemplissage());
            }

            // Load trucks
            List<Truck> trucks = truckService.get();
            System.out.println("Retrieved " + trucks.size() + " trucks from service");

            // Debug trucks data
            for (Truck truck : trucks) {
                System.out.println("Truck: " + truck.getCurrentLocation() +
                        " at (" + truck.getDestinationLatitude1() + ", " + truck.getDestinationLongitude1() + ")" +
                        " Status: " + truck.getStatut());
            }

            // Update statistics
            updateStatistics(bins, trucks);

            // Update map markers
            updateMapMarkers(bins, trucks);

            // Explicitly refresh table contents
            if (binsTable != null) {
                binsTable.getItems().clear();
                binsTable.getItems().addAll(bins);
                System.out.println("Refreshed bins table with " + bins.size() + " bins");
            } else {
                System.err.println("Bins table is null, cannot refresh");
            }

            if (trucksTable != null) {
                trucksTable.getItems().clear();
                trucksTable.getItems().addAll(trucks);
                System.out.println("Refreshed trucks table with " + trucks.size() + " trucks");
            } else {
                System.err.println("Trucks table is null, cannot refresh");
            }

        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            e.printStackTrace();
            showError("Data Error", "Failed to load data: " + e.getMessage());
        }
    }

    private void updateStatistics(List<Bin> bins, List<Truck> trucks) {
        // Update total bins
        if (lblTotalBins != null) {
            lblTotalBins.setText(String.valueOf(bins.size()));
        }

        // Update total trucks
        if (lblTotalTrucks != null) {
            lblTotalTrucks.setText(String.valueOf(trucks.size()));
        }

        // Update available trucks (status = "Available")
        if (lblActiveTrucks != null) {
            long availableTrucks = trucks.stream()
                    .filter(t -> "Available".equals(t.getStatut()))
                    .count();
            lblActiveTrucks.setText(String.valueOf(availableTrucks));
        }
    }

    private void updateMapMarkers() {
        try {
            List<Bin> bins = binService.get();
            List<Truck> trucks = truckService.get();
            updateMapMarkers(bins, trucks);
        } catch (Exception e) {
            System.err.println("Error updating map markers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateMapMarkers(List<Bin> bins, List<Truck> trucks) {
        try {
            if (mapView == null || mapView.getEngine() == null) {
                System.err.println("Map view or engine is null");
                return;
            }

            // Wait for map to be loaded
            if (mapView.getEngine().getLoadWorker().getState() != Worker.State.SUCCEEDED) {
                System.out.println("Map not yet loaded, skipping marker update");
                return;
            }

            // Clear existing markers
            mapView.getEngine().executeScript("clearMarkers();");

            // Add bin markers
            for (Bin bin : bins) {
                String script = String.format(
                        "addBinMarker(%f, %f, '%s', '%s', %f);",
                        bin.getLatitude(),
                        bin.getLongitude(),
                        bin.getLocation().replace("'", "\\'"),
                        bin.getTypeDechet(),
                        bin.getNiveauRemplissage()
                );
                mapView.getEngine().executeScript(script);
            }

            // Add truck markers
            for (Truck truck : trucks) {
                String script = String.format(
                        "addTruckMarker(%f, %f, '%s', '%s');",
                        truck.getDestinationLatitude1(),
                        truck.getDestinationLongitude1(),
                        truck.getCurrentLocation().replace("'", "\\'"),
                        truck.getStatut()
                );
                mapView.getEngine().executeScript(script);
            }
        } catch (Exception e) {
            System.err.println("Error updating map markers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void refreshData() {
        loadData();
    }

    /**
     * Navigate to bin management view using mainApp
     */
    @FXML
    public void showBinsView() {
        if (primaryStage == null && mainApp != null) {
            // Try to get stage from mainApp as fallback
            primaryStage = mainApp.getPrimaryStage();
        }

        if (primaryStage == null) {
            // Try to get stage from the current scene as last resort
            if (mapView != null && mapView.getScene() != null) {
                primaryStage = (Stage) mapView.getScene().getWindow();
            }

            if (primaryStage == null) {
                showError("Navigation Error", "Cannot navigate: stage reference is missing.");
                return;
            }
        }

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Bin-form.fxml"));
                Parent fragmentContent = loader.load();

                BinFormController controller = loader.getController();
                // Pass references to the controller
                controller.setPrimaryStage(primaryStage);
               // controller.setMainApp(mainApp);
                controller.setDashboardController(this);

                if (contentPane != null) {
                    // Load as fragment in content pane
                    contentPane.getChildren().setAll(fragmentContent);
                } else {
                    // Fallback to full scene navigation if contentPane not found
                    Scene scene = new Scene(fragmentContent);
                    MainprogGUI.applyStylesheets(scene);
                    primaryStage.setScene(scene);
                    primaryStage.setTitle("Bin Management");
                }
            } catch (IOException e) {
                showError("Navigation Error", "Failed to navigate to bin management: " + e.getMessage());
            }
        });
    }
    /**
     * Navigate to truck management view using mainApp
     */
    @FXML
    public void showTrucksView() {
        if (primaryStage == null && mainApp != null) {
            // Try to get stage from mainApp as fallback
            primaryStage = mainApp.getPrimaryStage();
        }

        if (primaryStage == null) {
            // Try to get stage from the current scene as last resort
            if (mapView != null && mapView.getScene() != null) {
                primaryStage = (Stage) mapView.getScene().getWindow();
            }

            if (primaryStage == null) {
                showError("Navigation Error", "Cannot navigate: stage reference is missing.");
                return;
            }
        }

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Truck-form.fxml"));
                Parent fragmentContent = loader.load();

                TruckFormController controller = loader.getController();
                // Pass references to the controller
                controller.setPrimaryStage(primaryStage);
              //  controller.setMainApp(mainApp);
                controller.setDashboardController(this);

                if (contentPane != null) {
                    // Load as fragment in content pane
                    contentPane.getChildren().setAll(fragmentContent);
                } else {
                    // Fallback to full scene navigation if contentPane not found
                    Scene scene = new Scene(fragmentContent);
                    MainprogGUI.applyStylesheets(scene);
                    primaryStage.setScene(scene);
                    primaryStage.setTitle("Truck Management");
                }
            } catch (IOException e) {
                showError("Navigation Error", "Failed to navigate to truck management: " + e.getMessage());
            }
        });
    }

    /**
     * Navigate to solar bin management view using mainApp
     */

    @FXML
    public void showSolarBinManagerView() {
        if (primaryStage == null && mainApp != null) {
            // Try to get stage from mainApp as fallback
            primaryStage = mainApp.getPrimaryStage();
        }

        if (primaryStage == null) {
            // Try to get stage from the current scene as last resort
            if (mapView != null && mapView.getScene() != null) {
                primaryStage = (Stage) mapView.getScene().getWindow();
            }

            if (primaryStage == null) {
                showError("Navigation Error", "Cannot navigate: stage reference is missing.");
                return;
            }
        }

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/SolarBinManager.fxml"));
                Parent fragmentContent = loader.load();

                SolarBinManagerController controller = loader.getController();
                // Pass references to the controller
                controller.setPrimaryStage(primaryStage);
                controller.setMainApp(mainApp);
               // controller.setDashboardController(this);

                if (contentPane != null) {
                    // Load as fragment in content pane
                    contentPane.getChildren().setAll(fragmentContent);
                } else {
                    // Fallback to full scene navigation if contentPane not found
                    Scene scene = new Scene(fragmentContent);
                    MainprogGUI.applyStylesheets(scene);
                    primaryStage.setScene(scene);
                    primaryStage.setTitle("Solar Bin Management");
                }
            } catch (IOException e) {
                showError("Navigation Error", "Failed to navigate to solar bin management: " + e.getMessage());
            }
        });
    }
    /**
     * Analytics view (to be implemented)
     */
    @FXML
    private void showAnalyticsView() {
        if (mainApp != null) {
            // You can add analytics scene to MainprogGUI if needed
            showInfo("Analytics", "Analytics view coming soon!");
        } else {
            showInfo("Analytics", "Analytics view coming soon!");
        }
    }

    /**
     * Alerts view (to be implemented)
     */
    @FXML
    private void showAlertsView() {
        if (mainApp != null) {
            // You can add alerts scene to MainprogGUI if needed
            showInfo("Alerts", "Alerts view coming soon!");
        } else {
            showInfo("Alerts", "Alerts view coming soon!");
        }
    }

    /**
     * Settings view (to be implemented)
     */
    @FXML
    private void showSettingsView() {
        if (mainApp != null) {
            mainApp.showSettingsScene();
        } else {
            showInfo("Settings", "Settings view coming soon!");
        }
    }

    /**
     * Handle logout button
     */
    @FXML
    private void handleLogout() {
        if (mainApp != null) {
            mainApp.logout();
        } else {
            System.exit(0);
        }
    }

    /**
     * Helper to set dashboard controller in other controllers
     */
    public void setDashboardController(DashboardController dashboardController) {
        // This is needed for some controllers that reference the dashboard
    }

    /**
     * Show info dialog
     */
    public void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}