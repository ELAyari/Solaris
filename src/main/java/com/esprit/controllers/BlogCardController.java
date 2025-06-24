package com.esprit.controllers;

import com.esprit.models.Blog;
import com.esprit.models.Commentaire;
import com.esprit.Services.CommentaireService;
import com.esprit.utils.ImagePathUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.esprit.Services.UserService;
import com.esprit.models.User;
import java.sql.SQLException;

public class BlogCardController {
    @FXML
    private ImageView blogImage;
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label authorLabel;
    
    @FXML
    private Label dateLabel;
    
    @FXML
    private TextArea contentPreview;
    
    @FXML
    private Label likesLabel;
    
    @FXML
    private Label commentsLabel;
    
    @FXML
    private Label summaryLabel;
    
    private Blog blog;
    private CommentaireService commentaireService;
    public BlogViewController parentController;

    public BlogCardController() {
        this.commentaireService = new CommentaireService();
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
        updateView();
        setAuthorNameAsync(blog.getAuteurId());
    }

    /**
     * Sets the parent controller reference.
     * This method is called by BlogViewController but the reference is not currently used.
     * Kept for compatibility.
     */
    public void setParentController(BlogViewController parentController) {
        this.parentController = parentController;
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
        if (blog == null) return;

        titleLabel.setText(blog.getTitre());
        dateLabel.setText(blog.getDatePublication() != null ? 
            blog.getDatePublication().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "");
        
        // Set summary/hook (show full summary/resume, or fallback to content)
        String summary = blog.getResume();
        if (summary == null || summary.isEmpty()) {
            String content = blog.getContenu();
            summary = (content != null && !content.isEmpty()) ? content : "";
        }
        if (summary != null && !summary.isEmpty()) {
            summaryLabel.setText(summary);
        } else {
            summaryLabel.setText("");
        }
        
        // Set likes and comments count
        likesLabel.setText(String.valueOf(blog.getLikes()));
        List<Commentaire> comments = commentaireService.recupererByBlogId(blog.getId());
        commentsLabel.setText(String.valueOf(comments != null ? comments.size() : 0));

        // Load blog image
        String defaultImagePath = "/images/default_renewable.png";
        boolean imageLoaded = false;
        if (blog.getImage() != null && !blog.getImage().isEmpty()) {
            try {
                File imageFile = ImagePathUtil.getBlogImageFile(blog.getImage());
                if (imageFile.exists()) {
                    String imageUri = imageFile.toURI().toString();
                    Image image = new Image(imageUri, 300, 200, true, true, true); // thumbnail size
                    image.errorProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            blogImage.setImage(new Image(defaultImagePath));
                        }
                    });
                    blogImage.setImage(image);
                    imageLoaded = true;
                }
            } catch (Exception e) {
                // fallback below
            }
        }
        if (!imageLoaded) {
            blogImage.setImage(new Image(defaultImagePath));
        }
    }

    private String getAuthorName(int authorId) {
        return ""; // No longer used
    }

    @FXML
    private void handleClick() {
        try {
            // Find the contentPane in the AdminDashboardController
            StackPane contentPane = findContentPane();
            if (contentPane == null) {
                System.err.println("Could not find contentPane in parent hierarchy");
                // Fallback to opening in a new scene if contentPane is not found
                openInNewScene();
                return;
            }
            // Load the BlogDetailView
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogDetailView.fxml"));
            Parent root = loader.load();
            // Configure the controller
            BlogDetailController controller = loader.getController();
            controller.setBlog(blog);
            // Pass blog list and index for navigation if parentController is set
            if (parentController != null) {
                List<Blog> blogs = parentController.getAllBlogs();
                int idx = blogs != null ? blogs.indexOf(blog) : -1;
                if (blogs != null && idx != -1) {
                    controller.setBlogList(blogs, idx);
                }
            }
            // If the controller has a method to set parent content pane, use it
            try {
                java.lang.reflect.Method method = controller.getClass().getMethod("setParentContentPane", StackPane.class);
                method.invoke(controller, contentPane);
            } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                // Method doesn't exist, that's okay
            }
            // Replace the content in the contentPane
            contentPane.getChildren().setAll(root);
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to opening in a new scene if there's an error
            openInNewScene();
        }
    }
    
    /**
     * Fallback method to open blog detail in a new scene if content pane is not found
     */
    private void openInNewScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogDetailView.fxml"));
            Parent root = loader.load();
            
            BlogDetailController controller = loader.getController();
            controller.setBlog(blog);
            
            // Replace the current scene with the detail view in the same window
            Stage stage = (Stage) blogImage.getScene().getWindow();
            stage.setTitle(blog.getTitre());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Finds the contentPane StackPane in the parent hierarchy
     * @return the contentPane or null if not found
     */
    private StackPane findContentPane() {
        // Navigate up the scene graph to find the contentPane
        Parent current = blogImage.getParent();
        while (current != null) {
            if (current.getParent() instanceof BorderPane) {
                BorderPane borderPane = (BorderPane) current.getParent();
                if (borderPane.getCenter() instanceof StackPane) {
                    return (StackPane) borderPane.getCenter();
                }
            }
            current = current.getParent();
        }
        return null;
    }
}
