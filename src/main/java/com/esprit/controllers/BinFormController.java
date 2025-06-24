package com.esprit.controllers;

import com.esprit.models.Bin;
import com.esprit.Services.BinService;
import com.esprit.Services.NominatimService;
import com.esprit.Services.SolarBinService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class BinFormController extends BaseBinController {
    // Form fields
    @FXML private TextField locationField;
    @FXML private ComboBox<String> wasteTypeCombo;
    @FXML private TextField fillLevelField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    
    // Solar bin fields
    @FXML private CheckBox solarPoweredCheckbox;
    @FXML private VBox solarOptionsBox;
    @FXML private TextField batteryLevelField;
    @FXML private TextField solarOutputField;

    // Data and services
    private Bin binToEdit;
    private BinService binService = new BinService();
    private NominatimService nominatimService = new NominatimService();
    private SolarBinService solarBinService = new SolarBinService(binService);
    private DashboardController dashboardController;
    
    // UI elements for sidebar
    @FXML private Circle sidebarAvatar;
    @FXML private Label sidebarUsername;

    // Available status options
    private final ObservableList<String> statusOptions = FXCollections.observableArrayList(
            "Active",
            "Inactive",
            "Maintenance"
    );
    private final ObservableList<String> wasteTypeOptions = FXCollections.observableArrayList(
            "Plastic",
            "Glass",
            "Metal",
            "Paper",
            "Organic",
            "Household",
            "Hazardous"
    );

    private java.util.Timer locationUpdateTimer;

    @FXML
    public void initialize() {
        // Init services
        nominatimService = new NominatimService();
        binService = new BinService();
        solarBinService = new SolarBinService(binService);
        
        // Set up the map
        initializeMap();
        
        // Initialize sidebar username
        if (sidebarUsername != null) {
            sidebarUsername.setText("Administrator");
        }
        
        // Initialize ComboBoxes
        wasteTypeCombo.setItems(wasteTypeOptions);
        wasteTypeCombo.setValue(wasteTypeOptions.get(0)); // Default to first option
        
        statusCombo.setItems(statusOptions);
        statusCombo.setValue(statusOptions.get(0)); // Default to first option
        
        // Set up the solar options visibility
        solarPoweredCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            solarOptionsBox.setVisible(newVal);
            solarOptionsBox.setManaged(newVal);
            
            // Set default values if checked
            if (newVal) {
                batteryLevelField.setText("100");
                solarOutputField.setText("120");
            }
        });
        
        // Add listener to location field to automatically update map when changed
        locationField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty() && !newValue.equals(oldValue)) {
                // Don't update immediately to avoid too many API calls while typing
                if (locationUpdateTimer != null) {
                    locationUpdateTimer.cancel();
                }
                
                // Create a new timer to update after a short delay
                locationUpdateTimer = new java.util.Timer();
                locationUpdateTimer.schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> {
                            try {
                                updateMapFromAddress(newValue);
                            } catch (Exception e) {
                                // Silent fail - we don't want to bother the user with geocoding errors while typing
                            }
                        });
                    }
                }, 800); // 800ms delay
            }
        });

        // If we're editing a bin, load its data
        if (binToEdit != null) {
            setBinData(binToEdit);
        }
    }

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void setBinData(Bin bin) {
        this.binToEdit = bin;
        locationField.setText(bin.getLocation());
        wasteTypeCombo.setValue(bin.getTypeDechet());
        fillLevelField.setText(String.valueOf(bin.getNiveauRemplissage()));
        statusCombo.setValue(bin.getStatut());
        latitudeField.setText(String.valueOf(bin.getLatitude()));
        longitudeField.setText(String.valueOf(bin.getLongitude()));
        
        // Update coordinate display label
        String coordText = String.format("%.6f, %.6f", bin.getLatitude(), bin.getLongitude());
        coordinateDisplayLabel.setText(coordText);
        
        // Set solar bin properties if applicable
        if (bin.isSolarPowered()) {
            solarPoweredCheckbox.setSelected(true);
            batteryLevelField.setText(String.valueOf(bin.getBatteryLevel()));
            solarOutputField.setText(String.valueOf(bin.getSolarPanelOutput()));
        }
        
        // Update map after setting coordinates
        updateMapFromCoordinates();
    }

    // Ajoutez ces nouveaux champs
    @FXML private WebView mapView;
    @FXML private Label mapStatusLabel;
    @FXML private Label coordinateDisplayLabel;

    private void initializeMap() {
        try {
            URL mapUrl = getClass().getResource("/views/map.html");
            if (mapUrl != null) {
                mapView.getEngine().load(mapUrl.toExternalForm());
                mapView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        System.out.println("Map loaded successfully");
                        mapStatusLabel.setText("Map ready");
                        // Update map if coordinates are available
                        updateMapFromCoordinates();
                    }
                });
            } else {
                System.err.println("Map HTML file not found");
                mapStatusLabel.setText("Error: Map file missing");
            }
        } catch (Exception e) {
            System.err.println("Map initialization error: " + e.getMessage());
            mapStatusLabel.setText("Map loading error");
        }
    }

    private void updateMapFromCoordinates() {
        try {
            double lat = Double.parseDouble(latitudeField.getText());
            double lng = Double.parseDouble(longitudeField.getText());
            
            if (isValidCoordinate(lat, lng)) {
                updateMapMarker(lat, lng, locationField.getText());
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid coordinates: " + e.getMessage());
        }
    }
    
    /**
     * Updates the map when an address is entered
     * 
     * @param address The address to geocode and show on the map
     * @throws Exception If geocoding fails
     */
    private void updateMapFromAddress(String address) throws Exception {
        if (address == null || address.trim().isEmpty()) {
            return;
        }
        
        // Get coordinates from the address
        double[] coordinates = nominatimService.getCoordinates(address);
        
        if (coordinates != null && coordinates.length == 2) {
            // Store the coordinates in the hidden fields
            latitudeField.setText(String.valueOf(coordinates[0]));
            longitudeField.setText(String.valueOf(coordinates[1]));
            
            // Update the map with the marker
            updateMapMarker(coordinates[0], coordinates[1], address);
            mapStatusLabel.setText("Location found: " + address);
        } else {
            mapStatusLabel.setText("Searching for location...");
        }
    }

    @FXML
    private void handleSearchCoordinates() {
        String address = locationField.getText().trim();
        if (address.isEmpty()) {
            showError("Error", "Please enter a location");
            return;
        }

        mapStatusLabel.setText("Searching location...");
        coordinateDisplayLabel.setText("Searching...");
        
        new Thread(() -> {
            try {
                double[] coords = nominatimService.getCoordinates(address);
                Platform.runLater(() -> {
                    // Update hidden fields
                    latitudeField.setText(String.format("%.6f", coords[0]));
                    longitudeField.setText(String.format("%.6f", coords[1]));
                    
                    // Update display label
                    String coordText = String.format("%.6f, %.6f", coords[0], coords[1]);
                    coordinateDisplayLabel.setText(coordText);
                    
                    // Update map
                    updateMapMarker(coords[0], coords[1], address);
                    mapStatusLabel.setText("Location found");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    mapStatusLabel.setText("Location search failed");
                    coordinateDisplayLabel.setText("Search failed");
                    showError("Location Error", "Could not find location: " + e.getMessage());
                });
            }
        }).start();
    }

    private void updateMapMarker(double lat, double lng, String title) {
        try {
            mapStatusLabel.setText("Updating map...");
            JSObject window = (JSObject) mapView.getEngine().executeScript("window");
            if (window != null) {
                // Clear existing markers
                window.call("clearMarkers");
                // Add new marker
                window.call("addBinMarker", lat, lng, title, "Location", 0);
                // Center and zoom the map
                window.call("centerMap", lat, lng, 16);
                mapStatusLabel.setText("Map updated");
            }
        } catch (Exception e) {
            System.err.println("Error updating map marker: " + e.getMessage());
            mapStatusLabel.setText("Map update failed");
        }
    }

    @FXML
    private void handleAddButton() {
        try {
            // Only validate the essential fields - no coordinates needed
            if (locationField.getText().trim().isEmpty() || 
                wasteTypeCombo.getValue() == null || 
                fillLevelField.getText().trim().isEmpty() || 
                statusCombo.getValue() == null) {
                showError("Missing Information", "Please fill in all required fields");
                return;
            }
            
            // Parse the fill level
            double niveauRemplissage;
            try {
                niveauRemplissage = parseDoubleLocalized(fillLevelField.getText());
                if (niveauRemplissage < 0 || niveauRemplissage > 100) {
                    showError("Invalid Input", "Fill level must be between 0 and 100");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Invalid Format", "Please enter a valid fill level");
                return;
            }

            if (binToEdit == null) {
                // For new bins, just use the address directly
                createSimpleBin(niveauRemplissage);
            } else {
                // For existing bins, update using the address
                updateSimpleBin(niveauRemplissage);
            }

            handleBackButton();

        } catch (Exception e) {
            showError("Error", e.getMessage());
        }
    }

    // This method is replaced by inline validation in handleAddButton

    private double parseDoubleLocalized(String value) throws NumberFormatException {
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException("Empty value");
        }
        return Double.parseDouble(value.replace(",", ".").trim());
    }

    // This method is replaced by inline validation in handleAddButton

    private boolean isValidCoordinate(double lat, double lon) {
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }

    /**
     * Creates a new bin using just the address - no coordinate handling needed
     */
    private void createSimpleBin(double remplissage) {
        // Get the address directly from the text field
        String address = locationField.getText().trim();
        
        if (solarPoweredCheckbox.isSelected()) {
            try {
                // Validate solar bin fields
                if (batteryLevelField.getText().trim().isEmpty() || solarOutputField.getText().trim().isEmpty()) {
                    showError("Missing Information", "Please fill in battery level and solar output");
                    return;
                }
                
                double batteryLevel = parseDoubleLocalized(batteryLevelField.getText());
                double solarOutput = parseDoubleLocalized(solarOutputField.getText());
                
                if (batteryLevel < 0 || batteryLevel > 100) {
                    showError("Invalid Input", "Battery level must be between 0 and 100");
                    return;
                }
                
                if (solarOutput <= 0) {
                    showError("Invalid Input", "Solar panel output must be greater than 0");
                    return;
                }
                
                // Create directly from address without showing coordinates
                try {
                    Bin newBin = solarBinService.createSolarBinByAddress(
                        address, wasteTypeCombo.getValue(), remplissage,
                        batteryLevel, solarOutput, nominatimService
                    );
                    
                    // Set status which isn't handled by the service
                    newBin.setStatut(statusCombo.getValue());
                    binService.update(newBin);
                    
                    showInfo("Success", "Solar bin added successfully at " + address);
                } catch (Exception e) {
                    showError("Location Error", "Could not find location: " + e.getMessage());
                }
            } catch (NumberFormatException e) {
                showError("Invalid Input", "Please enter valid numbers for battery level and solar output");
            }
        } else {
            // For regular bins, we also need to look up the coordinates
            try {
                // Geocode the address
                double[] coords = nominatimService.getCoordinates(address);
                
                // Create the bin
                Bin newBin = new Bin();
                newBin.setLocation(address);
                newBin.setTypeDechet(wasteTypeCombo.getValue());
                newBin.setNiveauRemplissage(remplissage);
                newBin.setStatut(statusCombo.getValue());
                newBin.setLatitude(coords[0]);
                newBin.setLongitude(coords[1]);

                binService.add(newBin);
                showInfo("Success", "Bin added successfully at " + address);
            } catch (Exception e) {
                showError("Location Error", "Could not find location: " + e.getMessage());
            }
        }
    }

    /**
     * Updates an existing bin using just the address - coordinates updated automatically
     */
    private void updateSimpleBin(double remplissage) {
        String address = locationField.getText().trim();
        
        // Update basic bin properties
        binToEdit.setLocation(address);
        binToEdit.setTypeDechet(wasteTypeCombo.getValue());
        binToEdit.setNiveauRemplissage(remplissage);
        binToEdit.setStatut(statusCombo.getValue());
        
        // Try to update coordinates if address changed
        try {
            // Only update coordinates if location changed
            if (!address.equals(binToEdit.getLocation())) {
                double[] coords = nominatimService.getCoordinates(address);
                binToEdit.setLatitude(coords[0]);
                binToEdit.setLongitude(coords[1]);
            }
        } catch (Exception e) {
            // If geocoding fails, keep existing coordinates
            System.err.println("Could not update coordinates: " + e.getMessage());
        }
        
        // Update solar bin properties if checkbox is selected
        boolean wasSolar = binToEdit.isSolarPowered();
        boolean isSolarNow = solarPoweredCheckbox.isSelected();
        
        if (isSolarNow) {
            try {
                // Validate solar bin fields
                if (batteryLevelField.getText().trim().isEmpty() || solarOutputField.getText().trim().isEmpty()) {
                    showError("Missing Information", "Please fill in battery level and solar output");
                    return;
                }
                
                double batteryLevel = parseDoubleLocalized(batteryLevelField.getText());
                double solarOutput = parseDoubleLocalized(solarOutputField.getText());
                
                if (batteryLevel < 0 || batteryLevel > 100) {
                    showError("Invalid Input", "Battery level must be between 0 and 100");
                    return;
                }
                
                if (solarOutput <= 0) {
                    showError("Invalid Input", "Solar panel output must be greater than 0");
                    return;
                }
                
                // Converting a regular bin to solar
                if (!wasSolar) {
                    solarBinService.convertToSolarBin(binToEdit.getId(), batteryLevel, solarOutput);
                    showInfo("Success", "Bin updated and converted to solar power");
                } else {
                    // Just updating an existing solar bin
                    binToEdit.setBatteryLevel(batteryLevel);
                    binToEdit.setSolarPanelOutput(solarOutput);
                    binService.update(binToEdit);
                    showInfo("Success", "Solar bin updated successfully");
                }
            } catch (NumberFormatException e) {
                showError("Invalid Input", "Please enter valid numbers for battery level and solar output");
            }
        } else {
            // If it was solar and now it's not, remove solar properties
            if (wasSolar) {
                binToEdit.setSolarPowered(false);
                binToEdit.setBatteryLevel(0);
                binToEdit.setSolarPanelOutput(0);
                binToEdit.setEnergyStatus("N/A");
            }
            
            binService.update(binToEdit);
            showInfo("Success", "Bin updated successfully");
        }
    }

 
    /**
     * Navigation method to go to dashboard
     */
    @FXML
    public void showDashboard() {
        navigateToDashboard();
    }
    
    /**
     * Navigation method to go to bin management (self-refresh)
     */
    @FXML
    public void showBinsView() {
        // This is the current view, so just refresh
        refreshMap();
    }
    
    /**
     * Navigation method to go to solar bin management
     */
    @FXML
    public void showSolarBinManagerView() {
        navigateToSolarBinManager();
    }
    
    /**
     * Navigation method to go to truck management
     */
    @FXML
    public void showTrucksView() {
        navigateToTruckManager();
    }
    
    /**
     * Refresh the map display
     */
    @FXML
    public void refreshMap() {
        if (locationField.getText() != null && !locationField.getText().trim().isEmpty()) {
            try {
                updateMapFromAddress(locationField.getText());
            } catch (Exception e) {
                showError("Map Error", "Failed to update map: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleGenerateSolarBinsButton() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Generate Sample Solar Bins");
        confirmAlert.setHeaderText("Create Sample Data");
        confirmAlert.setContentText("This will create 7 sample solar-powered bins with different statuses, fill levels, and battery levels. Continue?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                CompletableFuture.runAsync(() -> {
                    try {
                        generateSampleSolarBins();
                        Platform.runLater(() -> {
                            showInfo("Success", "Sample solar bins generated successfully!");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showError("Error", "Failed to generate sample bins: " + e.getMessage());
                        });
                    }
                });
            }
        });
    }
    
    @FXML
    private void handleDeleteAllSolarBinsButton() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete All Solar Bins");
        confirmAlert.setHeaderText("Caution: This action cannot be undone");
        confirmAlert.setContentText("This will delete ALL solar-powered bins from the database. Are you sure you want to continue?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                CompletableFuture.runAsync(() -> {
                    try {
                        int deletedCount = deleteAllSolarBins();
                        Platform.runLater(() -> {
                            showInfo("Success", deletedCount + " solar bins were deleted successfully!");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showError("Error", "Failed to delete solar bins: " + e.getMessage());
                        });
                    }
                });
            }
        });
    }
    
    private int deleteAllSolarBins() {
        List<Bin> solarBins = binService.getSolarPoweredBins();
        int count = 0;
        
        for (Bin bin : solarBins) {
            binService.delete(bin); // Pass the bin object, not just the ID
            count++;
        }
        
        return count;
    }
    
    private void generateSampleSolarBins() {
        // Sample data for bins
        List<SampleBinData> sampleBins = new ArrayList<>();
        
        // Bin 1: Downtown location with high fill level and good battery
        sampleBins.add(new SampleBinData(
            "Downtown Main Street", 
            "Plastic", 
            85.0,  // High fill level
            "Active", 
            36.1716, 
            -115.1391, 
            "Fully Charged", 
            85.0,  // Good battery level
            120.0, // Solar panel output in watts
            LocalDate.now().minusDays(30), 
            LocalDate.now().plusDays(60),
            "Regular maintenance completed last month"
        ));
        
        // Bin 2: Park location with medium fill level and low battery
        sampleBins.add(new SampleBinData(
            "Central Park", 
            "Organic", 
            45.0,  // Medium fill level
            "Active", 
            36.1699, 
            -115.1398, 
            "Low", 
            15.0,  // Low battery level
            100.0, // Solar panel output in watts
            LocalDate.now().minusDays(60), 
            LocalDate.now().minusDays(5),  // Maintenance overdue
            "Battery replacement may be needed"
        ));
        
        // Bin 3: Shopping mall with high fill level and medium battery
        sampleBins.add(new SampleBinData(
            "Westfield Mall", 
            "Mixed", 
            90.0,  // Very high fill level
            "Active", 
            36.1689, 
            -115.1382, 
            "Medium", 
            55.0,  // Medium battery level
            110.0, // Solar panel output in watts
            LocalDate.now().minusDays(45), 
            LocalDate.now().plusDays(45),
            "Compaction system upgraded"
        ));
        
        // Bin 4: School with low fill level but low battery
        sampleBins.add(new SampleBinData(
            "Lincoln High School", 
            "Paper", 
            20.0,  // Low fill level
            "Active", 
            36.1705, 
            -115.1375, 
            "Low", 
            10.0,  // Very low battery level
            90.0,  // Solar panel output in watts
            LocalDate.now().minusDays(90), 
            LocalDate.now().minusDays(10),  // Maintenance overdue
            "Battery efficiency decreasing"
        ));
        
        // Bin 5: Office complex with medium fill and full battery
        sampleBins.add(new SampleBinData(
            "Business District", 
            "Glass", 
            60.0,  // Medium fill level
            "Active", 
            36.1675, 
            -115.1410, 
            "Fully Charged", 
            98.0,  // Full battery
            130.0, // High solar panel output in watts
            LocalDate.now().minusDays(15), 
            LocalDate.now().plusDays(75),
            "New high-efficiency solar panels installed"
        ));
        
        // Bin 6: Restaurant area with high fill and medium battery
        sampleBins.add(new SampleBinData(
            "Restaurant Row", 
            "Organic", 
            75.0,  // High fill level
            "Active", 
            36.1722, 
            -115.1402, 
            "Medium", 
            45.0,  // Medium battery level
            105.0, // Solar panel output in watts
            LocalDate.now().minusDays(40), 
            LocalDate.now().plusDays(50),
            "Compaction frequency increased due to high usage"
        ));
        
        // Bin 7: Beach area with low fill but good battery
        sampleBins.add(new SampleBinData(
            "Waterfront", 
            "Plastic", 
            25.0,  // Low fill level
            "Active", 
            36.1730, 
            -115.1420, 
            "Fully Charged", 
            90.0,  // Good battery level
            115.0, // Solar panel output in watts
            LocalDate.now().minusDays(20), 
            LocalDate.now().plusDays(70),
            "Waterproof casing recently checked"
        ));
        
        // Create the bins in the database
        for (SampleBinData sampleBin : sampleBins) {
            Bin bin = new Bin();
            bin.setLocation(sampleBin.location);
            bin.setTypeDechet(sampleBin.wasteType);
            bin.setNiveauRemplissage(sampleBin.fillLevel);
            bin.setStatut(sampleBin.status);
            bin.setLatitude(sampleBin.latitude);
            bin.setLongitude(sampleBin.longitude);
            bin.setSolarPowered(true);
            bin.setEnergyStatus(sampleBin.energyStatus);
            bin.setBatteryLevel(sampleBin.batteryLevel);
            bin.setSolarPanelOutput(sampleBin.solarPanelOutput);
            bin.setLastMaintenanceDate(sampleBin.lastMaintenanceDate);
            bin.setNextScheduledMaintenance(sampleBin.nextScheduledMaintenance);
            bin.setMaintenanceNotes(sampleBin.maintenanceNotes);
            
            binService.add(bin);
        }
    }
    
    // Helper class to store sample bin data
    private static class SampleBinData {
        String location;
        String wasteType;
        double fillLevel;
        String status;
        double latitude;
        double longitude;
        String energyStatus;
        double batteryLevel;
        double solarPanelOutput;
        LocalDate lastMaintenanceDate;
        LocalDate nextScheduledMaintenance;
        String maintenanceNotes;
        
        public SampleBinData(String location, String wasteType, double fillLevel, String status,
                          double latitude, double longitude, String energyStatus, double batteryLevel,
                          double solarPanelOutput, LocalDate lastMaintenanceDate,
                          LocalDate nextScheduledMaintenance, String maintenanceNotes) {
            this.location = location;
            this.wasteType = wasteType;
            this.fillLevel = fillLevel;
            this.status = status;
            this.latitude = latitude;
            this.longitude = longitude;
            this.energyStatus = energyStatus;
            this.batteryLevel = batteryLevel;
            this.solarPanelOutput = solarPanelOutput;
            this.lastMaintenanceDate = lastMaintenanceDate;
            this.nextScheduledMaintenance = nextScheduledMaintenance;
            this.maintenanceNotes = maintenanceNotes;
        }
    }
    
    @FXML
    private void handleBackButton() {
        // Navigate back to dashboard using base controller method
        navigateToDashboard();
    }
}