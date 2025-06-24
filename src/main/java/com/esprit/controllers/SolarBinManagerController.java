package com.esprit.controllers;

import com.esprit.models.Bin;
import com.esprit.Services.BinService;
import com.esprit.Services.NominatimService;
import com.esprit.Services.SolarBinService;
import com.esprit.tests.MainprogGUI;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.DialogPane;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Controller for the Solar Bin Manager view
 */
public class SolarBinManagerController implements Initializable {

    @FXML
    private TableView<Bin> solarBinsTable;
    @FXML
    private TableColumn<Bin, Integer> idColumn;
    @FXML
    private TableColumn<Bin, String> locationColumn;
    @FXML
    private TableColumn<Bin, String> typeColumn;
    @FXML
    private TableColumn<Bin, Double> fillLevelColumn;
    @FXML
    private TableColumn<Bin, Double> batteryLevelColumn;
    @FXML
    private TableColumn<Bin, String> energyStatusColumn;
    @FXML
    private TableColumn<Bin, LocalDate> maintenanceDateColumn;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> energyStatusFilter;
    @FXML
    private Slider batteryLevelSlider;
    @FXML
    private Label batteryLevelLabel;
    @FXML
    private Button updateBatteryBtn;
    @FXML
    private Button scheduleMaintenanceBtn;
    @FXML
    private DatePicker maintenanceDatePicker;
    @FXML
    private TextArea maintenanceNotesArea;

    private BinService binService;
    private SolarBinService solarBinService;
    private ObservableList<Bin> solarBins = FXCollections.observableArrayList();

    private Stage primaryStage;
    private MainprogGUI mainApp;

