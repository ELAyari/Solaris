package com.esprit.controllers;

import com.esprit.models.Blog;
import com.esprit.Services.BlogService;
import com.esprit.Services.CommentaireService;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.esprit.tests.MainprogGUI;
import javafx.scene.layout.BorderPane;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BlogViewController {

    @FXML
    private FlowPane blogCardsContainer;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> categoryFilter;
    
    @FXML
    private Button addBlogButton;
    


    private BlogService blogService;
    private CommentaireService commentaireService;

    /**
     * Host services for opening external links.
     * Currently not used but kept for potential future functionality.
     */
    private HostServices hostServices;

    private ObservableList<Blog> allBlogs = FXCollections.observableArrayList();

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    private void initialize() {
        // Set up back button handler
        blogService = new BlogService();
        commentaireService = new CommentaireService();

        // Initialize categoryFilter with new sorting options
        categoryFilter.setItems(FXCollections.observableArrayList(
            "All",
            "Comments: Highest",
            "Comments: Lowest",
            "Likes: Highest",
            "Likes: Lowest"
        ));
        categoryFilter.getSelectionModel().selectFirst();

        // Add listeners for search and filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayBlogs());
        categoryFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayBlogs());
        
        // Add event handler for Create New Blog button
        addBlogButton.setOnAction(event -> handleCreateNewBlog());

        loadBlogs();
    }

    public void refreshBlogs() {
        loadBlogs();
    }

    void loadBlogs() {
        loadingIndicator.setVisible(true);
        // Get all blogs but only add the published ones to the view
        List<Blog> blogs = blogService.getAllBlogs().stream()
            .filter(blog -> "Published".equals(blog.getStatus()))
            .collect(Collectors.toList());
        allBlogs.setAll(blogs);
        filterAndDisplayBlogs();
        loadingIndicator.setVisible(false);
    }

    private void filterAndDisplayBlogs() {
        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        String filter = categoryFilter.getSelectionModel().getSelectedItem();

        List<Blog> filtered = allBlogs.stream()
            .filter(blog -> blog.getTitre().toLowerCase().contains(searchText) || blog.getResume().toLowerCase().contains(searchText))
            .collect(Collectors.toList());

        if (filter != null && !filter.equals("All")) {
            switch (filter) {
                case "Comments: Highest":
                    filtered = filtered.stream()
                        .sorted(Comparator.comparingInt((Blog b) -> commentaireService.getCommentCountByBlogId(b.getId())).reversed())
                        .collect(Collectors.toList());
                    break;
                case "Comments: Lowest":
                    filtered = filtered.stream()
                        .sorted(Comparator.comparingInt(b -> commentaireService.getCommentCountByBlogId(b.getId())))
                        .collect(Collectors.toList());
                    break;
                case "Likes: Highest":
                    filtered = filtered.stream()
                        .sorted(Comparator.comparingInt(Blog::getLikes).reversed())
                        .collect(Collectors.toList());
                    break;
                case "Likes: Lowest":
                    filtered = filtered.stream()
                        .sorted(Comparator.comparingInt(Blog::getLikes))
                        .collect(Collectors.toList());
                    break;
            }
        }

        displayBlogs(filtered);
    }

    private void displayBlogs(List<Blog> blogs) {
        Platform.runLater(() -> {
            blogCardsContainer.getChildren().clear();
            for (Blog blog : blogs) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogCard.fxml"));
                    Parent card = loader.load();

                    BlogCardController controller = loader.getController();
                    controller.setBlog(blog);
                    controller.setParentController(this);

                    blogCardsContainer.getChildren().add(card);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Opens a blog detail view in a new scene.
     * This method is kept for backward compatibility but is no longer used
     * as we now use the content pane approach for navigation.
     */
    private void openBlogDetail(Blog blog) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogDetailView.fxml"));
            Parent root = loader.load();

            BlogDetailController controller = loader.getController();
            controller.setBlog(blog);

            // Switch scene in the current window instead of opening new stage
            javafx.scene.Scene currentScene = blogCardsContainer.getScene();
            javafx.stage.Stage stage = (javafx.stage.Stage) currentScene.getWindow();
            stage.setTitle(blog.getTitre());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open blog detail view.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Handle the Create New Blog button click
     * Opens the blog form in ADD mode with Draft status by default
     */
    private void handleCreateNewBlog() {
        try {
            // Use MainprogGUI's loadContentFragment method to preserve tab selection
            MainprogGUI mainApp = MainprogGUI.getInstance();
            if (mainApp != null) {
                mainApp.loadContentFragment("/ClientBlogFormView.fxml", 
                    (ClientBlogFormController controller) -> {
                        // Set the parent content pane for proper navigation
                        BorderPane rootPane = (BorderPane) blogCardsContainer.getScene().getRoot();
                        if (rootPane != null && rootPane.getCenter() instanceof javafx.scene.layout.StackPane) {
                            controller.setParentContentPane((javafx.scene.layout.StackPane) rootPane.getCenter());
                        }
                    });
            } else {
                // Fallback to direct loading if MainprogGUI is not available
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientBlogFormView.fxml"));
                Parent root = loader.load();

                ClientBlogFormController controller = loader.getController();

                BorderPane rootPane = (BorderPane) blogCardsContainer.getScene().getRoot();
                if (rootPane != null) {
                    // Always ensure the center is a StackPane for fragment navigation
                    if (!(rootPane.getCenter() instanceof javafx.scene.layout.StackPane)) {
                        javafx.scene.layout.StackPane stackPane = new javafx.scene.layout.StackPane();
                        stackPane.getChildren().add(root);
                        rootPane.setCenter(stackPane);
                        controller.setParentContentPane(stackPane);
                    } else {
                        javafx.scene.layout.StackPane stackPane = (javafx.scene.layout.StackPane) rootPane.getCenter();
                        stackPane.getChildren().setAll(root);
                        controller.setParentContentPane(stackPane);
                    }
                } else {
                    // Fallback to switching scene if rootPane is null
                    Scene currentScene = blogCardsContainer.getScene();
                    Stage stage = (Stage) currentScene.getWindow();
                    stage.setTitle("Create New Blog");
                    stage.setScene(new Scene(root));
                    stage.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open client blog creation form: " + e.getMessage());
        }
    }
    
    /**
     * Handles the back button click event
     * Returns to the previous screen
     */
   
    public List<Blog> getAllBlogs() {
        return allBlogs;
    }
}
