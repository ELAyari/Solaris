package com.esprit.controllers;

import com.esprit.models.Produit;
import com.esprit.Services.CategorieService;
import com.esprit.Services.PanierService;
import com.esprit.Services.ProduitService;
import com.esprit.Services.UserService;
import com.esprit.Services.WishlistService;
import com.esprit.models.Categorie;
import com.esprit.models.User;
import com.esprit.utils.ImageUtils;
import com.esprit.utils.UIUtils;
import com.esprit.utils.UserSession;
import com.esprit.utils.SceneSwitcher;
import com.esprit.tests.MainprogGUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.sql.SQLException;

public class ProductDetailController extends BaseController<Produit> {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialization logic here
    }

    // Reference to the parent content pane for fragment-based navigation
    private javafx.scene.layout.StackPane parentContentPane;
    
    @FXML
    private BorderPane rootPane;

    @FXML
    private ImageView productImageView;

    @FXML
    private Label productNameLabel;

    @FXML
    private Label productDescriptionLabel;

    @FXML
    private Label productPriceLabel;
    
    @FXML
    private Label recyclableBadge;
    
    @FXML
    private Label stockStatusLabel;
    
    @FXML
    private Label stockCountLabel;
    
    @FXML
    private Label categoryLabel;
    
    @FXML
    private HBox companyBox;
    
    @FXML
    private ImageView companyLogoView;
    
    @FXML
    private Label companyNameLabel;

    @FXML
    private ComboBox<Integer> quantityComboBox;

    @FXML
    private Button addToCartButton;

    @FXML
    private Button addToWishlistButton;

    @FXML
    private Button backButton;

    @FXML
    private ScrollPane similarProductsScrollPane;

    @FXML
    private HBox similarProductsContainer;

    @FXML
    private NavBarController navBarController;

    private Produit product;

    private final PanierService panierService = new PanierService();

    private final ProduitService produitService = new ProduitService();
    
    private final CategorieService categorieService = new CategorieService();
    
    private final WishlistService wishlistService = new WishlistService();
    
    private User currentUser;
    private final UserSession userSession = UserSession.getInstance();
    private final UserService userService;

    public ProductDetailController() {
        try {
            userService = new UserService();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize UserService", e);
        }
    }

    /**
     * Gets the current user from session with validation
     */
    public User getCurrentUser() {
        if (currentUser == null) {
            currentUser = userSession.getUser();
            if (currentUser == null) {
                System.err.println("Warning: No active user session");
            }
        }
        return currentUser;
    }
    
    /**
     * Sets the current user and updates session
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        userSession.setUser(user);
        updateUserDependentUI();
        
        // Propagate to NavBarController if available
        if (navBarController != null) {
            navBarController.setCurrentUser(user);
            navBarController.setCurrentUser(user);
        }
        
        // Update navbar if available
        if (navBarController != null) {
            try {
                // Try to call setCurrentUser if it exists
                java.lang.reflect.Method method = navBarController.getClass().getMethod("setCurrentUser", User.class);
                method.invoke(navBarController, user);
            } catch (Exception e) {
                System.err.println("NavBarController.setCurrentUser() not available: " + e.getMessage());
                // Fallback to setCurrentUser if available
                try {
                    java.lang.reflect.Method fallbackMethod = navBarController.getClass().getMethod("setCurrentUser", User.class);
                    fallbackMethod.invoke(navBarController, user);
                } catch (Exception ex) {
                    System.err.println("NavBarController.setCurrentUser() also not available");
                }
            }
        }
    }
    
    /**
     * Updates UI elements that depend on user state
     */
    private void updateUserDependentUI() {
        updateActionButtonsState();
    }
    
    /**
     * Sets the parent content pane for fragment-based navigation
     * @param contentPane The parent StackPane to use for navigation
     */
    public void setParentContentPane(javafx.scene.layout.StackPane contentPane) {
        this.parentContentPane = contentPane;
    }

    public void setProduct(Produit product) {
        this.product = product;
        updateUI();
        loadSimilarProducts();
    }

    private void updateUI() {
        if (product == null) {
            return;
        }
        
        // Set basic product info
        productNameLabel.setText(product.getNom());
        productDescriptionLabel.setText(product.getDescription());
        productPriceLabel.setText(String.format("%.2f DT", product.getPrix()));

        // Load product image
        Image image = ImageUtils.loadImage(product.getImage());
        if (image != null) {
            productImageView.setImage(image);
        } else {
            productImageView.setImage(new Image("file:src/main/resources/images/default_renewable.png"));
        }
        
        // Show/hide recyclable badge
        if (product.isRecyclable()) {
            recyclableBadge.setVisible(true);
        } else {
            recyclableBadge.setVisible(false);
        }
        
        // Set company information if available
        if (product.getCompanyName() != null && !product.getCompanyName().isEmpty()) {
            companyNameLabel.setText(product.getCompanyName());
            companyBox.setVisible(true);
            
            // Load company logo if available
            if (product.getCompanyLogo() != null && !product.getCompanyLogo().isEmpty()) {
                Image logoImage = ImageUtils.loadLogoImage(product.getCompanyLogo());
                if (logoImage != null) {
                    companyLogoView.setImage(logoImage);
                } else {
                    companyLogoView.setImage(new Image("file:src/main/resources/images/logos/default_logo.png"));
                }
            } else {
                companyLogoView.setImage(new Image("file:src/main/resources/images/logos/default_logo.png"));
            }
        } else {
            companyBox.setVisible(false);
        }
        
        // Set category information
        try {
            Categorie categorie = categorieService.recupererParId(product.getCategorieId());
            if (categorie != null) {
                categoryLabel.setText(categorie.getNom());
            } else {
                categoryLabel.setText("Uncategorized");
            }
        } catch (Exception e) {
            categoryLabel.setText("Uncategorized");
        }
        
        // Set stock status
        updateStockStatus(product.getQuantiteStock());
        
        // Populate quantity combo box
        quantityComboBox.getItems().clear();
        if (product.getQuantiteStock() > 0) {
            for (int i = 1; i <= Math.min(product.getQuantiteStock(), 20); i++) { // Limit to 20 items max
                quantityComboBox.getItems().add(i);
            }
            quantityComboBox.setValue(1);
        }
        
        updateActionButtonsState();
    }

    private void loadSimilarProducts() {
        if (product == null) {
            return;
        }
        List<Produit> similarProducts = produitService.recupererParCategorie(product.getCategorieId());
        similarProductsContainer.getChildren().clear();

        for (Produit p : similarProducts) {
            if (p.getId() != product.getId()) {
                VBox productCard = createProductCard(p);
                similarProductsContainer.getChildren().add(productCard);
            }
        }
    }

    private VBox createProductCard(Produit produit) {
        VBox card = new VBox(8);
        card.setPrefWidth(180);
        card.getStyleClass().add("similar-product-card");
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e9ecef; -fx-padding: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Product Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.setStyle("-fx-background-radius: 4;");

        Image image = ImageUtils.loadImage(produit.getImage());
        if (image != null) {
            imageView.setImage(image);
        } else {
            imageView.setImage(new Image("file:src/main/resources/images/default_renewable.png"));
        }
        
        // Product Name
        Label nameLabel = new Label(produit.getNom());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #212529; -fx-wrap-text: true;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(150);
        
        // Company Name (if available)
        HBox companyInfo = new HBox(5);
        companyInfo.setStyle("-fx-alignment: center-left;");
        
        if (produit.getCompanyName() != null && !produit.getCompanyName().isEmpty()) {
            Label companyLabel = new Label(produit.getCompanyName());
            companyLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
            companyInfo.getChildren().add(companyLabel);
        }
        
        // Price
        Label priceLabel = new Label(String.format("%.2f DT", produit.getPrix()));
        priceLabel.setStyle("-fx-text-fill: #2a9d8f; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Add all components to card
        card.getChildren().addAll(imageView, nameLabel, companyInfo, priceLabel);

        // Add click event
        card.setOnMouseClicked(e -> {
            try {
                // Use MainprogGUI's centralized fragment loading method to preserve tab selection
                com.esprit.tests.MainprogGUI mainApp = com.esprit.tests.MainprogGUI.getInstance();
                if (mainApp != null) {
                    mainApp.loadContentFragment("/ProductDetailView.fxml", 
                        (ProductDetailController controller) -> {
                            controller.setProduct(produit);
                            controller.setCurrentUser(getCurrentUser());
                        });
                } else {
                    System.err.println("MainprogGUI not available, falling back to direct loading");
                    
                    // Fallback to direct loading if MainprogGUI is not available
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProductDetailView.fxml"));
                    Parent root = loader.load();
                    ProductDetailController controller = loader.getController();
                    controller.setProduct(produit);
                    controller.setCurrentUser(getCurrentUser());
                    
                    if (parentContentPane != null) {
                        controller.setParentContentPane(parentContentPane);
                        parentContentPane.getChildren().setAll(root);
                    } else {
                        Stage stage = (Stage) rootPane.getScene().getWindow();
                        stage.getScene().setRoot(root);
                        stage.setTitle("Product Details");
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                UIUtils.showAlert("Error", null, "Failed to open product detail view.", "ERROR");
            }
        });
        
        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #ced4da; -fx-padding: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 3); -fx-translate-y: -2;");
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e9ecef; -fx-padding: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        });

        return card;
    }

    /**
     * Finds the contentPane StackPane in the parent hierarchy
     * @return the contentPane or null if not found
     */
private javafx.scene.layout.StackPane findContentPane() {
    javafx.scene.Parent current = backButton.getParent();
    while (current != null) {
        if (current.getParent() instanceof javafx.scene.layout.BorderPane) {
            javafx.scene.layout.BorderPane borderPane = (javafx.scene.layout.BorderPane) current.getParent();
            if (borderPane.getCenter() instanceof javafx.scene.layout.StackPane) {
                return (javafx.scene.layout.StackPane) borderPane.getCenter();
            }
        }
        current = current.getParent();
    }
    return null;
}
    
@FXML
public void handleBack() {
    System.out.println("=== BACK BUTTON DEBUG START ===");
    System.out.println("Button click registered in ProductDetailController");
    
    try {
        // Use MainprogGUI's centralized fragment loading method to preserve tab selection
        MainprogGUI mainApp = MainprogGUI.getInstance();
        if (mainApp != null) {
            System.out.println("Using MainprogGUI.loadContentFragment for navigation");
            mainApp.loadContentFragment("/ProductCardView.fxml", 
                (ProductCardController controller) -> {
                    controller.setCurrentUser(getCurrentUser());
                });
        } else {
            System.out.println("MainprogGUI not available, falling back to direct loading");
            
            // Fallback to direct loading if MainprogGUI is not available
            StackPane contentPane = parentContentPane != null ? parentContentPane : findContentPane();
            
            if (contentPane != null) {
                System.out.println("Using content pane for navigation: " + contentPane);
                
                URL fxmlLocation = getClass().getResource("/ProductCardView.fxml");
                if (fxmlLocation == null) {
                    System.err.println("ERROR: ProductCardView.fxml not found in classpath");
                    return;
                }
                
                FXMLLoader loader = new FXMLLoader(fxmlLocation);
                Parent productsView = loader.load();
                
                ProductCardController controller = loader.getController();
                if (controller != null) {
                    System.out.println("Setting current user in ProductCardController");
                    controller.setCurrentUser(getCurrentUser());
                }
                
                System.out.println("Replacing content in pane");
                contentPane.getChildren().clear();
                contentPane.getChildren().add(productsView);
            } else {
                System.out.println("No content pane available, falling back to SceneSwitcher");
                SceneSwitcher.switchScene(backButton, "/ProductCardView.fxml", 
                    backButton.getScene().getWidth(), backButton.getScene().getHeight());
            }
        }
    } catch (IOException e) {
        System.err.println("ERROR loading ProductCardView: " + e.getMessage());
        e.printStackTrace();
    } catch (Exception e) {
        System.err.println("UNEXPECTED ERROR during navigation: " + e.getMessage());
        e.printStackTrace();
    }
    
    System.out.println("=== BACK BUTTON DEBUG END ===");
}

    @FXML
    private void handleAddToCart() {
        if (currentUser != null && product != null) {
            try {
                int quantity = quantityComboBox.getValue() != null ? quantityComboBox.getValue() : 1;
                panierService.addProductToPanier(currentUser.getId(), product, quantity);
                UIUtils.showToastNotification(product.getNom() + " added to panier.");
            } catch (Exception e) {
                UIUtils.showAlert("Error", null, "Failed to add product to cart: " + e.getMessage(), "ERROR");
            }
        }
        
    }
    
    @FXML
    private void handleAddToWishlist() {
        if (currentUser != null && product != null) {
            try {
                wishlistService.addProductToWishlist(currentUser.getId(), product.getId());
                UIUtils.showToastNotification(product.getNom() + " added to wishlist.");
            } catch (Exception e) {
                System.err.println("Error adding to wishlist: " + e.getMessage());
            }
        }
    }

    @FXML
    public void initialize() {
        // Verify NavBarController injection
        if (navBarController == null) {
            System.err.println("NavBarController not injected - check FXML file");
            return;
        }
        
        // Initialize only when user is available
        if (currentUser != null) {
            navBarController.setCurrentUser(currentUser);
            navBarController.setCurrentUser(currentUser);
        } else {
            // Setup listener for when user becomes available
            // Removed userProperty listener as UserSession does not have userProperty method
            // Consider implementing observer pattern if needed in future
        }
        
        try {
            // Initialize UI elements first
            recyclableBadge.setVisible(false);
            
            // Set up event handlers
            if (backButton != null) {
                backButton.setOnAction(event -> handleBack());
            }
            addToCartButton.setOnAction(e -> handleAddToCart());
            addToWishlistButton.setOnAction(e -> handleAddToWishlist());
            
            // Initialize user session
            if (currentUser == null) {
                currentUser = userSession.getUser();
            }
            
            // Apply animations
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), rootPane);
            fadeIn.setFromValue(0.7);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        } catch (Exception e) {
            System.err.println("Initialization error: " + e.getMessage());
        }
    }

    private void updateStockStatus(int quantity) {
        if (quantity > 10) {
            stockStatusLabel.setText("In Stock");
            stockStatusLabel.getStyleClass().setAll("stock-status", "in-stock");
            stockCountLabel.setText("(" + quantity + " available)");
        } else if (quantity > 0) {
            stockStatusLabel.setText("Low Stock");
            stockStatusLabel.getStyleClass().setAll("stock-status", "low-stock");
            stockCountLabel.setText("(" + quantity + " left)");
        } else {
            stockStatusLabel.setText("Out of Stock");
            stockStatusLabel.getStyleClass().setAll("stock-status", "out-of-stock");
            stockCountLabel.setText("(0 available)");
        }
    }

    /**
     * Updates the state of action buttons like 'Add to Cart' and 'Add to Wishlist'
     * based on product stock and user login status.
     */
    private void updateActionButtonsState() {
        boolean outOfStock = (product == null || product.getQuantiteStock() <= 0);
        boolean userNotLoggedIn = (currentUser == null);

        if (addToCartButton != null) {
            addToCartButton.setDisable(outOfStock || userNotLoggedIn);
        }
        if (quantityComboBox != null) {
            quantityComboBox.setDisable(outOfStock || userNotLoggedIn);
        }
        if (addToWishlistButton != null) {
            addToWishlistButton.setDisable(userNotLoggedIn);
        }
    }
}
