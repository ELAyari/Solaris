package com.esprit.controllers;

import com.esprit.models.Commentaire;
import com.esprit.Services.CommentaireService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;

import java.time.LocalDateTime;

public class CommentDashboardController {
    @FXML private Label blogTitleLabel;
    @FXML private TableView<Commentaire> commentTable;
    @FXML private TextArea commentField;
    
    private int blogId;
    private final CommentaireService commentaireService = new CommentaireService();
    private StackPane parentContentPane;
    private String blogTitle;
    
    public void setBlogId(int blogId) {
        this.blogId = blogId;
        updateBlogTitleLabel();
        loadComments();
    }
    
    /**
     * Sets the blog title for display
     * @param title the title of the blog
     */
    public void setBlogTitle(String title) {
        this.blogTitle = title;
        updateBlogTitleLabel();
    }
    
    /**
     * Updates the blog title label with the current blog title or ID
     */
    private void updateBlogTitleLabel() {
        if (blogTitleLabel != null) {
            if (blogTitle != null && !blogTitle.isEmpty()) {
                blogTitleLabel.setText("Comments for Blog: " + blogTitle);
            } else {
                blogTitleLabel.setText("Comments for Blog #" + blogId);
            }
        }
    }
    
    /**
     * Sets the parent content pane for fragment-based navigation
     * @param contentPane the parent StackPane to load fragments into
     */
    public void setParentContentPane(StackPane contentPane) {
        this.parentContentPane = contentPane;
    }
    
    @FXML
    private TableColumn<Commentaire, Integer> authorColumn;
    @FXML
    private TableColumn<Commentaire, String> commentColumn;
    @FXML
    private TableColumn<Commentaire, String> dateColumn;

    @FXML
    private void initialize() {
        commentTable.getColumns().forEach(column -> {
            column.setReorderable(false);
            column.setSortable(false);
        });

        authorColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getAuteurId()).asObject());
        commentColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getContenu()));
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDateCommentaire().toString()));
    }
    
    private void loadComments() {
        ObservableList<Commentaire> comments = FXCollections.observableArrayList(
            commentaireService.recupererByBlogId(blogId)
        );
        commentTable.setItems(comments);
    }
    
    @FXML
    private void handleAddComment() {
        String content = commentField.getText().trim();
        if (!content.isEmpty()) {
            Commentaire comment = new Commentaire();
            comment.setContenu(content);
            comment.setBlogId(blogId);
            comment.setAuteurId(1); // TODO: Get actual user ID
            comment.setDateCommentaire(LocalDateTime.now());
            
            commentaireService.ajouter(comment);
            commentField.clear();
            loadComments();
        }
    }
    
    @FXML
    private void handleBack() {
        if (parentContentPane != null) {
            try {
                // Load the blog dashboard view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogDashboardView.fxml"));
                Parent root = loader.load();
                
                // Replace the content in the contentPane
                parentContentPane.getChildren().setAll(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleDelete() {
        Commentaire selected = commentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText("Delete comment");
            confirm.setContentText("Are you sure?");
            
            if (confirm.showAndWait().get() == ButtonType.OK) {
                commentaireService.supprimer(selected);
                loadComments();
            }
        }
    }
    
  
}