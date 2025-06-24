package com.esprit.controllers;

import com.esprit.models.Panier;
import com.esprit.models.Produit;
import com.esprit.Services.ProduitService;
import com.esprit.Services.PanierService;
import com.esprit.Services.WishlistService;
import com.esprit.models.Wishlist;
import com.esprit.models.User;
import com.esprit.utils.ImageUtils;
import com.esprit.utils.UIUtils;
import com.esprit.utils.UserSession;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductCardController {

    @FXML
    private FlowPane productFlowPane;
    @FXML
    private NavBarController navBarController; // Injected from FXML include
    
    // No longer need a reference to the main container as we use findContentPane() method
    
    /**
     * Finds the contentPane StackPane in the parent hierarchy
     * @return the contentPane or null if not found
     */
    private javafx.scene.layout.StackPane findContentPane() {
        // Navigate up the scene graph to find the contentPane
        javafx.scene.Parent current = productFlowPane.getParent();
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

    private final ProduitService produitService = new ProduitService();
    private final PanierService panierService = new PanierService();
    private final WishlistService wishlistService = new WishlistService();
    // CategorieService is now primarily used by NavBarController
    
    /**
     * Handles navigation to the Panier view using content pane navigation
     */
    public void handlePanier() {
        try {
            // Find the contentPane in the parent hierarchy
            javafx.scene.layout.StackPane contentPane = findContentPane();
            if (contentPane == null) {
                System.err.println("Could not find contentPane in parent hierarchy");
                
                // Fallback to traditional scene switching
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/PanierView.fxml"));
                javafx.scene.Parent root = loader.load();
                
                PanierController controller = loader.getController();
                controller.setCurrentUser(currentUser);
                
                javafx.stage.Stage stage = (javafx.stage.Stage) productFlowPane.getScene().getWindow();
                stage.getScene().setRoot(root);
                stage.setTitle("Panier");
                return;
            }
            
            // Load the PanierView
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/PanierView.fxml"));
            javafx.scene.Parent root = loader.load();
            
            // Configure the controller
            PanierController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            // Replace the content in the contentPane
            contentPane.getChildren().setAll(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            UIUtils.showAlert("Error", null, "Failed to navigate to Panier view.", "ERROR");
        }
    }
    
    /**
     * Handles navigation to the Wishlist view using content pane navigation
     */
    public void handleWishlist() {
        try {
            // Find the contentPane in the parent hierarchy
            javafx.scene.layout.StackPane contentPane = findContentPane();
            if (contentPane == null) {
                System.err.println("Could not find contentPane in parent hierarchy");
                
                // Fallback to traditional scene switching
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/WishlistView.fxml"));
                javafx.scene.Parent root = loader.load();
                
                WishlistController controller = loader.getController();
                controller.setCurrentUser(currentUser);
                
                javafx.stage.Stage stage = (javafx.stage.Stage) productFlowPane.getScene().getWindow();
                stage.getScene().setRoot(root);
                stage.setTitle("Wishlist");
                return;
            }
            
            // Load the WishlistView
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/WishlistView.fxml"));
            javafx.scene.Parent root = loader.load();
            
            // Configure the controller
            WishlistController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            // Replace the content in the contentPane
            contentPane.getChildren().setAll(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            UIUtils.showAlert("Error", null, "Failed to navigate to Wishlist view.", "ERROR");
        }
    }

    // The panier field is used in setCurrentUser method to cache the user's panier
    private Panier panier;

    private User currentUser;

    private List<Produit> wishlistProducts = new ArrayList<>();
    // categoryNameToIdMap is now primarily managed by NavBarController

    // Removed FXML fields for panierButton, wishlistButton, categoryComboBox, searchTextField, clearFilterButton

    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        // Also update the singleton UserSession
        UserSession.getInstance().setUser(user);
        
        if (user != null) {
            System.out.println("ProductCard: Current user set to " + user.getNom() + " " + user.getPrenom() + " (ID: " + user.getId() + ")");
        } else {
            System.out.println("ProductCard: No user logged in");
        }
        
        if (navBarController != null) {
            navBarController.setCurrentUser(user);
        }
        
        // Re-initialize or update parts that depend on the user
        if (user != null) {
            panier = panierService.getPanierByUserId(user.getId());
        }
        
        loadWishlist(); // Wishlist depends on currentUser
        
        if (navBarController != null) {
            navBarController.updateButtonCounts();
        }
        
        // Products are typically loaded regardless of user, but filtering might apply
        filterProducts(); // Re-filter if products were already loaded
    }

public void initialize() {
    // Use setCurrentUser to ensure proper session sync
    setCurrentUser(UserSession.getInstance().getCurrentUser());
    
    // Set this controller as the userData of the root node
    // This allows the NavBarController to find this controller
    if (productFlowPane.getScene() != null) {
        productFlowPane.getScene().getRoot().setUserData(this);
    }
    
    // Setup NavBarController
    if (navBarController != null) {
        navBarController.setCurrentUser(currentUser);
        navBarController.showBackButton(false);
        
        // Set up filter callbacks
        navBarController.setFilterCallback(this::filterProducts);
        navBarController.setCategoryChangeCallback(category -> filterProducts());
        navBarController.setSearchChangeCallback(searchText -> filterProducts());
    }
    
    // Load user-dependent data if we have a user
    if (currentUser != null) {
        panier = panierService.getPanierByUserId(currentUser.getId());
        loadWishlist();
    }
    
    // Load products (not user-dependent)
    loadProducts();
}

    // updatePanierButtonCount, updateWishlistButtonCount, animatePanierButton, animateWishlistButton
    // are now handled by or delegated to NavBarController

    private void setupCardHoverAnimations(VBox card, HBox actionsBox) {
        // Show/hide actions on hover
        card.setOnMouseEntered(e -> {
            actionsBox.setOpacity(1.0);
            // Scale animation
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), card);
            scaleTransition.setToX(1.03);
            scaleTransition.setToY(1.03);
            scaleTransition.play();
        });
        
        card.setOnMouseExited(e -> {
            actionsBox.setOpacity(0.0);
            // Scale back animation
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), card);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();
        });
    }

    private void handleBuy(Produit produit) {
        try {
            User currentUser = UserSession.getInstance().getCurrentUser();
            if (currentUser == null) {
                UIUtils.showAlert("Login Required", null, "Please log in to add items to your cart.", "WARNING");
                return;
            }
            
            panierService.addProductToPanier(currentUser.getId(), produit, 1);
            if (navBarController != null) {
                navBarController.updateButtonCounts();
                navBarController.animatePanierButton();
            }
            UIUtils.showToastNotification(produit.getNom() + " added to panier.");
        } catch (Exception e) {
            UIUtils.showAlert("Error", null, "Failed to add product to cart: " + e.getMessage(), "ERROR");
        }
    }

    private void loadWishlist() {
        wishlistProducts.clear();
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            return; // Cannot load wishlist without a user
        }
        
        List<Wishlist> wishlists = wishlistService.recuperer(); // This might need to be filtered by user in service
        for (Wishlist w : wishlists) {
            if (w.getUser() != null && w.getUser().getId() == currentUser.getId() && w.getProduit() != null) {
                wishlistProducts.add(w.getProduit());
            }
        }
    }

    private void loadProducts() {
        // This method now just loads all products; filtering is separate
        List<Produit> allProducts = produitService.recuperer();
        productFlowPane.getChildren().clear();
        if (allProducts.isEmpty()) {
            // Optional: display a message if no products are available
            Label noProductsLabel = new Label("No products found.");
            noProductsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d;");
            productFlowPane.getChildren().add(noProductsLabel);
            return;
        }
        for (Produit produit : allProducts) {
            VBox card = createProductCard(produit);
            productFlowPane.getChildren().add(card);
        }
    }

    // setupButtons() is removed as NavBarController handles its own button actions.
    // loadCategories() is removed as NavBarController handles category loading.
    // setupFilters() is removed as NavBarController handles filter setup and callbacks.

    private void updateHeartButtonStyle(Button heartButton, Produit produit) {
        if (wishlistProducts.stream().anyMatch(p -> p.getId() == produit.getId())) {
            heartButton.setText("♥"); // Filled heart
            heartButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #f44336; -fx-font-size: 18px;");
        } else {
            heartButton.setText("♡"); // Empty heart
            heartButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #aed581; -fx-font-size: 18px;");
        }
    }

    private VBox createProductCard(Produit produit) {
        // Main card container
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(240);
        card.setPrefHeight(350);
        card.getStyleClass().add("product-card");

        // Image container
        VBox imageContainer = new VBox();
        imageContainer.getStyleClass().add("product-image-container");
        imageContainer.setAlignment(Pos.CENTER);
        
        // Product image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);

        Image image = ImageUtils.loadImage(produit.getImage());
        if (image != null) {
            imageView.setImage(image);
        } else {
            imageView.setImage(new Image("file:src/main/resources/images/default_renewable.png"));
        }
        
        imageContainer.getChildren().add(imageView);

        // Product name
        Label nameLabel = new Label(produit.getNom());
        nameLabel.getStyleClass().add("product-name");
        
        // Company name and logo (if available)
        HBox companyBox = new HBox(5);
        companyBox.setAlignment(Pos.CENTER_LEFT);
        companyBox.setVisible(false);
        companyBox.setManaged(false);
        
        if (produit.getCompanyName() != null && !produit.getCompanyName().isEmpty()) {
            // Company logo
            ImageView companyLogo = new ImageView();
            companyLogo.setFitWidth(20);
            companyLogo.setFitHeight(20);
            companyLogo.setPreserveRatio(true);
            
            if (produit.getCompanyLogo() != null && !produit.getCompanyLogo().isEmpty()) {
                companyLogo.setImage(ImageUtils.loadImage("logos/" + produit.getCompanyLogo()));
            } else {
                companyLogo.setImage(ImageUtils.loadImage("default_logo.png"));
            }
            
            // Company name label
            Label companyNameLabel = new Label(produit.getCompanyName());
            companyNameLabel.getStyleClass().add("company-name");
            
            companyBox.getChildren().addAll(companyLogo, companyNameLabel);
            companyBox.setVisible(true);
            companyBox.setManaged(true);
        }

        // Product description
        Label descriptionLabel = new Label(produit.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(200);
        descriptionLabel.getStyleClass().add("product-description");

        // Product price
        Label priceLabel = new Label(String.format("%.2f DT", produit.getPrix()));
        priceLabel.getStyleClass().add("product-price");

        // Stock status label
        Label stockLabel = new Label();
        stockLabel.getStyleClass().add("product-stock-status");
        if (produit.getQuantiteStock() > 5) {
            stockLabel.setText("In Stock");
            stockLabel.getStyleClass().add("stock-in");
        } else if (produit.getQuantiteStock() > 0) {
            stockLabel.setText("Only " + produit.getQuantiteStock() + " left!");
            stockLabel.getStyleClass().add("stock-low");
        }
        // Out of stock label is handled separately in actionsBox

        // Recyclable indicator
        Label recyclableLabel = new Label();
        if (produit.isRecyclable()) {
            recyclableLabel.setText("♻️ Recyclable");
            recyclableLabel.getStyleClass().add("recyclable-indicator");
        }

        // Action buttons container (initially hidden, shown on hover)
        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.getStyleClass().add("product-actions");

        // Buy button
        Button buyButton = new Button("Buy");
        buyButton.getStyleClass().add("btn-buy");
        buyButton.setOnAction(e -> {
            handleBuy(produit);
            e.consume(); // Prevent event from bubbling up to card click
        });

        // Heart/wishlist button
        Button heartButton = new Button();
        heartButton.getStyleClass().add("btn-heart");
        updateHeartButtonStyle(heartButton, produit);
        heartButton.setOnAction(e -> {
            toggleWishlist(produit, heartButton);
            e.consume(); // Prevent event from bubbling up to card click
        });
        
        // Add appropriate buttons/labels based on stock
        if (produit.getQuantiteStock() > 0) {
            actionsBox.getChildren().addAll(buyButton, heartButton);
        } else {
            Label outOfStockLabel = new Label("Out of Stock");
            outOfStockLabel.getStyleClass().add("out-of-stock");
            actionsBox.getChildren().addAll(outOfStockLabel, heartButton);
        }

        // Add all components to card
        // VBox for text content below image
        VBox textContent = new VBox(5); // spacing between text elements
        textContent.setAlignment(Pos.CENTER_LEFT);
        textContent.getChildren().addAll(nameLabel);
        
        // Add company box if visible
        if (companyBox.isVisible()) {
            textContent.getChildren().add(companyBox);
        }
        
        textContent.getChildren().addAll(descriptionLabel, priceLabel);
        if (!stockLabel.getText().isEmpty()) {
            textContent.getChildren().add(stockLabel);
        }
        if (!recyclableLabel.getText().isEmpty()) {
            textContent.getChildren().add(recyclableLabel);
        }
        
        card.getChildren().addAll(imageContainer, textContent, actionsBox);

        // Add hover animations
        setupCardHoverAnimations(card, actionsBox);

        // Card click event (navigate to product detail)
        card.setOnMouseClicked(e -> {
            try {
                // Use MainprogGUI's centralized fragment loading method to preserve tab selection
                com.esprit.tests.MainprogGUI mainApp = com.esprit.tests.MainprogGUI.getInstance();
                if (mainApp != null) {
                    mainApp.loadContentFragment("/ProductDetailView.fxml", 
                        (ProductDetailController controller) -> {
                            controller.setProduct(produit);
                            controller.setCurrentUser(currentUser);
                        });
                } else {
                    System.err.println("MainprogGUI not available, falling back to direct loading");
                    
                    // Fallback to direct loading if MainprogGUI is not available
                    javafx.scene.layout.StackPane contentPane = findContentPane();
                    if (contentPane == null) {
                        System.err.println("Could not find contentPane in parent hierarchy");
                        return;
                    }
                    
                    // Load the ProductDetailView
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProductDetailView.fxml"));
                    Parent root = loader.load();
                    
                    // Configure the controller
                    ProductDetailController controller = loader.getController();
                    controller.setProduct(produit);
                    controller.setCurrentUser(currentUser); // Pass current user
                    
                    // Replace the content in the contentPane
                    contentPane.getChildren().setAll(root);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                UIUtils.showAlert("Error", null, "Failed to open product detail view.", "ERROR");
            }
        });

        return card;
    }
    // setupFilters() is removed, handled by NavBarController

    private void filterProducts() {
        if (navBarController == null) { // Guard against early calls before navBarController is ready
            loadProducts(); // Load all products if navbar isn't ready for filtering
            return;
        }

        String selectedCategoryName = navBarController.getSelectedCategory();
        Integer selectedCategoryId = navBarController.getSelectedCategoryId(); // From map in NavBarController
        String searchText = navBarController.getSearchText().toLowerCase();

        List<Produit> allProducts = produitService.recuperer(); // Get all products once
        
        List<Produit> filteredProducts = allProducts.stream()
                .filter(produit -> {
                    boolean categoryMatch = selectedCategoryName == null ||
                                            selectedCategoryName.equals("All Categories") ||
                                            (selectedCategoryId != null && produit.getCategorieId() == selectedCategoryId);
                    
                    boolean searchMatch = searchText.isEmpty() ||
                                          produit.getNom().toLowerCase().contains(searchText) ||
                                          produit.getDescription().toLowerCase().contains(searchText);
                    return categoryMatch && searchMatch;
                })
                .toList();

        productFlowPane.getChildren().clear();
        if (filteredProducts.isEmpty()) {
            Label noResultsLabel = new Label("No products match your filters.");
            noResultsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d;");
            productFlowPane.getChildren().add(noResultsLabel);
        } else {
            for (Produit produit : filteredProducts) {
                VBox card = createProductCard(produit);
                productFlowPane.getChildren().add(card);
            }
        }
    }

    private void toggleWishlist(Produit produit, Button heartButton) {
        try {
            User currentUser = UserSession.getInstance().getCurrentUser();
            if (currentUser == null) {
                UIUtils.showAlert("Login Required", null, "Please log in to manage your wishlist.", "WARNING");
                return;
            }
            
            boolean isInWishlist = wishlistProducts.stream().anyMatch(p -> p.getId() == produit.getId());

            if (isInWishlist) {
                // Remove from wishlist
                Wishlist toRemove = null;
                // Assuming wishlistService.recuperer() gets all wishlists, needs filtering
                // It would be better if wishlistService.find(userId, productId) existed
                for (Wishlist w : wishlistService.recuperer()) {
                    if (w.getUser() != null && w.getUser().getId() == currentUser.getId() &&
                        w.getProduit() != null && w.getProduit().getId() == produit.getId()) {
                        toRemove = w;
                        break;
                    }
                }
                if (toRemove != null) {
                    wishlistService.supprimer(toRemove); // This should take ID or the object
                    wishlistProducts.removeIf(p -> p.getId() == produit.getId());
                    UIUtils.showToastNotification(produit.getNom() + " removed from wishlist.", "red");
                }
            } else {
                // Add to wishlist
                Wishlist newWishlist = new Wishlist();
                newWishlist.setUser(currentUser);
                newWishlist.setProduit(produit);
                newWishlist.setDateAdded(LocalDateTime.now());
                wishlistService.ajouter(newWishlist);
                wishlistProducts.add(produit);
                UIUtils.showToastNotification(produit.getNom() + " added to wishlist.");
            }
            
            updateHeartButtonStyle(heartButton, produit);
            
            if (navBarController != null) {
                navBarController.updateButtonCounts();
                navBarController.animateWishlistButton();
            }
        } catch (Exception e) {
            UIUtils.showAlert("Error", null, "Failed to update wishlist: " + e.getMessage(), "ERROR");
        }
    }
}
