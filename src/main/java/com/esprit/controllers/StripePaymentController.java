package com.esprit.controllers;

import com.esprit.Services.*;
import com.esprit.models.*;
import com.esprit.utils.UIUtils;
import com.esprit.utils.UserSession;
import java.sql.SQLException;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StripePaymentController {
    private final Stage ownerStage;
    private User currentUser;
    private final String checkoutUrl;
    private final String shipmentAddress;
    private final String paperSize;
    
    // Services
    private final CommandeService commandeService = new CommandeService();
    private final CommandeProduitService commandeProduitService = new CommandeProduitService();
    private final PanierService panierService = new PanierService();
    private final ProduitService produitService = new ProduitService();
    private UserService userService;
    private final PDFInvoiceService pdfService = new PDFInvoiceService();
    private final N8nWebhookService whatsappService = new N8nWebhookService("http://localhost:3000/api/sendText");

    public StripePaymentController(Stage ownerStage, int userId, String checkoutUrl, 
                                 String shipmentAddress, String paperSize) throws SQLException {
        this.ownerStage = ownerStage;
        
        // Initialize UserService
        try {
            this.userService = new UserService();
        } catch (SQLException e) {
            System.err.println("StripePayment: Failed to initialize UserService: " + e.getMessage());
            throw e; // Re-throw the exception to be handled by the caller
        }
        
        // Get current user from session
        this.currentUser = UserSession.getInstance().getCurrentUser();
        if (this.currentUser == null) {
            try {
                this.currentUser = userService.getById(userId);
                System.out.println("StripePayment: Loaded user from ID: " + userId);
            } catch (Exception e) {
                System.err.println("StripePayment: Failed to load user: " + e.getMessage());
            }
        } else {
            System.out.println("StripePayment: Using current user from session: " + currentUser.getNom() + " " + currentUser.getPrenom());
        }
        this.checkoutUrl = checkoutUrl;
        this.shipmentAddress = shipmentAddress;
        this.paperSize = paperSize;
    }

    public void showPaymentWindow() {
        try {
            // Apply blur effect to owner stage
            Scene ownerScene = ownerStage.getScene();
            GaussianBlur blur = new GaussianBlur(10);
            ownerScene.getRoot().setEffect(blur);

            // Create payment stage
            Stage paymentStage = new Stage();
            paymentStage.setTitle("Stripe Payment");
            
            WebView webView = new WebView();
            webView.getEngine().load(checkoutUrl);

            webView.getEngine().locationProperty().addListener((obs, oldLocation, newLocation) -> {
                if (newLocation.contains("success")) {
                    handlePaymentSuccess();
                    paymentStage.close();
                    ownerScene.getRoot().setEffect(null);
                } else if (newLocation.contains("cancel")) {
                    UIUtils.showAlert("Payment Cancelled", null, "Payment was cancelled.", "WARNING");
                    paymentStage.close();
                    ownerScene.getRoot().setEffect(null);
                }
            });

            paymentStage.setScene(new Scene(webView, 800, 600));
            paymentStage.initOwner(ownerStage);
            paymentStage.initModality(Modality.APPLICATION_MODAL);
            paymentStage.show();

            paymentStage.setOnCloseRequest(event -> {
                ownerScene.getRoot().setEffect(null);
            });

        } catch (Exception e) {
            UIUtils.showAlert("Error", "Payment Error", "Failed to process payment: " + e.getMessage(), "ERROR");
        }
    }

    private void handlePaymentSuccess() {
        try {
            // 1. Create the order
            Commande commande = createOrder();
            
            // 2. Add products to order and update stock
            addProductsToOrder(commande);
            
            // 3. Generate PDF invoice
           String pdfpath = pdfService.generateInvoicePDF(commande, "STANDARD", paperSize);
            
            // 4. Send WhatsApp notification
            sendWhatsAppNotification(commande,pdfpath);
            
            // 5. Clear cart
            panierService.clearPanier(currentUser.getId());

            // 6. Show toast notification
            UIUtils.showToastNotification("You will receive WhatsApp message");

            // 7. Replace CheckoutValidationView fragment with ProductCardView fragment
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
                javafx.scene.Scene scene = ownerStage.getScene();
                javafx.scene.Parent root = scene.getRoot();

                // Find contentPane StackPane in the scene graph
                javafx.scene.layout.StackPane contentPane = null;
                javafx.scene.Parent current = root;
                while (current != null) {
                    if (current instanceof javafx.scene.layout.BorderPane) {
                        javafx.scene.layout.BorderPane borderPane = (javafx.scene.layout.BorderPane) current;
                        if (borderPane.getCenter() instanceof javafx.scene.layout.StackPane) {
                            contentPane = (javafx.scene.layout.StackPane) borderPane.getCenter();
                            break;
                        }
                    }
                    current = current.getParent();
                }

                if (contentPane != null) {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/ProductCardView.fxml"));
                    javafx.scene.Parent productCardRoot = loader.load();

                    com.esprit.controllers.ProductCardController controller = loader.getController();
                    controller.setCurrentUser(currentUser);

                    // Replace the content in contentPane with product card view
                    contentPane.getChildren().setAll(productCardRoot);
                } else {
                    // Fallback: replace entire scene root
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/ProductCardView.fxml"));
                    javafx.scene.Parent productCardRoot = loader.load();

                    com.esprit.controllers.ProductCardController controller = loader.getController();
                    controller.setCurrentUser(currentUser);

                    scene.setRoot(productCardRoot);
                }
            }
                 
        } catch (Exception e) {
            UIUtils.showAlert("Error", "Order Processing Failed", e.getMessage(), "ERROR");
        }
    }

    private Commande createOrder() {
        Commande commande = new Commande();
        commande.setDate(new java.util.Date());
        commande.setStatut("Paid");
        commande.setClientId(currentUser.getId());
        commande.setShipmentAddress(shipmentAddress);
        commandeService.ajouter(commande);
        return commande;
    }

    private void addProductsToOrder(Commande commande) throws Exception {
        Panier panier = panierService.getPanierByUserId(currentUser.getId());
        
        for (PanierProduit panierProduit : panier.getProduits()) {
            // Create order product
            CommandeProduit cp = new CommandeProduit();
            cp.setCommandeId(commande.getId());
            cp.setProduitId(panierProduit.getProduit().getId());
            cp.setQuantite(panierProduit.getQuantite());
            cp.setSousTotal(panierProduit.getSousTotal());
            commandeProduitService.ajouter(cp);

            // Update product stock
            Produit produit = produitService.getProduitById(panierProduit.getProduit().getId());
            produit.setQuantiteStock(produit.getQuantiteStock() - panierProduit.getQuantite());
            produitService.modifier(produit);

            // Check stock level
            if (produit.getQuantiteStock() <= 15) {
                sendAdminStockAlert(produit);
            }
        }
        
        // Update order total
        commandeService.updateTotalForCommande(commande.getId());
    }

    private void sendWhatsAppNotification(Commande commande ,String pdfpath) {
        try { 
            N8nWebhookService whatsappService = new N8nWebhookService("http://localhost:3000/api/sendPdf");
            String phone = formatPhoneNumber(currentUser.getPhoneNumber());
            String message = buildConfirmationMessage(currentUser, commande);

            // Read PDF file and encode to base64
            java.nio.file.Path path = java.nio.file.Paths.get(pdfpath);
            byte[] pdfBytes = java.nio.file.Files.readAllBytes(path);
            String base64Pdf = java.util.Base64.getEncoder().encodeToString(pdfBytes);
            String invoiceFileName = java.nio.file.Paths.get(pdfpath).getFileName().toString();
            whatsappService.sendMessageWithBase64Pdf(phone + "@c.us", message, base64Pdf, invoiceFileName);
        } catch (Exception e) {
            System.err.println("WhatsApp notification failed: " + e.getMessage());
        }
    }

    private void sendAdminStockAlert(Produit produit) {
        try {
            String adminPhone = "+21624031727";
            String message = String.format(
                "Low Stock Alert!\nProduct: %s\nCurrent Stock: %d",
                produit.getNom(), 
                produit.getQuantiteStock()
            );
            whatsappService.sendMessageToN8n(adminPhone, message);
        } catch (Exception e) {
            System.err.println("Stock alert failed: " + e.getMessage());
        }
    }

    private String formatPhoneNumber(String phone) {
        return phone == null ? "" : phone.startsWith("+") ? phone.substring(1) : phone;
    }

    private String buildConfirmationMessage(User user, Commande commande) {
        Panier panier = panierService.getPanierByUserId(user.getId());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 4);
        String estimatedDeliveryDateStr = sdf.format(cal.getTime());
        return String.format(
            "Hello %s %s,\n\n" +
            "Your order #%d is confirmed!\n" +
            "Total : %.2f DT\n" +
            "Delivery to: %s\n\n" +
            "Estimated Delivery Date: %s\n\n" +
            "Thank you for your purchase!",
            user.getPrenom(),
            user.getNom(),
            commande.getId(),
            panier.getTotal(),
            shipmentAddress,
            estimatedDeliveryDateStr
        );
    }
}