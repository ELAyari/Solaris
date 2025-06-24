package com.esprit.utils;

import com.esprit.models.Blog;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Utility class for social media sharing functionality.
 */
public class SocialMediaSharing {

    private static final String BASE_URL = "http://localhost:8080/blog/"; // Replace with your actual base URL

    /**
     * Share a blog post on Facebook
     * @param blog The blog to share
     */
    public static void shareOnFacebook(Blog blog) {
        try {
            String url = BASE_URL + blog.getId();
            String encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
            String facebookUrl = "https://www.facebook.com/sharer/sharer.php?u=" + encodedUrl;
            
            openInBrowser(facebookUrl);
        } catch (Exception e) {
            showErrorAlert("Facebook Sharing Error", "Failed to share on Facebook: " + e.getMessage());
        }
    }

    /**
     * Share a blog post on Twitter
     * @param blog The blog to share
     */
    public static void shareOnTwitter(Blog blog) {
        try {
            String url = BASE_URL + blog.getId();
            String encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
            String encodedText = URLEncoder.encode(blog.getTitre(), StandardCharsets.UTF_8.toString());
            String twitterUrl = "https://twitter.com/intent/tweet?text=" + encodedText + "&url=" + encodedUrl;
            
            openInBrowser(twitterUrl);
        } catch (Exception e) {
            showErrorAlert("Twitter Sharing Error", "Failed to share on Twitter: " + e.getMessage());
        }
    }

    /**
     * Share a blog post on LinkedIn
     * @param blog The blog to share
     */
    public static void shareOnLinkedIn(Blog blog) {
        try {
            String url = BASE_URL + blog.getId();
            String encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
            String encodedTitle = URLEncoder.encode(blog.getTitre(), StandardCharsets.UTF_8.toString());
            String encodedSummary = URLEncoder.encode(blog.getResume(), StandardCharsets.UTF_8.toString());
            String linkedInUrl = "https://www.linkedin.com/shareArticle?mini=true&url=" + encodedUrl + 
                                 "&title=" + encodedTitle + "&summary=" + encodedSummary;
            
            openInBrowser(linkedInUrl);
        } catch (Exception e) {
            showErrorAlert("LinkedIn Sharing Error", "Failed to share on LinkedIn: " + e.getMessage());
        }
    }

    /**
     * Share a blog post via email
     * @param blog The blog to share
     */
    public static void shareViaEmail(Blog blog) {
        try {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Share via Email");
            dialog.setHeaderText("Enter recipient email address");
            dialog.setContentText("Email:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String recipient = result.get();
                String subject = URLEncoder.encode("Check out this blog post: " + blog.getTitre(), StandardCharsets.UTF_8.toString());
                String body = URLEncoder.encode("I thought you might be interested in this blog post:\n\n" + 
                                               blog.getTitre() + "\n\n" + 
                                               blog.getResume() + "\n\n" + 
                                               BASE_URL + blog.getId(), StandardCharsets.UTF_8.toString());
                String mailtoUrl = "mailto:" + recipient + "?subject=" + subject + "&body=" + body;
                
                openInBrowser(mailtoUrl);
            }
        } catch (Exception e) {
            showErrorAlert("Email Sharing Error", "Failed to share via email: " + e.getMessage());
        }
    }

    /**
     * Share a blog post via WhatsApp
     * @param blog The blog to share
     */
    public static void shareOnWhatsApp(Blog blog) {
        try {
            String text = blog.getTitre() + "\n\n" + blog.getResume() + "\n\n" + BASE_URL + blog.getId();
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
            String whatsappUrl = "https://api.whatsapp.com/send?text=" + encodedText;
            
            openInBrowser(whatsappUrl);
        } catch (Exception e) {
            showErrorAlert("WhatsApp Sharing Error", "Failed to share on WhatsApp: " + e.getMessage());
        }
    }

    /**
     * Generate a shareable link for the blog post
     * @param blog The blog to generate a link for
     * @return The shareable link
     */
    public static String generateShareableLink(Blog blog) {
        return BASE_URL + blog.getId();
    }

    /**
     * Show a dialog with sharing options
     * @param blog The blog to share
     */
    public static void showSharingDialog(Blog blog) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Share Blog Post");
        alert.setHeaderText("Share \"" + blog.getTitre() + "\"");
        
        VBox content = new VBox(10);
        
        Hyperlink facebookLink = new Hyperlink("Share on Facebook");
        facebookLink.setOnAction(e -> {
            alert.close();
            shareOnFacebook(blog);
        });
        
        Hyperlink twitterLink = new Hyperlink("Share on Twitter");
        twitterLink.setOnAction(e -> {
            alert.close();
            shareOnTwitter(blog);
        });
        
        Hyperlink linkedInLink = new Hyperlink("Share on LinkedIn");
        linkedInLink.setOnAction(e -> {
            alert.close();
            shareOnLinkedIn(blog);
        });
        
        Hyperlink emailLink = new Hyperlink("Share via Email");
        emailLink.setOnAction(e -> {
            alert.close();
            shareViaEmail(blog);
        });
        
        Hyperlink whatsappLink = new Hyperlink("Share on WhatsApp");
        whatsappLink.setOnAction(e -> {
            alert.close();
            shareOnWhatsApp(blog);
        });
        
        Text linkText = new Text("Shareable Link:");
        Hyperlink shareableLink = new Hyperlink(generateShareableLink(blog));
        shareableLink.setOnAction(e -> {
            copyToClipboard(generateShareableLink(blog));
            showInfoAlert("Link Copied", "The shareable link has been copied to your clipboard.");
        });
        
        content.getChildren().addAll(
            facebookLink, twitterLink, linkedInLink, emailLink, whatsappLink, 
            linkText, shareableLink
        );
        
        alert.getDialogPane().setContent(content);
        alert.getButtonTypes().add(ButtonType.CLOSE);
        
        alert.showAndWait();
    }

    /**
     * Open a URL in the default browser
     * @param url The URL to open
     * @throws IOException If an I/O error occurs
     * @throws URISyntaxException If the URL is not a valid URI
     */
    private static void openInBrowser(String url) throws IOException, URISyntaxException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(url));
        } else {
            showErrorAlert("Browser Error", "Unable to open the default browser.");
        }
    }

    /**
     * Copy text to the clipboard
     * @param text The text to copy
     */
    private static void copyToClipboard(String text) {
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    /**
     * Show an error alert
     * @param title The alert title
     * @param message The alert message
     */
    private static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show an information alert
     * @param title The alert title
     * @param message The alert message
     */
    private static void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
