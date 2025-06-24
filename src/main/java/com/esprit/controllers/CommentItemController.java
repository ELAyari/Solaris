package com.esprit.controllers;

import com.esprit.models.Commentaire;
import com.esprit.models.CommentReply;
import com.esprit.models.User;
import com.esprit.models.UserProfile;
import com.esprit.Services.CommentaireService;
import com.esprit.Services.CommentReplyService;
import com.esprit.Services.UserService;
import com.esprit.Services.UserProfileService;
import com.esprit.utils.UserSession;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.geometry.Insets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

/**
 * Controller for individual comment items in the blog detail view.
 * Handles displaying the comment, its replies, and actions like reply, edit, and delete.
 * Enhanced with better GIF loading and display capabilities.
 */
public class CommentItemController {

    @FXML private Label authorLabel;
    @FXML private Label dateLabel;
    @FXML private TextArea contentArea;
    @FXML private Button replyButton;
    @FXML private Button likeButton;
    @FXML private Label likeCountLabel;
    @FXML private VBox repliesContainer;
    @FXML private TextField replyField;
    @FXML private Button submitReplyButton;
    @FXML private HBox replyBox;
    @FXML private VBox contentContainer; // Container for text and GIFs
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button gifReplyButton;
    @FXML private VBox gifReplyPickerPane;
    
    private Commentaire comment;
    private CommentaireService commentaireService;
    private CommentReplyService replyService;
    private User currentUser;
    private int currentUserId = 1; // Will be set from the currentUser when available
    private UserService userService;
    private UserProfileService userProfileService;
    private BlogDetailController parentController;
    
    // Cache for GIF images to prevent reloading
    private static final Map<String, Image> GIF_CACHE = new ConcurrentHashMap<>();
    
    // Pattern to detect GIF URLs in comments
    private static final Pattern GIF_URL_PATTERN = Pattern.compile("https?://[^\\s]+\\.gif");
    
    private boolean isEditing = false;
    
