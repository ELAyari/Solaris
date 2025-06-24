package com.esprit.controllers;

import com.esprit.models.Commande;
import com.esprit.models.User;
import com.esprit.models.CommandeProduit;
import com.esprit.Services.CommandeService;
import com.esprit.Services.CommandeProduitService;
import com.esprit.Services.ProduitService;
import com.esprit.utils.UIUtils;
import com.esprit.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;


import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class OrderPreviewController extends BaseController<Commande> {

    @FXML private Label idLabel;
    @FXML private Label dateLabel;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label clientLabel;

    @FXML private TableView<CommandeProduit> itemsTable;
    @FXML private TableColumn<CommandeProduit, String> productNameColumn;
    @FXML private TableColumn<CommandeProduit, Integer> quantityColumn;
    @FXML private TableColumn<CommandeProduit, Double> priceColumn;
    @FXML private TableColumn<CommandeProduit, Double> totalColumn;

    @FXML
    public void initialize() {
        itemsTable.setStyle("-fx-alignment: CENTER;");
    }

    private Commande order;
    private CommandeService commandeService;
    private CommandeProduitService commandeProduitService;
    private ProduitService produitService;
    private Map<Integer, User> userMap = new HashMap<>();
    
    // Reference to the parent content pane for fragment-based navigation
    private StackPane parentContentPane;
    
    /**
     * Constructor with explicit SQLException handling
     * @throws SQLException if database connection fails
     */
    public OrderPreviewController() throws SQLException {
        commandeService = new CommandeService();
        commandeProduitService = new CommandeProduitService();
        produitService = new ProduitService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize UserService from BaseController
        initializeUserService();
        
        // Try to get current user from UserSession
        this.currentUser = UserSession.getInstance().getCurrentUser();
        
        // If UserSession doesn't have a user, fall back to default
        if (currentUser == null) {
            System.out.println("OrderPreviewController: No user in UserSession, using fallback");
            try {
                // Try to get a default user from the service
                this.currentUser = userService.getById(1);
                if (currentUser != null) {
                    setCurrentUser(currentUser);
                    System.out.println("OrderPreviewController: Loaded user from database ID: 1");
                }
            } catch (Exception e) {
                System.err.println("OrderPreviewController: Error getting user from database: " + e.getMessage());
            }
        } else {
            System.out.println("OrderPreviewController: Using current user from UserSession: " + currentUser.getNom() + " " + currentUser.getPrenom());
        }
        
        statusCombo.getItems().setAll("Pending", "Processing", "Completed", "Cancelled");

        // Get users from the service
        List<User> users = userService.get();
        if (users != null) {
            userMap.putAll(users.stream()
                .collect(Collectors.toMap(User::getId, u -> u)));
        }

        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));

        // NO properties or beans — populate manually later

        itemsTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double tableWidth = newWidth.doubleValue();
            productNameColumn.setPrefWidth(tableWidth * 0.4);
            quantityColumn.setPrefWidth(tableWidth * 0.15);
            priceColumn.setPrefWidth(tableWidth * 0.15);
            totalColumn.setPrefWidth(tableWidth * 0.3);
        });
    }
    
    /**
     * Sets the current user and updates the UI accordingly.
     * This method should be called after controller initialization.
     * 
     * @param user The current user
     */
    @Override
    public void setCurrentUser(User user) {
        super.setCurrentUser(user);
        
        // Update UserSession
        UserSession.getInstance().setUser(user);
    }

    public void setOrder(Commande order) {
        this.order = order;
        displayOrderDetails();
        loadOrderItems();
    }

    private void displayOrderDetails() {
        idLabel.setText(String.valueOf(order.getId()));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = (order.getDate() != null) ? sdf.format(order.getDate()) : "N/A";
        dateLabel.setText(formattedDate);

        String orderStatus = order.getStatut();
        if (orderStatus != null && statusCombo.getItems().contains(orderStatus)) {
            statusCombo.setValue(orderStatus);
        } else {
            statusCombo.setValue("Pending");
        }

        User client = userMap.get(order.getClientId());
        if (client != null) {
            clientLabel.setText(client.getPrenom() + " " + client.getNom());
        } else {
            clientLabel.setText("Unknown");
        }
    }

    private void loadOrderItems() {
        if (order == null) {
            return;
        }

        List<CommandeProduit> items = commandeProduitService.getOrderItemsByCommandeId(order.getId());
        ObservableList<CommandeProduit> observableItems = FXCollections.observableArrayList(items);
        itemsTable.setItems(observableItems);

        // Set custom cell value factories manually (only once after data is loaded)
        productNameColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    CommandeProduit cp = (CommandeProduit) getTableRow().getItem();
                    var produit = produitService.getProduitById(cp.getProduitId());
                    setText(produit != null ? produit.getNom() : "Unknown Product");
                }
            }
        });

        priceColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    CommandeProduit cp = (CommandeProduit) getTableRow().getItem();
                    var produit = produitService.getProduitById(cp.getProduitId());
                    setText(produit != null ? String.valueOf(produit.getPrix()) : "0.0");
                }
            }
        });
    }

    @FXML
    private void handleSave() {
        boolean confirmed = UIUtils.showConfirmation("Confirm Save", "Are you sure you want to save changes?");
        if (!confirmed) {
            return;
        }
        if (order != null) {
            String newStatus = statusCombo.getValue();
            if (newStatus != null && !newStatus.equals(order.getStatut())) {
                order.setStatut(newStatus);
                commandeService.modifier(order);
            }
        }
        backToDashboard();
    }
    
    /**
     * Sets the parent content pane for fragment-based navigation
     * @param contentPane The StackPane that will contain this view
     */
    public void setParentContentPane(StackPane contentPane) {
        this.parentContentPane = contentPane;
    }
    
    @FXML
    private void handleClose() {
        backToDashboard();
    }

    public void backToDashboard() {
        try {
            if (parentContentPane != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/OrderDashboardView.fxml"));
                Parent dashboard = loader.load();

                OrderDashboardController controller = loader.getController();
                controller.setParentContainer(parentContentPane);
                controller.loadOrderData();

                parentContentPane.getChildren().setAll(dashboard);
            } 
        } catch (Exception e) {
            UIUtils.showAlert("Navigation Error", null, "Failed to return to dashboard: " + e.getMessage(), "ERROR");
        }
    }




}
