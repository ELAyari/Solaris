package com.esprit.controllers;

import com.esprit.models.Blog;
import com.esprit.tests.MainprogGUI;
import com.esprit.Services.BlogService;
import com.esprit.Services.UserService;
import com.esprit.models.User;
import com.esprit.utils.SceneSwitcher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.util.Callback;
import com.esprit.Services.CommentaireService;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.io.File;
import java.time.format.DateTimeFormatter;

public class BlogDashboardController {
    @FXML private TableView<Blog> blogTable;
    @FXML private TableColumn<Blog, Integer> viewColumn;
    @FXML private TableColumn<Blog, String> titleColumn;
    @FXML private TableColumn<Blog, String> summaryColumn;
    @FXML private TableColumn<Blog, String> statusColumn;
    @FXML private TableColumn<Blog, String> dateColumn;
    @FXML private TableColumn<Blog, String> authorColumn;
    @FXML private TableColumn<Blog, String> imageColumn;
    @FXML private TableColumn<Blog, Integer> commentCountColumn;
    @FXML private TableColumn<Blog, Integer> likesCountColumn;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button publishButton;

    private final BlogService blogService = new BlogService();
    private UserService userService;
    private final CommentaireService commentaireService = new CommentaireService();
    private final ObservableList<Blog> blogs = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // Cache for comment counts to improve performance
    private final java.util.Map<Integer, Integer> commentCountCache = new java.util.HashMap<>();

    public BlogDashboardController() {
        try {
            userService = new UserService();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize UserService", e);
        }
    }

    @FXML
    public void initialize() {
        try {
            blogTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            setupTableColumns();
            loadBlogs();
            setupSelectionListener();
            setupStatusFilter();
            statusCombo.setItems(FXCollections.observableArrayList("All", "Draft", "Published"));
            blogTable.setPlaceholder(new Label("No blogs available"));
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Initialization Error", "Failed to initialize dashboard: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        // Title Column
        titleColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTitre()));
        