    public void initialize() {
        try {
            commentaireService = new CommentaireService();
            replyService = new CommentReplyService();
            userService = new UserService();
            userProfileService = new UserProfileService();
            
            // Get the current user from the session
            User sessionUser = UserSession.getInstance().getUser();
            setCurrentUser(sessionUser);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error initializing CommentItemController: " + e.getMessage());
        }
        
        // Hide reply field and GIF picker initially
        replyBox.setVisible(false);
        replyBox.setManaged(false);
        gifReplyPickerPane.setVisible(false);
        gifReplyPickerPane.setManaged(false);
        
        // Set up reply button action
        replyButton.setOnAction(e -> toggleReplyField());
        
        // Set up submit reply button action
        submitReplyButton.setOnAction(e -> submitReply());
        
        // Set up like button action
        likeButton.setOnAction(e -> likeComment());
        
        // Set up GIF picker for replies (inline below replyBox)
        gifReplyButton.setOnAction(e -> {
            if (gifReplyPickerPane.isVisible()) {
                gifReplyPickerPane.setVisible(false);
                gifReplyPickerPane.setManaged(false);
                gifReplyPickerPane.getChildren().clear();
                return;
            }
            gifReplyPickerPane.getChildren().clear();
            gifReplyPickerPane.setVisible(true);
            gifReplyPickerPane.setManaged(true);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/GifPickerView.fxml"));
                VBox gifPickerRoot = loader.load();
                GifPickerController gifPickerController = loader.getController();
                gifReplyPickerPane.getChildren().add(gifPickerRoot);
                gifPickerController.setGifSelectListener(gifUrl -> {
                    int pos = replyField.getCaretPosition();
                    replyField.insertText(pos, gifUrl);
                    gifReplyPickerPane.setVisible(false);
                    gifReplyPickerPane.setManaged(false);
                    gifReplyPickerPane.getChildren().clear();
                });
                gifPickerController.getGifCloseButton().setOnAction(ev -> {
                    gifReplyPickerPane.setVisible(false);
                    gifReplyPickerPane.setManaged(false);
                    gifReplyPickerPane.getChildren().clear();
                });
                gifPickerController.loadTrendingGifs();
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("GIF Picker Error", "Failed to open GIF picker: " + ex.getMessage());
                gifReplyPickerPane.setVisible(false);
                gifReplyPickerPane.setManaged(false);
                gifReplyPickerPane.getChildren().clear();
            }
        });
    }
    
    /**
     * Set the comment to display
     */
    public void setComment(Commentaire comment, BlogDetailController parentController) {
        this.comment = comment;
        this.parentController = parentController;
        
        // Get current user from parent controller if available
        if (parentController != null && parentController.getCurrentUser() != null) {
            setCurrentUser(parentController.getCurrentUser());
        }
        
        // Set comment data
        authorLabel.setText(getAuthorName(comment.getAuteurId()));
        dateLabel.setText(formatDate(comment.getDateCommentaire()));
        
        // Try to load user profile picture if available
        if (authorLabel.getGraphic() == null) {
            loadUserProfilePicture(comment.getAuteurId());
        }
        
        // Process content to display text and GIFs properly
        processCommentContent(comment.getContenu());
        
        // Set like count and button state
        updateLikeCount();
        updateLikeButtonState();
        
        // Load replies
        loadReplies();
        
        // Show Edit/Delete buttons only for the author
        if (comment.getAuteurId() == currentUserId) {
            editButton.setVisible(true);
            editButton.setOnAction(e -> startEdit());
            deleteButton.setVisible(true);
            deleteButton.setOnAction(e -> handleDeleteComment());
        } else {
            editButton.setVisible(false);
            deleteButton.setVisible(false);
        }
    }
    
    /**
     * Process comment content to separate text and GIFs
     */
    private void processCommentContent(String content) {
        // Check if the content contains GIFs
        Matcher matcher = GIF_URL_PATTERN.matcher(content);
        boolean hasGifs = matcher.find();
        
        if (!hasGifs) {
            // If no GIFs, just use the regular TextArea from FXML
            contentArea.setText(content);
            contentArea.setVisible(true);
            contentArea.setManaged(true);
            
            // If we previously created a content container, hide it and clear children
            if (contentContainer != null) {
                contentContainer.getChildren().clear();
                contentContainer.setVisible(false);
                contentContainer.setManaged(false);
            }
            return;
        }
        
        // If we have GIFs, we need to use our custom container
        // Hide the original TextArea and clear its text to avoid duplication
        contentArea.clear();
        contentArea.setVisible(false);
        contentArea.setManaged(false);
        
        // Create or clear the content container
        if (contentContainer != null) {
            contentContainer.getChildren().clear();
            contentContainer.setVisible(true);
            contentContainer.setManaged(true);
        } else {
            // If contentContainer doesn't exist, create it and add it to the scene
            contentContainer = new VBox(5);
            contentContainer.setPadding(new Insets(5));
            // Add it after the content area in the parent container
            VBox parent = (VBox) contentArea.getParent();
            int index = parent.getChildren().indexOf(contentArea);
            if (index >= 0) {
                parent.getChildren().add(index + 1, contentContainer);
            }
        }
        
        // Reset matcher to process all GIFs
        matcher = GIF_URL_PATTERN.matcher(content);
        int lastEnd = 0;
        
        // Process each match
        while (matcher.find()) {
            // Add text before the GIF
            String textBefore = content.substring(lastEnd, matcher.start());
            if (!textBefore.trim().isEmpty()) {
                Text text = new Text(textBefore);
                TextFlow textFlow = new TextFlow(text);
                contentContainer.getChildren().add(textFlow);
            }
            
            // Add the GIF
            String gifUrl = matcher.group();
            loadAndDisplayGif(gifUrl);
            
            lastEnd = matcher.end();
        }
        
        // Add remaining text after the last GIF
        if (lastEnd < content.length()) {
            String textAfter = content.substring(lastEnd);
            if (!textAfter.trim().isEmpty()) {
                Text text = new Text(textAfter);
                TextFlow textFlow = new TextFlow(text);
                contentContainer.getChildren().add(textFlow);
            }
        }
    }
    
    /**
     * Load and display a GIF in the content container
     */
    private void loadAndDisplayGif(String gifUrl) {
        // Create a placeholder for the GIF
        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        
        // Add a progress indicator while loading
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(50, 50);
        
        // Add placeholder to container
        VBox gifContainer = new VBox(5);
        gifContainer.setAlignment(javafx.geometry.Pos.CENTER);
        gifContainer.getChildren().addAll(progress);
        contentContainer.getChildren().add(gifContainer);
        
        // Load the GIF in background
        Task<Image> loadTask = new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                // Check if GIF is already in cache
                if (GIF_CACHE.containsKey(gifUrl)) {
                    return GIF_CACHE.get(gifUrl);
                }
                
                // Load image with background loading and caching enabled
                Image image = new Image(gifUrl, 150, 150, true, true, true);
                if (!image.isError()) {
                    GIF_CACHE.put(gifUrl, image);
                }
                return image;
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            Image image = loadTask.getValue();
            if (image != null && !image.isError()) {
                // Replace progress indicator with the GIF
                Platform.runLater(() -> {
                    imageView.setImage(image);
                    gifContainer.getChildren().clear();
                    gifContainer.getChildren().add(imageView);
                });
            } else {
                // Show error message if GIF couldn't be loaded
                Platform.runLater(() -> {
                    gifContainer.getChildren().clear();
                    gifContainer.getChildren().add(new Label("[GIF could not be loaded]"));
                });
            }
        });
        
        loadTask.setOnFailed(e -> {
            // Show error message if GIF couldn't be loaded
            Platform.runLater(() -> {
                gifContainer.getChildren().clear();
                gifContainer.getChildren().add(new Label("[GIF could not be loaded]"));
            });
        });
        
        // Start loading in background
        new Thread(loadTask).start();
    }
    
    /**
     * Toggle the visibility of the reply field
     */
    private void toggleReplyField() {
        replyBox.setVisible(!replyBox.isVisible());
        replyBox.setManaged(replyBox.isVisible());
        
        if (replyBox.isVisible()) {
            replyField.requestFocus();
        }
    }
    
    /**
     * Submit a reply to this comment
     */
    private void submitReply() {
        String replyContent = replyField.getText().trim();
        System.out.println("submitReply called with content: " + replyContent);
        if (replyContent.isEmpty()) {
            showAlert("Error", "Reply cannot be empty");
            return;
        }
        
        // Create and save the reply
        CommentReply reply = new CommentReply();
        reply.setContenu(replyContent);
        reply.setDateReply(LocalDateTime.now());
        reply.setAuteurId(currentUserId);
        reply.setCommentId(comment.getId());
        
        System.out.println("Adding reply to service: " + replyContent);
        replyService.ajouter(reply);
        
        // Clear the reply field and hide it
        replyField.clear();
        replyBox.setVisible(false);
        replyBox.setManaged(false);
        
        // Reload replies
        loadReplies();
        
        // Removed call to parentController.refreshComments() to prevent duplicate replies
        /*
        if (parentController != null) {
            parentController.refreshComments();
        }
        */
    }
    
    /**
     * Like or unlike the comment
     */
    private void likeComment() {
        boolean hasLiked = commentaireService.hasUserLikedComment(comment.getId(), currentUserId);
        boolean success;
        if (hasLiked) {
            success = commentaireService.unlikeComment(comment.getId(), currentUserId);
            if (success) {
                updateLikeCount();
                updateLikeButtonState();
            } else {
                showAlert("Error", "Failed to unlike this comment.");
            }
        } else {
            success = commentaireService.likeComment(comment.getId(), currentUserId);
            if (success) {
                updateLikeCount();
                updateLikeButtonState();
            } else {
                showAlert("Error", "Failed to like this comment.");
            }
        }
    }
    
    /**
     * Load and display replies for this comment
     */
    private void loadReplies() {
        System.out.println("loadReplies called for comment ID: " + comment.getId());
        repliesContainer.getChildren().clear();
        
        List<CommentReply> replies = replyService.getRepliesByCommentId(comment.getId());
        System.out.println("Number of replies loaded: " + replies.size());
        
        // Deduplicate replies by ID
        java.util.Set<Integer> seenReplyIds = new java.util.HashSet<>();
        
        for (CommentReply reply : replies) {
            if (seenReplyIds.contains(reply.getId())) {
                System.out.println("Skipping duplicate reply ID: " + reply.getId());
                continue;
            }
            seenReplyIds.add(reply.getId());
            
            // Create a simple reply view
            VBox replyView = createReplyView(reply);
            repliesContainer.getChildren().add(replyView);
        }
    }
    
    /**
     * Create a simple view for a reply
     */
    private VBox createReplyView(CommentReply reply) {
        System.out.println("Creating reply view for reply ID: " + reply.getId());
        VBox replyBox = new VBox(5);
        replyBox.setPadding(new Insets(5, 5, 5, 15)); // Indent replies
        replyBox.getStyleClass().add("reply-box");
        
        // Author and date
        HBox headerBox = new HBox(10);
        
        // Try to load user profile picture
        ImageView userPic = new ImageView();
        userPic.setFitWidth(24);
        userPic.setFitHeight(24);
        userPic.setPreserveRatio(true);
        loadUserProfilePictureToImageView(reply.getAuteurId(), userPic);
        
        Label authorLabel = new Label(getAuthorName(reply.getAuteurId()));
        authorLabel.getStyleClass().add("reply-author");
        Label dateLabel = new Label(formatDate(reply.getDateReply()));
        dateLabel.getStyleClass().add("reply-date");
        headerBox.getChildren().addAll(userPic, authorLabel, dateLabel);
        
        // Add header to the reply box
        replyBox.getChildren().add(headerBox);
        
        // Process content to display text and GIFs properly
        String content = reply.getContenu();
        
        // Find GIF URLs in the content
        Matcher matcher = GIF_URL_PATTERN.matcher(content);
        int lastEnd = 0;
        boolean hasGifs = false;
        VBox contentBox = new VBox(5);
        
        // Process each match
        while (matcher.find()) {
            hasGifs = true;
            // Add text before the GIF
            String textBefore = content.substring(lastEnd, matcher.start());
            if (!textBefore.trim().isEmpty()) {
                Text text = new Text(textBefore);
                text.getStyleClass().add("reply-content");
                TextFlow textFlow = new TextFlow(text);
                contentBox.getChildren().add(textFlow);
            }
            
            // Add the GIF
            String gifUrl = matcher.group();
            
            // Create a placeholder for the GIF
            ImageView imageView = new ImageView();
            imageView.setFitWidth(100); // Smaller size for replies
            imageView.setFitHeight(100);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setCache(true);
            
            // Check if GIF is already in cache
            if (GIF_CACHE.containsKey(gifUrl)) {
                imageView.setImage(GIF_CACHE.get(gifUrl));
                contentBox.getChildren().add(imageView);
            } else {
                // Add a progress indicator while loading
                ProgressIndicator progress = new ProgressIndicator();
                progress.setMaxSize(30, 30);
                
                // Add placeholder to container
                VBox gifContainer = new VBox(5);
                gifContainer.setAlignment(javafx.geometry.Pos.CENTER);
                gifContainer.getChildren().add(progress);
                contentBox.getChildren().add(gifContainer);
                
                // Load the GIF in background
                final String finalGifUrl = gifUrl;
                Task<Image> loadTask = new Task<Image>() {
                    @Override
                    protected Image call() throws Exception {
                        // Load image with background loading and caching enabled
                        return new Image(finalGifUrl, 100, 100, true, true, true);
                    }
                };
                loadTask.setOnSucceeded(e -> {
                    Image image = loadTask.getValue();
                    if (image != null && !image.isError()) {
                        // Cache the image
                        GIF_CACHE.put(finalGifUrl, image);
                        // Replace progress indicator with the GIF
                        Platform.runLater(() -> {
                            imageView.setImage(image);
                            gifContainer.getChildren().clear();
                            gifContainer.getChildren().add(imageView);
                        });
                    } else {
                        // Show error message if GIF couldn't be loaded
                        Platform.runLater(() -> {
                            gifContainer.getChildren().clear();
                            gifContainer.getChildren().add(new Label("[GIF]"));
                        });
                    }
                });
                
                // Start loading in background
                new Thread(loadTask).start();
            }
            
            lastEnd = matcher.end();
        }
        
        // Add remaining text after the last GIF
        if (lastEnd < content.length()) {
            String textAfter = content.substring(lastEnd);
            if (!textAfter.trim().isEmpty()) {
                Text contentText = new Text(textAfter);
                contentText.getStyleClass().add("reply-content");
                contentText.setWrappingWidth(contentArea.getWidth() - 30);
                TextFlow textFlow = new TextFlow(contentText);
                contentBox.getChildren().add(textFlow);
            }
        }
        
        // Add content box to the reply box
        replyBox.getChildren().add(contentBox);
        
        // Action buttons (edit/delete) for author
        if (reply.getAuteurId() == currentUserId) {
            HBox actions = new HBox(5);
            actions.setPadding(new Insets(0, 0, 0, 0));
            actions.setStyle("-fx-alignment: CENTER_RIGHT;");
            Button editBtn = new Button("Edit");
            editBtn.getStyleClass().add("btn-edit");
            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().add("btn-delete");
            actions.getChildren().addAll(editBtn, deleteBtn);
            replyBox.getChildren().add(actions);
            
            // Edit logic
            editBtn.setOnAction(e -> {
                if (!contentBox.getChildren().isEmpty() && contentBox.getChildren().get(0) instanceof TextFlow) {
                    TextFlow tf = (TextFlow) contentBox.getChildren().get(0);
                    if (!tf.getChildren().isEmpty() && tf.getChildren().get(0) instanceof Text) {
                        Text text = (Text) tf.getChildren().get(0);
                        TextArea editArea = new TextArea(text.getText());
                        editArea.setWrapText(true);
                        contentBox.getChildren().set(0, editArea);
                        editBtn.setText("Save");
                        Button cancelBtn = new Button("Cancel");
                        cancelBtn.getStyleClass().add("btn-cancel");
                        actions.getChildren().add(cancelBtn);
                        cancelBtn.setOnAction(ev -> {
                            contentBox.getChildren().set(0, tf);
                            editBtn.setText("Edit");
                            actions.getChildren().remove(cancelBtn);
                        });
                        editBtn.setOnAction(ev -> {
                            String newContent = editArea.getText().trim();
                            if (!newContent.isEmpty() && !newContent.equals(reply.getContenu())) {
                                reply.setContenu(newContent);
                                reply.setDateReply(LocalDateTime.now());
                                replyService.modifier(reply);
                                loadReplies();
                            }
                        });
                    }
                }
            });
            // Delete logic
            deleteBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Reply");
                confirm.setHeaderText("Are you sure you want to delete this reply?");
                confirm.setContentText("This action cannot be undone.");
                if (confirm.showAndWait().get() == ButtonType.OK) {
                    replyService.supprimer(reply);
                    loadReplies();
                }
            });
        }
        return replyBox;
    }
    
    /**
     * Update the like count display
     */
    private void updateLikeCount() {
        // No longer used, like count is included in button text
    }
    
    /**
     * Update the like button text based on whether the user has liked the comment
     */
    private void updateLikeButtonState() {
        int likeCount = commentaireService.getLikeCountForComment(comment.getId());
        boolean hasLiked = commentaireService.hasUserLikedComment(comment.getId(), currentUserId);
        String likeText = hasLiked ? "❤ Liked" : "❤ Like";
        likeButton.setText(likeText + " (" + likeCount + ")");
    }
    
    /**
     * Format a date for display
     */
    private String formatDate(LocalDateTime date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' HH:mm"));
    }
    
    /**
     * Get the author name from the author ID
     */
    private String getAuthorName(int authorId) {
        // Get actual author name from database
        User user = userService.getById(authorId);
        if (user != null) {
            return user.getNom() + " " + user.getPrenom();
        }
        return "User " + authorId; // Fallback if user not found
    }
    
    /**
     * Sets the current user and updates the UI accordingly
     * @param user The current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        if (user != null) {
            // Update the currentUserId from the user object
            this.currentUserId = user.getId();
            System.out.println("Comment Item: Current user set to " + user.getNom() + " " + user.getPrenom() + " (ID: " + user.getId() + ")");
        } else {
            // Default to 1 if no user is logged in (for testing purposes)
            this.currentUserId = 1;
            System.out.println("Comment Item: No user logged in, using default ID: " + currentUserId);
        }
        
        // Use the currentUser field to update UI elements if needed
        updateUIBasedOnCurrentUser();
    }
    
    /**
     * Update UI elements based on the current user
     * For example, show/hide edit/delete buttons if the comment belongs to the current user
     */
    private void updateUIBasedOnCurrentUser() {
        if (comment != null && currentUser != null) {
            // Show edit/delete buttons only if the current user is the author of the comment
            boolean isAuthor = comment.getAuteurId() == currentUser.getId();
            // You can add edit/delete buttons to the UI and control their visibility here
        }
    }
    
    /**
     * Show an alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Load user profile picture and set it to the author label
     * @param userId The user ID to load the profile picture for
     */
    private void loadUserProfilePicture(int userId) {
        try {
            // Create an ImageView for the profile picture
            ImageView profilePic = new ImageView();
            profilePic.setFitWidth(24);
            profilePic.setFitHeight(24);
            profilePic.setPreserveRatio(true);
            
            // Load the profile picture
            loadUserProfilePictureToImageView(userId, profilePic);
            
            // Set the ImageView as the graphic for the author label
            authorLabel.setGraphic(profilePic);
            authorLabel.setGraphicTextGap(5);
        } catch (Exception e) {
            System.err.println("Error loading profile picture for user " + userId + ": " + e.getMessage());
        }
    }
    
    /**
     * Load user profile picture to an ImageView
     * @param userId The user ID to load the profile picture for
     * @param imageView The ImageView to set the profile picture to
     */
    private void loadUserProfilePictureToImageView(int userId, ImageView imageView) {
        try {
            if (imageView == null) return;
            
            // Get the user from the database
            User user = userService.getById(userId);
            if (user == null) {
                // Set a default profile picture
                setDefaultProfilePicture(imageView);
                return;
            }
            
            // Get the user profile to check for a profile picture
            UserProfile userProfile = userProfileService.getUserProfileByUserId(userId);
            if (userProfile == null || userProfile.getProfilePhotoUrl() == null || userProfile.getProfilePhotoUrl().isEmpty()) {
                // Set a default profile picture
                setDefaultProfilePicture(imageView);
                return;
            }
            
            // Get the profile picture path
            String profilePicPath = userProfile.getProfilePhotoUrl();
            
            // Validate and fix the URL
            String validUrl = validateAndFixImageUrl(profilePicPath);
            if (validUrl == null) {
                System.err.println("Invalid image URL for user " + userId + ": " + profilePicPath + ". Using default avatar.");
                setDefaultProfilePicture(imageView);
                return;
            }
            
            // Load the profile picture
            try {
                // Load the image
                Image image = new Image(validUrl, 24, 24, true, true);
                imageView.setImage(image);
                
                // Add a clip to make the image circular
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(12, 12, 12);
                imageView.setClip(clip);
            } catch (Exception e) {
                System.err.println("Error loading profile picture: " + e.getMessage());
                setDefaultProfilePicture(imageView);
            }
        } catch (Exception e) {
            System.err.println("Error loading profile picture for user " + userId + ": " + e.getMessage());
            setDefaultProfilePicture(imageView);
        }
    }
    
    /**
     * Validate and fix image URL to ensure it's properly formatted
     */
    private String validateAndFixImageUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        
        url = url.trim();
        
        // Safety check: Reject only actual hardcoded paths that might cause issues
        // Only reject paths that contain specific hardcoded user directories
        if (url.contains("C:/Users/wael/") || url.contains("C:\\Users\\wael\\") || 
            url.contains("/Users/wael/") || url.contains("\\Users\\wael\\") ||
            url.contains("wael.jpeg") || url.contains("wael.jpg") ||
            url.contains("wael.png") || url.contains("wael.gif")) {
            System.err.println("Rejected hardcoded path: " + url + ". Using default avatar.");
            return null;
        }
        
        // If it's already a valid URL, return as is
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file:")) {
            return url;
        }
        
        // If it's a local file path, check if it exists
        try {
            java.io.File file = new java.io.File(url);
            if (file.exists() && file.isFile()) {
                return "file:" + file.getAbsolutePath();
            }
            
            // Try with file: prefix
            if (!url.startsWith("file:")) {
                file = new java.io.File(url);
                if (file.exists() && file.isFile()) {
                    return "file:" + file.getAbsolutePath();
                }
            }
            
            // Try to find the file in the project's profile_photo directory
            String projectRoot = System.getProperty("user.dir");
            String profilePhotoDir = projectRoot + "/src/main/resources/images/profile_photo";
            java.io.File profileDir = new java.io.File(profilePhotoDir);
            
            if (profileDir.exists() && profileDir.isDirectory()) {
                // Extract filename from the URL
                String filename = url;
                if (url.contains("/")) {
                    filename = url.substring(url.lastIndexOf("/") + 1);
                } else if (url.contains("\\")) {
                    filename = url.substring(url.lastIndexOf("\\") + 1);
                }
                
                java.io.File profileFile = new java.io.File(profileDir, filename);
                if (profileFile.exists() && profileFile.isFile()) {
                    return "file:" + profileFile.getAbsolutePath();
                }
            }
            
            // If file doesn't exist, return null to use default avatar
            System.err.println("Image file does not exist: " + url);
            return null;
        } catch (Exception e) {
            System.err.println("Error validating image URL: " + url + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Set a default profile picture to an ImageView
     * @param imageView The ImageView to set the default profile picture to
     */
    private void setDefaultProfilePicture(ImageView imageView) {
        try {
            // Try to load a default profile picture from resources
            String defaultPicPath = "/images/default_avatar.png";
            java.io.InputStream is = getClass().getResourceAsStream(defaultPicPath);
            
            if (is == null) {
                // Try alternative paths
                String[] alternativePaths = {
                    "/images/default.png",
                    "/default.png",
                    "/icons/user.png",
                    "/assets/default_avatar.png"
                };
                
                for (String path : alternativePaths) {
                    is = getClass().getResourceAsStream(path);
                    if (is != null) {
                        break;
                    }
                }
                
                // If still null, create a simple colored circle as default
                if (is == null) {
                    // Create a blank image with a colored circle
                    javafx.scene.layout.StackPane pane = new javafx.scene.layout.StackPane();
                    pane.setPrefSize(24, 24);
                    
                    javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(12);
                    circle.setFill(javafx.scene.paint.Color.LIGHTBLUE);
                    pane.getChildren().add(circle);
                    
                    // Add a text with the first letter of "User"
                    javafx.scene.text.Text text = new javafx.scene.text.Text("U");
                    text.setFill(javafx.scene.paint.Color.WHITE);
                    text.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 12));
                    pane.getChildren().add(text);
                    
                    // Render to an image
                    javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                    params.setFill(javafx.scene.paint.Color.TRANSPARENT);
                    javafx.scene.image.WritableImage image = pane.snapshot(params, null);
                    
                    imageView.setImage(image);
                    return;
                }
            }
            
            // Load the image from the input stream
            Image defaultImage = new Image(is, 24, 24, true, true);
            imageView.setImage(defaultImage);
            
            // Add a clip to make the image circular
            javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(12, 12, 12);
            imageView.setClip(clip);
        } catch (Exception e) {
            System.err.println("Error loading default profile picture: " + e.getMessage());
            // If all else fails, create a simple colored circle
            try {
                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(12, 12, 12);
                circle.setFill(javafx.scene.paint.Color.GRAY);
                
                javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                params.setFill(javafx.scene.paint.Color.TRANSPARENT);
                javafx.scene.image.WritableImage image = circle.snapshot(params, null);
                
                imageView.setImage(image);
                imageView.setClip(circle);
            } catch (Exception ex) {
                // Last resort - don't set any image
                System.err.println("Failed to create fallback profile image: " + ex.getMessage());
            }
        }
    }

    private void startEdit() {
        isEditing = true;
        contentArea.setEditable(true);
        contentArea.setFocusTraversable(true);
        editButton.setVisible(false);
        // Add Save and Cancel buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");
        saveButton.getStyleClass().add("btn-save");
        cancelButton.getStyleClass().add("btn-cancel");
        HBox actions = (HBox) editButton.getParent();
        actions.getChildren().addAll(saveButton, cancelButton);
        saveButton.setOnAction(e -> saveEdit(actions, saveButton, cancelButton));
        cancelButton.setOnAction(e -> cancelEdit(actions, saveButton, cancelButton));
    }

    private void saveEdit(HBox actions, Button saveButton, Button cancelButton) {
        String newContent = contentArea.getText().trim();
        if (!newContent.isEmpty() && !newContent.equals(comment.getContenu())) {
            comment.setContenu(newContent);
            // Update in DB
            com.esprit.Services.CommentaireService service = new com.esprit.Services.CommentaireService();
            service.update(comment);
            if (parentController != null) parentController.refreshComments();
        }
        finishEdit(actions, saveButton, cancelButton);
    }

    private void cancelEdit(HBox actions, Button saveButton, Button cancelButton) {
        contentArea.setText(comment.getContenu());
        finishEdit(actions, saveButton, cancelButton);
    }

    private void finishEdit(HBox actions, Button saveButton, Button cancelButton) {
        isEditing = false;
        contentArea.setEditable(false);
        contentArea.setFocusTraversable(false);
        actions.getChildren().removeAll(saveButton, cancelButton);
        editButton.setVisible(true);
    }

    private void handleDeleteComment() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Comment");
        confirm.setHeaderText("Are you sure you want to delete this comment?");
        confirm.setContentText("This action cannot be undone.");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            commentaireService.supprimer(comment);
            if (parentController != null) parentController.refreshComments();
        }
    }
}
