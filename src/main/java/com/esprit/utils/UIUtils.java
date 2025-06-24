package com.esprit.utils;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Optional;

public class UIUtils {

    public static void showAlert(String title, String header, String message, String alertTypeStr) {
        Alert.AlertType alertType;
        try {
            alertType = Alert.AlertType.valueOf(alertTypeStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            alertType = Alert.AlertType.INFORMATION; // default
        }
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);

        // Apply project stylesheet for consistent styling
        Scene scene = alert.getDialogPane().getScene();
        scene.getStylesheets().add(UIUtils.class.getResource("/styles/styles.css").toExternalForm());

        // Set application icon on alert stage
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        try {
            java.io.InputStream iconStream = UIUtils.class.getResourceAsStream("/images/logo.jfif");
            if (iconStream != null) {
                stage.getIcons().add(new javafx.scene.image.Image(iconStream));
            } else {
                System.err.println("Warning: Could not load alert icon image: Resource not found");
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load alert icon image: " + e.getMessage());
        }

        // Add style class for error alert if alertType is ERROR
        if (alertType == Alert.AlertType.ERROR) {
            alert.getDialogPane().getStyleClass().add("error-alert");
        }

        alert.showAndWait();
    }

    public static void showSuccessAlert(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);

        // Apply project stylesheet for consistent styling
        Scene scene = alert.getDialogPane().getScene();
        scene.getStylesheets().add(UIUtils.class.getResource("/styles/styles.css").toExternalForm());

        // Set application icon on alert stage
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        try {
            java.io.InputStream iconStream = UIUtils.class.getResourceAsStream("/images/logo.jfif");
            if (iconStream != null) {
                stage.getIcons().add(new javafx.scene.image.Image(iconStream));
            } else {
                System.err.println("Warning: Could not load alert icon image: Resource not found");
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load alert icon image: " + e.getMessage());
        }

        // Add style class for success alert
        alert.getDialogPane().getStyleClass().add("success-alert");

        // Add check mark graphic
        javafx.scene.control.Label checkMark = new javafx.scene.control.Label("\u2714");
        checkMark.setStyle("-fx-text-fill: #28a745; -fx-font-size: 48px; -fx-font-weight: bold;");
        alert.setGraphic(checkMark);

        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String message) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(title);
        confirm.setHeaderText(message);

        // Apply project stylesheet for consistent styling
        Scene scene = confirm.getDialogPane().getScene();
        scene.getStylesheets().add(UIUtils.class.getResource("/styles/styles.css").toExternalForm());

        // Set application icon on confirmation alert stage
        Stage stage = (Stage) confirm.getDialogPane().getScene().getWindow();
        try {
            java.io.InputStream iconStream = UIUtils.class.getResourceAsStream("/images/logo.jfif");
            if (iconStream != null) {
                stage.getIcons().add(new javafx.scene.image.Image(iconStream));
            } else {
                System.err.println("Warning: Could not load confirmation icon image: Resource not found");
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load confirmation icon image: " + e.getMessage());
        }

        Optional<ButtonType> result = confirm.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void showToastNotification(String message) {
        showToastNotification(message, "green");
    }

    public static void showToastNotification(String message, String color) {
        Platform.runLater(() -> {
            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.TRANSPARENT);
            toastStage.setAlwaysOnTop(true);

            String bgColor;
            String iconUnicode;
            switch (color.toLowerCase()) {
                case "red":
                    bgColor = "linear-gradient(to right, #ff4e50, #f9d423)";
                    iconUnicode = "\u2716"; // heavy multiplication X
                    break;
                case "green":
                default:
                    bgColor = "linear-gradient(to right, #56ab2f, #a8e063)";
                    iconUnicode = "\u2714"; // check mark
                    break;
            }

            Label iconLabel = new Label(iconUnicode);
            iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
            iconLabel.setPadding(new Insets(0, 10, 0, 0));

            Label toastLabel = new Label(message);
            toastLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            toastLabel.setFont(new Font(14));

            javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(iconLabel, toastLabel);
            hbox.setAlignment(Pos.CENTER_LEFT);

            StackPane root = new StackPane(hbox);
            root.setPadding(new Insets(15));
            root.setStyle("-fx-background-radius: 20; -fx-background-color: " + bgColor + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 2);");
            root.setOpacity(0);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            toastStage.setScene(scene);

            // Position dynamically relative to the owner window if available
            if (toastStage.getOwner() != null) {
                Stage owner = (Stage) toastStage.getOwner();
                toastStage.setX(owner.getX() + owner.getWidth() - 340); // wider width + margin
                toastStage.setY(owner.getY() + owner.getHeight() - 110); // taller height + margin
            } else {
                javafx.geometry.Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                toastStage.setX(screenBounds.getMaxX() - 340);
                toastStage.setY(screenBounds.getMaxY() - 110);
            }

            toastStage.show();

            // Fade in and scale in
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(Duration.millis(500), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            javafx.animation.ScaleTransition scaleIn = new javafx.animation.ScaleTransition(Duration.millis(500), root);
            scaleIn.setFromX(0.8);
            scaleIn.setFromY(0.8);
            scaleIn.setToX(1);
            scaleIn.setToY(1);

            javafx.animation.ParallelTransition showTransition = new javafx.animation.ParallelTransition(fadeIn, scaleIn);
            showTransition.setOnFinished(e -> {
                // Stay visible for 3 seconds then fade out
                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    Platform.runLater(() -> {
                        javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(Duration.millis(500), root);
                        fadeOut.setFromValue(1);
                        fadeOut.setToValue(0);
                        fadeOut.setOnFinished(event -> toastStage.close());
                        fadeOut.play();
                    });
                }).start();
            });
            showTransition.play();
        });
    }

    public static void updateBadge(javafx.scene.control.Button button, int count) {
        Platform.runLater(() -> {
            if (count > 0) {
                button.setText(String.valueOf(count));
                button.setVisible(true);
                
                // Add animation effect
                FadeTransition ft = new FadeTransition(Duration.millis(200), button);
                ft.setFromValue(0.3);
                ft.setToValue(1.0);
                ft.play();
            } else {
                button.setVisible(false);
            }
        });
    }
}
