package com.esprit.controllers;

import com.esprit.models.SolarisStation;
import com.esprit.Services.SolarisRouteOptimizationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.Date;
import java.util.List;

public class SolarisRouteController {
    @FXML private TextField startLatField;
    @FXML private TextField startLonField;
    @FXML private TextField endLatField;
    @FXML private TextField endLonField;
    @FXML private WebView mapView;
    @FXML private VBox stationsList;
    @FXML private Label co2SavingsLabel;
    @FXML private Label energyOutputLabel;
    @FXML private ProgressBar chargingProgress;
    @FXML private TableView<SolarisStation> stationsTable;

    private final SolarisRouteOptimizationService routeService;
    private final ObservableList<SolarisStation> stations;

    public SolarisRouteController() {
        this.routeService = new SolarisRouteOptimizationService();
        this.stations = FXCollections.observableArrayList();
        
        // Add some sample stations
        addSampleStations();
    }

    @FXML
    public void initialize() {
        setupStationsTable();
        setupMapView();
    }

    @FXML
    private void optimizeRoute() {
        try {
            double startLat = Double.parseDouble(startLatField.getText());
            double startLon = Double.parseDouble(startLonField.getText());
            double endLat = Double.parseDouble(endLatField.getText());
            double endLon = Double.parseDouble(endLonField.getText());

            List<SolarisStation> routeStations = routeService.findStationsAlongRoute(
                startLat, startLon, endLat, endLon);

            // Update stations table
            stations.setAll(routeStations);

            // Calculate and display CO2 savings
            double co2Savings = routeService.calculateCO2Savings(routeStations);
            co2SavingsLabel.setText(String.format("CO₂ Savings: %.2f kg", co2Savings));

            // Update map with route and stations
            updateMapWithRoute(startLat, startLon, endLat, endLon, routeStations);

            // Update energy output
            updateEnergyOutput(routeStations);

        } catch (NumberFormatException e) {
            showError("Please enter valid coordinates");
        }
    }

    private void setupStationsTable() {
        stationsTable.setItems(stations);
        
        TableColumn<SolarisStation, String> idColumn = new TableColumn<>("Station ID");
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        
        TableColumn<SolarisStation, Double> energyColumn = new TableColumn<>("Energy Output (kW)");
        energyColumn.setCellValueFactory(cellData -> 
            cellData.getValue().currentEnergyOutputProperty().asObject());
        
        TableColumn<SolarisStation, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        
        stationsTable.getColumns().addAll(idColumn, energyColumn, statusColumn);
    }

    private void setupMapView() {
        // Initialize map view with OpenStreetMap or Google Maps
        // This is a placeholder - you'll need to implement the actual map integration
        mapView.getEngine().load("https://www.openstreetmap.org/");
    }

    private void updateMapWithRoute(double startLat, double startLon, 
                                  double endLat, double endLon,
                                  List<SolarisStation> stations) {
        // Update map with route and station markers
        // This is a placeholder - you'll need to implement the actual map update logic
        String script = String.format(
            "var start = L.marker([%f, %f]).addTo(map);" +
            "var end = L.marker([%f, %f]).addTo(map);" +
            "var route = L.polyline([[%f, %f], [%f, %f]]).addTo(map);",
            startLat, startLon, endLat, endLon, startLat, startLon, endLat, endLon
        );
        mapView.getEngine().executeScript(script);
    }

    private void updateEnergyOutput(List<SolarisStation> stations) {
        double totalOutput = stations.stream()
            .mapToDouble(SolarisStation::getCurrentEnergyOutput)
            .sum();
        energyOutputLabel.setText(String.format("Total Energy Output: %.2f kW", totalOutput));
        
        // Update charging progress
        chargingProgress.setProgress(totalOutput / (100.0 * stations.size()));
    }

    private void addSampleStations() {
        // Add some sample stations for testing
        routeService.addStation(new SolarisStation("SOL-001", 48.8566, 2.3522, 5.0)); // Paris
        routeService.addStation(new SolarisStation("SOL-002", 47.2184, -1.5536, 5.0)); // Nantes
        routeService.addStation(new SolarisStation("SOL-003", 45.7640, 4.8357, 5.0)); // Lyon
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 