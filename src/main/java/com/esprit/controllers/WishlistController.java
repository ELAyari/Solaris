package com.esprit.controllers;

import com.esprit.models.Produit;
import com.esprit.models.User;
import com.esprit.models.Wishlist;
import com.esprit.Services.PanierService;
import com.esprit.Services.WishlistService;

import com.esprit.utils.ImageUtils;
import com.esprit.utils.UIUtils;
import com.esprit.utils.UserSession;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Duration;

import java.util.ArrayList;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class WishlistController extends BaseController<Wishlist> {

    // Reference to the main container for navigation
    @FXML
    private FlowPane wishlistFlowPane;
    // Removed FXML backButton
    @FXML
    private Button addAllToPanierButton;
    @FXML
    private NavBarController navBarController; // Injected from FXML include

    private final WishlistService wishlistService = new WishlistService();
    private final PanierService panierService = new PanierService();
    private List<Wishlist> wishlistItems = new ArrayList<>();

    @Override
    public void setCurrentUser(User user) {
        super.setCurrentUser(user);
        
        // Also update the singleton UserSession
        UserSession.getInstance().setUser(user);
        
        if (user != null) {
            System.out.println("Wishlist: Current user set to " + user.getNom() + " " + user.getPrenom() + " (ID: " + user.getId() + ")");
        } else {
            System.out.println("Wishlist: No user logged in");
        }
        
        loadWishlist();
    }
    
    /**
     * Finds the contentPane StackPane in the parent hierarchy
     * @return the contentPane or null if not found
     */
    private javafx.scene.layout.StackPane findContentPane() {
        // Navigate up the scene graph to find the contentPane
        javafx.scene.Parent current = wishlistFlowPane.getParent();
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
                    
                    javafx.stage.Stage stage = (javafx.stage.Stage) wishlistFlowPane.getScene().getWindow();
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        initializeUserService();
        
        // Get current user from session
        User currentUser = UserSession.getInstance().getCurrentUser();
        
        // Initialize NavBarController
        if (navBarController != null) {
            navBarController.setCurrentUser(currentUser);
            navBarController.showBackButton(true); // Show back button in wishlist view
            navBarController.showFilters(false); // Hide filters in wishlist view
            navBarController.updateButtonCounts();
            
            // Set up back button handler
            navBarController.setBackButtonAction(event -> handleBack());
        } else {
            System.err.println("Warning: navBarController is null in WishlistController");
        }
        
        // Set up button actions
        addAllToPanierButton.setOnAction(e -> handleAddAllToPanier());
        
        // Load wishlist data
        loadWishlist();
    }
    
    // initializeServices method removed as we now call initializeUserService directly

    // goBack() method is removed, navBarController handles back navigation.

    private void handleAddAllToPanier() {
        if (wishlistItems.isEmpty()) {
            UIUtils.showToastNotification("Wishlist is empty.", "red");
            return;
        }
        
        // Get current user from session
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            UIUtils.showAlert("Login Required", null, "Please log in to add items to your cart.", "WARNING");
            return;
        }
        
        int successCount = 0;
        List<Wishlist> toRemoveList = new ArrayList<>();
        for (Wishlist wishlist : wishlistItems) {
            try {
                panierService.addProductToPanier(currentUser.getId(), wishlist.getProduit(), 1);
                toRemoveList.add(wishlist);
                successCount++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (Wishlist w : toRemoveList) {
            wishlistService.supprimer(w);
            wishlistItems.remove(w);
        }
        displayWishlist(); // Refresh the displayed wishlist
        if (navBarController != null) { // Update counts in navbar
            navBarController.updateButtonCounts();
            navBarController.animatePanierButton(); // Animate panier button if items were added
        }
        if (successCount == toRemoveList.size() && successCount > 0) {
            UIUtils.showToastNotification("All items added to panier.");
        } else if (successCount > 0) {
            UIUtils.showToastNotification(successCount + " items added to panier. Some failed.", "orange");
        } else if (!wishlistItems.isEmpty() && successCount == 0) { // Check if wishlist was not empty to begin with
            UIUtils.showToastNotification("Failed to add items to panier.", "red");
        }
        // If wishlist was empty initially, no message or a specific one like "Wishlist was empty"
    }

    private void loadWishlist() {
        // Get current user from session
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            wishlistFlowPane.getChildren().clear(); // Clear display if no user
            wishlistFlowPane.getChildren().add(new Label("Please log in to see your wishlist."));
            return;
        }
        
        wishlistItems.clear();
        List<Wishlist> allWishlist = wishlistService.recuperer(); // This should ideally be wishlistService.recupererByUserId(currentUser.getId());
        for (Wishlist w : allWishlist) {
            if (w.getUser() != null && w.getUser().getId() == currentUser.getId() && w.getProduit() != null) {
                wishlistItems.add(w);
            }
        }
        displayWishlist();
    }

    private void displayWishlist() {
        wishlistFlowPane.getChildren().clear();

        // Filter duplicates by product ID
        List<Integer> seenProductIds = new ArrayList<>();
        List<Wishlist> uniqueWishlistItems = new ArrayList<>();
        for (Wishlist wishlist : wishlistItems) {
            int productId = wishlist.getProduit().getId();
            if (!seenProductIds.contains(productId)) {
                seenProductIds.add(productId);
                uniqueWishlistItems.add(wishlist);
            }
        }

        for (Wishlist wishlist : uniqueWishlistItems) {
            VBox card = createWishlistListItem(wishlist);
            wishlistFlowPane.getChildren().add(card);
        }
        if (uniqueWishlistItems.isEmpty() && currentUser != null) {
            VBox emptyStateContainer = new VBox(15);
            emptyStateContainer.setAlignment(Pos.CENTER);
            emptyStateContainer.setStyle("-fx-padding: 50px;");

            Label emptyLabel = new Label("Your Wishlist is Currently Empty");
            emptyLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #343a40; -fx-font-weight: bold;");
            
            Label subtextLabel = new Label("Looks like you haven't added any products yet. Start exploring!");
            subtextLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");

            Button browseButton = new Button("Browse Products");
            browseButton.getStyleClass().add("button"); // Use primary button style
            browseButton.setOnAction(e -> navBarController.navigateToProductCardView()); // Reuse existing nav method

            emptyStateContainer.getChildren().addAll(emptyLabel, subtextLabel, browseButton);
            wishlistFlowPane.getChildren().add(emptyStateContainer);
            wishlistFlowPane.setAlignment(Pos.CENTER); // Center the empty state message
        } else {
            wishlistFlowPane.setAlignment(Pos.TOP_LEFT); // Reset alignment if there are items
        }
        addAllToPanierButton.setDisable(uniqueWishlistItems.isEmpty()); // Disable button if wishlist is empty
    }

    private VBox createWishlistListItem(Wishlist wishlist) {
        Produit produit = wishlist.getProduit();

        VBox card = new VBox();
        card.getStyleClass().add("wishlist-item-card"); // Apply the main card style
        // card.setPadding(new Insets(10)); // Padding is now handled by CSS
        // card.setPrefWidth(760); // Width can be managed by FlowPane or CSS if needed

        HBox content = new HBox(15); // Spacing for content within the card
        content.setAlignment(Pos.CENTER_LEFT);
        content.setStyle("-fx-padding: 5px;"); // Inner padding for content

        ImageView imageView = new ImageView();
        imageView.setFitWidth(100);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);

        Image image = ImageUtils.loadImage(produit.getImage());
        if (image != null) {
            imageView.setImage(image);
        } else {
            imageView.setImage(new Image("file:src/main/resources/images/default_renewable.png"));
        }

        VBox details = new VBox(5);
        Label nameLabel = new Label(produit.getNom());
        nameLabel.getStyleClass().add("product-name-wl");

        Label descriptionLabel = new Label(produit.getDescription());
        descriptionLabel.getStyleClass().add("product-description-wl");
        // descriptionLabel.setWrapText(true); // Handled by CSS if needed
        // descriptionLabel.setMaxWidth(400); // Handled by CSS if needed

        Label priceLabel = new Label(String.format("%.2f DT", produit.getPrix()));
        priceLabel.getStyleClass().add("product-price-wl");

        details.getChildren().addAll(nameLabel, descriptionLabel, priceLabel);

        HBox.setHgrow(details, Priority.ALWAYS);

        VBox buttonContainer = new VBox(10);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);

        Button addToCartButton = new Button("Add to Panier");
        addToCartButton.getStyleClass().addAll("button", "btn-add-to-panier-wl"); // Apply general button and specific style
        if (produit.getQuantiteStock() <= 0) {
            addToCartButton.setDisable(true);
        }
        addToCartButton.setOnAction(event -> handleAddToPanier(produit));

        Button removeBtn = new Button("Remove");
        removeBtn.getStyleClass().addAll("button", "btn-remove-wl"); // Apply general button and specific style
        removeBtn.setOnAction(event -> {
            // Create a fade-out transition for the card
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), card);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(finishEvent -> {
                // Actions to perform after the animation finishes
                wishlistService.supprimer(wishlist);
                wishlistItems.remove(wishlist);
                // displayWishlist(); // No longer call displayWishlist directly, just remove the node
                wishlistFlowPane.getChildren().remove(card); // Remove the card from the FlowPane
                if (wishlistItems.isEmpty()) { // If it was the last item, refresh to show empty state
                    displayWishlist();
                }
                if (navBarController != null) { // Update counts in navbar
                    navBarController.updateButtonCounts();
                    navBarController.animateWishlistButton();
                }
                UIUtils.showToastNotification(produit.getNom() + " removed from wishlist.", "red");
                // Check if the wishlist is now empty and update addAllToPanierButton state
                addAllToPanierButton.setDisable(wishlistItems.isEmpty());
            });
            fadeOut.play(); // Start the animation
        });

        buttonContainer.getChildren().addAll(addToCartButton, removeBtn);

        content.getChildren().addAll(imageView, details, buttonContainer);
        card.getChildren().add(content);

        return card;
    }

    private void handleAddToPanier(Produit produit) {
        // Get current user from session
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            UIUtils.showAlert("Login Required", null, "Please log in to add items to your cart.", "WARNING");
            return;
        }

        Wishlist toRemove = null;
        for (Wishlist w : wishlistItems) {
            if (w.getProduit() != null && w.getProduit().getId() == produit.getId()) {
                toRemove = w;
                break;
            }
        }
        if (toRemove != null) {
            wishlistService.supprimer(toRemove); // Assumes this works by object or ID
            wishlistItems.remove(toRemove);
            // displayWishlist(); // Refresh display after removal from list
        }
        // Panier panier = panierService.getPanierByUserId(currentUser.getId()); // Not strictly needed here
        panierService.addProductToPanier(currentUser.getId(), produit, 1);
        displayWishlist(); // Refresh display, which will also update addAllToPanierButton state
        if (navBarController != null) { // Update counts in navbar
            navBarController.updateButtonCounts();
            navBarController.animatePanierButton();
        }
        UIUtils.showToastNotification(produit.getNom() + " added to panier.");
    }
}

