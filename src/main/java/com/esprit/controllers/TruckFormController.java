package com.esprit.controllers;

import com.esprit.models.EnergyEfficiencyMetrics;
import com.esprit.models.SolarBinStatus;
import com.esprit.models.Truck;
import com.esprit.models.TruckZone;
import com.esprit.models.Bin;
import com.esprit.Services.BinService;
import com.esprit.Services.EnergyEfficiencyService;
import com.esprit.Services.NearbyBinService;
import com.esprit.Services.NominatimService;
import com.esprit.Services.TruckService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

// Do not rely on static imports for important variables

public class TruckFormController extends BaseBinController {
    @FXML
    private TextField TC;  // Capacité Max
    @FXML
    private TextField TST; // Niveau Remplissage
    @FXML
    private ComboBox<String> TR;  // Section - Changed to ComboBox (hidden)
    @FXML
    private ComboBox<String> TS;  // Statut
    @FXML
    private ComboBox<String> truckBrandComboBox; // Truck brand selection
    @FXML
    private ComboBox<String> binTypeComboBox; // Bin type collection
    @FXML
    private TextField currentLocationField;
    @FXML
    private TextField destinationLocationField;
    @FXML
    private TextField destLat1Field;
    @FXML
    private TextField destLng1Field;
    @FXML
    private TextField destLat2Field;
    @FXML
    private TextField destLng2Field;
    @FXML
    private WebView mapView;
    @FXML
    private Label mapStatusLabel;

    // Solar bin status elements
    @FXML
    private TableView<SolarBinStatus> solarBinsTable;
    @FXML
    private Label solarBinStatusLabel;
    @FXML
    private Button refreshBinsButton;
    
    // Energy efficiency dashboard elements
    @FXML
    private Button calculateEfficiencyButton;
    @FXML
    private Label efficiencyStatusLabel;
    @FXML
    private Label energyConsumptionLabel;
    @FXML
    private Label co2EmissionsLabel;
    @FXML
    private Label efficiencyRatingLabel;
    @FXML
    private Label energySavingsLabel;
    @FXML
    private Label co2SavingsLabel;
    @FXML
    private Label wasteCollectedLabel;
    @FXML
    private Label optimizationTipLabel;

    // UI elements for sidebar
    @FXML private Circle sidebarAvatar;
    @FXML private Label sidebarUsername;
    
    private DashboardController dashboardController;
    private TruckService truckService = new TruckService();
    private NominatimService nominatimService = new NominatimService();
    private BinService binService = new BinService();
    private NearbyBinService nearbyBinService = new NearbyBinService(binService);
    private EnergyEfficiencyService energyEfficiencyService = new EnergyEfficiencyService(truckService, binService);
    private Truck truckToEdit;

    private final ObservableList<String> statusOptions = FXCollections.observableArrayList(
            "Available",
            "On Mission",
            "In Maintenance",
            "Out of Service"
    );

    private final ObservableList<String> truckBrandOptions = FXCollections.observableArrayList(
            "Solaris",
            "Volvo",
            "Mercedes-Benz",
            "Scania",
            "MAN",
            "Renault"
    );

    private final ObservableList<String> binTypeOptions = FXCollections.observableArrayList(
            "Organic",
            "Plastic",
            "Paper",
            "Glass",
            "Metal",
            "Electronic",
            "Mixed"
    );

