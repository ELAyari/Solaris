package com.esprit.controllers;

import com.esprit.models.Blog;
import com.esprit.Services.BlogService;
import com.esprit.Services.CommentaireService;
import com.esprit.models.Commentaire;
import com.esprit.models.User;
import com.esprit.utils.ImagePathUtil;
import com.esprit.utils.SceneSwitcher;
import com.esprit.utils.UserSession;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.Parent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.geometry.Insets;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import com.esprit.utils.SocialMediaSharing;
import com.esprit.Services.UserService;

import java.io.IOException;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import javafx.scene.control.ButtonBar;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

public class BlogDetailController implements Initializable {

    @FXML
    private Label titleLabel;

    @FXML
    private Label authorLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private ImageView imageView;

    @FXML
    private TextArea contentArea;

    @FXML
    private Button likeButton;

    @FXML
    private Button commentButton;

    @FXML
    private Button gifPickerButton;

    @FXML
    private Button backButton;

    @FXML
    private Label commentCountLabel;

    @FXML
    private ToggleButton newestFirstToggle;

    @FXML
    private ListView<VBox> commentsListView;

    @FXML
    private VBox gifPickerPane;

    @FXML
    private TextField gifSearchField;

    @FXML
    private javafx.scene.layout.FlowPane gifFlowPane;

    @FXML
    private ScrollPane gifScrollPane;

    @FXML
    private Button gifCloseButton;

    @FXML
    private TextField commentField;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Button prevBlogButton;

    @FXML
    private Button nextBlogButton;

    // Removed duplicate gifPickerPane field to fix duplicate field error

    private GifPickerController gifPickerController;

    private BlogService blogService;
    private CommentaireService commentaireService;
    private Blog currentBlog;
    private List<Commentaire> allComments;
    private User currentUser;
    private int currentUserId = 1; // Will be set from the currentUser when available
    
    // Reference to the parent content pane for fragment-based navigation
    private javafx.scene.layout.StackPane parentContentPane;

    // For navigation
    private List<Blog> blogList;
    private int currentIndex = -1;

    public BlogDetailController() {
        blogService = new BlogService();
        commentaireService = new CommentaireService();
    }
    
