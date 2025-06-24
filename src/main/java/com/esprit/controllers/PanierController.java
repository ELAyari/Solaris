package com.esprit.controllers;

import com.esprit.Services.PanierService;
import com.esprit.Services.ProduitService;
import com.esprit.Services.UserService;
import com.esprit.models.Panier;
import com.esprit.models.PanierProduit;
import com.esprit.models.Produit;
import com.esprit.models.User;
// UserRole and UserStatus are used indirectly through the User model
import com.esprit.utils.ImageUtils;
import com.esprit.utils.UIUtils;
import com.esprit.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class PanierController extends BaseController<Panier> {

    @FXML
    private TableView<PanierProduit> panierTable;
    @FXML
    private TableColumn<PanierProduit, ImageView> productImageColumn;
    @FXML
    private TableColumn<PanierProduit, String> productNameColumn;
    @FXML
    private TableColumn<PanierProduit, Integer> quantityColumn;
    @FXML
    private TableColumn<PanierProduit, Double> priceColumn;
    @FXML
    private TableColumn<PanierProduit, Double> subtotalColumn;
    @FXML
    private TableColumn<PanierProduit, Void> removeColumn;
    @FXML
    private Label totalLabel;
    @FXML
    private Button checkoutButton;
    // Removed FXML backButton, will be handled by navBarController
    @FXML
    private VBox mainContainer;
    @FXML
    private NavBarController navBarController; // Injected from FXML include

    private PanierService panierService;
    private ProduitService produitService;
    private UserService userService;
    private Panier panier;
    private User currentUser;

    /**
     * Sets the current user and updates the UI accordingly.
     * This method should be called after controller initialization.
     * 
     * @param user The current user
     */
    @Override
    public void setCurrentUser(User user) {
        super.setCurrentUser(user);
        // currentUser is now set by the parent class
        
        // Update UserSession
        UserSession.getInstance().setUser(user);
        
        // Re-initialize panier and UI elements that depend on the user
        if (panierService != null && this.currentUser != null) {
            try {
                panier = panierService.getPanierByUserId(this.currentUser.getId());
                loadPanierItems(); // Reload table
            } catch (Exception e) {
                System.err.println("Error loading panier for user: " + e.getMessage());
                e.printStackTrace();
            }
        }
        if (navBarController != null) {
            navBarController.setCurrentUser(this.currentUser);
            navBarController.updateButtonCounts();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // First, initialize services that don't depend on currentUser
        try {
            panierService = new PanierService();
            produitService = new ProduitService();
            
            initializeUserService();
        } catch (Exception e) {
            System.err.println("Error initializing services: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Try to get current user from UserSession first
        this.currentUser = UserSession.getInstance().getCurrentUser();
        
        // If UserSession doesn't have a user, fall back to default
        if (currentUser == null) {
            System.out.println("PanierController: No user in UserSession, using fallback");
            // This is a fallback, ideally setCurrentUser is called before showing the view.
            try {
                // Try to get a default user from the service
                if (userService != null) {
                    this.currentUser = userService.getById(1);
                    if (currentUser != null) {
                        // Don't call setCurrentUser here to avoid potential infinite loop
                        // Just update the UserSession
                        UserSession.getInstance().setUser(currentUser);
                        System.out.println("PanierController: Loaded user from database ID: 1");
                    } else {
                        createDefaultUser();
                    }
                } else {
                    createDefaultUser();
                }
            } catch (Exception e) {
                createDefaultUser();
                System.err.println("PanierController: Error getting user from database: " + e.getMessage());
            }
        } else {
            System.out.println("PanierController: Using current user from UserSession: " + currentUser.getNom() + " " + currentUser.getPrenom());
        }
        
        // Now complete the initialization with methods that require currentUser
        completeInitialization();
    }
    
    /**
     * Creates a default user when no user is found in the database or UserSession
     */
    private void createDefaultUser() {
        // Create a default user
        User tempUser = new User();
        tempUser.setId(1);
        tempUser.setNom("Default");
        tempUser.setPrenom("User");
        tempUser.setEmail("default@example.com");
        this.currentUser = tempUser;
        
        // Update UserSession
        UserSession.getInstance().setUser(tempUser);
        System.out.println("PanierController: Created default user");
    }
    
    /**
     * Complete initialization with operations that require currentUser to be set.
     * This is called after we ensure currentUser is not null.
     */
    private void completeInitialization() {
        if (currentUser == null) {
            System.err.println("Error: Attempted to complete initialization with null currentUser");
            return;
        }
        
        // Setup NavBarController
        if (navBarController != null) {
            navBarController.setCurrentUser(currentUser);
            navBarController.showBackButton(true); // Show back button in Panier view
            navBarController.showFilters(false); // Hide category/search filters
            navBarController.updateButtonCounts();
            
            // Set up back button handler
            navBarController.setBackButtonAction(event -> handleBack());
        } else {
            System.err.println("Warning: navBarController is null in PanierController");
        }

        // Get user's panier
        try {
            panier = panierService.getPanierByUserId(currentUser.getId());
        } catch (Exception e) {
            System.err.println("Error getting panier for user: " + e.getMessage());
            e.printStackTrace();
        }

        productImageColumn.setCellValueFactory(cellData -> {
            Produit produit = cellData.getValue().getProduit();
            Image image = ImageUtils.loadImage(produit.getImage());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            imageView.setPreserveRatio(true);
            return new javafx.beans.property.SimpleObjectProperty<>(imageView);
        });

        productNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                produitService.getProduitById(cellData.getValue().getProduit().getId()).getNom()
        ));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        priceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(
                produitService.getProduitById(cellData.getValue().getProduit().getId()).getPrix()
        ).asObject());
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));

        addRemoveButtonToTable();

        loadPanierItems();

        panierTable.setEditable(true);
        quantityColumn.setCellFactory(tc -> {
            return new TableCell<PanierProduit, Integer>() {
                private final Spinner<Integer> spinner = new Spinner<>(1, 1000, 1);

                {
                    spinner.setEditable(true);
                    spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                        PanierProduit pp = getTableRow() != null ? getTableRow().getItem() : null;
                        if (pp != null && pp.getProduit() != null && currentUser != null) {
                            panierService.updateProductQuantity(currentUser.getId(), pp.getProduit().getId(), newValue);
                            refreshPanier();
                        }
                    });
                }

                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        spinner.getValueFactory().setValue(item);
                        setGraphic(spinner);
                    }
                }
            };
        });

        panierTable.setRowFactory(tv -> {
            TableRow<PanierProduit> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem removeItem = new MenuItem("Remove");
            removeItem.setOnAction(event -> {
                PanierProduit selected = row.getItem();
                if (currentUser != null && selected != null && selected.getProduit() != null) {
                    panierService.removeProductFromPanier(currentUser.getId(), selected.getProduit().getId());
                    refreshPanier();
                }
            });
            contextMenu.getItems().add(removeItem);
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );
            return row;
        });

        checkoutButton.setOnAction(e -> {
            if (currentUser == null) {
                UIUtils.showAlert("Error", null, "User not identified. Cannot proceed to checkout.", "ERROR");
                return;
            }
            try {
                StackPane contentPane = findContentPane();
                if (contentPane != null) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/CheckoutValidationView.fxml"));
                    Parent checkoutView = loader.load();
                    
                    Object controller = loader.getController();
                    try {
                        java.lang.reflect.Method setCurrentUserMethod = controller.getClass().getMethod("setCurrentUser", User.class);
                        setCurrentUserMethod.invoke(controller, currentUser);
                        System.out.println("Successfully set current user on checkout controller");
                    } catch (Exception ex) {
                        System.err.println("Warning: Could not set current user on checkout controller");
                    }
                    
                    contentPane.getChildren().setAll(checkoutView);
                } else {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/CheckoutValidationView.fxml"));
                    Parent checkoutView = loader.load();
                    
                    Object controller = loader.getController();
                    try {
                        java.lang.reflect.Method setCurrentUserMethod = controller.getClass().getMethod("setCurrentUser", User.class);
                        setCurrentUserMethod.invoke(controller, currentUser);
                    } catch (Exception ex) {
                        System.err.println("Warning: Could not set current user on checkout controller");
                    }
                    
                    Stage stage = (Stage) checkoutButton.getScene().getWindow();
                    Scene scene = new Scene(checkoutView);
                    stage.setScene(scene);
                    stage.show();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                UIUtils.showAlert("Navigation Error", null, "Failed to open checkout validation page: " + ex.getMessage(), "ERROR");
            }
        });

        // backButton.setOnAction is removed, navBarController handles back navigation

        // Add column width bindings to fill 100% width dynamically
        productImageColumn.prefWidthProperty().bind(panierTable.widthProperty().multiply(0.1429));
        productNameColumn.prefWidthProperty().bind(panierTable.widthProperty().multiply(0.2857));
        quantityColumn.prefWidthProperty().bind(panierTable.widthProperty().multiply(0.1429));
        priceColumn.prefWidthProperty().bind(panierTable.widthProperty().multiply(0.1429));
        subtotalColumn.prefWidthProperty().bind(panierTable.widthProperty().multiply(0.1429));
        removeColumn.prefWidthProperty().bind(panierTable.widthProperty().multiply(0.1429));

        // Center content in table columns
        productImageColumn.setStyle("-fx-alignment: CENTER;");
        productNameColumn.setStyle("-fx-alignment: CENTER;");
        quantityColumn.setStyle("-fx-alignment: CENTER;");
        priceColumn.setStyle("-fx-alignment: CENTER;");
        subtotalColumn.setStyle("-fx-alignment: CENTER;");
        removeColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void addRemoveButtonToTable() {
        removeColumn.setCellFactory(col -> new TableCell<PanierProduit, Void>() {
            private final Button removeButton = new Button("Remove");

            {
                removeButton.setStyle("-fx-background-color: #e76f51; -fx-text-fill: white; -fx-font-weight: bold;");
                removeButton.setOnAction(e -> {
                    PanierProduit panierProduit = getTableView().getItems().get(getIndex());
                    if (currentUser != null && panierProduit != null && panierProduit.getProduit() != null) {
                        panierService.removeProductFromPanier(currentUser.getId(), panierProduit.getProduit().getId());
                        UIUtils.showToastNotification(panierProduit.getProduit().getNom() + " removed from panier.", "red");
                        refreshPanier();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
    }

    private void loadPanierItems() {
        if (panier == null || panier.getProduits() == null) {
            panierTable.setItems(FXCollections.observableArrayList()); // Empty list
            updateTotalLabel(); // Update total, likely to 0
            return;
        }
        ObservableList<PanierProduit> items = FXCollections.observableArrayList(panier.getProduits());
        panierTable.setItems(items);
        updateTotalLabel();
    }

    private void refreshPanier() {
        if (currentUser == null || panierService == null) {
            System.out.println("Cannot refresh panier: user or service is null");
            return;
        }
        
        try {
            panier = panierService.getPanierByUserId(currentUser.getId());
            loadPanierItems();
            if (navBarController != null) {
                navBarController.updateButtonCounts(); // Update counts in navbar
            }
        } catch (Exception e) {
            System.err.println("Error refreshing panier: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateTotalLabel() {
        if (panier != null) {
            totalLabel.setText(String.format("Total: %.2f DT", panier.getTotal()));
        } else {
            totalLabel.setText("Total: 0.00 DT");
        }
    }
    
    /**
     * Finds the contentPane StackPane in the parent hierarchy
     * @return the contentPane or null if not found
     */
    private javafx.scene.layout.StackPane findContentPane() {
        try {
            // Get a reference to any node that's part of the scene
            javafx.scene.Node sceneNode = panierTable;
            if (sceneNode == null) {
                System.err.println("Cannot find contentPane: panierTable is null");
                return null;
            }
            
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

    /**
     * Handles navigation back to the product list view
     * Uses the centralized MainprogGUI.loadContentFragment method to preserve tab selection
     */
    @FXML
    private void handleBack() {
        try {
            // Use MainprogGUI's centralized fragment loading method to preserve tab selection
            com.esprit.tests.MainprogGUI mainApp = com.esprit.tests.MainprogGUI.getInstance();
            if (mainApp != null) {
                mainApp.loadContentFragment("/ProductCardView.fxml", 
                    (ProductCardController controller) -> {
                        controller.setCurrentUser(currentUser);
                    });
            } else {
                System.err.println("MainprogGUI not available, falling back to direct loading");
                
                // Fallback to direct loading if MainprogGUI is not available
                javafx.scene.layout.StackPane contentPane = findContentPane();
                if (contentPane == null) {
                    System.err.println("Could not find contentPane in parent hierarchy");
                    
                    // Fallback to traditional scene switching
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/ProductCardView.fxml"));
                    javafx.scene.Parent root = loader.load();
                    
                    ProductCardController controller = loader.getController();
                    controller.setCurrentUser(currentUser);
                    
                    // Get the stage from any available node in the scene
                    javafx.scene.Node sceneNode = panierTable;
                    if (sceneNode == null) {
                        sceneNode = totalLabel; // Try another node if panierTable is null
                    }
                    
                    if (sceneNode == null) {
                        System.err.println("Cannot navigate: No UI components available to get scene");
                        return;
                    }
                    
                    javafx.stage.Stage stage = (javafx.stage.Stage) sceneNode.getScene().getWindow();
                    stage.getScene().setRoot(root);
                    stage.setTitle("Products");
                    return;
                }
                
                // Load the ProductCardView
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/ProductCardView.fxml"));
                javafx.scene.Parent root = loader.load();
                
                // Configure the controller
                ProductCardController controller = loader.getController();
                controller.setCurrentUser(currentUser);
                
                // Replace the content in the contentPane
                contentPane.getChildren().setAll(root);
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            UIUtils.showAlert("Error", null, "Failed to navigate back to product list.", "ERROR");
        }
    }

}
