package com.esprit.controllers;

import com.esprit.models.User;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class UserCell extends ListCell<User> {
    private final HBox cellContainer;
    private final VBox infoContainer;
    private final Circle avatar;
    private final Label nameLabel;
    private final Label roleLabel;

    public UserCell() {
        cellContainer = new HBox(10);
        cellContainer.getStyleClass().add("user-cell");

        avatar = new Circle(20);
        avatar.getStyleClass().add("user-avatar");

        infoContainer = new VBox(5);
        nameLabel = new Label();
        nameLabel.getStyleClass().add("user-name");

        roleLabel = new Label();
        roleLabel.getStyleClass().add("user-role");

        infoContainer.getChildren().addAll(nameLabel, roleLabel);
        cellContainer.getChildren().addAll(avatar, infoContainer);

        setGraphic(cellContainer);
    }

    @Override
    protected void updateItem(User item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        // Set user information
        nameLabel.setText(item.getNom() + " " + item.getPrenom());
        roleLabel.setText(String.valueOf(item.getRole()));
        
        // Set avatar background based on user role
        String roleColor = switch (item.getRole()) {
            default -> "#95A5A6";
        };
        avatar.setStyle("-fx-fill: " + roleColor);
    }
}
