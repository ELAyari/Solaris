package com.esprit.controllers;

import com.esprit.models.Commande;
import com.esprit.Services.CommandeService;
import com.esprit.Services.UserService;
import com.esprit.tests.MainprogGUI;
import com.esprit.utils.DataSource;
import com.esprit.utils.UIUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.StackPane;
import java.time.LocalDate;
import java.time.ZoneId;

import javafx.stage.Stage;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class OrderDashboardController extends BaseController<Commande> {

    @FXML private TableView<Commande> orderTable;
    @FXML private TableColumn<Commande, Integer> idColumn;
    @FXML private TableColumn<Commande, String> dateColumn;
    @FXML private TableColumn<Commande, String> statusColumn;
    @FXML private TableColumn<Commande, String> clientColumn;
    @FXML private TableColumn<Commande, Date> estimatedDeliveryColumn;
    @FXML private Button previewButton;
    @FXML private ComboBox<String> clientCombo;
    @FXML private ComboBox<String> statusCombo;

    private StackPane parentContainer;

    public void setParentContainer(StackPane parentContainer) {
        this.parentContainer = parentContainer;
    }

    @FXML private StackPane nestedContentPane;

    private final CommandeService commandeService = new CommandeService();
    private final UserService userService;
    
    public OrderDashboardController() {
        try {
            this.userService = new UserService();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize UserService", e);
        }
    }
    private Connection connection;

    private Map<String, Integer> clientMap = new HashMap<>();
    private List<Commande> allOrders = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.connection = DataSource.getInstance().getConnection();
            if (this.connection != null && !this.connection.isClosed()) {
                setupTableColumns();
                orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                loadOrderData();
                setupSelectionListener();
                loadClients();
                loadStatuses();
                setupFilters();
            } else {
                UIUtils.showAlert("Database Error", null, "Failed to establish database connection", "ERROR");
            }
        } catch (SQLException e) {
            UIUtils.showAlert("Database Error", null, "Failed to connect to database: " + e.getMessage(), "ERROR");
        }
    }



    private void setupTableColumns() {
    orderTable.getColumns().remove(idColumn);

    // Client column - minimal property usage
    clientColumn.setCellValueFactory(cellData -> {
        int clientId = cellData.getValue().getClientId();
        String clientName = clientMap.entrySet().stream()
                .filter(entry -> entry.getValue() == clientId)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("Unknown");
        return new SimpleStringProperty(clientName);
    });

    // Date column - minimal property usage
    dateColumn.setCellValueFactory(cellData -> {
        Date date = cellData.getValue().getDate();
        String formattedDate = (date != null)
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date)
                : "";
        return new SimpleStringProperty(formattedDate);
    });

    // Estimated Delivery Date column
    estimatedDeliveryColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getEstimatedDeliveryDate()));

    // Custom cell factory for estimatedDeliveryColumn to use DatePicker
    estimatedDeliveryColumn.setCellFactory(column -> new TableCell<Commande, Date>() {
        private final DatePicker datePicker = new DatePicker();

        {
            datePicker.setEditable(false);
            datePicker.setOnAction(event -> {
                LocalDate localDate = datePicker.getValue();
                if (localDate != null) {
                    Date newDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    Commande order = getTableView().getItems().get(getIndex());

                    if (UIUtils.showConfirmation("Confirm Date Change", "Change estimated delivery date to: " + localDate.toString())) {
                        order.setEstimatedDeliveryDate(newDate);
                        commandeService.modifier(order);
                        loadOrderData(); // Refresh data
                    } else {
                        cancelEdit();
                    }
                }
            });
        }

        @Override
        protected void updateItem(Date item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (item != null) {
                        LocalDate localDate = item.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        datePicker.setValue(localDate);
                    }
                    setGraphic(datePicker);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                } else {
                    setText(item != null ? new SimpleDateFormat("yyyy-MM-dd").format(item) : "");
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                }
            }
        }

        @Override
        public void startEdit() {
            super.startEdit();
            Date item = getItem();
            if (item != null) {
                LocalDate localDate = item.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                datePicker.setValue(localDate);
            }
            setGraphic(datePicker);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            datePicker.requestFocus();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem() != null ? new SimpleDateFormat("yyyy-MM-dd").format(getItem()) : "");
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }
    });

    // Status column (requires property for editing)
    statusColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getStatut()));

    statusColumn.setCellFactory(column -> new ComboBoxTableCell<>("Pending", "Processing", "Completed", "Cancelled"));

    statusColumn.setOnEditCommit(event -> {
        Commande order = event.getRowValue();
        String newStatus = event.getNewValue();

        if (UIUtils.showConfirmation("Confirm Status Change", "Change status to: " + newStatus)) {
            order.setStatut(newStatus);
            commandeService.modifier(order);
            loadOrderData(); // Refresh data
        } else {
            orderTable.refresh(); // Revert visual change
        }
    });

    orderTable.setEditable(true);
}


    void loadOrderData() {
        allOrders = commandeService.recuperer();
        orderTable.setItems(FXCollections.observableArrayList(allOrders));
    }

    // ... (rest of the methods remain unchanged)
    private void setupSelectionListener() {
        orderTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    previewButton.setDisable(newSelection == null);
                });
    }

    private void loadClients() {
        clientMap.clear();
        clientCombo.getItems().clear();
        clientCombo.getItems().add("All Clients");

        userService.get().forEach(client -> {
            String fullName = client.getPrenom() + " " + client.getNom();
            clientMap.put(fullName, client.getId());
            clientCombo.getItems().add(fullName);
        });
        clientCombo.getSelectionModel().selectFirst();
    }

    private void loadStatuses() {
        statusCombo.getItems().clear();
        statusCombo.getItems().addAll("All Statuses", "Pending", "Processing", "Completed", "Cancelled");
        statusCombo.getSelectionModel().selectFirst();
    }

    private void applyFilters() {
        String selectedClient = clientCombo.getValue();
        String selectedStatus = statusCombo.getValue();

        List<Commande> filtered = allOrders.stream()
                .filter(order -> 
                    (selectedClient == null || selectedClient.equals("All Clients") || 
                     clientMap.getOrDefault(selectedClient, -1) == order.getClientId()) &&
                    (selectedStatus == null || selectedStatus.equals("All Statuses") || 
                     selectedStatus.equalsIgnoreCase(order.getStatut()))
                )
                .collect(Collectors.toList());

        orderTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void setupFilters() {
        clientCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

   

    @FXML
    private void handlePreview() {
        Commande selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showAlert("Warning", null, "Please select an order to preview.", "WARNING");
            return;
        }

        try {
            if (parentContainer != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/OrderPreviewView.fxml"));
                Parent preview = loader.load();

                OrderPreviewController controller = loader.getController();
                controller.setOrder(selected);
                controller.setParentContentPane(parentContainer);

                parentContainer.getChildren().setAll(preview);
                loadOrderData(); // Refresh after preview
            } else {
                MainprogGUI.getInstance().loadContentFragment("/OrderPreviewView.fxml", (OrderPreviewController controller) -> {
                    controller.setOrder(selected);
                });
                loadOrderData(); // Refresh after preview
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log stack trace for debugging
            UIUtils.showAlert("Error", null, "Failed to open preview: " + e.getMessage(), "ERROR");
        }
    }

    public void backToDashboard() {
        try {
            if (parentContainer != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/OrderDashboardView.fxml"));
                Parent dashboard = loader.load();

                OrderDashboardController controller = loader.getController();
                controller.setParentContainer(parentContainer);
                controller.loadOrderData();

                parentContainer.getChildren().setAll(dashboard);
            } else {
                MainprogGUI app = MainprogGUI.getInstance();
                app.loadContentFragment("/OrderDashboardView.fxml", (OrderDashboardController controller) -> {
                    controller.loadOrderData();
                });
            }
        } catch (Exception e) {
            UIUtils.showAlert("Navigation Error", null, "Failed to return to dashboard: " + e.getMessage(), "ERROR");
        }
    }

    // Custom TableCell implementation for DatePicker editing
    private class DatePickerTableCell extends TableCell<Commande, Date> {
        private DatePicker datePicker;

        public DatePickerTableCell() {
            datePicker = new DatePicker();
            datePicker.setEditable(false);

            datePicker.setOnAction(event -> {
                LocalDate localDate = datePicker.getValue();
                if (localDate != null) {
                    Date newDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    Commande order = getTableView().getItems().get(getIndex());

                    if (UIUtils.showConfirmation("Confirm Date Change", "Change estimated delivery date to: " + localDate.toString())) {
                        order.setEstimatedDeliveryDate(newDate);
                        commandeService.modifier(order);
                        loadOrderData(); // Refresh data
                    } else {
                        cancelEdit();
                    }
                }
            });

            this.setGraphic(datePicker);
            this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        public void startEdit() {
            super.startEdit();
            if (getItem() != null) {
                LocalDate localDate = getItem().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                datePicker.setValue(localDate);
            }
            datePicker.requestFocus();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem() != null ? new SimpleDateFormat("yyyy-MM-dd").format(getItem()) : "");
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }

        @Override
        protected void updateItem(Date item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (datePicker != null) {
                        LocalDate localDate = item.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        datePicker.setValue(localDate);
                    }
                    setGraphic(datePicker);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                } else {
                    setText(item != null ? new SimpleDateFormat("yyyy-MM-dd").format(item) : "");
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                }
            }
        }
    }
}
