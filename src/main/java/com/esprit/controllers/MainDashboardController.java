package com.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;

import java.io.IOException;

public class MainDashboardController {

    @FXML
    private StackPane productsTabContent;

    @FXML
    private StackPane orderDashboardTabContent;

    @FXML
    public void initialize() {
        try {
            // Load initial ProductDashboardView.fxml into productsTabContent
            FXMLLoader productLoader = new FXMLLoader(getClass().getResource("/ProductDashboardView.fxml"));
            Node productDashboard = productLoader.load();
            productsTabContent.getChildren().setAll(productDashboard);

            // Set parentContainer on ProductDashboardController
            Object productController = productLoader.getController();
            if (productController instanceof ProductDashboardController) {
                ((ProductDashboardController) productController).setParentContainer(productsTabContent);
            }

            // Load initial OrderDashboardView.fxml into orderDashboardTabContent
            FXMLLoader orderLoader = new FXMLLoader(getClass().getResource("/OrderDashboardView.fxml"));
            Node orderDashboard = orderLoader.load();
            orderDashboardTabContent.getChildren().setAll(orderDashboard);

            // Set parentContainer on OrderDashboardController
            Object orderController = orderLoader.getController();
            if (orderController instanceof OrderDashboardController) {
                ((OrderDashboardController) orderController).setParentContainer(orderDashboardTabContent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StackPane getProductsTabContent() {
        return productsTabContent;
    }

    public StackPane getOrderDashboardTabContent() {
        return orderDashboardTabContent;
    }
}