    @FXML
    public void initialize() {
        TS.setItems(statusOptions);
        
        // Initialize sidebar username
        if (sidebarUsername != null) {
            sidebarUsername.setText("Administrator");
        }

        // Initialize section options from TruckSection enum but hide the control
        ObservableList<String> sectionOptions = FXCollections.observableArrayList();
        for (TruckZone section : TruckZone.values()) {
            sectionOptions.add(section.getDisplayName());
        }
        
        // Add null checks to prevent NullPointerException
        if (TR != null) {
            TR.setItems(sectionOptions);
            TR.setValue(TruckZone.UNKNOWN.getDisplayName()); // Set default value
            TR.setVisible(false); // Hide the section combobox
        } else {
            System.err.println("WARNING: TR ComboBox is null in TruckFormController.initialize()");
        }

        // Setup truck brand options
        if (truckBrandComboBox != null) {
            truckBrandComboBox.setItems(truckBrandOptions);
            truckBrandComboBox.setValue("Solaris"); // Default to Solaris

            // When Solaris is selected, recommend renewable energy options
            truckBrandComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if ("Solaris".equals(newVal)) {
                    showRenewableEnergyRecommendation();
                }
            });
        }

        // Setup bin type options
        if (binTypeComboBox != null) {
            binTypeComboBox.setItems(binTypeOptions);
        }

        // Initialize solar bin table
        initializeSolarBinTable();
        
        // Initialize map
        initializeMap();
    }

    /**
     * Initialize the solar bin table with proper columns
     */
    private void initializeSolarBinTable() {
        if (solarBinsTable == null) {
            System.err.println("WARNING: solarBinsTable is null in initializeSolarBinTable()");
            return;
        }
        
        // The table columns are already defined in the FXML
        // Let's ensure they have the correct cell value factories
        solarBinsTable.getColumns().forEach(column -> {
            String columnTitle = column.getText();
            switch (columnTitle) {
                case "ID":
                    ((TableColumn<SolarBinStatus, String>) column).setCellValueFactory(
                            new PropertyValueFactory<>("id"));
                    break;
                case "Location":
                    ((TableColumn<SolarBinStatus, String>) column).setCellValueFactory(
                            new PropertyValueFactory<>("location"));
                    break;
                case "Fill Level":
                    ((TableColumn<SolarBinStatus, Double>) column).setCellValueFactory(
                            new PropertyValueFactory<>("fillLevel"));
                    break;
                case "Battery":
                    ((TableColumn<SolarBinStatus, Double>) column).setCellValueFactory(
                            new PropertyValueFactory<>("batteryLevel"));
                    break;
                case "Energy Status":
                    ((TableColumn<SolarBinStatus, String>) column).setCellValueFactory(
                            new PropertyValueFactory<>("energyStatus"));
                    break;
                case "Distance (km)":
                    ((TableColumn<SolarBinStatus, Double>) column).setCellValueFactory(
                            new PropertyValueFactory<>("distance"));
                    break;
                case "Priority":
                    ((TableColumn<SolarBinStatus, String>) column).setCellValueFactory(
                            new PropertyValueFactory<>("priorityStatus"));
                    break;
            }
        });
        
        // Add custom styling for priority column
        addPriorityStyling();
    }
    
    /**
     * Add styling to priority column to color-code the priorities
     */
    private void addPriorityStyling() {
        // Find the priority column
        solarBinsTable.getColumns().stream()
            .filter(col -> "Priority".equals(col.getText()))
            .findFirst()
            .ifPresent(column -> {
                @SuppressWarnings("unchecked")
                TableColumn<SolarBinStatus, String> priorityColumn = (TableColumn<SolarBinStatus, String>) column;
                
                // Set cell factory for custom styling
                priorityColumn.setCellFactory(col -> new TableCell<SolarBinStatus, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            switch (item) {
                                case "HIGH":
                                    setStyle("-fx-background-color: #ffcccc; -fx-text-fill: #cc0000; -fx-font-weight: bold;");
                                    break;
                                case "MEDIUM":
                                    setStyle("-fx-background-color: #fff4cc; -fx-text-fill: #cc7a00;");
                                    break;
                                case "LOW":
                                    setStyle("-fx-background-color: #ccffcc; -fx-text-fill: #00cc00;");
                                    break;
                                default:
                                    setStyle("");
                            }
                        }
                    }
                });
            });
    }

    private void initializeMap() {
        try {
            URL mapUrl = getClass().getResource("/views/map.html");
            if (mapUrl != null) {
                mapView.getEngine().load(mapUrl.toExternalForm());
                mapView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        System.out.println("Map loaded successfully");
                        mapStatusLabel.setText("Map ready");
                        
                        // Refresh the solar bin data when the map is loaded
                        refreshSolarBinData();
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
    
    /**
     * Handles the refresh button click for solar bins
     */
    @FXML
    private void handleRefreshSolarBins() {
        solarBinStatusLabel.setText("Refreshing solar bin data...");
        refreshSolarBinData();
    }
    
    /**
     * Handles the calculate efficiency button click
     */
    @FXML
    private void handleCalculateEfficiency() {
        efficiencyStatusLabel.setText("Calculating efficiency metrics...");
        calculateEnergyEfficiency();
    }
    
    /**
     * Calculates energy efficiency metrics based on current truck and selected solar bins
     */
    private void calculateEnergyEfficiency() {
        // Need current truck coordinates and data to calculate efficiency
        if (currentLocationField.getText().isEmpty()) {
            efficiencyStatusLabel.setText("Set truck location first");
            return;
        }
        
        // Get or create a truck object with current data
        Truck currentTruck = truckToEdit != null ? truckToEdit : new Truck();
        
        try {
            // Make sure the truck has the necessary properties set
            if (truckBrandComboBox != null && truckBrandComboBox.getValue() != null) {
                currentTruck.setBrand(truckBrandComboBox.getValue());
            }
            
            // Get the selected bins from the table if any
            List<Bin> selectedBins = new ArrayList<>();
            ObservableList<SolarBinStatus> tableBins = solarBinsTable.getItems();
            
            if (tableBins != null && !tableBins.isEmpty()) {
                // In a real implementation, we would convert the SolarBinStatus objects back to Bin objects
                // or fetch the actual Bin objects from the database using their IDs
                // For now, we'll create dummy Bin objects based on the SolarBinStatus data
                for (SolarBinStatus binStatus : tableBins) {
                    Bin bin = new Bin();
                    // Parse the String ID from SolarBinStatus to an integer for the Bin
                    try {
                        bin.setId(Integer.parseInt(binStatus.getId()));
                    } catch (NumberFormatException e) {
                        // If the ID can't be parsed, set a default value
                        System.err.println("Could not parse bin ID: " + binStatus.getId());
                        bin.setId(0); // Use 0 or another default value
                    }
                    bin.setLocation(binStatus.getLocation());
                    bin.setNiveauRemplissage(binStatus.getFillLevel());
                    bin.setSolarPowered(true);
                    bin.setBatteryLevel(binStatus.getBatteryLevel());
                    // Set some estimated coordinates based on the distance
                    // In reality, these would come from the actual bin data
                    bin.setLatitude(currentTruck.getCurrentLatitude() + (Math.random() * 0.01));
                    bin.setLongitude(currentTruck.getCurrentLongitude() + (Math.random() * 0.01));
                    selectedBins.add(bin);
                }
            }
            
            if (selectedBins.isEmpty()) {
                efficiencyStatusLabel.setText("No bins selected for efficiency calculation");
                return;
            }
            
            // Calculate metrics
            EnergyEfficiencyMetrics metrics = energyEfficiencyService.calculateMetricsForSolarBins(
                    currentTruck, selectedBins);
            
            // Update the UI with the calculated metrics
            updateEfficiencyDashboard(metrics);
            
            efficiencyStatusLabel.setText("Efficiency calculated successfully");
            
        } catch (Exception e) {
            System.err.println("Error calculating efficiency: " + e.getMessage());
            e.printStackTrace();
            efficiencyStatusLabel.setText("Error calculating efficiency");
        }
    }
    
    /**
     * Updates the efficiency dashboard UI with the calculated metrics
     */
    private void updateEfficiencyDashboard(EnergyEfficiencyMetrics metrics) {
        if (metrics == null) {
            efficiencyStatusLabel.setText("No metrics available");
            return;
        }
        
        // Format numbers to 2 decimal places
        DecimalFormat df = new DecimalFormat("#.##");
        
        // Get the fuel type to determine units
        boolean isElectric = truckToEdit != null && "Electric".equals(truckToEdit.getFuelType());
        String energyUnit = isElectric ? "kWh" : "L";
        
        // Update consumption
        energyConsumptionLabel.setText(df.format(metrics.getEnergyConsumption()) + " " + energyUnit);
        
        // Update emissions
        co2EmissionsLabel.setText(df.format(metrics.getCo2Emissions()) + " kg");
        
        // Update rating
        efficiencyRatingLabel.setText(metrics.getEfficiencyRating());
        styleEfficiencyRating(metrics.getEfficiencyRating());
        
        // Update savings
        energySavingsLabel.setText(df.format(metrics.getEnergySavings()) + " L");
        co2SavingsLabel.setText(df.format(metrics.getCo2Savings()) + " kg");
        
        // Update waste collected
        wasteCollectedLabel.setText(df.format(metrics.getTotalWasteCollected()) + " kg");
        
        // Update optimization tip
        optimizationTipLabel.setText(metrics.getOptimizationTip());
    }
    
    /**
     * Styles the efficiency rating label based on the rating value
     */
    private void styleEfficiencyRating(String rating) {
        String style = "-fx-font-weight: bold; -fx-font-size: 16px;";
        
        switch (rating) {
            case "A":
                efficiencyRatingLabel.setStyle(style + "-fx-text-fill: #008800;"); // Dark green
                break;
            case "B":
                efficiencyRatingLabel.setStyle(style + "-fx-text-fill: #00AA00;"); // Green
                break;
            case "C":
                efficiencyRatingLabel.setStyle(style + "-fx-text-fill: #AAAA00;"); // Yellow
                break;
            case "D":
                efficiencyRatingLabel.setStyle(style + "-fx-text-fill: #AA5500;"); // Orange
                break;
            case "E":
                efficiencyRatingLabel.setStyle(style + "-fx-text-fill: #AA0000;"); // Red
                break;
            default:
                efficiencyRatingLabel.setStyle(style + "-fx-text-fill: #000000;"); // Default black
        }
    }
    
    /**
     * Refreshes the solar bin data for the current truck
     */
    private void refreshSolarBinData() {
        // Need current truck coordinates to find nearby bins
        if (currentLocationField.getText().isEmpty()) {
            solarBinStatusLabel.setText("Set truck location first");
            return;
        }
        
        // Get or create a truck object with current coordinates
        Truck currentTruck = truckToEdit != null ? truckToEdit : new Truck();
        try {
            // Use coordinates from the hidden fields if available
            if (!destLat1Field.getText().isEmpty() && !destLng1Field.getText().isEmpty()) {
                double lat = Double.parseDouble(destLat1Field.getText().replace(",", "."));
                double lng = Double.parseDouble(destLng1Field.getText().replace(",", "."));
                currentTruck.setCurrentLatitude(lat);
                currentTruck.setCurrentLongitude(lng);
                
                // Find nearby solar bins (within 10km)
                List<SolarBinStatus> nearbySolarBins = nearbyBinService.findNearbySolarBins(currentTruck, 10.0);
                
                // Update the table with the results
                Platform.runLater(() -> {
                    if (nearbySolarBins.isEmpty()) {
                        solarBinStatusLabel.setText("No solar bins found nearby");
                        solarBinsTable.setItems(FXCollections.observableArrayList());
                    } else {
                        solarBinStatusLabel.setText(nearbySolarBins.size() + " solar bins found");
                        solarBinsTable.setItems(FXCollections.observableArrayList(nearbySolarBins));
                        
                        // Show critical bins in red
                        List<SolarBinStatus> criticalBins = nearbySolarBins.stream()
                            .filter(bin -> "HIGH".equals(bin.getPriorityStatus()))
                            .collect(java.util.stream.Collectors.toList());
                        
                        if (!criticalBins.isEmpty()) {
                            solarBinStatusLabel.setText(criticalBins.size() + " critical bins require attention!");
                        }
                        
                        // Add the bins to the map
                        displaySolarBinsOnMap(nearbySolarBins);
                    }
                });
            } else {
                solarBinStatusLabel.setText("Truck location coordinates not set");
            }
        } catch (Exception e) {
            System.err.println("Error refreshing solar bin data: " + e.getMessage());
            solarBinStatusLabel.setText("Error refreshing data");
        }
    }
    
    /**
     * Displays solar bins on the map
     */
    private void displaySolarBinsOnMap(List<SolarBinStatus> bins) {
        try {
            JSObject window = (JSObject) mapView.getEngine().executeScript("window");
            if (window == null) {
                System.err.println("Window object is null");
                return;
            }
            
            // Clear existing bin markers
            window.call("clearBinMarkers");
            
            // Add each bin to the map
            for (SolarBinStatus bin : bins) {
                try {
                    // Determine the marker color based on priority
                    String markerColor;
                    switch (bin.getPriorityStatus()) {
                        case "HIGH":
                            markerColor = "red";
                            break;
                        case "MEDIUM":
                            markerColor = "orange";
                            break;
                        default:
                            markerColor = "green";
                    }
                    
                    // Call the JavaScript function to add a bin marker
                    window.call("addBinMarker", 
                        // We need to get the actual bin coordinates from somewhere
                        // This is a placeholder - in real code, you'd get these from the bin
                        0.0, 0.0,  // latitude, longitude placeholder
                        "Bin #" + bin.getId(),
                        "Fill Level: " + bin.getFillLevel() + "%<br>" +
                        "Battery: " + bin.getBatteryLevel() + "%<br>" +
                        "Status: " + bin.getEnergyStatus(),
                        markerColor);
                } catch (Exception e) {
                    System.err.println("Error adding bin marker: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error displaying bins on map: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearchCurrentLocation() {
        searchLocation(currentLocationField.getText(), true);
    }

    @FXML
    private void handleSearchDestinationLocation() {
        searchLocation(destinationLocationField.getText(), false);
    }

    private void searchLocation(String address, boolean isCurrentLocation) {
        if (address == null || address.trim().isEmpty()) {
            showError("Error", "Please enter a location");
            return;
        }

        mapStatusLabel.setText("Searching location...");
        new Thread(() -> {
            try {
                double[] coords = nominatimService.getCoordinates(address);
                Platform.runLater(() -> {
                    try {
                        if (isCurrentLocation) {
                            destLat1Field.setText(String.valueOf(coords[0]).replace(",", "."));
                            destLng1Field.setText(String.valueOf(coords[1]).replace(",", "."));
                            mapStatusLabel.setText("Current location found");
                        } else {
                            destLat2Field.setText(String.valueOf(coords[0]).replace(",", "."));
                            destLng2Field.setText(String.valueOf(coords[1]).replace(",", "."));
                            mapStatusLabel.setText("Destination location found");
                        }

                        // Update map with the new location
                        JSObject window = (JSObject) mapView.getEngine().executeScript("window");
                        if (window != null) {
                            // Clear existing markers
                            window.call("clearMapElements");

                            // Add marker for current location if we have coordinates
                            if (!destLat1Field.getText().isEmpty() && !destLng1Field.getText().isEmpty()) {
                                double lat1 = parseCoordinate(destLat1Field.getText());
                                double lng1 = parseCoordinate(destLng1Field.getText());
                                window.call("addTruckMarker",
                                        lat1,
                                        lng1,
                                        currentLocationField.getText(),
                                        null
                                );
                            }

                            // Add marker for destination if we have coordinates
                            if (!destLat2Field.getText().isEmpty() && !destLng2Field.getText().isEmpty()) {
                                double lat2 = parseCoordinate(destLat2Field.getText());
                                double lng2 = parseCoordinate(destLng2Field.getText());
                                window.call("addTruckMarker",
                                        lat2,
                                        lng2,
                                        destinationLocationField.getText(),
                                        null
                                );

                                // If we have both locations, show the route
                                if (!destLat1Field.getText().isEmpty() && !destLng1Field.getText().isEmpty()) {
                                    double lat1 = parseCoordinate(destLat1Field.getText());
                                    double lng1 = parseCoordinate(destLng1Field.getText());
                                    window.call("showRoute",
                                            lat1, lng1,
                                            lat2, lng2,
                                            currentLocationField.getText(),
                                            destinationLocationField.getText()
                                    );
                                }
                            }

                            // Center map on the searched location
                            window.call("centerMap", coords[0], coords[1], 13);
                            window.call("fitAllMarkers");

                            mapStatusLabel.setText("Map updated");
                        }
                    } catch (Exception e) {
                        System.err.println("Error updating map: " + e.getMessage());
                        mapStatusLabel.setText("Error updating map");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    mapStatusLabel.setText("Location search failed");
                    showError("Location Error", "Could not find location: " + e.getMessage());
                });
            }
        }).start();
    }

    private double parseCoordinate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException("Empty coordinate value");
        }
        // Replace comma with period for decimal point
        return Double.parseDouble(value.replace(",", ".").trim());
    }

    private void updateMapWithLocations() {
        try {
            JSObject window = (JSObject) mapView.getEngine().executeScript("window");
            if (window == null) {
                System.err.println("Window object is null");
                return;
            }

            // Clear existing markers
            window.call("clearMapElements");

            // If we have both locations, show the route
            if (!destLat1Field.getText().isEmpty() && !destLng1Field.getText().isEmpty() &&
                    !destLat2Field.getText().isEmpty() && !destLng2Field.getText().isEmpty()) {

                double lat1 = parseCoordinate(destLat1Field.getText());
                double lng1 = parseCoordinate(destLng1Field.getText());
                double lat2 = parseCoordinate(destLat2Field.getText());
                double lng2 = parseCoordinate(destLng2Field.getText());

                window.call("updateMapWithRoute", lat1, lng1, lat2, lng2,
                        currentLocationField.getText() + " → " + destinationLocationField.getText());
                mapStatusLabel.setText("Route displayed");
            }
            // If we only have current location
            else if (!destLat1Field.getText().isEmpty() && !destLng1Field.getText().isEmpty()) {
                double lat1 = parseCoordinate(destLat1Field.getText());
                double lng1 = parseCoordinate(destLng1Field.getText());
                window.call("addTruckMarker", lat1, lng1, currentLocationField.getText(), false);
                window.call("centerMap", lat1, lng1, 16);
                mapStatusLabel.setText("Current location displayed");
            }
            // If we only have destination location
            else if (!destLat2Field.getText().isEmpty() && !destLng2Field.getText().isEmpty()) {
                double lat2 = parseCoordinate(destLat2Field.getText());
                double lng2 = parseCoordinate(destLng2Field.getText());
                window.call("addTruckMarker", lat2, lng2, destinationLocationField.getText(), true);
                window.call("centerMap", lat2, lng2, 16);
                mapStatusLabel.setText("Destination displayed");
            }
        } catch (NumberFormatException e) {
            // Silently handle parsing errors as coordinates might not be set yet
            System.out.println("Coordinates not yet available");
        } catch (Exception e) {
            System.err.println("Error updating map: " + e.getMessage());
            mapStatusLabel.setText("Error updating map");
        }
    }

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }


    /**
     * Sets the primary stage for this controller
     * @param stage The JavaFX stage
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        System.out.println("Primary stage set in TruckFormController: " + (stage != null));
    }

    public void setTruckData(Truck truck) {
        truckToEdit = truck;

        // Fill fields with truck data
        TC.setText(String.valueOf(truck.getCapaciteMax()));
        TST.setText(String.valueOf(truck.getNiveauRemplissageActuel()));
        
        // Set section ComboBox value if section is not null and TR exists
        if (TR != null) {
            if (truck.getSection() != null) {
                TR.setValue(truck.getSection().toString());
            } else {
                TR.setValue(TruckZone.UNKNOWN.toString());
            }
        } else {
            System.err.println("WARNING: TR ComboBox is null in setTruckData()");
        }
        
        TS.setValue(truck.getStatut());
        currentLocationField.setText(truck.getCurrentLocation());
        destLat1Field.setText(String.valueOf(truck.getDestinationLatitude1()));
        destLng1Field.setText(String.valueOf(truck.getDestinationLongitude1()));
        destLat2Field.setText(String.valueOf(truck.getDestinationLatitude2()));
        destLng2Field.setText(String.valueOf(truck.getDestinationLongitude2()));

        // Set truck brand if available
        if (truckBrandComboBox != null) {
            String brand = truck.getBrand();
            if (brand != null && !brand.isEmpty()) {
                if (truckBrandOptions.contains(brand)) {
                    truckBrandComboBox.setValue(brand);
                }
            } else {
                truckBrandComboBox.setValue(truckBrandOptions.get(0)); // Default to first option
            }
        }

        // Set bin type if available
        if (binTypeComboBox != null && truck.getSupportedWasteTypes() != null &&
                truck.getSupportedWasteTypes().length > 0) {
            String wasteType = truck.getSupportedWasteTypes()[0];
            if (binTypeOptions.contains(wasteType)) {
                binTypeComboBox.setValue(wasteType);
            } else {
                binTypeComboBox.setValue(binTypeOptions.get(0)); // Default to first option
            }
        }

        updateMapWithLocations();
    }

    @FXML
    private void handleAddButton() {
        try {
            if (!validateFields()) {
                return;
            }

            double capaciteMax = parseDoubleLocalized(TC.getText());
            double niveauRemplissage = parseDoubleLocalized(TST.getText());

            // Get coordinates from hidden fields
            double destLat1 = parseDoubleLocalized(destLat1Field.getText());
            double destLng1 = parseDoubleLocalized(destLng1Field.getText());
            double destLat2 = parseDoubleLocalized(destLat2Field.getText());
            double destLng2 = parseDoubleLocalized(destLng2Field.getText());

            if (!validateNumbers(capaciteMax, niveauRemplissage, destLat1, destLng1, destLat2, destLng2)) {
                return;
            }

            if (truckToEdit == null) {
                createNewTruck(capaciteMax, niveauRemplissage, destLat1, destLng1, destLat2, destLng2);
            } else {
                updateExistingTruck(capaciteMax, niveauRemplissage, destLat1, destLng1, destLat2, destLng2);
            }

            handleBackButton();

        } catch (NumberFormatException e) {
            showError("Invalid Format", "Please enter valid numbers");
        } catch (Exception e) {
            showError("Error", e.getMessage());
        }
    }

    private boolean validateFields() {
        if (TC.getText().trim().isEmpty() || TST.getText().trim().isEmpty() ||
                currentLocationField.getText().trim().isEmpty() ||
                destinationLocationField.getText().trim().isEmpty() || TS.getValue() == null) {
            showError("Missing Information", "Please fill in all required fields");
            return false;
        }
        return true;
    }

    private double parseDoubleLocalized(String value) throws NumberFormatException {
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException("Empty value");
        }
        return Double.parseDouble(value.replace(",", ".").trim());
    }

    private boolean validateNumbers(double capaciteMax, double remplissage,
                                    double lat1, double lng1, double lat2, double lng2) {
        if (capaciteMax <= 0) {
            showError("Invalid Input", "Maximum capacity must be greater than 0");
            return false;
        }
        if (remplissage < 0 || remplissage > capaciteMax) {
            showError("Invalid Input", "Fill level must be between 0 and maximum capacity");
            return false;
        }
        if (!isValidCoordinate(lat1, lng1) || !isValidCoordinate(lat2, lng2)) {
            showError("Invalid Coordinates", "Please enter valid coordinates or search for locations");
            return false;
        }
        return true;
    }

    private boolean isValidCoordinate(double lat, double lng) {
        return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
    }

    private void createNewTruck(double capaciteMax, double remplissage,
                                double destLat1, double destLng1, double destLat2, double destLng2) {
        Truck newTruck = new Truck();
        newTruck.setCapaciteMax(capaciteMax);
        newTruck.setNiveauRemplissageActuel(remplissage);
        
        // Use the string-based method for setting section with null check
        if (TR != null) {
            newTruck.setSection(TR.getValue()); // Still set section but it's hidden in UI
        } else {
            // If TR is null, use the default UNKNOWN zone
            newTruck.setSection(TruckZone.UNKNOWN.getDisplayName());
            System.err.println("WARNING: TR ComboBox is null in createNewTruck(), using default section");
        }
        
        newTruck.setStatut(TS.getValue());
        newTruck.setCurrentLocation(currentLocationField.getText());
        newTruck.setDestinationLatitude1(destLat1);
        newTruck.setDestinationLongitude1(destLng1);
        newTruck.setDestinationLatitude2(destLat2);
        newTruck.setDestinationLongitude2(destLng2);

        // Set the truck brand if available
        if (truckBrandComboBox != null && truckBrandComboBox.getValue() != null) {
            newTruck.setBrand(truckBrandComboBox.getValue());

            // If it's a Solaris truck, set electric as the fuel type
            if ("Solaris".equals(truckBrandComboBox.getValue())) {
                newTruck.setFuelType("Electric");
                newTruck.setBatteryLevel(100.0); // Full battery for new truck
                newTruck.setEnergyConsumption(15.0); // Example value: 15 kWh/100km
            }
        }

        // Set the supported waste types if available
        if (binTypeComboBox != null && binTypeComboBox.getValue() != null) {
            newTruck.setSupportedWasteTypes(new String[]{binTypeComboBox.getValue()});
        }

        truckService.add(newTruck);
        showInfo("Success", "Truck added successfully");
    }

    private void updateExistingTruck(double capaciteMax, double remplissage,
                                     double destLat1, double destLng1, double destLat2, double destLng2) {
        truckToEdit.setCapaciteMax(capaciteMax);
        truckToEdit.setNiveauRemplissageActuel(remplissage);
        
        // Use the string-based method for setting section with null check
        if (TR != null) {
            truckToEdit.setSection(TR.getValue());
        } else {
            // If TR is null, use the default UNKNOWN zone
            truckToEdit.setSection(TruckZone.UNKNOWN.getDisplayName());
            System.err.println("WARNING: TR ComboBox is null in updateExistingTruck(), using default section");
        }
        
        truckToEdit.setStatut(TS.getValue());
        truckToEdit.setCurrentLocation(currentLocationField.getText());
        truckToEdit.setDestinationLatitude1(destLat1);
        truckToEdit.setDestinationLongitude1(destLng1);
        truckToEdit.setDestinationLatitude2(destLat2);
        truckToEdit.setDestinationLongitude2(destLng2);

        // Set the truck brand if available
        if (truckBrandComboBox != null && truckBrandComboBox.getValue() != null) {
            truckToEdit.setBrand(truckBrandComboBox.getValue());

            // If it's a Solaris truck, set electric as the fuel type
            if ("Solaris".equals(truckBrandComboBox.getValue())) {
                truckToEdit.setFuelType("Electric");
                // Only set battery level if it's not already set
                if (truckToEdit.getBatteryLevel() <= 0) {
                    truckToEdit.setBatteryLevel(100.0);
                }
                truckToEdit.setEnergyConsumption(15.0); // Example value: 15 kWh/100km
            }
        }

        // Set the supported waste types if available
        if (binTypeComboBox != null && binTypeComboBox.getValue() != null) {
            truckToEdit.setSupportedWasteTypes(new String[]{binTypeComboBox.getValue()});
        }

        truckService.update(truckToEdit);
        showInfo("Success", "Truck updated successfully");
    }

    /**
     * Navigation method to go to dashboard
     */
    @FXML
    public void showDashboard() {
        navigateToDashboard();
    }
    
    /**
     * Navigation method to go to bin management
     */
    @FXML
    public void showBinsView() {
        navigateToBinManager();
    }
    
    /**
     * Navigation method to go to solar bin management
     */
    @FXML
    public void showSolarBinManagerView() {
        navigateToSolarBinManager();
    }
    
    /**
     * Navigation method to go to truck management (self-refresh)
     */
    @FXML
    public void showTrucksView() {
        // This is the current view, so just refresh
        refreshMap();
    }
    
    /**
     * Refresh the map display
     */
    @FXML
    public void refreshMap() {
        updateMapWithLocations();
    }

    /**
     * Display recommendations for renewable energy options when Solaris trucks are selected
     */
    private void showRenewableEnergyRecommendation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Renewable Energy Recommendation");
        alert.setHeaderText("Solaris Truck Renewable Energy Options");
        alert.setContentText("Solaris trucks work best with:\n- Solar-powered charging stations\n- Electric battery systems (100kWh capacity)\n- Optimized for collecting recyclable waste types\n- Zero emissions for urban environments\n- Compatible with smart bin technologies");
        alert.showAndWait();
    }

    @FXML
    private void handleBackButton() {
        // Navigate back to dashboard using base controller method
        navigateToDashboard();
    }
}

