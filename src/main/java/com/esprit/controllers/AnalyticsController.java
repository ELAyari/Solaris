package com.esprit.controllers;

import com.esprit.Services.CommandeService;
import com.esprit.Services.CommandeProduitService;
import com.esprit.Services.ProduitService;
import com.esprit.Services.UserService;
import com.esprit.Services.WishlistService;
import com.esprit.Services.CategorieService;
import com.esprit.models.Commande;
import com.esprit.models.CommandeProduit;
import com.esprit.models.Produit;
import com.esprit.models.User;
import com.esprit.models.Wishlist;
import com.esprit.models.Categorie;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.application.Platform;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.LineChart;
// Chart axis are defined in FXML

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.sql.SQLException;

public class AnalyticsController {

    @FXML
    private BarChart<String, Number> monthlySalesBarChart;

    @FXML
    private BarChart<String, Number> topProductsBarChart;

    @FXML
    private PieChart topCustomersPieChart;

    @FXML
    private PieChart categorySalesPieChart;
    
    @FXML
    private PieChart wishlistedProductsPieChart;

    @FXML
    private Label totalIncomeLabel;

    @FXML
    private Label averageOrderValueLabel;

    @FXML
    private Label totalCustomersLabel;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label recyclableProductsLabel;

    @FXML
    private ScatterChart<Number, Number> productPerformanceScatterChart;
    
    @FXML
    private LineChart<String, Number> revenueForecastLineChart;
    
    

    private final CommandeService commandeService = new CommandeService();
    private final CommandeProduitService commandeProduitService = new CommandeProduitService();
    private final ProduitService produitService = new ProduitService();
    private UserService userService;
    
    // Initialize services in initialize method to avoid constructor exceptions
    private final WishlistService wishlistService = new WishlistService();

    @FXML
    public void initialize() {
        // Initialize userService
        try {
            this.userService = new UserService();
        } catch (SQLException e) {
            System.err.println("Failed to initialize UserService: " + e.getMessage());
            e.printStackTrace();
        }
        
        loadProductsData();
        loadTopCustomersData();
        loadCategorySalesData();
        loadAdditionalMetrics();
        loadRenewableEnergyMetrics();
        loadRenewableRatioAnalytics();
        loadWishlistedProductsData();
    }

