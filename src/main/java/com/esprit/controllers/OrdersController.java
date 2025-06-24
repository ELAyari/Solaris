package com.esprit.controllers;

import com.esprit.Services.CommandeService;
import com.esprit.Services.CommandeProduitService;
import com.esprit.Services.ProduitService;
import com.esprit.Services.UserService;
import com.esprit.models.Commande;
import com.esprit.models.CommandeProduit;
import com.esprit.models.Produit;
import com.esprit.models.User;
// UserRole and UserStatus are used indirectly through the User model

import com.esprit.utils.UIUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class OrdersController extends BaseController<Commande> {

    @FXML
    private ListView<Commande> ordersListView;

    @FXML
    private Button viewDetailsButton;

    private final CommandeService commandeService = new CommandeService();
    private final CommandeProduitService commandeProduitService = new CommandeProduitService();
    private final ProduitService produitService = new ProduitService();
    private UserService userService;

    private List<Commande> orders;
    private User currentUser;
    private int utilisateurId = 1; // Will be set from currentUser when available

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        initializeServices();
        
        // Load orders (will use default user ID until setCurrentUser is called)
        loadOrders();

        if (viewDetailsButton != null) {
            viewDetailsButton.setOnAction(e -> handleViewDetails());
        }
    }
    
    /**
     * Initialize services required by this controller.
     * This approach makes integration with PI_DEV_G5 easier.
     */
    private void initializeServices() {
        try {
            // Initialize UserService directly
            this.userService = new UserService();
        } catch (Exception e) {
            System.err.println("Error initializing services: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Sets the current user and reloads orders for this user.
     * This method should be called after controller initialization.
     * 
     * @param user The current user
     */
    @Override
    public void setCurrentUser(User user) {
        super.setCurrentUser(user);
        if (user != null) {
            this.utilisateurId = user.getId();
            // Reload orders for this user
            loadOrders();
        }
    }

    private void loadOrders() {
        try {
            // If currentUser is set, use its ID; otherwise use the default ID
            int userIdToUse = (currentUser != null) ? currentUser.getId() : utilisateurId;
            
            orders = commandeService.recuperer();
            // Filter orders by user ID
            orders.removeIf(order -> order.getClientId() != userIdToUse);
            ObservableList<Commande> ordersList = FXCollections.observableArrayList(orders);
            ordersListView.setItems(ordersList);
            
            // Custom cell factory to display order information
            ordersListView.setCellFactory(param -> new ListCell<Commande>() {
                @Override
                protected void updateItem(Commande order, boolean empty) {
                    super.updateItem(order, empty);
                    if (empty || order == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        String orderDate = dateFormat.format(order.getDate());
                        String status = order.getStatut();
                        String total = String.format("%.2f DT", order.getTotal());
                        
                        setText("Order #" + order.getId() + " - " + orderDate + " - Status: " + status + " - Total: " + total);
                    }
                }
            });
        } catch (Exception e) {
            UIUtils.showAlert("Error", null, "Failed to load orders: " + e.getMessage(), "ERROR");
        }
    }

    private void handleViewDetails() {
        Commande selectedOrder = ordersListView.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            UIUtils.showAlert("Info", null, "Please select an order to view details.", "INFORMATION");
            return;
        }
        
        try {
            // Get order details
            List<CommandeProduit> orderProducts = commandeProduitService.getOrderItemsByCommandeId(selectedOrder.getId());
            
            // Create dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Order Details");
            dialog.setHeaderText("Order #" + selectedOrder.getId() + " Details");
            
            // Create content
            VBox content = new VBox(10);
            content.setStyle("-fx-padding: 20;");
            
            // Order info
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Label orderDate = new Label("Date: " + dateFormat.format(selectedOrder.getDate()));
            Label orderStatus = new Label("Status: " + selectedOrder.getStatut());
            Label orderTotal = new Label("Total: " + String.format("%.2f DT", selectedOrder.getTotal()));
            
            // Add user information if available
            if (currentUser != null) {
                Label userInfo = new Label("Customer: " + currentUser.getPrenom() + " " + currentUser.getNom());
                content.getChildren().addAll(userInfo, orderDate, orderStatus, orderTotal);
            } else {
                // Try to get user info from service
                try {
                    User user = userService.getById(selectedOrder.getClientId());
                    if (user != null) {
                        Label userInfo = new Label("Customer: " + user.getPrenom() + " " + user.getNom());
                        content.getChildren().addAll(userInfo, orderDate, orderStatus, orderTotal);
                    } else {
                        content.getChildren().addAll(orderDate, orderStatus, orderTotal);
                    }
                } catch (Exception e) {
                    // If user lookup fails, just show order info without user details
                    content.getChildren().addAll(orderDate, orderStatus, orderTotal);
                }
            }
            
            // Products list
            Label productsHeader = new Label("Products:");
            productsHeader.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0;");
            content.getChildren().add(productsHeader);
            
            for (CommandeProduit cp : orderProducts) {
                Produit product = produitService.getProduitById(cp.getProduitId());
                HBox productRow = new HBox(10);
                
                Label productName = new Label(product.getNom());
                productName.setPrefWidth(200);
                
                Label productQuantity = new Label("Quantity: " + cp.getQuantite());
                productQuantity.setPrefWidth(100);
                
                Label productPrice = new Label(String.format("Price: %.2f DT", product.getPrix()));
                productPrice.setPrefWidth(100);
                
                Label productTotal = new Label(String.format("Subtotal: %.2f DT", cp.getSousTotal()));
                productTotal.setPrefWidth(150);
                
                productRow.getChildren().addAll(productName, productQuantity, productPrice, productTotal);
                content.getChildren().add(productRow);
            }
            
            // Set content and add close button
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            dialog.showAndWait();
            
        } catch (Exception e) {
            UIUtils.showAlert("Error", null, "Failed to load order details: " + e.getMessage(), "ERROR");
        }
    }
}
