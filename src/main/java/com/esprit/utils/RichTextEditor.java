package com.esprit.utils;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.geometry.Insets;
import javafx.scene.control.TextInputDialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * A rich text editor component that provides formatting capabilities for blog content.
 */
public class RichTextEditor extends VBox {
    
    private final HTMLEditor htmlEditor;
    
    public RichTextEditor() {
        // Set spacing and padding
        setSpacing(10);
        setPadding(new Insets(10));
        
        // Create HTML Editor
        htmlEditor = new HTMLEditor();
        htmlEditor.setPrefHeight(400);
        VBox.setVgrow(htmlEditor, Priority.ALWAYS);
        
        // Create custom buttons for additional formatting options
        Button insertImageButton = new Button("Insert Image");
        insertImageButton.setOnAction(e -> insertImage());
        
        Button insertCodeButton = new Button("Insert Code Block");
        insertCodeButton.setOnAction(e -> insertCodeBlock());
        
        Button insertLinkButton = new Button("Insert Link");
        insertLinkButton.setOnAction(e -> insertLink());
        
        Button headingButton = new Button("Heading");
        headingButton.setOnAction(e -> insertHeading());
        
        // Create toolbar for custom buttons
        ToolBar customToolbar = new ToolBar();
        customToolbar.getItems().addAll(insertImageButton, insertCodeButton, insertLinkButton, headingButton);
        
        // Add components to the layout
        getChildren().addAll(customToolbar, htmlEditor);
    }
    
    /**
     * Inserts an image into the HTML editor
     */
    private void insertImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
        if (selectedFile != null) {
            // Copy the image to the project's resources folder
            try {
                File destDir = new File(System.getProperty("user.dir") + "/src/main/resources/images/blogs");
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }
                
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File destFile = new File(destDir, fileName);
                
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                // Insert the image into the HTML content
                String imageUrl = destFile.toURI().toString();
                String imgHtml = "<img src='" + imageUrl + "' alt='Blog Image' style='max-width:100%;'/>";
                htmlEditor.setHtmlText(htmlEditor.getHtmlText() + imgHtml);
                
            } catch (IOException e) {
                e.printStackTrace();
                // Show error message
            }
        }
    }
    
    /**
     * Inserts a code block into the HTML editor
     */
    private void insertCodeBlock() {
        String codeBlockHtml = "<pre style='background-color:#f5f5f5;padding:10px;border-radius:5px;font-family:monospace;'>" + 
                              "<code>// Your code here</code>" + 
                              "</pre>";
        htmlEditor.setHtmlText(htmlEditor.getHtmlText() + codeBlockHtml);
    }
    
    /**
     * Inserts a link into the HTML editor
     */
    private void insertLink() {
        TextInputDialog urlDialog = new TextInputDialog("https://");
        urlDialog.setTitle("Insert Link");
        urlDialog.setHeaderText("Enter the URL for the link:");
        urlDialog.setContentText("URL:");
        
        Optional<String> urlResult = urlDialog.showAndWait();
        
        if (urlResult.isPresent()) {
            String url = urlResult.get();
            
            TextInputDialog textDialog = new TextInputDialog("Link Text");
            textDialog.setTitle("Insert Link");
            textDialog.setHeaderText("Enter the text for the link:");
            textDialog.setContentText("Text:");
            
            Optional<String> textResult = textDialog.showAndWait();
            
            if (textResult.isPresent()) {
                String text = textResult.get();
                String linkHtml = "<a href='" + url + "' target='_blank'>" + text + "</a>";
                htmlEditor.setHtmlText(htmlEditor.getHtmlText() + linkHtml);
            }
        }
    }
    
    /**
     * Inserts a heading into the HTML editor
     */
    private void insertHeading() {
        TextInputDialog dialog = new TextInputDialog("Heading Text");
        dialog.setTitle("Insert Heading");
        dialog.setHeaderText("Enter heading text:");
        dialog.setContentText("Text:");
        
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            String headingText = result.get();
            String headingHtml = "<h2>" + headingText + "</h2>";
            htmlEditor.setHtmlText(htmlEditor.getHtmlText() + headingHtml);
        }
    }
    
    /**
     * Gets the HTML content from the editor
     * @return HTML content as a string
     */
    public String getHtmlContent() {
        return htmlEditor.getHtmlText();
    }
    
    /**
     * Sets the HTML content in the editor
     * @param htmlContent HTML content as a string
     */
    public void setHtmlContent(String htmlContent) {
        htmlEditor.setHtmlText(htmlContent);
    }
    
    /**
     * Converts HTML to plain text (simplified version).
     * @return Plain text content
     */
    public String getPlainText() {
        String html = htmlEditor.getHtmlText();
        // Very basic HTML to text conversion
        return html.replaceAll("<[^>]*>", "");
    }
}
