package com.esprit.controllers;

import com.esprit.models.Blog;
import com.esprit.Services.BlogService;
import com.esprit.utils.ImagePathUtil;
import com.esprit.utils.UIUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Map;

import javafx.concurrent.Task;
import javafx.application.Platform;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.FileOutputStream;


import com.esprit.Services.GeminiImageGenerationService;
import com.esprit.Services.GeminiImageToTextService;

import java.util.Base64;

public class BlogFormController {
    public enum Mode { ADD, EDIT }
    
    // Constants for character limits
    private static final int TITLE_MAX_LENGTH = 100;
    private static final int SUMMARY_MAX_LENGTH = 500;
    private static final int CONTENT_MAX_LENGTH = 5000;
    
    @FXML private TextField titleField;
    @FXML private TextField summaryField;
    // Using TextArea instead of RichTextEditor for now
    @FXML private TextArea contentArea;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label imageNameLabel;
    @FXML private ImageView imagePreview;
    @FXML private Button generateImageButton;
    @FXML private Button generateTextFromImageButton;
    
    // New UI elements from client blog form
    @FXML private Label titleCharCount;
    @FXML private Label summaryCharCount;
    @FXML private Label contentCharCount;
    @FXML private Label titleValidationLabel;
    @FXML private Label summaryValidationLabel;
    @FXML private Label contentValidationLabel;
    @FXML private ProgressIndicator saveProgressIndicator;
    @FXML private Label saveStatusLabel;
    @FXML private Label noImageLabel;
    @FXML private Button saveDraftButton;
    @FXML private Button publishButton;
    @FXML private Button saveChangesButton;
    @FXML private Button deleteBlogButton;
    
    private Mode mode = Mode.ADD;
    private Blog blog;
    private boolean saved = false;
    private File selectedImage;

    @FXML
    private GeminiImageGenerationService geminiService = new GeminiImageGenerationService();
    @FXML
    private GeminiImageToTextService geminiImageToTextService = new GeminiImageToTextService();
    
    private StackPane parentContentPane;
    
    public void setMode(Mode mode) {
        this.mode = mode;
    }
    
    /**
     * Sets the parent content pane for fragment-based navigation
     * @param contentPane the parent StackPane to load fragments into
     */
    public void setParentContentPane(StackPane contentPane) {
        this.parentContentPane = contentPane;
    }
    
    @FXML
    private void initialize() {
        // Initialize the status combo box with values
        statusCombo.getItems().addAll("Draft", "Published", "Archived");
        
        // Set default status
        if (statusCombo.getItems().size() > 0) {
            statusCombo.setValue("Draft");
        }
        
        // Hide status field for regular users (non-admin)
        boolean isAdmin = isAdminUser();
        statusCombo.setVisible(isAdmin);
        statusCombo.setManaged(isAdmin);
        
        // Also hide the label for status if not admin
        Label statusLabel = (Label) statusCombo.getParent().getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof Label && "Status:".equals(((Label) node).getText()))
                .findFirst().orElse(null);
        
        if (statusLabel != null) {
            statusLabel.setVisible(isAdmin);
            statusLabel.setManaged(isAdmin);
        }
        
        // Initialize character counters
        if (titleCharCount != null) {
            updateCharCount(titleCharCount, "", TITLE_MAX_LENGTH);
        }
        if (summaryCharCount != null) {
            updateCharCount(summaryCharCount, "", SUMMARY_MAX_LENGTH);
        }
        if (contentCharCount != null) {
            updateCharCount(contentCharCount, "", CONTENT_MAX_LENGTH);
        }
        
