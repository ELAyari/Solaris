package com.esprit.Services;

import com.esprit.models.Commande;
import com.esprit.models.CommandeProduit;
import com.esprit.models.User;
import com.esprit.utils.UIUtils;
import com.esprit.utils.UserSession;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PDFInvoiceService {

    private static final String BUSINESS_NAME = "Solaris";
    private static final String BUSINESS_LOGO_PATH = "src/main/resources/images/logo.png";
    private static final String BUSINESS_CONTACT_PHONE = "+216 24 031 727";
    private static final String BUSINESS_EMAIL = "contact@Solaris.com";
    private static final String BUSINESS_WEBSITE = "www.Solaris.com";
    private static final String SAVE_DIRECTORY = "src/main/resources/facture/";
    
    // Color scheme
    private static final float[] PRIMARY_COLOR = {0.13f, 0.59f, 0.95f}; // Blue
    private static final float[] SECONDARY_COLOR = {0.96f, 0.96f, 0.96f}; // Light gray
    private static final float[] ACCENT_COLOR = {0.2f, 0.2f, 0.2f}; // Dark gray
    private static final float[] WARNING_COLOR = {0.9f, 0.2f, 0.2f}; // Red for errors

    // Layout constants
    private static final float MARGIN = 50;
    private static final float LOGO_WIDTH = 120;
    private static final float LOGO_HEIGHT = 60;
    private static final float HEADER_HEIGHT = 140;
    private static final float FOOTER_HEIGHT = 80;
    private static final float ROW_HEIGHT = 25;
    private static final float SECTION_SPACING = 20;

    public String generateInvoicePDF(Commande commande, String pdfFormat, String paperSize) {
        CommandeProduitService commandeProduitService = new CommandeProduitService();
        UserService userService;
        try {
            userService = new UserService();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize UserService", e);
        }

        

        // Try to get user from session first, then fall back to database if needed
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null || user.getId() != commande.getClientId()) {
            user = userService.getById(commande.getClientId());
            System.out.println("PDFInvoice: Loaded user from database: " + user.getNom() + " " + user.getPrenom());
        } else {
            System.out.println("PDFInvoice: Using current user from session: " + user.getNom() + " " + user.getPrenom());
        }
      

        List<CommandeProduit> produits = commandeProduitService.findByCommandeId(commande.getId());
      


        try (PDDocument document = new PDDocument()) {
            PDRectangle pageSizeRect = paperSize.equalsIgnoreCase("A3") ? PDRectangle.A3 : PDRectangle.A4;
            PDPage page = new PDPage(pageSizeRect);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Calculate available height for content
            float contentHeight = pageSizeRect.getHeight() - HEADER_HEIGHT - FOOTER_HEIGHT;
            float currentY = pageSizeRect.getHeight() - MARGIN;

            // Draw all invoice components
            drawBackgroundElements(contentStream, pageSizeRect);
            currentY = drawHeader(contentStream, document, pageSizeRect, currentY);
            currentY -= SECTION_SPACING;
            currentY = drawInvoiceDetails(contentStream, commande, pageSizeRect, currentY);
            currentY -= SECTION_SPACING;
            currentY = drawCustomerInfo(contentStream, user, commande, pageSizeRect, currentY);
            currentY -= SECTION_SPACING;
            currentY = drawProductTable(contentStream, produits, pageSizeRect, currentY);
            currentY -= SECTION_SPACING;
            drawTotals(contentStream, commande, pageSizeRect, produits, currentY);
            drawFooter(contentStream, pageSizeRect);

            contentStream.close();

            // Generate filename
            String userName = user.getNom().replaceAll("\\s+", "_") + "_" + user.getPrenom().replaceAll("\\s+", "_");
            String dateStr = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = String.format("Invoice_%s_%s_%d.pdf", userName, dateStr, commande.getId());

            // Save document
            String fullPath = SAVE_DIRECTORY + fileName;
            document.save(fullPath);

            UIUtils.showAlert("Invoice Generated", null, "Invoice PDF saved to: " + fullPath, "INFORMATION");
       return fullPath;
        } catch (IOException e) {
            e.printStackTrace();
            UIUtils.showAlert("Error", null, "Failed to generate invoice PDF: " + e.getMessage(), "ERROR");
            return null; // Return null in case of error
        }
        
    }

    private void drawBackgroundElements(PDPageContentStream contentStream, PDRectangle pageSize) throws IOException {
        // Light watermark
        contentStream.setNonStrokingColor(0.95f, 0.95f, 0.95f);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD_OBLIQUE, 72);
        String watermark = BUSINESS_NAME;
        float watermarkWidth = (PDType1Font.HELVETICA_BOLD_OBLIQUE.getStringWidth(watermark) / 1000) * 72;
        contentStream.newLineAtOffset((pageSize.getWidth() - watermarkWidth) / 2, pageSize.getHeight() / 2);
        contentStream.showText(watermark);
        contentStream.endText();
        contentStream.setNonStrokingColor(0, 0, 0); // Reset to black
    }

    private float drawHeader(PDPageContentStream contentStream, PDDocument document, PDRectangle pageSize, float startY) throws IOException {
        // Add a subtle background for the header area
        contentStream.setNonStrokingColor(SECONDARY_COLOR[0], SECONDARY_COLOR[1], SECONDARY_COLOR[2]);
        contentStream.addRect(MARGIN - 10, startY - HEADER_HEIGHT, pageSize.getWidth() - 2 * (MARGIN - 10), HEADER_HEIGHT);
        contentStream.fill();
        contentStream.setNonStrokingColor(0, 0, 0);
        
        // Draw logo
        try {
            PDImageXObject pdImage = PDImageXObject.createFromFile(BUSINESS_LOGO_PATH, document);
            float logoX = MARGIN;
            float logoY = startY - LOGO_HEIGHT - 10; // Add some padding at the top
            contentStream.drawImage(pdImage, logoX, logoY, LOGO_WIDTH, LOGO_HEIGHT);
        } catch (IOException e) {
            System.err.println("Warning: Could not load business logo: " + e.getMessage());
        }

        // Business name with improved styling
        contentStream.beginText();
        contentStream.setNonStrokingColor(PRIMARY_COLOR[0], PRIMARY_COLOR[1], PRIMARY_COLOR[2]);
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 28); // Larger font
        contentStream.newLineAtOffset(MARGIN + LOGO_WIDTH + 20, startY - 35); // Better positioning
        contentStream.showText(BUSINESS_NAME);
        contentStream.endText();
        
        // Tagline/Slogan
        contentStream.beginText();
        contentStream.setNonStrokingColor(ACCENT_COLOR[0], ACCENT_COLOR[1], ACCENT_COLOR[2]);
        contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
        contentStream.newLineAtOffset(MARGIN + LOGO_WIDTH + 20, startY - 55);
        contentStream.showText("Renewable Energy Solutions");
        contentStream.endText();
        contentStream.setNonStrokingColor(0, 0, 0);

        // Contact info in a more organized layout
        float infoX = pageSize.getWidth() - MARGIN - 150; // Right-aligned info block
        float infoY = startY - 30;
        
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.newLineAtOffset(infoX, infoY);
        contentStream.showText("Contact Information:");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(infoX, infoY - 15);
        contentStream.showText("Phone: " + BUSINESS_CONTACT_PHONE);
        contentStream.newLineAtOffset(0, -14);
        contentStream.showText("Email: " + BUSINESS_EMAIL);
        contentStream.newLineAtOffset(0, -14);
        contentStream.showText("Website: " + BUSINESS_WEBSITE);
        contentStream.endText();

        // Decorative double line for a more professional look
        // First line (thicker)
        contentStream.setStrokingColor(PRIMARY_COLOR[0], PRIMARY_COLOR[1], PRIMARY_COLOR[2]);
        contentStream.setLineWidth(2.0f);
        contentStream.moveTo(MARGIN - 10, startY - HEADER_HEIGHT + 10);
        contentStream.lineTo(pageSize.getWidth() - MARGIN + 10, startY - HEADER_HEIGHT + 10);
        contentStream.stroke();
        
        // Second line (thinner)
        contentStream.setStrokingColor(ACCENT_COLOR[0], ACCENT_COLOR[1], ACCENT_COLOR[2]);
        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(MARGIN - 10, startY - HEADER_HEIGHT + 5);
        contentStream.lineTo(pageSize.getWidth() - MARGIN + 10, startY - HEADER_HEIGHT + 5);
        contentStream.stroke();
        contentStream.setStrokingColor(0, 0, 0);

        return startY - HEADER_HEIGHT;
    }

    private float drawInvoiceDetails(PDPageContentStream contentStream, Commande commande, PDRectangle pageSize, float startY) throws IOException {
        // Invoice title
        contentStream.beginText();
        contentStream.setNonStrokingColor(PRIMARY_COLOR[0], PRIMARY_COLOR[1], PRIMARY_COLOR[2]);
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 22);
        contentStream.newLineAtOffset(MARGIN, startY - 30);
        contentStream.showText("INVOICE #" + commande.getId());
        contentStream.endText();
        contentStream.setNonStrokingColor(0, 0, 0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // Invoice details box
        float boxWidth = 200;
        float boxHeight = 80;
        float boxX = pageSize.getWidth() - MARGIN - boxWidth;
        float boxY = startY - 30;

        // Box background
        contentStream.setNonStrokingColor(SECONDARY_COLOR[0], SECONDARY_COLOR[1], SECONDARY_COLOR[2]);
        contentStream.addRect(boxX, boxY - boxHeight, boxWidth, boxHeight);
        contentStream.fill();
        contentStream.setNonStrokingColor(0, 0, 0);

        // Box border
        contentStream.setStrokingColor(ACCENT_COLOR[0], ACCENT_COLOR[1], ACCENT_COLOR[2]);
        contentStream.setLineWidth(0.5f);
        contentStream.addRect(boxX, boxY - boxHeight, boxWidth, boxHeight);
        contentStream.stroke();
        contentStream.setStrokingColor(0, 0, 0);

        // Invoice details
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(boxX + 10, boxY - 25);
        contentStream.showText("INVOICE DATE");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(boxX + 10, boxY - 40);
        contentStream.showText(dateFormat.format(commande.getDate()));
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(boxX + 10, boxY - 60);
        contentStream.showText("STATUS");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setNonStrokingColor(
            commande.getStatut().equalsIgnoreCase("Paid") ? 0.2f : WARNING_COLOR[0],
            commande.getStatut().equalsIgnoreCase("Paid") ? 0.6f : WARNING_COLOR[1],
            commande.getStatut().equalsIgnoreCase("Paid") ? 0.2f : WARNING_COLOR[2]
        );
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.newLineAtOffset(boxX + 10, boxY - 75);
        contentStream.showText(commande.getStatut().toUpperCase());
        contentStream.endText();
        contentStream.setNonStrokingColor(0, 0, 0);

        // Section separator
        contentStream.setStrokingColor(PRIMARY_COLOR[0], PRIMARY_COLOR[1], PRIMARY_COLOR[2]);
        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(MARGIN, startY - boxHeight - 40);
        contentStream.lineTo(pageSize.getWidth() - MARGIN, startY - boxHeight - 40);
        contentStream.stroke();
        contentStream.setStrokingColor(0, 0, 0);

        return startY - boxHeight - 50;
    }

    private float drawCustomerInfo(PDPageContentStream contentStream, User user, Commande commande, PDRectangle pageSize, float startY) throws IOException {
        // Customer title
        contentStream.beginText();
        contentStream.setNonStrokingColor(PRIMARY_COLOR[0], PRIMARY_COLOR[1], PRIMARY_COLOR[2]);
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        contentStream.newLineAtOffset(MARGIN, startY - 20);
        contentStream.showText("BILL TO:");
        contentStream.endText();
        contentStream.setNonStrokingColor(0, 0, 0);

        // Customer info box
        float boxWidth = pageSize.getWidth() - 2 * MARGIN;
        float boxHeight = 90;
        float boxX = MARGIN;
        float boxY = startY - 40;

        // Box background
        contentStream.setNonStrokingColor(SECONDARY_COLOR[0], SECONDARY_COLOR[1], SECONDARY_COLOR[2]);
        contentStream.addRect(boxX, boxY - boxHeight, boxWidth, boxHeight);
        contentStream.fill();
        contentStream.setNonStrokingColor(0, 0, 0);

        // Box border
        contentStream.setStrokingColor(ACCENT_COLOR[0], ACCENT_COLOR[1], ACCENT_COLOR[2]);
        contentStream.setLineWidth(0.5f);
        contentStream.addRect(boxX, boxY - boxHeight, boxWidth, boxHeight);
        contentStream.stroke();
        contentStream.setStrokingColor(0, 0, 0);

        // Customer info
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(boxX + 10, boxY - 20);
        contentStream.showText(user.getNom() + " " + user.getPrenom());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(boxX + 10, boxY - 35);
        contentStream.showText("Email: " + (user.getEmail() != null ? user.getEmail() : "N/A"));
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(boxX + 10, boxY - 50);
        contentStream.showText("Phone: " + (user.getPhoneNumber() != null ? user.getPhoneNumber() : "N/A"));
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(boxX + 10, boxY - 65);
        contentStream.showText("Address: " + (commande.getShipmentAddress() != null ? commande.getShipmentAddress() : "N/A"));
        contentStream.endText();

        // Section separator
        contentStream.setStrokingColor(PRIMARY_COLOR[0], PRIMARY_COLOR[1], PRIMARY_COLOR[2]);
        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(MARGIN, boxY - boxHeight - 10);
        contentStream.lineTo(pageSize.getWidth() - MARGIN, boxY - boxHeight - 10);
        contentStream.stroke();
        contentStream.setStrokingColor(0, 0, 0);

        return boxY - boxHeight - 20;
    }

    private float drawProductTable(PDPageContentStream contentStream, List<CommandeProduit> produits, PDRectangle pageSize, float startY) throws IOException {
        float tableWidth = pageSize.getWidth() - 2 * MARGIN;
        float y = startY;

        // Column widths
        float productColWidth = tableWidth * 0.45f;
        float qtyColWidth = tableWidth * 0.15f;
        float unitPriceColWidth = tableWidth * 0.2f;
        float totalColWidth = tableWidth * 0.2f;

        // Table header
        contentStream.setNonStrokingColor(PRIMARY_COLOR[0], PRIMARY_COLOR[1], PRIMARY_COLOR[2]);
        contentStream.addRect(MARGIN, y - ROW_HEIGHT, tableWidth, ROW_HEIGHT);
        contentStream.fill();
        contentStream.setNonStrokingColor(1, 1, 1);

        // Header text
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(MARGIN + 10, y - 15);
        contentStream.showText("PRODUCT");
        contentStream.newLineAtOffset(productColWidth, 0);
        contentStream.showText("QTY");
        contentStream.newLineAtOffset(qtyColWidth, 0);
        contentStream.showText("UNIT PRICE");
        contentStream.newLineAtOffset(unitPriceColWidth, 0);
        contentStream.showText("TOTAL");
        contentStream.endText();
        contentStream.setNonStrokingColor(0, 0, 0);

        y -= ROW_HEIGHT;

        // Table rows
        boolean alternate = false;
        contentStream.setFont(PDType1Font.HELVETICA, 11);

        for (CommandeProduit cp : produits) {
            // Row background
            contentStream.setNonStrokingColor(alternate ? SECONDARY_COLOR[0] : 1, 
                                            alternate ? SECONDARY_COLOR[1] : 1, 
                                            alternate ? SECONDARY_COLOR[2] : 1);
            contentStream.addRect(MARGIN, y - ROW_HEIGHT, tableWidth, ROW_HEIGHT);
            contentStream.fill();
            contentStream.setNonStrokingColor(0, 0, 0);
            alternate = !alternate;

            // Product name
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN + 10, y - 15);
            contentStream.showText(cp.getProduit().getNom());
            contentStream.endText();

            // Quantity
            String qtyStr = String.valueOf(cp.getQuantite());
            float qtyWidth = (PDType1Font.HELVETICA.getStringWidth(qtyStr) / 1000) * 11;
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN + productColWidth + (qtyColWidth - qtyWidth)/2, y - 15);
            contentStream.showText(qtyStr);
            contentStream.endText();

            // Unit price
            String unitPriceStr = String.format("%.2f DT", cp.getProduit().getPrix());
            float unitPriceWidth = (PDType1Font.HELVETICA.getStringWidth(unitPriceStr) / 1000) * 11;
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN + productColWidth + qtyColWidth + unitPriceColWidth - unitPriceWidth - 5, y - 15);
            contentStream.showText(unitPriceStr);
            contentStream.endText();

            // Total
            String totalStr = String.format("%.2f DT", cp.getSousTotal());
            float totalWidth = (PDType1Font.HELVETICA.getStringWidth(totalStr) / 1000) * 11;
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN + productColWidth + qtyColWidth + unitPriceColWidth + totalColWidth - totalWidth - 5, y - 15);
            contentStream.showText(totalStr);
            contentStream.endText();

            y -= ROW_HEIGHT;
        }

        // Table borders
        contentStream.setStrokingColor(ACCENT_COLOR[0], ACCENT_COLOR[1], ACCENT_COLOR[2]);
        contentStream.setLineWidth(0.5f);

        // Outer border
        contentStream.addRect(MARGIN, y, tableWidth, startY - y);
        contentStream.stroke();

        // Vertical lines
        float x = MARGIN;
        contentStream.moveTo(x + productColWidth, y);
        contentStream.lineTo(x + productColWidth, startY);
        x += productColWidth;
        contentStream.moveTo(x + qtyColWidth, y);
        contentStream.lineTo(x + qtyColWidth, startY);
        x += qtyColWidth;
        contentStream.moveTo(x + unitPriceColWidth, y);
        contentStream.lineTo(x + unitPriceColWidth, startY);
        contentStream.stroke();
        contentStream.setStrokingColor(0, 0, 0);

        return y;
    }

    private void drawTotals(PDPageContentStream contentStream, Commande commande, PDRectangle pageSize, List<CommandeProduit> produits, float startY) throws IOException {
        double subtotal = produits.stream().mapToDouble(CommandeProduit::getSousTotal).sum();
        double delivery = 7.0;
        double total = subtotal + delivery;

        // Totals box
        float boxWidth = 250;
        float boxHeight = 100;
        float boxX = pageSize.getWidth() - MARGIN - boxWidth;
        float boxY = startY;

        // Box background
        contentStream.setNonStrokingColor(SECONDARY_COLOR[0], SECONDARY_COLOR[1], SECONDARY_COLOR[2]);
        contentStream.addRect(boxX, boxY - boxHeight, boxWidth, boxHeight);
        contentStream.fill();
        contentStream.setNonStrokingColor(0, 0, 0);

        // Box border
        contentStream.setStrokingColor(ACCENT_COLOR[0], ACCENT_COLOR[1], ACCENT_COLOR[2]);
        contentStream.setLineWidth(0.5f);
        contentStream.addRect(boxX, boxY - boxHeight, boxWidth, boxHeight);
        contentStream.stroke();
        contentStream.setStrokingColor(0, 0, 0);

        // Totals text
        float labelX = boxX + 10;
        float valueX = boxX + boxWidth - 10;
        float y = boxY - 20;

        // Subtotal
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 11);
        contentStream.newLineAtOffset(labelX, y);
        contentStream.showText("Subtotal:");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 11);
        float subtotalWidth = (PDType1Font.HELVETICA.getStringWidth(String.format("%.2f DT", subtotal)) / 1000) * 11;
        contentStream.newLineAtOffset(valueX - subtotalWidth, y);
        contentStream.showText(String.format("%.2f DT", subtotal));
        contentStream.endText();

        // Delivery
        y -= 20;
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 11);
        contentStream.newLineAtOffset(labelX, y);
        contentStream.showText("Delivery:");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 11);
        float deliveryWidth = (PDType1Font.HELVETICA.getStringWidth(String.format("%.2f DT", delivery)) / 1000) * 11;
        contentStream.newLineAtOffset(valueX - deliveryWidth, y);
        contentStream.showText(String.format("%.2f DT", delivery));
        contentStream.endText();

        // Total separator
        contentStream.setStrokingColor(ACCENT_COLOR[0], ACCENT_COLOR[1], ACCENT_COLOR[2]);
        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(labelX, y - 10);
        contentStream.lineTo(valueX, y - 10);
        contentStream.stroke();

        // Total
        y -= 25;
        contentStream.beginText();
        contentStream.setNonStrokingColor(PRIMARY_COLOR[0], PRIMARY_COLOR[1], PRIMARY_COLOR[2]);
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(labelX, y);
        contentStream.showText("TOTAL:");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setNonStrokingColor(PRIMARY_COLOR[0], PRIMARY_COLOR[1], PRIMARY_COLOR[2]);
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        float totalWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth(String.format("%.2f DT", total)) / 1000) * 14;
        contentStream.newLineAtOffset(valueX - totalWidth, y);
        contentStream.showText(String.format("%.2f DT", total));
        contentStream.endText();
        contentStream.setNonStrokingColor(0, 0, 0);
    }

    private void drawFooter(PDPageContentStream contentStream, PDRectangle pageSize) throws IOException {
        String footerText = "Thank you for your business!";
        String footerContact = "Contact us at: " + BUSINESS_EMAIL + " | " + BUSINESS_CONTACT_PHONE;
        String footerNote = "Payment due within 15 days of invoice date";

        // Footer line
        contentStream.setStrokingColor(PRIMARY_COLOR[0], PRIMARY_COLOR[1], PRIMARY_COLOR[2]);
        contentStream.setLineWidth(1f);
        contentStream.moveTo(MARGIN, FOOTER_HEIGHT - 30);
        contentStream.lineTo(pageSize.getWidth() - MARGIN, FOOTER_HEIGHT - 30);
        contentStream.stroke();
        contentStream.setStrokingColor(0, 0, 0);

        // Footer text
        contentStream.beginText();
        contentStream.setNonStrokingColor(PRIMARY_COLOR[0], PRIMARY_COLOR[1], PRIMARY_COLOR[2]);
        contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
        float textWidth = (PDType1Font.HELVETICA_OBLIQUE.getStringWidth(footerText) / 1000) * 12;
        contentStream.newLineAtOffset((pageSize.getWidth() - textWidth) / 2, FOOTER_HEIGHT - 50);
        contentStream.showText(footerText);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        float contactWidth = (PDType1Font.HELVETICA.getStringWidth(footerContact) / 1000) * 10;
        contentStream.newLineAtOffset((pageSize.getWidth() - contactWidth) / 2, FOOTER_HEIGHT - 65);
        contentStream.showText(footerContact);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 9);
        float noteWidth = (PDType1Font.HELVETICA.getStringWidth(footerNote) / 1000) * 9;
        contentStream.newLineAtOffset((pageSize.getWidth() - noteWidth) / 2, FOOTER_HEIGHT - 80);
        contentStream.showText(footerNote);
        contentStream.endText();
        contentStream.setNonStrokingColor(0, 0, 0);
    }
}