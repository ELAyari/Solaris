package com.esprit.controllers;

import com.esprit.models.Blog;
import com.esprit.Services.BlogService;
import com.esprit.utils.ImagePathUtil;
import com.esprit.utils.UIUtils;
import com.esprit.tests.MainprogGUI;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import java.util.Timer;
import java.util.TimerTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.esprit.Services.GeminiImageGenerationService;
import com.esprit.Services.GeminiImageToTextService;

public class ClientBlogFormController {
    // Removed Mode enum as it is not used in the controller

    @FXML private TextField titleField;
    @FXML private TextField summaryField;
    @FXML private TextArea contentArea;
    @FXML private Label imageNameLabel;
    @FXML private ImageView imagePreview;
    @FXML private Button generateImageButton;
    @FXML private Button generateTextFromImageButton;
    @FXML private FlowPane previousBlogsContainer;
    @FXML private Label noPreviousBlogsLabel;
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
    @FXML private Button previewButton;
    @FXML private Button deleteBlogButton;

    private BlogService blogService = new BlogService();
    private GeminiImageGenerationService geminiService = new GeminiImageGenerationService();
    private GeminiImageToTextService geminiImageToTextService = new GeminiImageToTextService();

    private StackPane parentContentPane;
    private File selectedImage;
    private boolean saved = false;
    private Blog blog;

    private static final int TITLE_MAX_LENGTH = 100;
    private static final int SUMMARY_MAX_LENGTH = 500;
    private static final int CONTENT_MAX_LENGTH = 5000;

    private boolean editMode = false;
    private int editingBlogId = -1;

    public void setParentContentPane(StackPane contentPane) {
        this.parentContentPane = contentPane;
    }

    @FXML
    private void initialize() {
        loadPreviousBlogs();
        setupRealTimeValidation();
        updateImagePreview();
        saveChangesButton.setVisible(false);
        saveDraftButton.setVisible(true);
        publishButton.setVisible(false);
        deleteBlogButton.setVisible(false);
    }
    
    @FXML
    private void handleDeleteImage() {
        if (blog != null && blog.getImage() != null && !blog.getImage().isEmpty()) {
            File imageFile = new File(ImagePathUtil.getBlogImagesPath(), blog.getImage());
            if (imageFile.exists()) {
                if (!imageFile.delete()) {
                    UIUtils.showAlert("Delete Error", null, "Failed to delete the image file.", "ERROR");
                    return;
                }
            }
            blog.setImage(null);
        }
        selectedImage = null;
        imageNameLabel.setText("No image selected");
        imagePreview.setImage(null);
        updateImagePreview();
        UIUtils.showToastNotification("Image removed successfully");
    }

