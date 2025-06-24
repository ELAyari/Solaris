package com.esprit.utils;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

/**
 * Utility class for enhancing chart tooltips and interactions
 */
public class ChartTooltipUtils {
    
    /**
     * Adds enhanced tooltips and hover effects to a pie chart
     * @param pieChart The pie chart to enhance
     */
    public static void enhancePieChart(PieChart pieChart) {
        ObservableList<PieChart.Data> pieChartData = pieChart.getData();
        
        for (PieChart.Data data : pieChartData) {
            // Apply tooltip when node is available
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    // Create enhanced tooltip
                    Tooltip tooltip = createDetailedTooltip(data, pieChartData);
                    Tooltip.install(newNode, tooltip);
                    
                    // Add hover effects
                    applyHoverEffects(data, newNode);
                    
                    // Add click handler
                    newNode.setOnMouseClicked(e -> {
                        ActivityLogger.logInfo("PieChart", "User clicked on segment: " + data.getName());
                        // Data can be used for drill-down or additional info display
                    });
                }
            });
        }
    }
    
    /**
     * Creates a detailed tooltip for pie chart data
     */
    private static Tooltip createDetailedTooltip(PieChart.Data data, ObservableList<PieChart.Data> allData) {
        // Calculate percentage
        double total = allData.stream().mapToDouble(PieChart.Data::getPieValue).sum();
        double percentage = (data.getPieValue() / total) * 100;
        
        // Create tooltip with formatted information
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
        
        return tooltip;
    }
    
    /**
     * Applies hover effects to pie chart slices
     */
    private static void applyHoverEffects(PieChart.Data data, Node node) {
        // Create a glow effect for hovering
        DropShadow glow = new DropShadow();
        glow.setColor(Color.LIGHTBLUE);
        glow.setWidth(20);
        glow.setHeight(20);
        
        // Apply hover effects
        node.setOnMouseEntered(e -> {
            // Scale up
            node.setScaleX(1.1);
            node.setScaleY(1.1);
            
            // Add glow effect
            node.setEffect(glow);
            
            // Explode the slice slightly
            double currentX = node.getTranslateX();
            double currentY = node.getTranslateY();
            node.setTranslateX(currentX * 1.05);
            node.setTranslateY(currentY * 1.05);
        });
        
        node.setOnMouseExited(e -> {
            // Return to normal scale
            node.setScaleX(1.0);
            node.setScaleY(1.0);
            
            // Remove glow effect
            node.setEffect(null);
            
            // Return to original position
            double currentX = node.getTranslateX();
            double currentY = node.getTranslateY();
            node.setTranslateX(currentX / 1.05);
            node.setTranslateY(currentY / 1.05);
        });
    }
    
    /**
     * Formats a table cell based on the action type
     * @param action The action string to format
     * @return A formatted string with icon and styling information
     */
    public static String[] formatActionCell(String action) {
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
}
