package com.esprit.controllers;

import com.esprit.Services.CategorieService;
import com.esprit.Services.PanierService;
import com.esprit.Services.WishlistService;
import com.esprit.models.Categorie;
import com.esprit.models.User;
import com.esprit.utils.UIUtils;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class NavBarController {

    @FXML
    private Button backButton;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private TextField searchTextField;
    @FXML
    private Button clearFilterButton;
    @FXML
    private Button panierButton;
    @FXML
    private Button wishlistButton;

    private final CategorieService categorieService = new CategorieService();
    private final PanierService panierService = new PanierService();
    private final WishlistService wishlistService = new WishlistService();
    private User currentUser; // Set this from the parent controller

    // We'll find the content pane in the parent hierarchy instead of storing a reference
    
    private Map<String, Integer> categoryNameToIdMap = new HashMap<>();

    // Callbacks for product filtering
    private Runnable filterCallback;
    private Consumer<String> categoryChangeCallback;
    private Consumer<String> searchChangeCallback;


    public void initialize() {
        // Initialize services
        
        // currentUser should be set by the parent controller before this is fully used
        // For now, let's assume a default or it will be set.
        // If currentUser is null, counts might not work initially.
        
        setupActionButtons();
        loadCategories();
        updateButtonCounts(); // Initial counts

        // Back button action
        backButton.setOnAction(event -> navigateToProductCardView());

        // Filter actions
        clearFilterButton.setOnAction(event -> {
            categoryComboBox.setValue("All Categories");
            searchTextField.clear();
            if (filterCallback != null) {
                filterCallback.run();
            }
        });

        categoryComboBox.setOnAction(event -> {
            if (categoryChangeCallback != null) {
                categoryChangeCallback.accept(categoryComboBox.getValue());
            }
        });

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (searchChangeCallback != null) {
                searchChangeCallback.accept(newValue);
            }
        });
    }
    
    /**
     * Sets the current user for this controller.
     * This method should be called after controller initialization.
     * 
     * @param user The current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateButtonCounts();
        
        // Navigation buttons are always visible in client UI
        panierButton.setVisible(true);
        wishlistButton.setVisible(true);
        
        System.out.println("Navigation buttons set to visible");
    }

    public void setFilterCallback(Runnable callback) {
        this.filterCallback = callback;
    }
    
    public void setCategoryChangeCallback(Consumer<String> callback) {
        this.categoryChangeCallback = callback;
    }

    public void setSearchChangeCallback(Consumer<String> callback) {
        this.searchChangeCallback = callback;
    }
    


    public String getSelectedCategory() {
        return categoryComboBox.getValue();
    }

    public Integer getSelectedCategoryId() {
        return categoryNameToIdMap.get(getSelectedCategory());
    }

    public String getSearchText() {
        return searchTextField.getText();
    }
    
    public ComboBox<String> getCategoryComboBox() {
        return categoryComboBox;
    }

    public TextField getSearchTextField() {
        return searchTextField;
    }
    
    public Button getClearFilterButton() {
        return clearFilterButton;
    }
    
    /**
     * Sets a custom action for the back button
     * @param handler The event handler to run when the back button is clicked
     */
    public void setBackButtonAction(javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        backButton.setOnAction(handler);
    }


    private void setupActionButtons() {
        // For the panier button, first try to find a parent ProductCardController
        panierButton.setOnAction(e -> {
            // Try to find the parent controller
            javafx.scene.Parent current = panierButton.getParent();
            while (current != null) {
                if (current.getUserData() instanceof ProductCardController) {
                    // Found a ProductCardController, use its handlePanier method
                    ProductCardController controller = (ProductCardController) current.getUserData();
                    controller.handlePanier();
                    return;
                }
                current = current.getParent();
            }
            
            // Fallback to traditional navigation
            navigateToView("/PanierView.fxml", "Panier", PanierController.class);
        });
        
        // For the wishlist button, first try to find a parent ProductCardController
        wishlistButton.setOnAction(e -> {
            // Try to find the parent controller
            javafx.scene.Parent current = wishlistButton.getParent();
            while (current != null) {
                if (current.getUserData() instanceof ProductCardController) {
                    // Found a ProductCardController, use its handleWishlist method
                    ProductCardController controller = (ProductCardController) current.getUserData();
                    controller.handleWishlist();
                    return;
                }
                current = current.getParent();
            }
            
            // Fallback to traditional navigation
            navigateToView("/WishlistView.fxml", "Wishlist", WishlistController.class);
        });
    }

    /**
     * Finds the contentPane StackPane in the parent hierarchy
     * @return the contentPane or null if not found
     */
    private javafx.scene.layout.StackPane findContentPane() {
        // Navigate up the scene graph to find the contentPane
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
    
    /**
     * Navigates to a specified view and passes the current user to the controller.
     * Supports both content pane-based navigation and fallback to traditional scene switching.
     * 
     * @param fxmlPath The path to the FXML file
     * @param title The title for the stage
     * @param controllerClass The class of the controller for the view
     */
    private void navigateToView(String fxmlPath, String title, Class<?> controllerClass) {
        try {
            // Find the contentPane in the parent hierarchy
            javafx.scene.layout.StackPane contentPane = findContentPane();
            if (contentPane == null) {
                System.err.println("Could not find contentPane in parent hierarchy");
                
                // Fallback to traditional scene switching
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                Object controller = loader.getController();
                
                // Pass current user to the controller
                try {
                    // Check if controller has setCurrentUser method
                    java.lang.reflect.Method setCurrentUserMethod = 
                        controller.getClass().getMethod("setCurrentUser", User.class);
                    // If method exists, invoke it
                    setCurrentUserMethod.invoke(controller, currentUser);
                    System.out.println("Successfully set current user on " + controller.getClass().getSimpleName());
                } catch (Exception e) {
                    System.err.println("Warning: Could not set current user on " + 
                        controller.getClass().getSimpleName() + ": " + e.getMessage());
                    // Continue anyway, as this is not a critical error
                }
                
                Stage stage = (Stage) panierButton.getScene().getWindow();
                stage.setTitle(title);
                stage.getScene().setRoot(root);
                return;
            }
            
            // Load the view
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Configure the controller
            Object controller = loader.getController();
            
            // Pass current user to the controller
            try {
                // Check if controller has setCurrentUser method
                java.lang.reflect.Method setCurrentUserMethod = 
                    controller.getClass().getMethod("setCurrentUser", User.class);
                // If method exists, invoke it
                setCurrentUserMethod.invoke(controller, currentUser);
                System.out.println("Successfully set current user on " + controller.getClass().getSimpleName());
            } catch (Exception e) {
                System.err.println("Warning: Could not set current user on " + 
                    controller.getClass().getSimpleName() + ": " + e.getMessage());
                // Continue anyway, as this is not a critical error
            }
            
            // Replace the content in the contentPane
            contentPane.getChildren().setAll(root);
        } catch (IOException ex) {
            ex.printStackTrace();
            UIUtils.showAlert("Navigation Error", null, "Failed to open " + title + " view.", "ERROR");
        }
    }
    
    /**
     * Navigates to the product card view and passes the current user to the controller.
     * Uses the centralized MainprogGUI.loadContentFragment method to preserve tab selection.
     */
    public void navigateToProductCardView() { // Changed to public
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
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProductCardView.fxml"));
                    Parent root = loader.load();
                    
                    // Pass current user to the new controller using reflection
                    Object controller = loader.getController();
                    try {
                        // Check if controller has setCurrentUser method
                        java.lang.reflect.Method setCurrentUserMethod = 
                            controller.getClass().getMethod("setCurrentUser", User.class);
                        // If method exists, invoke it
                        setCurrentUserMethod.invoke(controller, currentUser);
                        System.out.println("Successfully set current user on " + controller.getClass().getSimpleName());
                    } catch (Exception e) {
                        System.err.println("Warning: Could not set current user on " + 
                            controller.getClass().getSimpleName() + ": " + e.getMessage());
                        // Continue anyway, as this is not a critical error
                    }
                    
                    Stage stage = (Stage) backButton.getScene().getWindow();
                    stage.setTitle("Products");
                    stage.getScene().setRoot(root);
                    return;
                }
                
                // Load the ProductCardView
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProductCardView.fxml"));
                Parent root = loader.load();
                
                // Configure the controller
                Object controller = loader.getController();
                
                // Pass current user to the controller
                try {
                    // Check if controller has setCurrentUser method
                    java.lang.reflect.Method setCurrentUserMethod = 
                        controller.getClass().getMethod("setCurrentUser", User.class);
                    // If method exists, invoke it
                    setCurrentUserMethod.invoke(controller, currentUser);
                    System.out.println("Successfully set current user on " + controller.getClass().getSimpleName());
                } catch (Exception e) {
                    System.err.println("Warning: Could not set current user on " + 
                        controller.getClass().getSimpleName() + ": " + e.getMessage());
                    // Continue anyway, as this is not a critical error
                }
                
                // Replace the content in the contentPane
                contentPane.getChildren().setAll(root);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            UIUtils.showAlert("Navigation Error", null, "Failed to open Product view.", "ERROR");
        }
    }

    private void loadCategories() {
        categoryNameToIdMap.clear();
        List<Categorie> categories = categorieService.get();
        categoryComboBox.getItems().clear();
        categoryComboBox.getItems().add("All Categories");
        for (Categorie cat : categories) {
            categoryComboBox.getItems().add(cat.getNom());
            categoryNameToIdMap.put(cat.getNom(), cat.getId());
        }
        categoryComboBox.setValue("All Categories");
    }

    public void updateButtonCounts() {
        if (currentUser != null) {
            int panierItemCount = panierService.getPanierItemCount(currentUser.getId());
            panierButton.setText(String.format("🛒 Panier (%d)", panierItemCount));

            int wishlistCount = wishlistService.getWishlistCountForUser(currentUser.getId());
            wishlistButton.setText(String.format("❤️ Wishlist (%d)", wishlistCount));
        } else {
            panierButton.setText("🛒 Panier (0)");
            wishlistButton.setText("❤️ Wishlist (0)");
        }
    }
    
    public void animatePanierButton() {
        animateButton(panierButton);
    }

    public void animateWishlistButton() {
        animateButton(wishlistButton);
    }

    private void animateButton(Button button) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.2);
        st.setToY(1.2);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.play();
    }

    public void showBackButton(boolean show) {
        backButton.setVisible(show);
        backButton.setManaged(show); // Ensures layout respects visibility
    }

    public void showFilters(boolean show) {
        categoryComboBox.setVisible(show);
        categoryComboBox.setManaged(show);
        searchTextField.setVisible(show);
        searchTextField.setManaged(show);
        clearFilterButton.setVisible(show);
        clearFilterButton.setManaged(show);
    }
}