        // Add listeners for character counting
        if (titleField != null) {
            titleField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (titleCharCount != null) {
                    updateCharCount(titleCharCount, newValue, TITLE_MAX_LENGTH);
                }
            });
        }
        
        if (summaryField != null) {
            summaryField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (summaryCharCount != null) {
                    updateCharCount(summaryCharCount, newValue, SUMMARY_MAX_LENGTH);
                }
            });
        }
        
        if (contentArea != null) {
            contentArea.textProperty().addListener((observable, oldValue, newValue) -> {
                if (contentCharCount != null) {
                    updateCharCount(contentCharCount, newValue, CONTENT_MAX_LENGTH);
                }
            });
        }
        
        // Initialize image preview
        if (noImageLabel != null) {
            noImageLabel.setVisible(true);
        }
    }
    
    /**
     * Updates character count display
     */
    private void updateCharCount(Label charCountLabel, String text, int maxLength) {
        if (charCountLabel != null) {
            int currentLength = text != null ? text.length() : 0;
            charCountLabel.setText(currentLength + "/" + maxLength);
            
            // Change color based on length
            if (currentLength > maxLength) {
                charCountLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
            } else if (currentLength > maxLength * 0.8) {
                charCountLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 12px;");
            } else {
                charCountLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
            }
        }
    }
    
    public void setBlog(Blog blog) {
        this.blog = blog;
        if (blog != null) {
            titleField.setText(blog.getTitre());
            summaryField.setText(blog.getResume());
            
            // Set content to TextArea
            if (contentArea != null && blog.getContenu() != null) {
                contentArea.setText(blog.getContenu());
            }
            
            // Set status if it exists in the combo box items
            if (blog.getStatus() != null && statusCombo.getItems().contains(blog.getStatus())) {
                statusCombo.setValue(blog.getStatus());
            } else if (statusCombo.getItems().size() > 0) {
                // Default to first item if status doesn't match
                statusCombo.setValue(statusCombo.getItems().get(0));
            }
            
            if (blog.getImage() != null && !blog.getImage().isEmpty()) {
                imageNameLabel.setText(blog.getImage());
                // Load and display the image from images folder
                File imageFile = ImagePathUtil.getBlogImageFile(blog.getImage());
                if (imageFile.exists()) {
                    // Set the selectedImage to the existing blog image
                    selectedImage = imageFile;
                    String imageUri = imageFile.toURI().toString();
                    System.out.println("Loading existing blog image: " + imageUri);
                    imagePreview.setImage(new Image(imageUri));
                    if (noImageLabel != null) {
                        noImageLabel.setVisible(false);
                    }
                } else {
                    System.out.println("Blog image file does not exist: " + imageFile.getAbsolutePath());
                }
            }
            
            // Update mode to EDIT
            mode = Mode.EDIT;
            
            // Show edit-specific buttons
            if (saveChangesButton != null) {
                saveChangesButton.setVisible(true);
            }
            if (deleteBlogButton != null) {
                deleteBlogButton.setVisible(true);
            }
            if (saveDraftButton != null) {
                saveDraftButton.setVisible(false);
            }
            if (publishButton != null) {
                publishButton.setVisible(false);
            }
        }
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    /**
     * Saves image bytes to a file in the image storage directory.
     * This method is part of the API for handling generated images.
     * 
     * @param imageBytes The image data as a byte array
     * @return The saved image file or null if an error occurred
     */
    private File saveImageToFile(byte[] imageBytes) {
        try {
            String sanitizedTitle = titleField.getText().trim().replaceAll("[^a-zA-Z0-9\\-_]", "_");
            String fileName = sanitizedTitle + "_generated.png";
            File destDir = new File(getImageStoragePath());
            if (!destDir.exists()) {
                boolean created = destDir.mkdirs();
                if (!created) {
                    System.err.println("Failed to create directory: " + destDir.getAbsolutePath());
                }
            }
            File outputFile = new File(destDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(imageBytes);
                fos.flush();
            }
            return outputFile;
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> UIUtils.showToastNotification("Failed to save generated image: " + e.getMessage(), "red"));
            return null;
        }
    }

    /**
     * Sets the image preview from a file.
     * This method is part of the API for handling image previews.
     * 
     * @param imageFile The image file to display
     */
    private void setImagePreview(File imageFile) {
        try {
            if (imageFile != null && imageFile.exists()) {
                // First try loading directly
                try {
                    // Use URI format which is more reliable
                    String imageUri = imageFile.toURI().toString();
                    Image img = new Image(imageUri, true); // true for background loading
                    
                    // Check if image loaded successfully
                    if (img.isError()) {
                        System.out.println("Direct loading failed, trying conversion: " + img.getException());
                        throw new Exception("Direct loading failed: " + img.getException());
                    }
                    
                    imagePreview.setImage(img);
                    System.out.println("Image set in preview via URI: " + imageUri);
                    return; // Successfully loaded, exit method
                } catch (Exception e) {
                    System.out.println("Error with direct image loading: " + e.getMessage());
                    // Continue to conversion approach
                }
                
                // If direct loading failed, try converting to PNG first
                try {
                    File convertedFile = ImagePathUtil.validateAndConvertImage(imageFile);
                    if (convertedFile != null && convertedFile.exists()) {
                        String convertedUri = convertedFile.toURI().toString();
                        Image convertedImg = new Image(convertedUri, true);
                        imagePreview.setImage(convertedImg);
                        System.out.println("Image set in preview via converted URI: " + convertedUri);
                        
                        // Update the selectedImage to use the converted file
                        selectedImage = convertedFile;
                    }
                } catch (Exception e) {
                    System.err.println("Failed to convert image for preview: " + e.getMessage());
                    e.printStackTrace();
                    // Show a placeholder or error image
                    UIUtils.showToastNotification("The selected image could not be loaded or converted. Please try a different image format like PNG or JPG.", "red");
                }
            } else {
                System.out.println("Image file is null or does not exist: " + (imageFile != null ? imageFile.getAbsolutePath() : "null"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load image from file: " + e.getMessage());
            UIUtils.showToastNotification("Failed to load the selected image. Please try a different image.", "red");
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Blog Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        
        File selectedFile = fileChooser.showOpenDialog(titleField.getScene().getWindow());
        if (selectedFile != null) {
            selectedImage = selectedFile;
            imageNameLabel.setText(selectedFile.getName());
            
            // Load and display the image
            try {
                String imageUri = selectedFile.toURI().toString();
                Image image = new Image(imageUri);
                imagePreview.setImage(image);
                
                // Hide the "no image" label
                if (noImageLabel != null) {
                    noImageLabel.setVisible(false);
                }
                
                System.out.println("Image loaded successfully: " + imageUri);
            } catch (Exception e) {
                e.printStackTrace();
                UIUtils.showToastNotification("Failed to load image: " + e.getMessage(), "red");
            }
        }
    }

    /**
     * Sets the image preview from a generated image URI.
     * This method is part of the API for handling generated image previews.
     * 
     * @param imageUri The URI of the generated image to display
     */
    private void setGeneratedImagePreview(String imageUri) {
        try {
            Image img = new Image(imageUri);
            imagePreview.setImage(img);
            System.out.println("Generated image set in preview via URI.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load generated image from URI.");
        }
    }

    @FXML
    private void handleDeleteImage() {
        selectedImage = null;
        imageNameLabel.setText("No image selected");
        imagePreview.setImage(null);
        
        // Show the "no image" label
        if (noImageLabel != null) {
            noImageLabel.setVisible(true);
        }
        
        // If we're in edit mode and have an existing blog image, clear it
        if (blog != null && blog.getImage() != null && !blog.getImage().isEmpty()) {
            blog.setImage(null);
        }
    }

    @FXML
    private void handleGenerateImage() {
        String description = summaryField.getText().trim();
        if (description == null || description.isEmpty()) {
            UIUtils.showToastNotification("Blog description/summary is empty. Please enter a valid description.", "red");
            return;
        }

        generateImageButton.setDisable(true);
        UIUtils.showToastNotification("Image generation started. Please wait...", "green");

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Map<String, Object> response = geminiService.generateImage(description);
                    if (response != null) {
                        if (response.containsKey("image_uri")) {
                            String imageUri = (String) response.get("image_uri");
                            Platform.runLater(() -> {
                                selectedImage = new File(java.net.URI.create(imageUri));
                                imageNameLabel.setText(selectedImage.getName());
                                imagePreview.setImage(new Image(imageUri));
                                
                                // Hide the "no image" label
                                if (noImageLabel != null) {
                                    noImageLabel.setVisible(false);
                                }
                                
                                UIUtils.showToastNotification("Image generated and loaded successfully.", "green");
                            });
                        } else if (response.containsKey("error")) {
                            String error = (String) response.get("error");
                            Platform.runLater(() -> UIUtils.showToastNotification("Gemini API error: " + error, "red"));
                        } else {
                            Platform.runLater(() -> UIUtils.showToastNotification("No image data received from Gemini API.", "red"));
                        }
                    } else {
                        Platform.runLater(() -> UIUtils.showToastNotification("No response received from Gemini API.", "red"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> UIUtils.showToastNotification("Exception during image generation: " + e.getMessage(), "red"));
                } finally {
                    Platform.runLater(() -> generateImageButton.setDisable(false));
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    @FXML
    private void handleGenerateTextFromImage() {
        if (selectedImage == null) {
            // Check if we're in edit mode and have an image path but no selectedImage
            if (mode == Mode.EDIT && blog != null && blog.getImage() != null && !blog.getImage().isEmpty()) {
                // Try to load the image file
                File imageFile = ImagePathUtil.getBlogImageFile(blog.getImage());
                if (imageFile.exists()) {
                    selectedImage = imageFile;
                    System.out.println("Using existing blog image for text generation: " + imageFile.getAbsolutePath());
                } else {
                    UIUtils.showToastNotification("The blog's image file could not be found. Please select an image first.", "red");
                    return;
                }
            } else {
                UIUtils.showToastNotification("No image selected. Please select an image first.", "red");
                return;
            }
        }

        if (!selectedImage.exists()) {
            UIUtils.showToastNotification("The selected image file no longer exists: " + selectedImage.getAbsolutePath(), "red");
            return;
        }

        generateTextFromImageButton.setDisable(true);
        UIUtils.showToastNotification("Text generation from image started. Please wait...", "green");

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    byte[] imageBytes = Files.readAllBytes(selectedImage.toPath());
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);

                    Map<String, String> response = geminiImageToTextService.generateTextFromImage(base64Image);
                    if (response != null) {
                        Platform.runLater(() -> {
                            titleField.setText(response.getOrDefault("title", ""));
                            summaryField.setText(response.getOrDefault("summary", ""));
                            contentArea.setText(response.getOrDefault("content", ""));
                            UIUtils.showToastNotification("Text generated from image successfully.", "green");
                        });
                    } else {
                        Platform.runLater(() -> UIUtils.showToastNotification("No response received from Gemini API.", "red"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> UIUtils.showToastNotification("Exception during text generation from image: " + e.getMessage(), "red"));
                } finally {
                    Platform.runLater(() -> generateTextFromImageButton.setDisable(false));
                }
                return null;
            }
        };
        new Thread(task).start();
    }
    
    private File downloadImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            
            String extension = ".png";
            String sanitizedTitle = titleField.getText().trim().replaceAll("[^a-zA-Z0-9\\-_]", "_");
            String fileName = sanitizedTitle + "_generated" + extension;
            File destDir = new File(getImageStoragePath());
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            File outputFile = new File(destDir, fileName);
            
            try (InputStream in = conn.getInputStream();
                 FileOutputStream out = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return outputFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @FXML
    private void handleSave() {
        if (validateInput()) {
            BlogService blogService = new BlogService();

            if (mode == Mode.ADD) {
                blog = new Blog();
            }
            
            // Set blog properties from form fields
            blog.setTitre(titleField.getText().trim());
            blog.setResume(summaryField.getText().trim());
            blog.setContenu(contentArea.getText());
            blog.setStatus(statusCombo.getValue());
            blog.setDatePublication(LocalDateTime.now());
            blog.setAuteurId(getCurrentUserId());
            
            // --- Delete previous image if a new one is set (edit mode) ---
            if (mode == Mode.EDIT && selectedImage != null && blog != null && blog.getImage() != null && !blog.getImage().isEmpty()) {
                File oldImageFile = ImagePathUtil.getBlogImageFile(blog.getImage());
                // Only delete if the new image is different from the old one
                if (!selectedImage.getName().equals(blog.getImage()) && oldImageFile.exists()) {
                    oldImageFile.delete();
                }
            }
            // --- End delete logic ---
            // Handle image
            if (selectedImage != null) {
                try {
                    // Use the enhanced image utility to copy and validate the image
                    String destFileName = ImagePathUtil.copyImageToBlogDirectory(selectedImage);
                    blog.setImage(destFileName);
                    
                    // Verify the image can be loaded by JavaFX
                    File destFile = ImagePathUtil.getBlogImageFile(destFileName);
                    String imageUri = destFile.toURI().toString();
                    try {
                        // Test loading the image to verify it works
                        Image testImage = new Image(imageUri, false); // false = load synchronously for testing
                        if (testImage.isError()) {
                            throw new IOException("JavaFX cannot load the image: " + testImage.getException().getMessage());
                        }
                        System.out.println("Image successfully verified for JavaFX compatibility");
                    } catch (Exception e) {
                        System.err.println("Warning: Image may not be compatible with JavaFX: " + e.getMessage());
                        // Continue anyway, as the image was saved successfully
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    UIUtils.showToastNotification("Failed to save image: " + e.getMessage(), "red");
                    return;
                }
            }
            
            // Save blog to database
            if (mode == Mode.ADD) {
                blogService.ajouter(blog);
                UIUtils.showToastNotification("Blog added successfully", "green");
            } else {
                blogService.modifier(blog);
                UIUtils.showToastNotification("Blog updated successfully", "green");
            }
            
            saved = true;
            // Navigate back to blog view
            navigateToBlogView();
        }
    }

    private int getCurrentUserId() {
        // TODO: Replace with actual user session or authentication logic
        return 1;
    }
    
    /**
     * Check if the current user is an admin
     * This is a placeholder - in a real application, this would check user roles
     * @return true if the user is an admin, false otherwise
     */
    private boolean isAdminUser() {
        // TODO: Replace with actual user role checking logic
        // For now, we'll assume regular users (non-admins)
        return false;
    }
    
    /**
     * Get the path where blog images are stored
     * @deprecated Use ImagePathUtil.getBlogImagesPath() instead
     */
    @Deprecated
    private String getImageStoragePath() {
        return ImagePathUtil.getBlogImagesPath();
    }
    
    /**
     * Validate the input fields
     */
    private boolean validateInput() {
        if (titleField.getText().trim().isEmpty()) {
            UIUtils.showToastNotification("Title cannot be empty", "red");
            return false;
        }
        
        if (summaryField.getText().trim().isEmpty()) {
            UIUtils.showToastNotification("Summary cannot be empty", "red");
            return false;
        }
        
        // Check if content area is empty
        if (contentArea == null || contentArea.getText() == null || contentArea.getText().trim().isEmpty()) {
            UIUtils.showToastNotification("Content cannot be empty", "red");
            return false;
        }
        
        if (statusCombo.getValue() == null) {
            UIUtils.showToastNotification("Please select a status", "red");
            return false;
        }
        
        return true;
    }
    
    // closeWindow method removed as it's no longer needed
    


    /**
     * Navigate back to the blog view
     */
    private void navigateToBlogView() {
        try {
            if (parentContentPane != null) {
                // Load the blog dashboard view as a fragment
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogDashboardView.fxml"));
                Parent root = loader.load();
                
                // Replace the content in the contentPane
                parentContentPane.getChildren().setAll(root);
            } else {
                // Fallback to loading as a new scene if parentContentPane is not set
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogDashboardView.fxml"));
                Parent root = loader.load();
                
                // Get the current stage
                Stage stage = (Stage) titleField.getScene().getWindow();
                
                // Set the new scene
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            UIUtils.showToastNotification("Failed to navigate to blog view: " + e.getMessage(), "red");
        }
    }
    
    /**
     * Handle the back button action
     */
    @FXML
    private void handleBack() {
        // Check if there are unsaved changes
        boolean hasUnsavedChanges = !saved && 
                                  (titleField.getText().trim().length() > 0 || 
                                   summaryField.getText().trim().length() > 0);
        
        // Check if content area has content
        if (!saved && contentArea != null && contentArea.getText() != null && 
            contentArea.getText().trim().length() > 0) {
            hasUnsavedChanges = true;
        }
        
        if (hasUnsavedChanges) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unsaved Changes");
            alert.setHeaderText("You have unsaved changes");
            alert.setContentText("Do you want to discard your changes?");
            
            if (alert.showAndWait().get() == ButtonType.CANCEL) {
                return; // User canceled, don't close
            }
        }
        
        // Navigate back to blog view
        navigateToBlogView();
    }
    
    /**
     * Handle save as draft button action
     */
    @FXML
    private void handleSaveDraft() {
        UIUtils.showToastNotification("Saving as draft...", "green");
        
        // Set status to Draft
        statusCombo.setValue("Draft");
        
        // Save the blog
        if (validateInput()) {
            BlogService blogService = new BlogService();
            
            if (mode == Mode.ADD) {
                blog = new Blog();
            }
            
            // Set blog properties
            blog.setTitre(titleField.getText().trim());
            blog.setResume(summaryField.getText().trim());
            blog.setContenu(contentArea.getText());
            blog.setStatus("Draft");
            blog.setDatePublication(LocalDateTime.now());
            blog.setAuteurId(getCurrentUserId());
            
            // Handle image
            if (selectedImage != null) {
                try {
                    String destFileName = ImagePathUtil.copyImageToBlogDirectory(selectedImage);
                    blog.setImage(destFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    UIUtils.showToastNotification("Failed to save image: " + e.getMessage(), "red");
                    return;
                }
            }
            
            // Save to database
            if (mode == Mode.ADD) {
                blogService.ajouter(blog);
                UIUtils.showToastNotification("Draft saved successfully!", "green");
            } else {
                blogService.modifier(blog);
                UIUtils.showToastNotification("Draft updated successfully!", "green");
            }
            
            saved = true;
        } else {
            UIUtils.showToastNotification("Validation failed", "red");
        }
    }
    
    /**
     * Handle publish button action
     */
    @FXML
    private void handlePublish() {
        UIUtils.showToastNotification("Publishing blog...", "green");
        
        // Set status to Published
        statusCombo.setValue("Published");
        
        // Save the blog
        if (validateInput()) {
            BlogService blogService = new BlogService();
            
            if (mode == Mode.ADD) {
                blog = new Blog();
            }
            
            // Set blog properties
            blog.setTitre(titleField.getText().trim());
            blog.setResume(summaryField.getText().trim());
            blog.setContenu(contentArea.getText());
            blog.setStatus("Published");
            blog.setDatePublication(LocalDateTime.now());
            blog.setAuteurId(getCurrentUserId());
            
            // Handle image
            if (selectedImage != null) {
                try {
                    String destFileName = ImagePathUtil.copyImageToBlogDirectory(selectedImage);
                    blog.setImage(destFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    UIUtils.showToastNotification("Failed to save image: " + e.getMessage(), "red");
                    return;
                }
            }
            
            // Save to database
            if (mode == Mode.ADD) {
                blogService.ajouter(blog);
                UIUtils.showToastNotification("Blog published successfully!", "green");
            } else {
                blogService.modifier(blog);
                UIUtils.showToastNotification("Blog updated and published!", "green");
            }
            
            saved = true;
            
            // Navigate back after a short delay
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(this::navigateToBlogView);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } else {
            UIUtils.showToastNotification("Validation failed", "red");
        }
    }
    
    /**
     * Handle save changes button action (for edit mode)
     */
    @FXML
    private void handleSaveChanges() {
        // Show confirmation dialog
        if (UIUtils.showConfirmation("Save Changes", "Are you sure you want to save these changes?")) {
            UIUtils.showToastNotification("Saving changes...", "green");
            
            if (validateInput()) {
                BlogService blogService = new BlogService();
                
                // Update blog properties
                blog.setTitre(titleField.getText().trim());
                blog.setResume(summaryField.getText().trim());
                blog.setContenu(contentArea.getText());
                blog.setStatus(statusCombo.getValue());
                
                // Handle image changes
                if (selectedImage != null) {
                    try {
                        // Delete old image if it exists
                        if (blog.getImage() != null && !blog.getImage().isEmpty()) {
                            File oldImageFile = ImagePathUtil.getBlogImageFile(blog.getImage());
                            if (oldImageFile.exists() && !selectedImage.getName().equals(blog.getImage())) {
                                oldImageFile.delete();
                            }
                        }
                        
                        String destFileName = ImagePathUtil.copyImageToBlogDirectory(selectedImage);
                        blog.setImage(destFileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                        UIUtils.showToastNotification("Failed to save image: " + e.getMessage(), "red");
                        return;
                    }
                }
                
                // Update in database
                blogService.modifier(blog);
                UIUtils.showToastNotification("Changes saved successfully!", "green");
                saved = true;
                
                // Navigate back after a short delay
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        Platform.runLater(this::handleBack);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            } else {
                UIUtils.showToastNotification("Validation failed", "red");
            }
        }
    }
    
    /**
     * Handle delete blog button action
     */
    @FXML
    private void handleDeleteBlog() {
        if (blog == null || blog.getId() == 0) {
            UIUtils.showToastNotification("No blog to delete", "red");
            return;
        }
        
        if (UIUtils.showConfirmation("Delete Blog", "Are you sure you want to delete this blog? This action cannot be undone.")) {
            UIUtils.showToastNotification("Deleting blog...", "green");
            
            BlogService blogService = new BlogService();
            
            try {
                // Delete the blog image if it exists
                if (blog.getImage() != null && !blog.getImage().isEmpty()) {
                    File imageFile = ImagePathUtil.getBlogImageFile(blog.getImage());
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }
                
                // Delete from database
                blogService.supprimer(blog);
                UIUtils.showToastNotification("Blog deleted successfully!", "green");
                
                // Navigate back after a short delay
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        Platform.runLater(this::navigateToBlogView);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
                
            } catch (Exception e) {
                e.printStackTrace();
                UIUtils.showToastNotification("Failed to delete blog: " + e.getMessage(), "red");
            }
        }
    }
}
