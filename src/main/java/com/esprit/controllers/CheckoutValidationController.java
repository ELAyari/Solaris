package com.esprit.controllers;

import com.esprit.Services.PanierService;
import com.esprit.Services.UserService;
import com.esprit.models.Panier;
import com.esprit.models.PanierProduit;
import com.esprit.models.User;
import com.esprit.payment.StripePaymentService;
import com.esprit.utils.UIUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.Parent; // Added import
import javafx.scene.Scene;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.io.IOException;

/**
 * Controller for the checkout validation screen.
 * This controller handles the final step of the checkout process where the user
 * confirms their order details, shipping address, and proceeds to payment.
 * It has been updated to work with the PI_DEV_G5 user management system.
 */
public class CheckoutValidationController implements Initializable {

    @FXML private RadioButton homeAddressRadio;
    @FXML private RadioButton workAddressRadio;
    @FXML private Label addressField;    
    @FXML private RadioButton a4Radio;
    @FXML private RadioButton a3Radio;
    @FXML private Button paymentButton;
    @FXML private Button backButton;
    @FXML private VBox workAddressFields;
    @FXML private TextField streetField;
    @FXML private TextField cityField;
    @FXML private TextField postalCodeField;
    @FXML private VBox commandePreviewBox;
    @FXML private Label totalAmountText;
    @FXML private Label shipmentAddressText;
    @FXML private ListView<PanierProduit> panierItemsListView;

    private User currentUser;
    private Pane parentContentPane;
    private final PanierService panierService = new PanierService();
    private final StripePaymentService stripeService = new StripePaymentService();
    private UserService userService;

    /**
     * Sets the current user and updates the UI accordingly.
     * This method should be called after controller initialization.
     * 
     * @param user The current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        // After user is set, re-initialize dependent UI parts
        if (this.currentUser != null) {
            loadUserData(); // Load user-specific data like addresses
            loadCartItems(); // Load cart items for this user
        } else {
            // If no user is provided, disable payment functionality
            UIUtils.showAlert("Error", "User Not Found", "Please log in to proceed with checkout.", "WARNING");
            paymentButton.setDisable(true);
        }
    }

    public void setParentContentPane(Pane parentContentPane) {
        this.parentContentPane = parentContentPane;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        initializeServices();
        
        setupAddressSelection();

        // Initially hide and unmanage workAddressFields so it does not take space
        workAddressFields.setVisible(false);
        workAddressFields.setManaged(false);
        
        // Set custom cell factory for panierItemsListView to display item details
        setupListView();
        
        // If we already have a user, load user-dependent data
        if (currentUser != null) {
            loadUserData();
            loadCartItems();
        }
        
        setupPaperSizeSelection();
    }
    
    /**
     * Initialize services required by this controller.
     * This approach makes integration with PI_DEV_G5 easier.
     */
    private void initializeServices() {
        try {
            // Direct instantiation of UserService
            this.userService = new UserService();
        } catch (Exception e) {
            System.err.println("Error initializing UserService: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Sets up the list view with a custom cell factory
     */
    private void setupListView() {
        // Set custom cell factory for panierItemsListView to display item details
        panierItemsListView.setCellFactory(listView -> new ListCell<PanierProduit>() {
            @Override
            protected void updateItem(PanierProduit item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(5);
                    grid.setStyle("-fx-padding: 10; -fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");

                    Label nameLabel = new Label(item.getProduit().getNom());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
                    GridPane.setConstraints(nameLabel, 0, 0);

                    Label quantityLabel = new Label("Quantity: " + item.getQuantite());
                    quantityLabel.setStyle("-fx-font-size: 13;");
                    GridPane.setConstraints(quantityLabel, 1, 0);

                    Label priceLabel = new Label(String.format("Price: %.2f DT", item.getProduit().getPrix()));
                    priceLabel.setStyle("-fx-font-size: 13;");
                    GridPane.setConstraints(priceLabel, 0, 1);

                    Label subtotalLabel = new Label(String.format("Subtotal: %.2f DT", item.getSousTotal()));
                    subtotalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
                    GridPane.setConstraints(subtotalLabel, 1, 1);

                    grid.getChildren().addAll(nameLabel, quantityLabel, priceLabel, subtotalLabel);
                    setGraphic(grid);
                }
            }
        });
    }

    private void setupAddressSelection() {
        homeAddressRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                loadHomeAddress();
                workAddressFields.setVisible(false);
                workAddressFields.setManaged(false);
            }
        });

        workAddressRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                loadWorkAddress();
                workAddressFields.setVisible(true);
                workAddressFields.setManaged(true);
            }
        });
    }

    private void loadUserData() {
        if (currentUser != null) {
            String address = currentUser.getAddress();
            if (address == null || address.trim().isEmpty()) {
                // Set a default address for the user
                address = "123 Main Street, Tunis, 1000";
                currentUser.setAddress(address);
                
                // Update user in database with default address
                try {
                    userService.update(currentUser);
                    System.out.println("Updated user with default address: " + address);
                } catch (Exception e) {
                    System.err.println("Failed to update user with default address: " + e.getMessage());
                }
            }
            addressField.setText(address);
            homeAddressRadio.setText("Home Address: " + address);
        }
    }

    private void loadCartItems() {
        if (currentUser != null) {
            Panier panier = panierService.getPanierByUserId(currentUser.getId());
            if (panier != null) {
                panierItemsListView.getItems().setAll(panier.getProduits());
                totalAmountText.setText(String.format("Total Amount: %.2f DT", panier.getTotal()));
            }
        }
    }

    private void setupPaperSizeSelection() {
        // Default to A4
        a4Radio.setSelected(true);
    }

    private void loadHomeAddress() {
        if (currentUser != null) {
            String address = currentUser.getAddress();
            if (address == null || address.trim().isEmpty()) {
                // This shouldn't happen now that we set a default address in loadUserData
                // But just in case, set it again
                address = "123 Main Street, Tunis, 1000";
                currentUser.setAddress(address);
            }
            addressField.setText(address);
            shipmentAddressText.setText("Shipment Address: " + address);
        }
    }

    private void loadWorkAddress() {
        streetField.clear();
        cityField.clear();
        postalCodeField.clear();
        homeAddressRadio.setText("Home Address");
    }

    @FXML
    private void handlePayment() {
        if (validateFields()) {
            try {
                String paperSize = a4Radio.isSelected() ? "A4" : "A3";
                String shipmentAddress = getSelectedAddress();
                
                // Extra validation for shipment address
                if (shipmentAddress == null || shipmentAddress.trim().isEmpty() ||
                    shipmentAddress.contains("No address found")) {
                    UIUtils.showAlert("Address Required", null, 
                        "Please provide a valid shipping address before proceeding to payment.", "WARNING");
                    return;
                }
                
                double totalAmount = panierService.getTotalAmount(currentUser.getId());

                // Create Stripe session
                var session = stripeService.createCheckoutSession(
                    Math.round(totalAmount * 100),
                    "usd",
                    "https://yourdomain.com/success",
                    "https://yourdomain.com/cancel"
                );

                // Open payment window
                Stage ownerStage = (Stage) paymentButton.getScene().getWindow();
                try {
                    StripePaymentController paymentController = new StripePaymentController(
                        ownerStage,
                        currentUser.getId(),
                        session.getUrl(),
                        shipmentAddress,
                        paperSize
                    );
                    paymentController.showPaymentWindow();
                } catch (SQLException sqlEx) {
                    UIUtils.showAlert("Database Error", null,
                        "Failed to connect to the database: " + sqlEx.getMessage(), "ERROR");
                }

            } catch (Exception e) {
                UIUtils.showAlert("Payment Error", null,
                    "An error occurred while processing your payment: " + e.getMessage(), "ERROR");
            }
        }
    }

    private String getSelectedAddress() {
        if (homeAddressRadio.isSelected()) {
            String address = currentUser.getAddress();
            if (address == null || address.trim().isEmpty()) {
                return null; // Will trigger validation error
            }
            return address;
        } else {
            // Combine work address fields
            String street = streetField.getText().trim();
            String city = cityField.getText().trim();
            String postalCode = postalCodeField.getText().trim();
            
            if (street.isEmpty() || city.isEmpty() || postalCode.isEmpty()) {
                return null; // Will trigger validation error
            }
            
            return street + ", " + city + ", " + postalCode;
        }
    }

    private boolean validateFields() {
        if (homeAddressRadio.isSelected()) {
            String address = currentUser.getAddress();
            if (address == null || address.trim().isEmpty()) {
                UIUtils.showAlert("Validation Error", null, 
                    "Your profile doesn't have a home address. Please use the work address option.", "WARNING");
                workAddressRadio.setSelected(true);
                return false;
            }
        } else if (workAddressRadio.isSelected()) {
            if (streetField.getText().trim().isEmpty()) {
                UIUtils.showAlert("Validation Error", null, "Please enter a street address.", "WARNING");
                streetField.requestFocus();
                return false;
            }
            if (cityField.getText().trim().isEmpty()) {
                UIUtils.showAlert("Validation Error", null, "Please enter a city.", "WARNING");
                cityField.requestFocus();
                return false;
            }
            if (postalCodeField.getText().trim().isEmpty()) {
                UIUtils.showAlert("Validation Error", null, "Please enter a postal code.", "WARNING");
                postalCodeField.requestFocus();
                return false;
            }
        }
        return true;
    }
    
    private StackPane findContentPane() {
        try {
            // Use backButton as the starting node since it's always part of the scene
            javafx.scene.Node sceneNode = backButton;
            
            // Navigate up the scene graph to find the contentPane
            javafx.scene.Parent current = sceneNode.getParent();
            while (current != null) {
                if (current.getParent() instanceof javafx.scene.layout.BorderPane) {
                    javafx.scene.layout.BorderPane borderPane = (javafx.scene.layout.BorderPane) current.getParent();
                    if (borderPane.getCenter() instanceof javafx.scene.layout.StackPane) {
                        return (javafx.scene.layout.StackPane) borderPane.getCenter();
                    }
                }
                current = current.getParent();
            }
        } catch (Exception e) {
            System.err.println("Error finding contentPane: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    void handleBack() {
        try {
            StackPane contentPane = findContentPane();
            
            if (contentPane != null) {
                // Load the panier view as a fragment
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PanierView.fxml"));
                Parent root = loader.load();
                
                // Replace the content in the contentPane
                contentPane.getChildren().setAll(root);
                
                // Initialize controller if needed
                PanierController controller = loader.getController();
                controller.setCurrentUser(currentUser);
            } else {
                // Fallback to loading as a new scene
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PanierView.fxml"));
                Parent root = loader.load();
                
                Stage stage = (Stage) backButton.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            }
        } catch (Exception e) {
            System.err.println("Navigation error: " + e.getMessage());
            e.printStackTrace();
            UIUtils.showAlert("Navigation Error", "", "Failed to panier  view", "ERROR");
        }
    }
}