    /**
     * Set the main application reference
     * @param mainApp The main application instance
     */
    public void setMainApp(MainprogGUI mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Set the primary stage reference for navigation
     * @param stage The JavaFX stage
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        binService = new BinService();
        solarBinService = new SolarBinService(binService);

        // Load solar bins on initialization
        loadSolarBins();

        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeDechet"));
        fillLevelColumn.setCellValueFactory(new PropertyValueFactory<>("niveauRemplissage"));
        batteryLevelColumn.setCellValueFactory(new PropertyValueFactory<>("batteryLevel"));
        batteryLevelColumn.setCellFactory(column -> new TableCell<Bin, Double>() {
            @Override
            protected void updateItem(Double batteryLevel, boolean empty) {
                super.updateItem(batteryLevel, empty);
                if (empty || batteryLevel == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(String.format("%.1f%%", batteryLevel));
                    if (batteryLevel < 20) {
                        setTextFill(Color.RED);
                    } else if (batteryLevel < 50) {
                        setTextFill(Color.ORANGE);
                    } else {
                        setTextFill(Color.GREEN);
                    }
                }
            }
        });
        energyStatusColumn.setCellValueFactory(new PropertyValueFactory<>("energyStatus"));
        maintenanceDateColumn.setCellValueFactory(new PropertyValueFactory<>("lastMaintenanceDate"));

        // Initialize energy status filter
        energyStatusFilter.setItems(FXCollections.observableArrayList(
                "All", "Fully Charged", "Medium", "Low"
        ));
        energyStatusFilter.setValue("All");
        energyStatusFilter.setOnAction(e -> filterSolarBins());

        // Initialize battery level slider
        batteryLevelSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            batteryLevelLabel.setText(String.format("%.1f%%", newVal.doubleValue()));
        });

        // Initialize update button
        updateBatteryBtn.setOnAction(e -> updateBatteryLevel());

        // Initialize schedule maintenance button
        scheduleMaintenanceBtn.setOnAction(e -> scheduleMaintenance());

        // Initialize maintenance date picker
        maintenanceDatePicker.setValue(LocalDate.now().plusDays(7));

        // Load data
        loadSolarBins();

        // Set up search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterSolarBins());

        // Set up table selection listener
        solarBinsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        batteryLevelSlider.setValue(newSelection.getBatteryLevel());
                        if (newSelection.getMaintenanceNotes() != null) {
                            maintenanceNotesArea.setText(newSelection.getMaintenanceNotes());
                        } else {
                            maintenanceNotesArea.setText("");
                        }
                    }
                });
    }

    /**
     * Load all solar-powered bins from the database
     */
    private void loadSolarBins() {
        List<Bin> bins = binService.getSolarPoweredBins();
        solarBins.clear();
        solarBins.addAll(bins);
        solarBinsTable.setItems(solarBins);
    }

    /**
     * Filter solar bins based on search text and energy status
     */
    private void filterSolarBins() {
        String searchText = searchField.getText().toLowerCase();
        String energyStatus = energyStatusFilter.getValue();

        ObservableList<Bin> filteredList = FXCollections.observableArrayList();

        for (Bin bin : binService.getSolarPoweredBins()) {
            boolean matchesSearch = searchText.isEmpty() ||
                    bin.getLocation().toLowerCase().contains(searchText) ||
                    bin.getTypeDechet().toLowerCase().contains(searchText);

            boolean matchesStatus = "All".equals(energyStatus) ||
                    bin.getEnergyStatus().equals(energyStatus);

            if (matchesSearch && matchesStatus) {
                filteredList.add(bin);
            }
        }

        solarBinsTable.setItems(filteredList);
    }

    /**
     * Update the battery level of the selected bin
     */
    private void updateBatteryLevel() {
        Bin selectedBin = solarBinsTable.getSelectionModel().getSelectedItem();
        if (selectedBin == null) {
            showAlert(Alert.AlertType.WARNING, "No Bin Selected",
                    "Please select a bin to update.");
            return;
        }

        double newBatteryLevel = batteryLevelSlider.getValue();
        boolean success = solarBinService.updateBinBatteryLevel(
                selectedBin.getId(), newBatteryLevel);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Update Successful",
                    "Battery level updated to " + String.format("%.1f%%", newBatteryLevel));
            loadSolarBins();
        } else {
            showAlert(Alert.AlertType.ERROR, "Update Failed",
                    "Failed to update battery level.");
        }
    }

    /**
     * Schedule maintenance for the selected bin
     */
    private void scheduleMaintenance() {
        Bin selectedBin = solarBinsTable.getSelectionModel().getSelectedItem();
        if (selectedBin == null) {
            showAlert(Alert.AlertType.WARNING, "No Bin Selected",
                    "Please select a bin to schedule maintenance.");
            return;
        }

        LocalDate maintenanceDate = maintenanceDatePicker.getValue();
        if (maintenanceDate == null) {
            showAlert(Alert.AlertType.WARNING, "No Date Selected",
                    "Please select a maintenance date.");
            return;
        }

        String notes = maintenanceNotesArea.getText();
        if (notes.isEmpty()) {
            notes = "Routine maintenance scheduled.";
        }

        // Update bin with maintenance information
        selectedBin.setNextScheduledMaintenance(maintenanceDate);
        selectedBin.setMaintenanceNotes(notes);
        binService.update(selectedBin);

        showAlert(Alert.AlertType.INFORMATION, "Maintenance Scheduled",
                "Maintenance scheduled for " + maintenanceDate);
        loadSolarBins();
    }

    /**
     * Simulate solar charging for all bins
     */
    @FXML
    private void simulateCharging() {
        int count = solarBinService.simulateSolarCharging();
        showAlert(Alert.AlertType.INFORMATION, "Simulation Complete",
                count + " bins have been charged.");
        loadSolarBins();
    }

    /**
     * Simulate battery usage for all bins
     */
    @FXML
    private void simulateUsage() {
        int count = solarBinService.simulateBatteryDrain();
        showAlert(Alert.AlertType.INFORMATION, "Simulation Complete",
                count + " bins have been updated with battery usage.");
        loadSolarBins();
    }

    /**
     * Create a new solar bin with user input
     */
    @FXML
    private void createNewSolarBin() {
        // Create dialog for solar bin creation
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Solar Bin");
        dialog.setHeaderText("Enter details for the new solar bin");

        // Set the button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, cancelButtonType);

        // Create the layout and add to dialog
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Services for geocoding
        NominatimService nominatimService = new NominatimService();

        TextField locationField = new TextField();
        locationField.setPromptText("Location description");
        TextField latitudeField = new TextField("36.8065"); // Default Tunis coordinates
        TextField longitudeField = new TextField("10.1815");

        // Add a focus listener to the location field to update coordinates when user exits the field
        locationField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !locationField.getText().isEmpty()) { // When focus is lost and field is not empty
                String location = locationField.getText();
                try {
                    // Get coordinates from location using NominatimService
                    double[] coordinates = nominatimService.getCoordinates(location);
                    if (coordinates != null && coordinates.length == 2) {
                        latitudeField.setText(String.valueOf(coordinates[0]));
                        longitudeField.setText(String.valueOf(coordinates[1]));
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Geocoding Issue",
                                "Could not find coordinates for the specified location.");
                    }
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Geocoding Error",
                            "Error finding coordinates: " + ex.getMessage());
                }
            }
        });

        // Add a button to explicitly trigger geocoding
        Button geocodeButton = new Button("Get Coordinates");
        geocodeButton.setOnAction(e -> {
            String location = locationField.getText();
            if (!location.isEmpty()) {
                try {
                    double[] coordinates = nominatimService.getCoordinates(location);
                    if (coordinates != null && coordinates.length == 2) {
                        latitudeField.setText(String.valueOf(coordinates[0]));
                        longitudeField.setText(String.valueOf(coordinates[1]));
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Geocoding Issue",
                                "Could not find coordinates for the specified location.");
                    }
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Geocoding Error",
                            "Error finding coordinates: " + ex.getMessage());
                }
            }
        });
        ComboBox<String> wasteTypeCombo = new ComboBox<>();
        wasteTypeCombo.getItems().addAll("Mixed", "Plastic", "Paper", "Glass", "Metal", "Organic");
        wasteTypeCombo.setValue("Mixed");
        Slider batterySlider = new Slider(0, 100, 100);
        batterySlider.setShowTickLabels(true);
        batterySlider.setShowTickMarks(true);
        TextField panelOutputField = new TextField("50.0");
        panelOutputField.setPromptText("Solar panel output in watts");

        grid.add(new Label("Location:"), 0, 0);
        grid.add(locationField, 1, 0);
        grid.add(geocodeButton, 2, 0);  // Add the geocode button next to location field
        grid.add(new Label("Latitude:"), 0, 1);
        grid.add(latitudeField, 1, 1);
        grid.add(new Label("Longitude:"), 0, 2);
        grid.add(longitudeField, 1, 2);
        grid.add(new Label("Waste Type:"), 0, 3);
        grid.add(wasteTypeCombo, 1, 3);
        grid.add(new Label("Battery Level (%):"), 0, 4);
        grid.add(batterySlider, 1, 4);
        grid.add(new Label("Panel Output (W):"), 0, 5);
        grid.add(panelOutputField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Apply styling
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        // Request focus on the location field
        Platform.runLater(locationField::requestFocus);

        // Convert the result to a bin when the create button is clicked
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == createButtonType) {
            try {
                double latitude = Double.parseDouble(latitudeField.getText());
                double longitude = Double.parseDouble(longitudeField.getText());
                String location = locationField.getText();
                if (location.isEmpty()) location = "New Solar Bin";
                String wasteType = wasteTypeCombo.getValue();
                double batteryLevel = batterySlider.getValue();
                double panelOutput = Double.parseDouble(panelOutputField.getText());

                Bin newBin = solarBinService.createSolarBin(
                        latitude, longitude, location, wasteType, batteryLevel, panelOutput);

                if (newBin != null) {
                    showAlert(Alert.AlertType.INFORMATION, "Bin Created",
                            "New solar bin created with ID: " + newBin.getId());
                    loadSolarBins();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Creation Failed",
                            "Failed to create new solar bin.");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input",
                        "Please enter valid numeric values for coordinates and panel output.");
            }
        }
    }

    /**
     * Refresh the solar bin list
     */
    @FXML
    private void refreshBins() {
        loadSolarBins();
    }

    /**
     * Show an alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Apply consistent styling
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        alert.showAndWait();
    }

    /**
     * Return to the main dashboard using mainApp
     */
    @FXML
    private void returnToDashboard() {
        if (mainApp != null) {
            mainApp.showMainDashboardScene();
        } else {
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Main application reference not set. Cannot navigate to dashboard.");
        }
    }

    /**
     * Method to make the sidebar Dashboard button work
     */
    @FXML
    public void showDashboard() {
        if (mainApp != null) {
            mainApp.showMainDashboardScene();
        } else {
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Main application reference not set. Cannot navigate to dashboard.");
        }
    }

    /**
     * Navigate to bins view using mainApp
     */
    @FXML
    public void showBinsView() {
        if (mainApp != null) {
            // You can add a method to show bin form in MainprogGUI if needed
            // For now, we'll use the existing navigation pattern
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Bin-form.fxml"));
                Parent root = loader.load();

                BinFormController controller = loader.getController();
                controller.setPrimaryStage(primaryStage);

                Scene scene = new Scene(root);
                MainprogGUI.applyStylesheets(scene);

                if (primaryStage != null) {
                    primaryStage.setScene(scene);
                    primaryStage.setTitle("Bin Management");
                }
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Navigation Error",
                        "Failed to navigate to bin management: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Main application reference not set. Cannot navigate to bin management.");
        }
    }

    /**
     * Navigate to trucks view using mainApp
     */
    @FXML
    public void showTrucksView() {
        if (mainApp != null) {
            // You can add a method to show truck form in MainprogGUI if needed
            // For now, we'll use the existing navigation pattern
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Truck-form.fxml"));
                Parent root = loader.load();

                TruckFormController controller = loader.getController();
                controller.setPrimaryStage(primaryStage);

                Scene scene = new Scene(root);
                MainprogGUI.applyStylesheets(scene);

                if (primaryStage != null) {
                    primaryStage.setScene(scene);
                    primaryStage.setTitle("Truck Management");
                }
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Navigation Error",
                        "Failed to navigate to truck management: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Main application reference not set. Cannot navigate to truck management.");
        }
    }
}