    private void loadPreviousBlogs() {
        previousBlogsContainer.getChildren().clear();
        noPreviousBlogsLabel.setVisible(false);

        Task<List<Blog>> loadBlogsTask = new Task<>() {
            @Override
            protected List<Blog> call() {
                return blogService.getBlogsByCurrentUser();
            }
        };

        loadBlogsTask.setOnSucceeded(event -> {
            List<Blog> clientBlogs = loadBlogsTask.getValue();
            previousBlogsContainer.getChildren().clear();
            if (clientBlogs == null || clientBlogs.isEmpty()) {
                noPreviousBlogsLabel.setVisible(true);
            } else {
                noPreviousBlogsLabel.setVisible(false);
                for (Blog blog : clientBlogs) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogCard.fxml"));
                        Parent card = loader.load();
                        com.esprit.controllers.BlogCardController controller = loader.getController();
                        controller.setBlog(blog);
                        card.setOnMouseClicked(e -> openBlogForEdit(blog));
                        previousBlogsContainer.getChildren().add(card);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        loadBlogsTask.setOnFailed(event -> {
            noPreviousBlogsLabel.setText("Failed to load blogs.");
            noPreviousBlogsLabel.setVisible(true);
        });

        new Thread(loadBlogsTask).start();
    }

    private void openBlogForEdit(Blog blogToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientBlogFormView.fxml"));
            Parent root = loader.load();
            ClientBlogFormController controller = loader.getController();
            controller.setParentContentPane(parentContentPane);
            controller.loadBlogForEdit(blogToEdit);
            if (parentContentPane != null) {
                parentContentPane.getChildren().setAll(root);
            } else {
                Stage stage = (Stage) titleField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            UIUtils.showAlert("Error", null, "Failed to open blog for editing: " + e.getMessage(), "ERROR");
        }
    }
   
    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Blog Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File chosenFile = fileChooser.showOpenDialog(imagePreview.getScene().getWindow());

        if (chosenFile != null) {
            try {
                File validatedImage = ImagePathUtil.validateAndConvertImage(chosenFile);
                selectedImage = validatedImage;
                imageNameLabel.setText(selectedImage.getName());
                String imageUri = selectedImage.toURI().toString();
                Image img = new Image(imageUri);
                if (!img.isError()) {
                    imagePreview.setImage(img);
                    updateImagePreview();
                    UIUtils.showToastNotification("Image selected successfully");
                } else {
                    UIUtils.showAlert("Image Error", null, "The selected image could not be displayed. Please try a different image.", "ERROR");
                }
            } catch (Exception e) {
                e.printStackTrace();
                UIUtils.showAlert("Image Error", null, "The selected image could not be processed. Please try a different image format like PNG.", "ERROR");
            }
        }
    }

    @FXML
    private void handleGenerateImage() {
        String prompt = titleField.getText() + " " + summaryField.getText();
        if (prompt.trim().isEmpty()) {
            UIUtils.showAlert("Input Required", null, "Please enter a title or summary to generate an image.", "WARNING");
            return;
        }

        generateImageButton.setDisable(true);
        UIUtils.showAlert("Info", null, "Image generation started. Please wait...", "INFORMATION");

        try {
            Map<String, Object> response = geminiService.generateImage(prompt);
            if (response != null && response.containsKey("image_uri")) {
                String imageUri = (String) response.get("image_uri");
                File tempImage = new File(java.net.URI.create(imageUri));
                // Copy to blog images directory
                String destFileName = ImagePathUtil.copyImageToBlogDirectory(tempImage);
                File blogImage = new File(ImagePathUtil.getBlogImagesPath(), destFileName);
                selectedImage = blogImage;
                imageNameLabel.setText(selectedImage.getName());
                imagePreview.setImage(new Image(blogImage.toURI().toString()));
                updateImagePreview();
                UIUtils.showToastNotification("Image generated successfully!");
            } else {
                UIUtils.showAlert("Generation Failed", null, "Failed to generate image. Please try again.", "ERROR");
            }
        } catch (Exception e) {
            e.printStackTrace();
            UIUtils.showAlert("Generation Error", null, "Error generating image: " + e.getMessage(), "ERROR");
        } finally {
            generateImageButton.setDisable(false);
        }
    }

    @FXML
    private void handleGenerateTextFromImage() {
        if (selectedImage == null) {
            UIUtils.showAlert("No Image", null, "Please select an image first to generate text from.", "WARNING");
            return;
        }

        generateTextFromImageButton.setDisable(true);
        UIUtils.showAlert("Info", null, "Text generation from image started. Please wait...", "INFORMATION");

        try {
            byte[] imageBytes = java.nio.file.Files.readAllBytes(selectedImage.toPath());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            Map<String, String> response = geminiImageToTextService.generateTextFromImage(base64Image);
            if (response != null && !response.isEmpty()) {
                titleField.setText(response.getOrDefault("title", ""));
                summaryField.setText(response.getOrDefault("summary", ""));
                contentArea.setText(response.getOrDefault("content", ""));
                UIUtils.showToastNotification("Text generated from image successfully!");
            } else {
                UIUtils.showAlert("Analysis Failed", null, "Could not generate text from the image. Please try a different image.", "ERROR");
            }
        } catch (Exception e) {
            e.printStackTrace();
            UIUtils.showAlert("Analysis Error", null, "Error analyzing image: " + e.getMessage(), "ERROR");
        } finally {
            generateTextFromImageButton.setDisable(false);
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        Blog blog = new Blog();
        blog.setTitre(titleField.getText().trim());
        blog.setResume(summaryField.getText().trim());
        blog.setContenu(contentArea.getText());
        blog.setDatePublication(LocalDateTime.now());
        blog.setAuteurId(getCurrentUserId());
        blog.setStatus("Draft"); // Client blogs default to Draft

        if (selectedImage != null) {
            try {
                String destFileName = ImagePathUtil.copyImageToBlogDirectory(selectedImage);
                blog.setImage(destFileName);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to save image: " + e.getMessage());
                return;
            }
        }

        BlogService blogService = new BlogService();
        blogService.ajouter(blog);
        showAlert("Success", "Blog added successfully");

        saved = true;
        loadPreviousBlogs();

        // Navigate back to client blog view
        try {
            com.esprit.tests.MainprogGUI app = com.esprit.tests.MainprogGUI.getInstance();
            if (app != null) {
                app.showClientDashboardScene();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to navigate back: " + e.getMessage());
        }
    }

    private int getCurrentUserId() {
        try {
            com.esprit.utils.UserSession session = com.esprit.utils.UserSession.getInstance();
            if (session != null && session.getUser() != null) {
                return session.getUser().getId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private boolean validateInput() {
        String title = titleField.getText().trim();
        String summary = summaryField.getText().trim();
        String content = contentArea.getText().trim();

        if (title.isEmpty()) {
            UIUtils.showAlert("Validation Error", null, "Title is required.", "WARNING");
            titleField.requestFocus();
            return false;
        }

        if (summary.isEmpty()) {
            UIUtils.showAlert("Validation Error", null, "Summary is required.", "WARNING");
            summaryField.requestFocus();
            return false;
        }

        if (content.isEmpty()) {
            UIUtils.showAlert("Validation Error", null, "Content is required.", "WARNING");
            contentArea.requestFocus();
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message) {
        UIUtils.showAlert(title, null, message, "ERROR");
    }

    @FXML
    private void handleBack() {
        try {
            // Always return to the client BlogView (BlogView.fxml)
            if (parentContentPane != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogView.fxml"));
                Parent root = loader.load();
                parentContentPane.getChildren().setAll(root);
                
                // Restore the active tab state in the client dashboard
                MainprogGUI mainApp = MainprogGUI.getInstance();
                if (mainApp != null) {
                    // Find the client dashboard controller and restore active tab
                    javafx.scene.Scene scene = parentContentPane.getScene();
                    if (scene != null) {
                        javafx.scene.Node rootNode = scene.getRoot();
                        if (rootNode instanceof javafx.scene.layout.BorderPane) {
                            javafx.scene.layout.BorderPane borderPane = (javafx.scene.layout.BorderPane) rootNode;
                            javafx.scene.Node topNode = borderPane.getTop();
                            if (topNode != null) {
                                // Look for the client dashboard controller in the top section
                                javafx.fxml.FXMLLoader topLoader = (javafx.fxml.FXMLLoader) topNode.getUserData();
                                if (topLoader != null) {
                                    Object controller = topLoader.getController();
                                    if (controller instanceof ClientDashboardController) {
                                        ((ClientDashboardController) controller).restoreActiveTab();
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogView.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) titleField.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to navigate to blog view: " + e.getMessage());
        }
    }

    private void setupRealTimeValidation() {
        // Title validation
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateCharCount(titleCharCount, newVal, TITLE_MAX_LENGTH);
            validateTitle(newVal);
        });

        // Summary validation
        summaryField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateCharCount(summaryCharCount, newVal, SUMMARY_MAX_LENGTH);
            validateSummary(newVal);
        });

        // Content validation
        contentArea.textProperty().addListener((obs, oldVal, newVal) -> {
            updateCharCount(contentCharCount, newVal, CONTENT_MAX_LENGTH);
            validateContent(newVal);
        });
    }

    private void updateCharCount(Label charCountLabel, String text, int maxLength) {
        int currentLength = text != null ? text.length() : 0;
        charCountLabel.setText(currentLength + "/" + maxLength);
        
        if (currentLength > maxLength * 0.9) {
            charCountLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else if (currentLength > maxLength * 0.7) {
            charCountLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
        } else {
            charCountLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            titleValidationLabel.setText("Title is required");
            titleField.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 3;");
        } else if (title.length() > TITLE_MAX_LENGTH) {
            titleValidationLabel.setText("Title too long");
            titleField.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 3;");
        } else {
            titleValidationLabel.setText("");
            titleField.setStyle("-fx-border-color: #2ecc71; -fx-border-radius: 3;");
        }
    }

    private void validateSummary(String summary) {
        if (summary == null || summary.trim().isEmpty()) {
            summaryValidationLabel.setText("Summary is required");
            summaryField.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 3;");
        } else if (summary.length() > SUMMARY_MAX_LENGTH) {
            summaryValidationLabel.setText("Summary too long");
            summaryField.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 3;");
        } else {
            summaryValidationLabel.setText("");
            summaryField.setStyle("-fx-border-color: #2ecc71; -fx-border-radius: 3;");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            contentValidationLabel.setText("Content is required");
            contentArea.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 3;");
        } else if (content.length() > CONTENT_MAX_LENGTH) {
            contentValidationLabel.setText("Content too long");
            contentArea.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 3;");
        } else {
            contentValidationLabel.setText("");
            contentArea.setStyle("-fx-border-color: #2ecc71; -fx-border-radius: 3;");
        }
    }

    @FXML
    private void refreshPreviousBlogs() {
        loadPreviousBlogs();
        UIUtils.showToastNotification("Previous blogs refreshed");
    }

    @FXML
    private void handlePreview() {
        if (!validateInput()) {
            showAlert("Validation Error", "Please fix the validation errors before previewing.");
            return;
        }
        
        try {
            // Create a preview window
            Stage previewStage = new Stage();
            previewStage.setTitle("Blog Preview - " + titleField.getText());
            
            // Load the blog detail view for preview
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogDetailView.fxml"));
            Parent root = loader.load();
            
            BlogDetailController controller = loader.getController();
            Blog previewBlog = createBlogFromForm("Preview");
            controller.setBlog(previewBlog);
            
            // Disable interaction buttons in preview
            controller.disableInteractionButtons();
            
            Scene scene = new Scene(root, 800, 600);
            previewStage.setScene(scene);
            previewStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Preview Error", "Failed to open preview: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveDraft() {
        if (!validateInput()) {
            return;
        }

        try {
            Blog newBlog = createBlogFromForm("DRAFT");
            if (editMode) {
                newBlog.setId(editingBlogId);
                blogService.modifier(newBlog);
            } else {
                blogService.ajouter(newBlog);
            }
            
            UIUtils.showSuccessAlert("Draft Saved", "Success", "Your blog draft has been saved successfully!");
            saved = true;
            if (!editMode) {
                clearForm();
            }
            loadPreviousBlogs();
        } catch (Exception e) {
            e.printStackTrace();
            UIUtils.showAlert("Save Error", null, "Error saving draft: " + e.getMessage(), "ERROR");
        }
    }

    @FXML
    private void handlePublish() {
        if (!validateInput()) {
            return;
        }

        try {
            Blog newBlog = createBlogFromForm("PUBLISHED");
            if (editMode) {
                newBlog.setId(editingBlogId);
                blogService.modifier(newBlog);
            } else {
                blogService.ajouter(newBlog);
            }
            
            UIUtils.showSuccessAlert("Blog Published", "Success", "Your blog has been published successfully!");
            saved = true;
            if (!editMode) {
                clearForm();
            }
            loadPreviousBlogs();
        } catch (Exception e) {
            e.printStackTrace();
            UIUtils.showAlert("Publish Error", null, "Error publishing blog: " + e.getMessage(), "ERROR");
        }
    }

    private Blog createBlogFromForm(String status) {
        Blog blog = new Blog();
        blog.setTitre(titleField.getText().trim());
        blog.setResume(summaryField.getText().trim());
        blog.setContenu(contentArea.getText());
        blog.setDatePublication(LocalDateTime.now());
        blog.setAuteurId(getCurrentUserId());
        blog.setStatus(status);

        // Handle image logic for both new blogs and updates
        if (selectedImage != null) {
            try {
                String destFileName = ImagePathUtil.copyImageToBlogDirectory(selectedImage);
                blog.setImage(destFileName);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to save image: " + e.getMessage());
            }
        } else if (editMode && this.blog != null && this.blog.getImage() != null && !this.blog.getImage().isEmpty()) {
            // If editing and no new image selected, keep the existing image
            blog.setImage(this.blog.getImage());
        }
        
        return blog;
    }

    private void updateImagePreview() {
        if (imagePreview.getImage() != null) {
            noImageLabel.setVisible(false);
            imagePreview.setVisible(true);
        } else {
            noImageLabel.setVisible(true);
            imagePreview.setVisible(false);
        }
    }

    @Override
    public void finalize() {
        // No need to cancel any timers or clean up resources
    }

    public void loadBlogForEdit(Blog blogToEdit) {
        this.blog = blogToEdit;
        this.editMode = true;
        this.editingBlogId = blogToEdit.getId();
        titleField.setText(blogToEdit.getTitre());
        summaryField.setText(blogToEdit.getResume());
        contentArea.setText(blogToEdit.getContenu());
        if (blogToEdit.getImage() != null && !blogToEdit.getImage().isEmpty()) {
            File imageFile = new File(ImagePathUtil.getBlogImagesPath(), blogToEdit.getImage());
            if (imageFile.exists()) {
                selectedImage = imageFile;
                imageNameLabel.setText(selectedImage.getName());
                imagePreview.setImage(new Image(imageFile.toURI().toString()));
                updateImagePreview();
            }
        }
        // Show Save Changes, hide Save as Draft/Publish
        saveChangesButton.setVisible(true);
        saveDraftButton.setVisible(false);
        publishButton.setVisible(false);
        // Show Delete Blog button in edit mode
        if (deleteBlogButton != null) {
            deleteBlogButton.setVisible(true);
        }
    }

    @FXML
    private void handleDeleteBlog() {
        if (blog == null || blog.getId() == 0) {
            UIUtils.showAlert("No Blog", null, "No blog selected for deletion.", "WARNING");
            return;
        }

        boolean confirmed = UIUtils.showConfirmation("Delete Blog", 
            "Are you sure you want to delete this blog? This action cannot be undone.");
        
        if (confirmed) {
            try {
                blogService.supprimer(blog);
                UIUtils.showSuccessAlert("Blog Deleted", "Success", "Blog has been deleted successfully!");
                handleBack();
            } catch (Exception e) {
                e.printStackTrace();
                UIUtils.showAlert("Delete Error", null, "Error deleting blog: " + e.getMessage(), "ERROR");
            }
        }
    }

    @FXML
    private void handleSaveChanges() {
        if (!validateInput()) {
            return;
        }

        if (blog == null || blog.getId() == 0) {
            UIUtils.showAlert("No Blog", null, "No blog selected for editing.", "WARNING");
            return;
        }

        try {
            Blog updatedBlog = createBlogFromForm(blog.getStatus());
            updatedBlog.setId(blog.getId());
            blogService.modifier(updatedBlog);
            
            UIUtils.showSuccessAlert("Changes Saved", "Success", "Your blog changes have been saved successfully!");
            saved = true;
            loadPreviousBlogs();
        } catch (Exception e) {
            e.printStackTrace();
            UIUtils.showAlert("Save Error", null, "Error saving changes: " + e.getMessage(), "ERROR");
        }
    }

    // Add this method to clear the form fields
    private void clearForm() {
        titleField.clear();
        summaryField.clear();
        contentArea.clear();
        imageNameLabel.setText("No image selected");
        imagePreview.setImage(null);
        selectedImage = null;
        updateImagePreview();
        titleValidationLabel.setText("");
        summaryValidationLabel.setText("");
        contentValidationLabel.setText("");
        updateCharCount(titleCharCount, "", TITLE_MAX_LENGTH);
        updateCharCount(summaryCharCount, "", SUMMARY_MAX_LENGTH);
        updateCharCount(contentCharCount, "", CONTENT_MAX_LENGTH);
    }
}