    /**
     * Sets the parent content pane for fragment-based navigation
     * @param contentPane The parent StackPane to use for navigation
     */
    public void setParentContentPane(javafx.scene.layout.StackPane contentPane) {
        this.parentContentPane = contentPane;
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
            System.out.println("Blog Detail: Current user set to " + user.getNom() + " " + user.getPrenom() + " (ID: " + user.getId() + ")");
        } else {
            // Default to 1 if no user is logged in (for testing purposes)
            this.currentUserId = 1;
            System.out.println("Blog Detail: No user logged in, using default ID: " + currentUserId);
        }
    }
    
    /**
     * Get the current user
     * @return The current user or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services if they're null
        if (blogService == null) blogService = new BlogService();
        if (commentaireService == null) commentaireService = new CommentaireService();
        
        // Get the current user from the session
        User sessionUser = UserSession.getInstance().getUser();
        setCurrentUser(sessionUser);
        
        // Initialize GIF picker pane but don't load content yet
        gifPickerPane.setVisible(false);
        gifPickerPane.setManaged(false);
        
        // We'll load the GIF picker on demand when the user clicks the GIF button
        // This improves initial loading performance

        // Set up the newest first toggle button
        newestFirstToggle.setSelected(true);
        newestFirstToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            loadComments();
        });

        likeButton.setOnAction(e -> handleLike());
        commentButton.setOnAction(e -> handleAddComment());
        backButton.setOnAction(e -> handleBack());

        // Add listener to scene property to trigger layout update on first showing
        commentsListView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsWin, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.showingProperty().addListener((obsShow, wasShowing, isShowing) -> {
                            if (isShowing) {
                                Platform.runLater(() -> {
                                    commentsListView.requestLayout();
                                    commentsListView.getParent().requestLayout();
                                });
                            }
                        });
                    }
                });
            }
        });

        // Removed the call to gifPickerController.fetchTrendingGifs() here to avoid NPE

        if (prevBlogButton != null) {
            prevBlogButton.setOnAction(e -> navigateToBlog(currentIndex - 1));
        }
        if (nextBlogButton != null) {
            nextBlogButton.setOnAction(e -> navigateToBlog(currentIndex + 1));
        }
    }

    @FXML
    private void handleOpenGifPicker(javafx.event.ActionEvent event) {
        // If GIF picker is already visible, just hide it
        if (gifPickerPane.isVisible()) {
            gifPickerPane.setVisible(false);
            gifPickerPane.setManaged(false);
            return;
        }
        
        // Show loading indicator while preparing GIF picker
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);
        VBox loadingBox = new VBox(loadingIndicator);
        loadingBox.setAlignment(javafx.geometry.Pos.CENTER);
        loadingBox.setPadding(new Insets(20));
        
        // Clear and prepare the GIF picker pane
        gifPickerPane.getChildren().clear();
        gifPickerPane.getChildren().add(loadingBox);
        gifPickerPane.setVisible(true);
        gifPickerPane.setManaged(true);
        
        // Make sure the GIF picker is fully visible by scrolling to it if needed
        Platform.runLater(() -> {
            double gifPickerY = gifPickerPane.localToScene(0, 0).getY();
            double sceneHeight = gifPickerPane.getScene().getHeight();
            
            if (gifPickerY + gifPickerPane.getHeight() > sceneHeight) {
                // GIF picker is partially off-screen, scroll to make it visible
                scrollPane.setVvalue(scrollPane.getVvalue() + 0.1); // Adjust as needed
            }
        });
        
        // Use a background task to initialize the GIF picker
        Task<Void> initTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Small delay to ensure smooth UI
                Thread.sleep(100);
                return null;
            }
        };
        
        initTask.setOnSucceeded(e -> {
            // Restore the GIF picker content
            gifPickerPane.getChildren().clear();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/GifPickerView.fxml"));
                VBox gifPickerRoot = loader.load();
                gifPickerController = loader.getController();
                
                // Add the loaded root to gifPickerPane
                gifPickerPane.getChildren().add(gifPickerRoot);
                
                // Set up close button handler
                gifPickerController.getGifCloseButton().setOnAction(closeEvent -> {
                    gifPickerPane.setVisible(false);
                    gifPickerPane.setManaged(false);
                });
                
                // Set up GIF selection listener
                gifPickerController.setGifSelectListener(gifUrl -> {
                    insertGifInComment(gifUrl);
                    gifPickerPane.setVisible(false);
                    gifPickerPane.setManaged(false);
                });
                
                // Apply some animations for a smoother appearance
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), gifPickerRoot);
                fadeIn.setFromValue(0.3);
                fadeIn.setToValue(1.0);
                fadeIn.play();
                
                // Load trending GIFs in background
                gifPickerController.loadTrendingGifs();
            } catch (Exception ex) {
                System.err.println("Error initializing GIF picker: " + ex.getMessage());
                ex.printStackTrace();
                
                // Show error message
                Label errorLabel = new Label("Failed to load GIF picker. Please try again.");
                errorLabel.getStyleClass().add("error-text");
                gifPickerPane.getChildren().clear();
                gifPickerPane.getChildren().add(errorLabel);
            }
        });
        
        new Thread(initTask).start();
    }

    public void setBlog(Blog blog) {
        this.currentBlog = blog;
        updateView();
        setAuthorNameAsync(blog.getAuteurId());
        // Increment view count when blog is set/viewed
        if (currentBlog != null) {
            blogService.incrementViewCount(currentBlog.getId());
            // Update the views count locally
            currentBlog.setViews(currentBlog.getViews() + 1);
        }
    }

    private void setAuthorNameAsync(int authorId) {
        new Thread(() -> {
            try {
                UserService userService = new UserService();
                User user = userService.getUserById(authorId);
                String name = (user != null) ? user.getNom() + " " + user.getPrenom() : "Unknown";
                javafx.application.Platform.runLater(() -> authorLabel.setText("By " + name));
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> authorLabel.setText("By Unknown"));
            }
        }).start();
    }

    private void updateView() {
        if (currentBlog == null) return;

        titleLabel.setText(currentBlog.getTitre());
        dateLabel.setText(currentBlog.getDatePublication() != null ?
                currentBlog.getDatePublication().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "");

        // Format content with paragraphs and styling
        String formattedContent = formatContent(currentBlog.getContenu());
        contentArea.setText(formattedContent);

        // Set likes count with proper formatting
        String likesText = String.format("❤ Like (%d)", currentBlog.getLikes());
        likeButton.setText(likesText);

        // Ensure like button is always enabled to allow toggling
        likeButton.setDisable(false);

        // Load image with fade-in effect
        String defaultImagePath = "/images/default_renewable.png";
        boolean imageLoaded = false;
        if (currentBlog.getImage() != null && !currentBlog.getImage().isEmpty()) {
            try {
                File imageFile = ImagePathUtil.getBlogImageFile(currentBlog.getImage());
                if (imageFile.exists()) {
                    String imageUri = imageFile.toURI().toString();
                    Image image = new Image(imageUri, 800, 400, true, true, true); // full-size for detail
                    image.errorProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            imageView.setImage(new Image(defaultImagePath));
                        }
                    });
                    imageView.setImage(image);
                    imageLoaded = true;
                }
            } catch (Exception e) {
                // fallback below
            }
        }
        if (!imageLoaded) {
            imageView.setImage(new Image(defaultImagePath));
        }

        loadComments();
    }

    private String formatContent(String content) {
        // Split content into paragraphs
        String[] paragraphs = content.split("\n\n");
        StringBuilder formattedContent = new StringBuilder();

        for (String paragraph : paragraphs) {
            // Add proper spacing and formatting
            formattedContent.append(paragraph.trim()).append("\n\n");
        }

        return formattedContent.toString().trim();
    }

    /**
     * Get the author name from the author ID
     * This is a placeholder that should be replaced with actual user data
     */
    public String getAuthorName(int authorId) {
        return ""; // No longer used
    }

    /**
     * Refresh the comments list
     * Called by child comment controllers when comments change
     */
    public void refreshComments() {
        loadComments();
    }

    // Map to track comment items in the list view by their ID
    private Map<Integer, VBox> commentBoxMap = new HashMap<>();
    
    private void loadComments() {
        // Clear the list view but keep track of existing comment boxes
        Map<Integer, VBox> oldCommentBoxMap = new HashMap<>(commentBoxMap);
        commentBoxMap.clear();
        commentsListView.getItems().clear();

        if (commentaireService == null) {
            commentaireService = new CommentaireService();
        }
        
        // Show loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);
        VBox loadingBox = new VBox(loadingIndicator);
        loadingBox.setAlignment(javafx.geometry.Pos.CENTER);
        loadingBox.setPadding(new Insets(20));
        commentsListView.getItems().add(loadingBox);

        Task<List<Commentaire>> task = new Task<>() {
            @Override
            protected List<Commentaire> call() {
                return commentaireService.recupererByBlogId(currentBlog.getId());
            }
        };

        task.setOnSucceeded(e -> {
            allComments = task.getValue();

            // Update comment count label
            commentCountLabel.setText("(" + allComments.size() + ")");

            // Sort comments based on toggle selection
            if (newestFirstToggle.isSelected()) {
                // Sort by newest first
                allComments.sort((c1, c2) -> c2.getDateCommentaire().compareTo(c1.getDateCommentaire()));
            } else {
                // Sort by oldest first
                allComments.sort((c1, c2) -> c1.getDateCommentaire().compareTo(c2.getDateCommentaire()));
            }

            commentsListView.getItems().clear();
            
            // Load all comments at once with reuse of existing comment boxes
            loadAllComments(allComments, oldCommentBoxMap);
        });

        task.setOnFailed(e -> {
            // Handle failure
            commentsListView.getItems().clear();
            Label errorLabel = new Label("Failed to load comments. Please try again.");
            errorLabel.getStyleClass().add("error-text");
            commentsListView.getItems().add(new VBox(errorLabel));
            e.getSource().getException().printStackTrace();
        });

        new Thread(task).start();
    }
    
    /**
     * Load all comments at once with potential reuse of existing comment boxes
     */
    private void loadAllComments(List<Commentaire> comments, Map<Integer, VBox> oldCommentBoxMap) {
        // Clear our tracking map since we're rebuilding the entire list
        commentBoxMap.clear();
        
        for (Commentaire comment : comments) {
            // Skip if we've already added this comment ID (should never happen here, but just in case)
            if (commentBoxMap.containsKey(comment.getId())) {
                System.out.println("Skipping duplicate comment ID during load: " + comment.getId());
                continue;
            }
            
            VBox commentBox;
            
            // Check if we already have a box for this comment ID that we can reuse
            if (oldCommentBoxMap.containsKey(comment.getId())) {
                commentBox = oldCommentBoxMap.get(comment.getId());
                // Update the comment data if needed
                // This is optional since comments are typically immutable once created
            } else {
                // Create a new comment box
                try {
                    // Load the comment item component
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/CommentItem.fxml"));
                    commentBox = loader.load();
                    
                    // Store the comment ID as user data for future reference
                    commentBox.setUserData(comment.getId());

                    // Get the controller and set the comment
                    CommentItemController controller = loader.getController();
                    controller.setComment(comment, this);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    // Fallback to simple comment display if loading fails
                    commentBox = createSimpleCommentView(comment);
                    commentBox.setUserData(comment.getId());
                }
            }
            
            // Add to the list view and our tracking map
            commentsListView.getItems().add(commentBox);
            commentBoxMap.put(comment.getId(), commentBox);
            System.out.println("Added comment ID during load: " + comment.getId());
        }
    }

    /**
     * Create a simple comment view as fallback
     */
    private VBox createSimpleCommentView(Commentaire comment) {
        VBox commentBox = new VBox(5);
        commentBox.getStyleClass().add("comment-box");

        // Author and date
        HBox headerBox = new HBox(10);
        Label authorLabel = new Label(getAuthorName(comment.getAuteurId()));
        authorLabel.getStyleClass().add("comment-author");
        Label dateLabel = new Label(comment.getDateCommentaire().format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' HH:mm")));
        dateLabel.getStyleClass().add("comment-date");
        headerBox.getChildren().addAll(authorLabel, dateLabel);

        // Comment content
        Text contentText = new Text(comment.getContenu());
        contentText.getStyleClass().add("comment-content");
        TextFlow contentFlow = new TextFlow(contentText);
        contentFlow.setPrefWidth(commentsListView.getWidth() - 20);

        // Add components to the comment box
        commentBox.getChildren().addAll(headerBox, contentFlow);

        return commentBox;
    }

    private void handleLike() {
        boolean hasLiked = blogService.hasUserLikedBlog(currentBlog.getId(), currentUserId);
        boolean success;
        if (hasLiked) {
            success = blogService.unlikeBlog(currentBlog.getId(), currentUserId);
            if (success) {
                currentBlog.setLikes(currentBlog.getLikes() - 1);
                String likesText = String.format("❤ Like (%d)", currentBlog.getLikes());
                likeButton.setText(likesText);
            } else {
                showAlert("Error", "Failed to unlike this blog.");
            }
        } else {
            success = blogService.likeBlog(currentBlog.getId(), currentUserId);
            if (success) {
                currentBlog.setLikes(currentBlog.getLikes() + 1);
                String likesText = String.format("❤ Liked (%d)", currentBlog.getLikes());
                likeButton.setText(likesText);

                // Add animation effect
                likeButton.setScaleX(1.2);
                likeButton.setScaleY(1.2);
                javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), likeButton);
                st.setToX(1);
                st.setToY(1);
                st.play();
            } else {
                showAlert("Error", "Failed to like this blog.");
            }
        }
    }

    @FXML
    private void handleAddComment() {
        if (currentBlog == null) return;
        String commentText = commentField.getText().trim();
        if (commentText.isEmpty()) {
            showAlert("Empty Comment", "Comment cannot be empty.");
            return;
        }
        if (currentUser == null) {
            showAlert("Authentication Required", "You must be logged in to comment.");
            return;
        }
        int userId = currentUser.getId();
        Commentaire commentaire = new Commentaire();
        commentaire.setContenu(commentText);
        commentaire.setDateCommentaire(LocalDateTime.now());
        commentaire.setAuteurId(userId);
        commentaire.setBlogId(currentBlog.getId());
        try {
            int newCommentId = commentaireService.ajouter(commentaire);
            if (newCommentId <= 0) {
                showAlert("Error", "Failed to add comment: Invalid comment ID returned.");
                return;
            }
            commentField.clear();
            commentaire.setId(newCommentId);
            commentsListView.getItems().clear();
            commentBoxMap.clear();
            List<Commentaire> freshComments = commentaireService.recupererByBlogId(currentBlog.getId());
            if (newestFirstToggle.isSelected()) {
                freshComments.sort((c1, c2) -> c2.getDateCommentaire().compareTo(c1.getDateCommentaire()));
            } else {
                freshComments.sort((c1, c2) -> c1.getDateCommentaire().compareTo(c2.getDateCommentaire()));
            }
            commentCountLabel.setText("(" + freshComments.size() + ")");
            for (Commentaire comment : freshComments) {
                createAndAddCommentBox(comment);
            }
            showToast("Comment posted!");
        } catch (Exception e) {
            showAlert("Error", "Failed to add your comment. Please try again.");
            e.printStackTrace();
        }
    }
    
    /**
     * Create a comment box and add it to the list view
     */
    private void createAndAddCommentBox(Commentaire comment) {
        // Skip if this comment ID is already in the list view
        if (commentBoxMap.containsKey(comment.getId())) {
            System.out.println("Skipping duplicate comment ID: " + comment.getId());
            return;
        }
        
        try {
            // Load the comment item component
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CommentItem.fxml"));
            VBox commentBox = loader.load();
            
            // Store the comment ID as user data for future reference
            commentBox.setUserData(comment.getId());

            // Get the controller and set the comment
            CommentItemController controller = loader.getController();
            controller.setComment(comment, this);

            // Add comment box to the list
            commentsListView.getItems().add(commentBox);
            
            // Add to our tracking map
            commentBoxMap.put(comment.getId(), commentBox);
            System.out.println("Added comment ID: " + comment.getId() + " to UI");
        } catch (IOException ex) {
            ex.printStackTrace();
            // Fallback to simple comment display if loading fails
            VBox commentBox = createSimpleCommentView(comment);
            commentBox.setUserData(comment.getId());
            commentsListView.getItems().add(commentBox);
            commentBoxMap.put(comment.getId(), commentBox);
        }
    }

    @FXML
    private void handleBack() {
        try {
            if (parentContentPane != null) {
                // Load the BlogView in the content pane
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogView.fxml"));
                Parent root = loader.load();
                
                // Replace the content in the parent content pane
                parentContentPane.getChildren().setAll(root);
            } else {
                // Fallback to using SceneSwitcher if parentContentPane is not set
                SceneSwitcher.switchScene(backButton, "/BlogView.fxml", 900, 600, "Blog View");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not navigate back: " + e.getMessage());
            
            // Fallback: close the current stage if all navigation fails
            if (parentContentPane == null) {
                javafx.stage.Stage stage = (javafx.stage.Stage) backButton.getScene().getWindow();
                stage.close();
            }
        }
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private void insertGifInComment(String gifUrl) {
        if (gifUrl == null || gifUrl.isEmpty()) return;
        
        // Add the GIF URL to the comment field
        String currentText = commentField.getText();
        String newText = currentText + " " + gifUrl + " ";
        commentField.setText(newText);
        
        // Show a preview of the GIF
        showGifPreview(gifUrl);
        
        // Focus the comment field for continued typing
        commentField.requestFocus();
    }
    
    /**
     * Show a preview of the selected GIF below the comment field
     */
    private void showGifPreview(String gifUrl) {
        // Create a preview container if it doesn't exist
        VBox previewContainer = new VBox(5);
        previewContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        previewContainer.setPadding(new Insets(5));
        previewContainer.getStyleClass().add("gif-preview-container");
        
        // Add a label
        Label previewLabel = new Label("GIF Preview:");
        previewLabel.getStyleClass().add("preview-label");
        
        // Create image view for the GIF
        ImageView imageView = new ImageView();
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        
        // Add a remove button
        Button removeButton = new Button("Remove");
        removeButton.setOnAction(e -> {
            // Remove the GIF URL from the comment field
            String text = commentField.getText();
            text = text.replace(gifUrl, "").trim();
            commentField.setText(text);
            
            // Remove the preview
            Parent parent = commentField.getParent();
            if (parent instanceof Pane) {
                ((Pane) parent).getChildren().remove(previewContainer);
            }
        });
        
        // Add a loading indicator
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(30, 30);
        
        // Add components to the preview container
        HBox previewHeader = new HBox(10, previewLabel, removeButton);
        previewHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        previewContainer.getChildren().addAll(previewHeader, progress);
        
        // Add the preview container below the comment field
        Parent parent = commentField.getParent();
        if (parent instanceof Pane) {
            Pane parentPane = (Pane) parent;
            // Remove any existing preview first
            parentPane.getChildren().removeIf(node -> 
                node.getStyleClass() != null && 
                node.getStyleClass().contains("gif-preview-container"));
            parentPane.getChildren().add(previewContainer);
        }
        
        // Load the GIF in background
        Task<Image> loadTask = new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                return new Image(gifUrl, 100, 100, true, true, true);
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            Image image = loadTask.getValue();
            if (image != null && !image.isError()) {
                Platform.runLater(() -> {
                    imageView.setImage(image);
                    previewContainer.getChildren().remove(progress);
                    previewContainer.getChildren().add(imageView);
                });
            } else {
                Platform.runLater(() -> {
                    previewContainer.getChildren().remove(progress);
                    previewContainer.getChildren().add(new Label("[GIF preview not available]"));
                });
            }
        });
        
        loadTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                previewContainer.getChildren().remove(progress);
                previewContainer.getChildren().add(new Label("[GIF preview not available]"));
            });
        });
        
        new Thread(loadTask).start();
    }

    // Toast message for comment post success
    private void showToast(String message) {
        Label toast = new Label(message);
        toast.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5; -fx-font-size: 16px;");
        toast.setOpacity(0.95);
        VBox parent = (VBox) commentField.getParent().getParent();
        parent.getChildren().add(toast);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.seconds(2), toast);
        ft.setFromValue(0.95);
        ft.setToValue(0);
        ft.setOnFinished(e -> parent.getChildren().remove(toast));
        ft.play();
    }

    public void setBlogList(List<Blog> blogs, int index) {
        this.blogList = blogs;
        this.currentIndex = index;
        updateNavButtons();
    }

    private void updateNavButtons() {
        if (blogList == null || blogList.size() <= 1 || currentIndex == -1) {
            prevBlogButton.setDisable(true);
            nextBlogButton.setDisable(true);
            return;
        }
        prevBlogButton.setDisable(currentIndex <= 0);
        nextBlogButton.setDisable(currentIndex >= blogList.size() - 1);
    }

    private void navigateToBlog(int newIndex) {
        if (blogList == null || newIndex < 0 || newIndex >= blogList.size()) return;
        Blog newBlog = blogList.get(newIndex);
        setBlog(newBlog);
        currentIndex = newIndex;
        updateNavButtons();
    }

    /**
     * Disable interaction buttons for preview mode
     * Used when the blog detail view is opened in preview mode
     */
    public void disableInteractionButtons() {
        if (likeButton != null) likeButton.setDisable(true);
        if (commentButton != null) commentButton.setDisable(true);
        if (commentField != null) commentField.setDisable(true);
        if (gifPickerButton != null) gifPickerButton.setDisable(true);
        if (newestFirstToggle != null) newestFirstToggle.setDisable(true);
        if (prevBlogButton != null) prevBlogButton.setDisable(true);
        if (nextBlogButton != null) nextBlogButton.setDisable(true);
    }
}
