package com.esprit.controllers;

import com.esprit.models.Produit;
import com.esprit.models.User;
import com.esprit.Services.ProduitService;
import com.esprit.tests.MainprogGUI;
import com.esprit.utils.ImageUtils;
import com.esprit.utils.UIUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.TextFormatter;

public class ProductFormController {
    public enum Mode {
        ADD, EDIT
    }

    @FXML private TextField nameField;
    @FXML private TextField descriptionField;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private CheckBox recyclableCheck;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField companyNameField;
    @FXML private ImageView productImage;
    @FXML private ImageView companyLogoImage;
    @FXML private Button saveButton;
    
    private Produit produit;
    private User currentUser;
    private boolean saved = false;
    private Mode mode;
    private Map<String, Integer> categoryMap = new HashMap<>();
    private File imageFile;
    private File companyLogoFile;

    private ProductDashboardController dashboardController;

    private StackPane parentContainer;

    public void setDashboardController(ProductDashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    
    public void setParentContainer(StackPane parentContainer) {
        this.parentContainer = parentContainer;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        // Show or hide company fields based on user role
        boolean isProductOwner = user != null && "PRODUCT_OWNER".equals(user.getRole());
        companyNameField.setVisible(isProductOwner);
        companyNameField.setManaged(isProductOwner);
        
        // Show/hide company logo fields
        if (companyLogoImage != null) {
            companyLogoImage.getParent().setVisible(isProductOwner);
            companyLogoImage.getParent().setManaged(isProductOwner);
        }
        
        // If product owner, pre-fill company name if editing existing product
        if (isProductOwner && produit != null && mode == Mode.EDIT) {
            companyNameField.setText(produit.getCompanyName());
        }
    }


    public void setProduct(Produit produit) {
        this.produit = produit;
        if (mode == Mode.EDIT) {
            nameField.setText(produit.getNom());
            descriptionField.setText(produit.getDescription());
            priceField.setText(String.format("%.2f", produit.getPrix()));
            stockField.setText(String.valueOf(produit.getQuantiteStock()));
            recyclableCheck.setSelected(produit.isRecyclable());
            companyNameField.setText(produit.getCompanyName());
            
            categoryMap.forEach((name, id) -> {
                if (id == produit.getCategorieId()) {
                    categoryCombo.getSelectionModel().select(name);
                }
            });
            
            // Load company logo if available
            if (produit.getCompanyLogo() != null && !produit.getCompanyLogo().isEmpty()) {
                String logoFileName = produit.getCompanyLogo().trim();
                java.io.File logoFile = new java.io.File("src/main/resources/images/logos/" + logoFileName);
                if (logoFile.exists() && logoFile.isFile()) {
                    companyLogoImage.setImage(ImageUtils.loadImage("logos/" + logoFileName));
                } else {
                    companyLogoImage.setImage(ImageUtils.loadImage("default_logo.png"));
                }
            }

            if (produit.getImage() != null && !produit.getImage().isEmpty()) {
                String imageFileName = produit.getImage().trim();
                java.io.File imgFile = new java.io.File("src/main/resources/images/products/" + imageFileName);
                if (imgFile.exists() && imgFile.isFile()) {
                    productImage.setImage(ImageUtils.loadImage(imageFileName));
                } else {
                    productImage.setImage(ImageUtils.loadImage("default_renewable.png"));
                }
            }
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode == Mode.ADD) {
            this.produit = new Produit(0, "", "", 0.0, 0, "", false, 0);
        }
    }

    public void setCategories(Map<String, Integer> categories) {
        this.categoryMap = categories;
        categoryCombo.getItems().clear();
        categoryCombo.getItems().addAll(categories.keySet());
    }

    public void initialize() {
        // Real-time validation listeners
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        priceField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        stockField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        categoryCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        companyNameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());

        // Restrict priceField to valid decimal numbers
        priceField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*(\\.\\d{0,2})?")) {
                return change;
            }
            return null;
        }));
        // Restrict stockField to integers only
        stockField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        }));
        validateForm();
    }

    private void validateForm() {
        boolean valid = true;
        // Name validation
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            setInvalid(nameField, true);
            valid = false;
        } else {
            setInvalid(nameField, false);
        }
        // Description validation
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
            setInvalid(descriptionField, true);
            valid = false;
        } else {
            setInvalid(descriptionField, false);
        }
        // Company name validation for product owners
        if (companyNameField.isVisible() && (companyNameField.getText() == null || companyNameField.getText().trim().isEmpty())) {
            setInvalid(companyNameField, true);
            valid = false;
        } else {
            setInvalid(companyNameField, false);
        }
        // Price validation
        String priceText = priceField.getText();
        if (priceText == null || priceText.trim().isEmpty()) {
            setInvalid(priceField, true);
            valid = false;
        } else {
            try {
                double price = Double.parseDouble(priceText);
                if (price <= 0) {
                    setInvalid(priceField, true);
                    valid = false;
                } else {
                    setInvalid(priceField, false);
                }
            } catch (Exception e) {
                setInvalid(priceField, true);
                valid = false;
            }
        }
        // Stock validation
        String stockText = stockField.getText();
        if (stockText == null || stockText.trim().isEmpty()) {
            setInvalid(stockField, true);
            valid = false;
        } else {
            try {
                int stock = Integer.parseInt(stockText);
                if (stock <= 0) {
                    setInvalid(stockField, true);
                    valid = false;
                } else {
                    setInvalid(stockField, false);
                }
            } catch (Exception e) {
                setInvalid(stockField, true);
                valid = false;
            }
        }
        // Category validation
        if (categoryCombo.getValue() == null || categoryCombo.getValue().trim().isEmpty() || !categoryMap.containsKey(categoryCombo.getValue())) {
            categoryCombo.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2px; -fx-background-color: #fff0f3;");
            valid = false;
        } else {
            categoryCombo.setStyle("");
        }
        if (saveButton != null) {
            saveButton.setDisable(!valid);
        }
    }

    private void setInvalid(TextField field, boolean invalid) {
        if (invalid) {
            field.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2px; -fx-background-color: #fff0f3;");
        } else {
            field.setStyle("");
        }
    }

    @FXML
    private void handleSave() {
        // Input validation
        String name = nameField.getText();
        String description = descriptionField.getText();
        String priceText = priceField.getText();
        String stockText = stockField.getText();
        String selectedCategory = categoryCombo.getValue();
        String companyName = companyNameField.getText();

        if (name == null || name.trim().isEmpty()) {
            UIUtils.showAlert("Validation Error", null, "Name is required.", "ERROR");
            return;
        }
        if (description == null || description.trim().isEmpty()) {
            UIUtils.showAlert("Validation Error", null, "Description is required.", "ERROR");
            return;
        }
        if (priceText == null || priceText.trim().isEmpty()) {
            UIUtils.showAlert("Validation Error", null, "Price is required.", "ERROR");
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price <= 0) {
                UIUtils.showAlert("Validation Error", null, "Price cannot be negative or zero.", "ERROR");
                return;
            }
        } catch (NumberFormatException e) {
            UIUtils.showAlert("Validation Error", null, "Price must be a valid number.", "ERROR");
            return;
        }
        if (stockText == null || stockText.trim().isEmpty()) {
            UIUtils.showAlert("Validation Error", null, "Stock quantity is required.", "ERROR");
            return;
        }
        int stock;
        try {
            stock = Integer.parseInt(stockText);
            if (stock <= 0) {
                UIUtils.showAlert("Validation Error", null, "Stock quantity cannot be negative or zero.", "ERROR");
                return;
            }
        } catch (NumberFormatException e) {
            UIUtils.showAlert("Validation Error", null, "Stock quantity must be a valid integer.", "ERROR");
            return;
        }
        if (selectedCategory == null || selectedCategory.trim().isEmpty()) {
            UIUtils.showAlert("Validation Error", null, "Category must be selected.", "ERROR");
            return;
        }
        if (!categoryMap.containsKey(selectedCategory)) {
            UIUtils.showAlert("Validation Error", null, "Selected category is invalid.", "ERROR");
            return;
        }

        // Set product fields after validation
        produit.setNom(name);
        produit.setDescription(description);
        produit.setPrix(price);
        produit.setQuantiteStock(stock);
        produit.setRecyclable(recyclableCheck.isSelected());
        produit.setCategorieId(categoryMap.get(selectedCategory));
        produit.setCompanyName(companyName);
        
        // Set owner ID if current user is a product owner
        if (currentUser != null && "PRODUCT_OWNER".equals(currentUser.getRole())) {
            produit.setOwnerId(currentUser.getId());
        }
        
        // Save company logo if available
        if (companyLogoFile != null) {
            if (produit.getCompanyLogo() != null && !produit.getCompanyLogo().isEmpty() && !produit.getCompanyLogo().equals("default_logo.png")) {
                ImageUtils.deleteCompanyLogo(produit.getCompanyLogo());
            }
            String logoPath = ImageUtils.saveCompanyLogo(companyLogoFile, produit.getCompanyName());
            produit.setCompanyLogo(logoPath);
        }

        ProduitService produitService = new ProduitService();

        if (imageFile != null) {
            if (produit.getImage() != null && !produit.getImage().isEmpty() && !produit.getImage().equals("default_renewable.png")) {
                ImageUtils.deleteProductImage(produit.getImage());
            }
            String imagePath = ImageUtils.saveProductImage(imageFile, produit.getNom());
            produit.setImage(imagePath);
        }

        if (mode == Mode.ADD) {
            produitService.ajouter(produit);
            UIUtils.showSuccessAlert("Success", null, "Product '" + produit.getNom() + "' added successfully.");
        } else {
            produitService.modifier(produit);
            UIUtils.showSuccessAlert("Success", null, "Product '" + produit.getNom() + "' modified successfully.");
        }

        saved = true;

        if (dashboardController != null) {
            dashboardController.loadProductData();
        }

        backToDashboard();
    }

    @FXML
    private void handleBack() {
        backToDashboard();
    }

    private void backToDashboard() {
        try {
            if (parentContainer != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProductDashboardView.fxml"));
                Parent dashboard = loader.load();

                ProductDashboardController controller = loader.getController();
                controller.setCategoryMap(this.categoryMap);
                controller.setParentContainer(parentContainer);
                controller.loadProductData();

                parentContainer.getChildren().setAll(dashboard);
            } else {
                MainprogGUI app = MainprogGUI.getInstance();
                app.loadContentFragment("/ProductDashboardView.fxml", (ProductDashboardController controller) -> {
                    controller.setCategoryMap(this.categoryMap);
                    controller.loadProductData();
                });
            }
        } catch (Exception e) {
            UIUtils.showAlert("Navigation Error", null, "Failed to return to dashboard: " + e.getMessage(), "ERROR");
        }
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        imageFile = fileChooser.showOpenDialog(productImage.getScene().getWindow());
        
        if (imageFile != null) {
            productImage.setImage(new Image(imageFile.toURI().toString()));
        }
    }
    
    @FXML
    private void handleBrowseCompanyLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Company Logo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        companyLogoFile = fileChooser.showOpenDialog(companyLogoImage.getScene().getWindow());
        
        if (companyLogoFile != null) {
            companyLogoImage.setImage(new Image(companyLogoFile.toURI().toString()));
        }
    }

    @FXML
    private void handleDeleteImage() {
        if (produit != null && produit.getImage() != null && !produit.getImage().isEmpty() && !"default_renewable.png".equals(produit.getImage())) {
            ImageUtils.deleteProductImage(produit.getImage());
        }
        imageFile = null;
        productImage.setImage(ImageUtils.loadImage("default_renewable.png"));
        if (produit != null) {
            produit.setImage("default_renewable.png");
            ProduitService produitService = new ProduitService();
            produitService.modifier(produit);
            if (dashboardController != null) {
                dashboardController.loadProductData();
            }
        }
    }
    
    @FXML
    private void handleDeleteCompanyLogo() {
        if (produit != null && produit.getCompanyLogo() != null && !produit.getCompanyLogo().isEmpty() && !"default_logo.png".equals(produit.getCompanyLogo())) {
            ImageUtils.deleteCompanyLogo(produit.getCompanyLogo());
        }
        companyLogoFile = null;
        companyLogoImage.setImage(ImageUtils.loadImage("default_logo.png"));
        if (produit != null) {
            produit.setCompanyLogo("default_logo.png");
            ProduitService produitService = new ProduitService();
            produitService.modifier(produit);
            if (dashboardController != null) {
                dashboardController.loadProductData();
            }
        }
    }
}

