package com.esprit.utils;

import com.esprit.models.UserActivity;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for enhancing dashboard UI components
 */
public class DashboardEnhancer {
    
    /**
     * Enhances a pie chart with interactive tooltips and hover effects
     * @param pieChart The pie chart to enhance
     */
    public static void enhanceUserStatusChart(PieChart pieChart) {
        if (pieChart == null) return;
        
        ObservableList<PieChart.Data> pieChartData = pieChart.getData();
        
        double total = pieChartData.stream()
                .mapToDouble(PieChart.Data::getPieValue)
                .sum();
                
        for (PieChart.Data data : pieChartData) {
            // Create tooltip with percentage info
            double percentage = (data.getPieValue() / total) * 100;
            Tooltip tooltip = new Tooltip(String.format(
                "%s\nCount: %.0f\nPercentage: %.1f%%",
                data.getName(),
                data.getPieValue(),
                percentage
            ));
            
            // Style the tooltip
            tooltip.setStyle(
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-color: rgba(40,40,40,0.9); " +
                "-fx-text-fill: white; " +
                "-fx-padding: 10px;"
            );
            
            // Add hover and click effects to the pie slice
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    // Install tooltip
                    Tooltip.install(newNode, tooltip);
                    
                    // Add hover effects
                    DropShadow glow = new DropShadow();
                    glow.setColor(Color.LIGHTBLUE);
                    glow.setWidth(20);
                    glow.setHeight(20);
                    
                    newNode.setOnMouseEntered(e -> {
                        // Scale up
                        newNode.setScaleX(1.1);
                        newNode.setScaleY(1.1);
                        
                        // Add glow effect
                        newNode.setEffect(glow);
                        
                        // Explode the slice slightly
                        double currentX = newNode.getTranslateX();
                        double currentY = newNode.getTranslateY();
                        newNode.setTranslateX(currentX * 1.05);
                        newNode.setTranslateY(currentY * 1.05);
                    });
                    
                    newNode.setOnMouseExited(e -> {
                        // Return to normal scale
                        newNode.setScaleX(1.0);
                        newNode.setScaleY(1.0);
                        
                        // Remove glow effect
                        newNode.setEffect(null);
                        
                        // Return to original position
                        double currentX = newNode.getTranslateX();
                        double currentY = newNode.getTranslateY();
                        newNode.setTranslateX(currentX / 1.05);
                        newNode.setTranslateY(currentY / 1.05);
                    });
                    
                    // Add click handler for detailed information
                    newNode.setOnMouseClicked(e -> {
                        ActivityLogger.logInfo("PieChart", "User clicked on segment: " + data.getName());
                        showDetailedStatusInfo(data.getName(), (int)data.getPieValue());
                    });
                }
            });
        }
    }
    
    /**
     * Shows a dialog with detailed information about a specific user status
     */
    private static void showDetailedStatusInfo(String status, int count) {
        // Create a popup dialog with detailed information
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("User Status Details");
        dialog.setHeaderText(status + " Users");
        
        // Create a VBox for the content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);
        
        // Add summary information
        Label summaryLabel = new Label("There are " + count + " users with status: " + status);
        summaryLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        content.getChildren().add(summaryLabel);
        
        // Set the dialog content
        dialog.getDialogPane().setContent(content);
        
        // Add close button
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);
        
        // Show the dialog
        dialog.showAndWait();
    }
    
    /**
     * Enhances the user activity table with custom cell factories and row effects
     * @param table The table to enhance
     * @param userColumn The column containing user names
     * @param actionColumn The column containing action descriptions
     * @param timestampColumn The column containing timestamps
     */
    public static void enhanceActivityTable(
            TableView<UserActivity> table,
            TableColumn<UserActivity, String> userColumn, 
            TableColumn<UserActivity, String> actionColumn,
            TableColumn<UserActivity, Date> timestampColumn) {
            
        if (table == null) return;
        
        // Setup user column with avatars
        userColumn.setCellFactory(column -> new TableCell<UserActivity, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create a HBox to hold the avatar and username
                    HBox container = new HBox(10);
                    container.setAlignment(Pos.CENTER_LEFT);
                    
                    // Create a small avatar circle
                    Circle avatar = new Circle(12);
                    avatar.setFill(Color.LIGHTGRAY);
                    
                    // Add the first letter of the username as the avatar text
                    Label avatarText = new Label(item.substring(0, 1).toUpperCase());
                    avatarText.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                    StackPane avatarStack = new StackPane(avatar, avatarText);
                    
                    // Add username label
                    Label usernameLabel = new Label(item);
                    usernameLabel.setStyle("-fx-font-weight: bold;");
                    
                    container.getChildren().addAll(avatarStack, usernameLabel);
                    setGraphic(container);
                }
            }
        });
        
        // Setup action column with icons
        actionColumn.setCellFactory(column -> new TableCell<UserActivity, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create a label with an icon based on the action type
                    HBox container = new HBox(5);
                    container.setAlignment(Pos.CENTER_LEFT);
                    
                    // Get appropriate icon and style for this action
                    String[] formatting = formatActionCell(item);
                    String icon = formatting[0];
                    String style = formatting[1];
                    
                    Label iconLabel = new Label(icon);
                    Label actionLabel = new Label(item);
                    actionLabel.setStyle(style);
                    
                    container.getChildren().addAll(iconLabel, actionLabel);
                    setGraphic(container);
                }
            }
        });
        
        // Setup timestamp column with formatting
        timestampColumn.setCellFactory(column -> new TableCell<UserActivity, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Format the date
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm");
                    setText(sdf.format(item));
                    
                    // Color recent activities (less than 1 hour old)
                    long now = System.currentTimeMillis();
                    long timestamp = item.getTime();
                    if (now - timestamp < 3600000) { // Less than 1 hour
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2c3e50;");
                    }
                    
                    // Add a tooltip with exact time
                    SimpleDateFormat fullSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Tooltip tooltip = new Tooltip(fullSdf.format(item));
                    Tooltip.install(this, tooltip);
                }
            }
        });
        
        // Add row hover effect for the entire table
        table.setRowFactory(tv -> {
            TableRow<UserActivity> row = new TableRow<>();
            row.setOnMouseEntered(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: rgba(46,204,113,0.1);");
                }
            });
            row.setOnMouseExited(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("");
                }
            });
            return row;
        });
    }
    
    /**
     * Formats a table cell based on the action type
     * @param action The action string to format
     * @return A formatted string with icon and styling information
     */
    private static String[] formatActionCell(String action) {
        String icon = "🔄"; // Default icon
        String color = "-fx-text-fill: #2c3e50;"; // Default color
        
        // Determine appropriate icon and color based on action type
        if (action.toLowerCase().contains("login")) {
            icon = "🔐";
            color = "-fx-text-fill: #27ae60; -fx-font-weight: bold;";
        } else if (action.toLowerCase().contains("logout")) {
            icon = "🚪";
            color = "-fx-text-fill: #2980b9;";
        } else if (action.toLowerCase().contains("update") || action.toLowerCase().contains("edit")) {
            icon = "✏️";
            color = "-fx-text-fill: #f39c12;";
        } else if (action.toLowerCase().contains("delete") || action.toLowerCase().contains("remove")) {
            icon = "🗑️";
            color = "-fx-text-fill: #e74c3c;";
        } else if (action.toLowerCase().contains("add") || action.toLowerCase().contains("create")) {
            icon = "➕";
            color = "-fx-text-fill: #16a085;";
        } else if (action.toLowerCase().contains("view") || action.toLowerCase().contains("read")) {
            icon = "👁️";
            color = "-fx-text-fill: #3498db;";
        }
        
        return new String[] { icon, color };
    }

    /**
     * Applies enhancers to an area chart
     * @param userActivityChart The area chart to enhance
     */
    public static void enhanceAreaChart(AreaChart<String, Number> userActivityChart) {
        if (userActivityChart == null) return;

        // Apply custom styling to the chart
        userActivityChart.setAnimated(true);
        userActivityChart.setCreateSymbols(true);
        userActivityChart.setLegendVisible(true);

        // Remove chart border and add drop shadow
        userActivityChart.lookup(".chart-plot-background").setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );

        // Apply drop shadow to the chart
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(10);
        shadow.setOffsetY(5);
        userActivityChart.setEffect(shadow);

        // Apply custom styles to the chart areas
        int seriesIndex = 0;
        for (XYChart.Series<String, Number> series : userActivityChart.getData()) {
            // Apply custom fill colors with transparency to each series
            String fillColor = switch (seriesIndex) {
                case 0 -> "rgba(52, 152, 219, 0.6)"; // Blue for logins
                case 1 -> "rgba(46, 204, 113, 0.6)"; // Green for actions
                default -> "rgba(155, 89, 182, 0.6)"; // Purple for other series
            };

            String lineColor = switch (seriesIndex) {
                case 0 -> "rgba(41, 128, 185, 1.0)"; // Darker blue
                case 1 -> "rgba(39, 174, 96, 1.0)"; // Darker green
                default -> "rgba(142, 68, 173, 1.0)"; // Darker purple
            };

            // Apply series-specific styles
            series.getNode().lookup(".chart-series-area-fill").setStyle(
                    "-fx-fill: " + fillColor + ";"
            );

            series.getNode().lookup(".chart-series-area-line").setStyle(
                    "-fx-stroke: " + lineColor + ";" +
                            "-fx-stroke-width: 2px;"
            );

            // Add tooltips and hover effects to data points
            for (XYChart.Data<String, Number> data : series.getData()) {
                // Install tooltips when the node is created
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        // Create detailed tooltip with formatted information
                        Tooltip tooltip = new Tooltip(
                                series.getName() + "\n" +
                                        "Date: " + data.getXValue() + "\n" +
                                        "Value: " + data.getYValue()
                        );
                        tooltip.setStyle(
                                "-fx-font-size: 12px;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-background-color: rgba(255, 255, 255, 0.95);" +
                                        "-fx-background-radius: 5px;" +
                                        "-fx-border-color: #ddd;" +
                                        "-fx-border-radius: 5px;" +
                                        "-fx-padding: 8px;"
                        );
                        Tooltip.install(newNode, tooltip);

                        // Add hover effects
                        newNode.setOnMouseEntered(e -> {
                            // Scale up and add shadow on hover
                            AnimationUtils.scaleUp(newNode, 1.5, 150);

                            DropShadow pointShadow = new DropShadow();
                            pointShadow.setColor(Color.rgb(0, 0, 0, 0.3));
                            pointShadow.setRadius(5);
                            newNode.setEffect(pointShadow);

                            // Highlight the specific point
                            newNode.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
                        });

                        newNode.setOnMouseExited(e -> {
                            // Restore original size and remove effects
                            AnimationUtils.scaleDown(newNode, 1.0, 150);
                            newNode.setEffect(null);
                            newNode.setStyle("");
                        });
                    }
                });
            }

            seriesIndex++;
        }

        // Style the chart grid lines to be less intrusive
        userActivityChart.lookup(".chart-vertical-grid-lines").setStyle(
                "-fx-stroke: rgba(200, 200, 200, 0.3);" +
                        "-fx-stroke-width: 0.5px;" +
                        "-fx-stroke-dash-array: 2 4;"
        );

        userActivityChart.lookup(".chart-horizontal-grid-lines").setStyle(
                "-fx-stroke: rgba(200, 200, 200, 0.3);" +
                        "-fx-stroke-width: 0.5px;" +
                        "-fx-stroke-dash-array: 2 4;"
        );

        // Style the axes for better readability
        userActivityChart.getXAxis().setTickLabelFill(Color.web("#555"));
        userActivityChart.getYAxis().setTickLabelFill(Color.web("#555"));

        // Add subtle animation when the chart appears
        userActivityChart.setOpacity(0);
        AnimationUtils.fadeIn(userActivityChart, 800, 300);
    }

    public static void enhancePieChart(PieChart userStatusPieChart) {
    }
}