        // Summary Column
        summaryColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getResume()));
        
        // Status Column
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus()));
        
        // Date Column
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDatePublication() != null) {
                return new SimpleStringProperty(
                    cellData.getValue().getDatePublication().format(dateFormatter)
                );
            } else {
                return new SimpleStringProperty("");
            }
        });
        
        // Author Column
        authorColumn.setCellValueFactory(cellData -> {
            int authorId = cellData.getValue().getAuteurId();
            User author = userService.getById(authorId);
            String authorName = (author != null) ? author.getNom() + " " + author.getPrenom() : "Unknown";
            return new SimpleStringProperty(authorName);
        });
        
        // Comment Count Column - Use cached values
        commentCountColumn.setCellValueFactory(cellData -> {
            int blogId = cellData.getValue().getId();
            int count = commentCountCache.getOrDefault(blogId, 0);
            return new SimpleIntegerProperty(count).asObject();
        });
        
        // Likes Count Column
        likesCountColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getLikes()).asObject());
        
        // Views Column
        viewColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getViews()).asObject());
        
        // Image Column
        imageColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getImage()));
        
        // Set cell factories for consistent styling
        setupCellFactories();
    }

    private void setupCellFactories() {
        // Center-aligned text cell factory
        Callback<TableColumn<Blog, String>, TableCell<Blog, String>> centerStringCellFactory = 
            column -> new TableCell<Blog, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                    }
                }
            };

        // Center-aligned integer cell factory
        Callback<TableColumn<Blog, Integer>, TableCell<Blog, Integer>> centerIntegerCellFactory = 
            column -> new TableCell<Blog, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item.toString());
                    }
                }
            };

        // Image cell factory
        Callback<TableColumn<Blog, String>, TableCell<Blog, String>> imageCellFactory = 
            column -> new TableCell<Blog, String>() {
                private final ImageView imageView = new ImageView();
                {
                    imageView.setFitWidth(75);
                    imageView.setFitHeight(75);
                    imageView.setPreserveRatio(true);
                }
                
                @Override
                protected void updateItem(String imageName, boolean empty) {
                    super.updateItem(imageName, empty);
                    if (empty || imageName == null || imageName.isEmpty()) {
                        setGraphic(null);
                    } else {
                        File imageFile = new File(
                            String.join(File.separator,
                                System.getProperty("user.dir"),
                                "src", "main", "resources", "images", "blogs"),
                            imageName);
                        if (imageFile.exists()) {
                            imageView.setImage(new Image(imageFile.toURI().toString()));
                            setGraphic(imageView);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            };

        // Apply cell factories
        titleColumn.setCellFactory(centerStringCellFactory);
        summaryColumn.setCellFactory(centerStringCellFactory);
        statusColumn.setCellFactory(centerStringCellFactory);
        dateColumn.setCellFactory(centerStringCellFactory);
        authorColumn.setCellFactory(centerStringCellFactory);
        commentCountColumn.setCellFactory(centerIntegerCellFactory);
        likesCountColumn.setCellFactory(centerIntegerCellFactory);
        viewColumn.setCellFactory(centerIntegerCellFactory);
        imageColumn.setCellFactory(imageCellFactory);
    }

    private void loadBlogs() {
        try {
            // Clear cache before loading new data
            commentCountCache.clear();
            
            // Load blogs
            blogs.setAll(blogService.recuperer());
            
            // Pre-calculate comment counts for all blogs
            for (Blog blog : blogs) {
                try {
                    int count = commentaireService.getCommentCountByBlogId(blog.getId());
                    commentCountCache.put(blog.getId(), count);
                } catch (Exception e) {
                    // If comment count fails, set to 0
                    commentCountCache.put(blog.getId(), 0);
                    System.err.println("Failed to get comment count for blog " + blog.getId() + ": " + e.getMessage());
                }
            }
            
            blogTable.setItems(blogs);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Data Loading Error", "Failed to load blogs: " + e.getMessage());
        }
    }

    private void setupSelectionListener() {
        blogTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                boolean isSelected = newSelection != null;
                updateButton.setDisable(!isSelected);
                deleteButton.setDisable(!isSelected);
                publishButton.setDisable(!isSelected);
            }
        );
    }

    private void setupStatusFilter() {
        statusCombo.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> filterBlogsByStatus(newVal)
        );
    }

    private void filterBlogsByStatus(String status) {
        if (status == null || status.equals("All")) {
            blogTable.setItems(blogs);
        } else {
            ObservableList<Blog> filtered = FXCollections.observableArrayList(
                blogs.stream()
                    .filter(blog -> blog.getStatus() != null && 
                                   blog.getStatus().equalsIgnoreCase(status))
                    .toList()
            );
            blogTable.setItems(filtered);
        }
    }

    @FXML
    private void handleAdd() {
        try {
            // Find the contentPane in the AdminDashboardController
            StackPane contentPane = findContentPane();
            if (contentPane == null) {
                System.err.println("Could not find contentPane in parent hierarchy");
                return;
            }
            
            // Load the BlogFormView
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogFormView.fxml"));
            Parent root = loader.load();
            
            // Configure the controller
            BlogFormController controller = loader.getController();
            controller.setMode(BlogFormController.Mode.ADD);
            controller.setParentContentPane(contentPane);
            
            // Replace the content in the contentPane
            contentPane.getChildren().setAll(root);
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
        Parent current = blogTable.getParent();
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

    @FXML
    private void handleUpdate() {
        Blog selected = blogTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                // Find the contentPane in the AdminDashboardController
                StackPane contentPane = findContentPane();
                if (contentPane == null) {
                    System.err.println("Could not find contentPane in parent hierarchy");
                    return;
                }
                
                // Load the BlogFormView
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogFormView.fxml"));
                Parent root = loader.load();
                
                // Configure the controller
                BlogFormController controller = loader.getController();
                controller.setMode(BlogFormController.Mode.EDIT);
                controller.setBlog(selected);
                controller.setParentContentPane(contentPane);
                
                // Replace the content in the contentPane
                contentPane.getChildren().setAll(root);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleDelete() {
        Blog selected = blogTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Show confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Delete Blog");
            alert.setContentText("Are you sure you want to delete '" + selected.getTitre() + "'?");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        blogService.supprimer(selected);
                        loadBlogs(); // Refresh table
                        showAlert("Success", "Blog deleted successfully");
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Error", "Failed to delete blog: " + e.getMessage());
                    }
                }
            });
        } else {
            showAlert("Warning", "Please select a blog to delete");
        }
    }

    @FXML
    private void handlePublish() {
        Blog selected = blogTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                selected.setStatus("Published");
                blogService.modifier(selected);
                loadBlogs(); // Refresh table
                showAlert("Success", "Blog published successfully");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to publish blog: " + e.getMessage());
            }
        } else {
            showAlert("Warning", "Please select a blog to publish");
        }
    }

    @FXML
    private void handleViewComments() {
        Blog selected = blogTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                // Find the contentPane in the AdminDashboardController
                StackPane contentPane = findContentPane();
                if (contentPane == null) {
                    System.err.println("Could not find contentPane in parent hierarchy");
                    showAlert("Error", "Navigation error: Could not find content pane");
                    return;
                }
                
                // Load the CommentDashboardView
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/CommentDashboardView.fxml"));
                Parent root = loader.load();

                // Configure the controller
                CommentDashboardController controller = loader.getController();
                controller.setBlogId(selected.getId());
                controller.setBlogTitle(selected.getTitre());
                controller.setParentContentPane(contentPane);
                
                // Replace the content in the contentPane
                contentPane.getChildren().setAll(root);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to load comments: " + e.getMessage());
            }
        } else {
            showAlert("Warning", "Please select a blog to view comments");
        }
    }

    /**
     * Refresh the table data
     */
   

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
     * Show an error alert dialog
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Object setMainApp(MainprogGUI mainprogGUI) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setMainApp'");
    }
}