    private void loadOrdersData() {
        List<Commande> orders = commandeService.recuperer();

        // Map to hold month-year as key and total sales as value
        Map<String, Double> monthlySales = new HashMap<>();

        for (Commande order : orders) {
            if ("paid".equalsIgnoreCase(order.getStatut()) && order.getDate() != null) {
                LocalDate localDate = order.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                String monthYear = localDate.getMonth().toString() + " " + localDate.getYear();
                monthlySales.put(monthYear, monthlySales.getOrDefault(monthYear, 0.0) + order.getTotal());
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Sales");

        // Sort by year and month (convert month name to month number for sorting)
        monthlySales.entrySet().stream()
            .sorted((e1, e2) -> {
                String[] parts1 = e1.getKey().split(" ");
                String[] parts2 = e2.getKey().split(" ");
                int year1 = Integer.parseInt(parts1[1]);
                int year2 = Integer.parseInt(parts2[1]);
                int month1 = LocalDate.parse("2000-" + parts1[0].substring(0,3) + "-01", java.time.format.DateTimeFormatter.ofPattern("yyyy-MMM-dd")).getMonthValue();
                int month2 = LocalDate.parse("2000-" + parts2[0].substring(0,3) + "-01", java.time.format.DateTimeFormatter.ofPattern("yyyy-MMM-dd")).getMonthValue();
                if (year1 != year2) {
                    return Integer.compare(year1, year2);
                } else {
                    return Integer.compare(month1, month2);
                }
            })
            .forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

        ObservableList<XYChart.Series<String, Number>> data = FXCollections.observableArrayList();
        data.add(series);

        monthlySalesBarChart.setData(data);
    }

    private void loadProductsData() {
        List<CommandeProduit> allOrderItems = commandeProduitService.recuperer();

        Map<Integer, Integer> productQuantityMap = new HashMap<>();
        for (CommandeProduit item : allOrderItems) {
            productQuantityMap.put(item.getProduitId(), productQuantityMap.getOrDefault(item.getProduitId(), 0) + item.getQuantite());
        }

        // Get top 5 products
        List<Map.Entry<Integer, Integer>> topProducts = productQuantityMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Top Products");

        for (Map.Entry<Integer, Integer> entry : topProducts) {
            Produit product = produitService.getProduitById(entry.getKey());
            if (product != null) {
                series.getData().add(new XYChart.Data<>(product.getNom(), entry.getValue()));
            }
        }

        ObservableList<XYChart.Series<String, Number>> data = FXCollections.observableArrayList();
        data.add(series);
        topProductsBarChart.setData(data);
    }

    private final CategorieService categorieService = new CategorieService();

    private String getCategoryName(int categoryId) {
        Categorie categorie = categorieService.recupererParId(categoryId);
        if (categorie != null) {
            return categorie.getNom();
        } else {
            return "Renewable Energy " + categoryId;
        }
    }

    private void loadTotalIncome() {
        List<Commande> orders = commandeService.recuperer();
        double totalIncome = orders.stream()
                .filter(order -> "paid".equalsIgnoreCase(order.getStatut()))
                .mapToDouble(Commande::getTotal)
                .sum();
        totalIncomeLabel.setText(String.format("Total Income: %.2f DT", totalIncome));
    }

    private void loadTopCustomersData() {
        List<Commande> orders = commandeService.recuperer();

        // Calculate customer spending and order frequency
        Map<Integer, CustomerStats> customerStats = new HashMap<>();
        for (Commande order : orders) {
            if ("paid".equalsIgnoreCase(order.getStatut())) {
                int clientId = order.getClientId();
                CustomerStats stats = customerStats.computeIfAbsent(clientId, k -> new CustomerStats());
                stats.totalSpent += order.getTotal();
                stats.orderCount++;
            }
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        customerStats.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue().totalSpent, a.getValue().totalSpent))
                .limit(5)
                .forEach(entry -> {
                    User user = userService.getById(entry.getKey());
                    String customerName = user != null ? user.getPrenom() + " " + user.getNom() : "Unknown";
                    CustomerStats stats = entry.getValue();
                    String label = String.format("%s (%.0f DT)", customerName, stats.totalSpent);
                    PieChart.Data slice = new PieChart.Data(label, stats.totalSpent);
                    pieChartData.add(slice);
                    // Tooltips will be attached below after rendering
                });

        topCustomersPieChart.setData(pieChartData);
        topCustomersPieChart.setTitle("Top 5 Customers by Purchase Amount");

        // Attach tooltips and events after chart is rendered
        Platform.runLater(() -> {
            for (PieChart.Data slice : pieChartData) {
                User user = null;
                for (Map.Entry<Integer, CustomerStats> entry : customerStats.entrySet()) {
                    if (String.format("%s (%.0f DT)",
                        (userService.getById(entry.getKey()) != null ? userService.getById(entry.getKey()).getPrenom() + " " + userService.getById(entry.getKey()).getNom() : "Unknown"),
                        entry.getValue().totalSpent).equals(slice.getName())) {
                        user = userService.getById(entry.getKey());
                        CustomerStats stats = entry.getValue();
                        Tooltip tooltip = new Tooltip(String.format(
                            "Customer: %s\nTotal Spent: %.2f DT\nOrders: %d\nAvg Order: %.2f DT",
                            user != null ? user.getPrenom() + " " + user.getNom() : "Unknown",
                            stats.totalSpent, stats.orderCount,
                            stats.totalSpent / stats.orderCount
                        ));
                        slice.getNode().setOnMouseEntered(e -> {
                            slice.getNode().setStyle("-fx-opacity: 0.8;");
                            tooltip.show(slice.getNode(), e.getScreenX() + 10, e.getScreenY() + 10);
                        });
                        slice.getNode().setOnMouseExited(e -> {
                            slice.getNode().setStyle("-fx-opacity: 1;");
                            tooltip.hide();
                        });
                        break;
                    }
                }
            }
        });
    }

    private void loadCategorySalesData() {
        List<CommandeProduit> orderItems = commandeProduitService.recuperer();
        List<Produit> products = produitService.recuperer();

        Map<String, Double> categorySales = new HashMap<>();
        for (CommandeProduit item : orderItems) {
            Produit product = products.stream()
                    .filter(p -> p.getId() == item.getProduitId())
                    .findFirst()
                    .orElse(null);

            if (product != null) {
                String categoryName = getCategoryName(product.getCategorieId());
                double saleAmount = item.getQuantite() * product.getPrix();
                categorySales.put(categoryName, categorySales.getOrDefault(categoryName, 0.0) + saleAmount);
            }
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        // Sort categories by sales amount
        categorySales.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .forEach(entry -> {
                String label = String.format("%s (%.0f DT)", entry.getKey(), entry.getValue());
                pieChartData.add(new PieChart.Data(label, entry.getValue()));
            });

        categorySalesPieChart.setData(pieChartData);
        categorySalesPieChart.setTitle("Product Category Sales Distribution");
    }

    private static class CustomerStats {
        double totalSpent = 0;
        int orderCount = 0;
    }

    private void loadAdditionalMetrics() {
        List<Commande> orders = commandeService.recuperer();
        
        // Calculate total and paid orders
        long totalOrders = orders.size();
        long paidOrders = orders.stream()
                .filter(order -> "paid".equalsIgnoreCase(order.getStatut()))
                .count();
        // Calculate completion rate for potential future use
        // double orderCompletionRate = totalOrders > 0 ? (paidOrders * 100.0 / totalOrders) : 0;
        totalOrdersLabel.setText(String.format("%d", totalOrders));
        
        // Calculate average order value and trend
        double totalValue = orders.stream()
                .filter(order -> "paid".equalsIgnoreCase(order.getStatut()))
                .mapToDouble(Commande::getTotal)
                .sum();
        double averageValue = paidOrders > 0 ? totalValue / paidOrders : 0;
        
        // Calculate monthly trend
        // Calculate average order value for last month
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        double lastMonthValue = orders.stream()
                .filter(order -> "paid".equalsIgnoreCase(order.getStatut()))
                .filter(order -> {
                    Date d = order.getDate();
                    if (d == null) return false;
                    LocalDate localDate = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return !localDate.isBefore(oneMonthAgo);
                })
                .mapToDouble(Commande::getTotal)
                .average()
                .orElse(0);
        
        String trend = lastMonthValue > averageValue ? "↑" : "↓";
        averageOrderValueLabel.setText(String.format("%.2f DT %s", averageValue, trend));
        
        // Calculate customer metrics
        long uniqueCustomers = orders.stream()
                .map(Commande::getClientId)
                .distinct()
                .count();
        
        // Calculate repeat customer rate
        Map<Integer, Long> orderCounts = orders.stream()
                .collect(Collectors.groupingBy(Commande::getClientId, Collectors.counting()));
        long repeatCustomers = orderCounts.values().stream().filter(count -> count > 1).count();
        double repeatRate = uniqueCustomers > 0 ? (repeatCustomers * 100.0 / uniqueCustomers) : 0;
        
        totalCustomersLabel.setText(String.format("%d", uniqueCustomers));
    }
    
    private void loadRenewableEnergyMetrics() {
        // Calculate renewable energy metrics based on product data
        List<Produit> products = produitService.recuperer();
        List<CommandeProduit> orderItems = commandeProduitService.recuperer();
        
        // Calculate total renewable products sold
        long renewableProductsSold = orderItems.stream()
                .filter(item -> {
                    Produit product = products.stream()
                            .filter(p -> p.getId() == item.getProduitId())
                            .findFirst()
                            .orElse(null);
                    return product != null && product.isRecyclable();
                })
                .count();
        
        // Calculate percentage of renewable products in total sales
        double renewablePercentage = orderItems.isEmpty() ? 0 : 
                (double) renewableProductsSold / orderItems.size() * 100;
        
        // Update recyclable products label with more detailed information
        recyclableProductsLabel.setText(String.format("%.1f%% (of total sales)", renewablePercentage));
    }
    
    private void loadRenewableRatioAnalytics() {
        List<Produit> products = produitService.recuperer();
        // We're using the recyclable flag to represent renewable energy sources
        long renewableCount = products.stream().filter(Produit::isRecyclable).count();
        double renewablePercentage = products.isEmpty() ? 0 : (renewableCount * 100.0 / products.size());
        recyclableProductsLabel.setText(String.format("%.1f%%", renewablePercentage));
    }
    
    
    
   
    
   
    
   
    
  
    
   /* private void loadCompanyPerformanceMetrics() {
        // Add dummy data for company performance metrics
        Label revenueLabel = new Label("Revenue: 100,000 DT");
        revenueLabel.getStyleClass().add("company-performance-metric");
        clvLeaderboardBox.getChildren().add(revenueLabel);
        
        Label profitLabel = new Label("Profit: 20,000 DT");
        profitLabel.getStyleClass().add("company-performance-metric");
        clvLeaderboardBox.getChildren().add(profitLabel);
        
        Label employeeCountLabel = new Label("Employee Count: 50");
        employeeCountLabel.getStyleClass().add("company-performance-metric");
        clvLeaderboardBox.getChildren().add(employeeCountLabel);
        
        Label customerCountLabel = new Label("Customer Count: 1000");
        customerCountLabel.getStyleClass().add("company-performance-metric");
        clvLeaderboardBox.getChildren().add(customerCountLabel);
    }
     */
    
    /**
     * Loads data for the Product Owners by Sales pie chart
     * Shows which product owners have the most items sold
     */
    
    /**
     * Loads data for the Product Owners by Income pie chart
     * Shows which product owners have generated the most income
     */
   
    
    /**
     * Loads data for the Most Wishlisted Products pie chart
     * Shows which products are most frequently added to wishlists
     */
    private void loadWishlistedProductsData() {
        // Get all wishlist items
        List<Wishlist> allWishlistItems = wishlistService.recuperer();
        
        // Map to store product ID and wishlist count
        Map<Integer, Integer> productWishlistCountMap = new HashMap<>();
        
        // Count wishlist occurrences for each product
        for (Wishlist wishlistItem : allWishlistItems) {
            Produit product = wishlistItem.getProduit();
            if (product != null) {
                int productId = product.getId();
                productWishlistCountMap.put(productId, productWishlistCountMap.getOrDefault(productId, 0) + 1);
            }
        }
        
        // Create pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        // Get top 5 wishlisted products
        productWishlistCountMap.entrySet().stream()
            .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(entry -> {
                int productId = entry.getKey();
                Produit product = produitService.getProduitById(productId);
                String productName = (product != null) ? product.getNom() : "Product ID " + productId;
                
                PieChart.Data slice = new PieChart.Data(productName + " (" + entry.getValue() + " wishlists)", entry.getValue());
                pieChartData.add(slice);
            });
        
        // Set the data to the pie chart
        wishlistedProductsPieChart.setData(pieChartData);
        
        // Add tooltips
        for (PieChart.Data data : pieChartData) {
            Tooltip tooltip = new Tooltip(data.getName() + "\nWishlisted: " + data.getPieValue() + " times");
            Tooltip.install(data.getNode(), tooltip);
        }
    }
    

}
