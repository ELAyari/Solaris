package com.esprit.controllers;

import com.esprit.Services.CommandeService;
import com.esprit.Services.CommandeProduitService;
import com.esprit.Services.ProduitService;
import com.esprit.Services.UserService;
import com.esprit.models.Commande;
import com.esprit.models.CommandeProduit;
import com.esprit.models.Produit;
import com.esprit.models.ProductOwner;
import com.esprit.models.User;
import com.esprit.models.UserRole;
import com.esprit.models.UserStatus;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Controller for the Product Owner Dashboard view.
 * This dashboard provides analytics and insights for product owners about their products' performance,
 * including sales data, customer information, and inventory status.
 */
public class ProductOwnerDashboardController {

    @FXML private Label companyNameLabel;
    @FXML private Label totalSalesLabel;
    @FXML private Label salesTrendLabel;
    @FXML private Label unitsSoldLabel;
    @FXML private Label unitsTrendLabel;
    @FXML private Label avgRatingLabel;
    @FXML private Label ratingCountLabel;
    @FXML private Label inventoryStatusLabel;
    @FXML private Label lowStockLabel;
    @FXML private LineChart<String, Number> monthlySalesChart;
    @FXML private PieChart productDistributionChart;
    @FXML private BarChart<String, Number> productPerformanceChart;
    @FXML private PieChart customerDemographicsChart;
    @FXML private VBox topCustomersContainer;
    @FXML private LineChart<String, Number> salesForecastChart;
    
    // New UI components for enhancements
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Pagination customerPagination;
    @FXML private Label loadingLabel;

    // Services
    private final CommandeService commandeService = new CommandeService();
    private final CommandeProduitService commandeProduitService = new CommandeProduitService();
    private final ProduitService produitService = new ProduitService();
    private UserService userService;

    // Data fields
    private User currentUser;
    private List<Produit> ownerProducts = new ArrayList<>();
    private List<CommandeProduit> productOrders = new ArrayList<>();
    private double totalSales = 0;
    private int totalUnitsSold = 0;
    
    // Cache for performance optimization
    private Map<Integer, Commande> orderCache = new HashMap<>();
    private Map<Integer, Produit> productCache = new HashMap<>();
    private Map<Integer, User> userCache = new HashMap<>();
    
    // Date range filter
    private LocalDate filterStartDate;
    private LocalDate filterEndDate;
    
    // Pagination settings
    private static final int ITEMS_PER_PAGE = 5;

    /**
     * Initializes the dashboard controller.
     * This method is automatically called after the FXML has been loaded.
     */
    @FXML
    public void initialize() {
        // Initialize services
        initializeServices();
        
        // Initialize date filters to last 6 months by default
        filterEndDate = LocalDate.now();
        filterStartDate = filterEndDate.minusMonths(6);
        
        if (startDatePicker != null && endDatePicker != null) {
            startDatePicker.setValue(filterStartDate);
            endDatePicker.setValue(filterEndDate);
            
            // Add listeners for date pickers
            startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    filterStartDate = newVal;
                    refreshDashboard();
                }
            });
            
            endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    filterEndDate = newVal;
                    refreshDashboard();
                }
            });
        }
        
        // Set up pagination if available
        if (customerPagination != null) {
            customerPagination.setPageCount(1);
            customerPagination.currentPageIndexProperty().addListener(
                (obs, oldVal, newVal) -> loadTopCustomersPage(newVal.intValue()));
        }
        
        // Show loading indicator
        if (loadingLabel != null) {
            loadingLabel.setVisible(true);
        }
        
        // Load data asynchronously
        loadDataAsync();
    }
    
    /**
     * Initialize services required by this controller.
     * This approach makes integration with PI_DEV_G5 easier.
     */
    private void initializeServices() {
        try {
            // Initialize UserService directly
            this.userService = new UserService();
        } catch (Exception e) {
            System.err.println("Error initializing services: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads data asynchronously to improve UI responsiveness.
     */
    private void loadDataAsync() {
        CompletableFuture.runAsync(() -> {
            
            if (currentUser == null) {
                // Fallback to a default user for development purposes
                // This will be removed during integration with PI_DEV_G5
                currentUser = userService.getById(1);
            }
            cacheUser(currentUser);
            
            // Load products owned by this user
            loadOwnerProducts();
            
            // Load sales data for these products
            loadSalesData();
            
            // Update UI on JavaFX thread
            Platform.runLater(() -> {
                // Set company name (using product owner's name instead of company name)
                if (currentUser != null) {
                    // Check if user is a ProductOwner or has PRODUCT_OWNER role
                    if ((currentUser.getRole() == UserRole.PRODUCT_OWNER && currentUser.getStatus() == UserStatus.CONFIRMED) ||
                        (currentUser instanceof ProductOwner && currentUser.getStatus() == UserStatus.CONFIRMED)) {
                        companyNameLabel.setText(currentUser.getPrenom() + "'s Products");
                    } else {
                        companyNameLabel.setText("Your Products");
                    }
                } else {
                    companyNameLabel.setText("Your Products");
                }
                
                // Update dashboard metrics and charts
                updateDashboardMetrics();
                loadMonthlySalesChart();
                loadProductDistributionChart();
                loadProductPerformanceChart();
                loadCustomerDemographics();
                loadTopCustomers();
                loadSalesForecast();
                
                // Hide loading indicator
                if (loadingLabel != null) {
                    loadingLabel.setVisible(false);
                }
            });
        });
    }
    
    /**
     * Refreshes the dashboard with current filter settings.
     */
    private void refreshDashboard() {
        if (loadingLabel != null) {
            loadingLabel.setVisible(true);
        }
        
        CompletableFuture.runAsync(() -> {
            // Reload sales data with new date filters
            loadSalesData();
            
            Platform.runLater(() -> {
                updateDashboardMetrics();
                loadMonthlySalesChart();
                loadProductDistributionChart();
                loadProductPerformanceChart();
                loadTopCustomers();
                loadSalesForecast();
                
                if (loadingLabel != null) {
                    loadingLabel.setVisible(false);
                }
            });
        });
    }

    private void loadOwnerProducts() {
        if (currentUser == null) return;
        
        // Get all products owned by this user
        ownerProducts = produitService.recupererParOwnerId(currentUser.getId());
        
        if (ownerProducts == null) {
            ownerProducts = new ArrayList<>();
        }
    }

    private void loadSalesData() {
        if (ownerProducts.isEmpty()) return;
        
        // Get all order items for these products
        List<Integer> productIds = ownerProducts.stream()
                .map(Produit::getId)
                .collect(Collectors.toList());
        
        productOrders = new ArrayList<>();
        List<CommandeProduit> allOrderItems = commandeProduitService.recuperer();
        
        for (CommandeProduit orderItem : allOrderItems) {
            if (productIds.contains(orderItem.getProduitId())) {
                productOrders.add(orderItem);
                
                // Calculate total sales and units sold
                Produit product = ownerProducts.stream()
                        .filter(p -> p.getId() == orderItem.getProduitId())
                        .findFirst()
                        .orElse(null);
                
                if (product != null) {
                    // Check if the order is completed/paid
                    Commande order = commandeService.getCommandeById(orderItem.getCommandeId());
                    if (order != null && "paid".equalsIgnoreCase(order.getStatut())) {
                        totalSales += orderItem.getQuantite() * product.getPrix();
                        totalUnitsSold += orderItem.getQuantite();
                    }
                }
            }
        }
    }

    private void updateDashboardMetrics() {
        // Update total sales
        totalSalesLabel.setText(String.format("%.2f DT", totalSales));
        
        // Calculate sales trend (comparing to previous month)
        double previousMonthSales = calculatePreviousMonthSales();
        double currentMonthSales = calculateCurrentMonthSales();
        
        if (previousMonthSales > 0) {
            double salesTrend = ((currentMonthSales - previousMonthSales) / previousMonthSales) * 100;
            String trendSymbol = salesTrend >= 0 ? "↑" : "↓";
            salesTrendLabel.setText(String.format("%s %.1f%%", trendSymbol, Math.abs(salesTrend)));
            salesTrendLabel.getStyleClass().add(salesTrend >= 0 ? "metric-trend-up" : "metric-trend-down");
        } else {
            salesTrendLabel.setText("New");
        }
        
        // Update units sold
        unitsSoldLabel.setText(String.valueOf(totalUnitsSold));
        
        // Calculate units trend
        int previousMonthUnits = calculatePreviousMonthUnits();
        int currentMonthUnits = calculateCurrentMonthUnits();
        
        if (previousMonthUnits > 0) {
            double unitsTrend = ((double)(currentMonthUnits - previousMonthUnits) / previousMonthUnits) * 100;
            String trendSymbol = unitsTrend >= 0 ? "↑" : "↓";
            unitsTrendLabel.setText(String.format("%s %.1f%%", trendSymbol, Math.abs(unitsTrend)));
            unitsTrendLabel.getStyleClass().add(unitsTrend >= 0 ? "metric-trend-up" : "metric-trend-down");
        } else {
            unitsTrendLabel.setText("New");
        }
        
        // Update average rating (simulated for now)
        double avgRating = 4.2; // Simulated average rating
        int ratingCount = 18; // Simulated rating count
        avgRatingLabel.setText(String.format("%.1f ★", avgRating));
        ratingCountLabel.setText(String.format("%d reviews", ratingCount));
        
        // Update inventory status
        int totalStock = ownerProducts.stream()
                .mapToInt(Produit::getQuantiteStock)
                .sum();
        
        int lowStockCount = (int) ownerProducts.stream()
                .filter(p -> p.getQuantiteStock() < 10)
                .count();
        
        inventoryStatusLabel.setText(String.format("%d items", totalStock));
        lowStockLabel.setText(String.format("%d low stock", lowStockCount));
        
        if (lowStockCount > 0) {
            lowStockLabel.getStyleClass().add("metric-warning");
        }
    }

    private double calculatePreviousMonthSales() {
        if (productOrders == null || productOrders.isEmpty()) return 0;
        
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = LocalDate.of(now.getYear(), now.getMonth(), 1);
        LocalDate firstDayOfPreviousMonth = firstDayOfMonth.minusMonths(1);
        LocalDate lastDayOfPreviousMonth = firstDayOfMonth.minusDays(1);
        
        return productOrders.stream()
                .filter(item -> {
                    Commande order = commandeService.getCommandeById(item.getCommandeId());
                    if (order == null || order.getDate() == null || !"paid".equalsIgnoreCase(order.getStatut())) {
                        return false;
                    }
                    
                    LocalDate orderDate = order.getDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    
                    return !orderDate.isBefore(firstDayOfPreviousMonth) && !orderDate.isAfter(lastDayOfPreviousMonth);
                })
                .mapToDouble(item -> {
                    Produit product = ownerProducts.stream()
                            .filter(p -> p.getId() == item.getProduitId())
                            .findFirst()
                            .orElse(null);
                    
                    return product != null ? item.getQuantite() * product.getPrix() : 0;
                })
                .sum();
    }

    private double calculateCurrentMonthSales() {
        if (productOrders == null || productOrders.isEmpty()) return 0;
        
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = LocalDate.of(now.getYear(), now.getMonth(), 1);
        
        return productOrders.stream()
                .filter(item -> {
                    Commande order = commandeService.getCommandeById(item.getCommandeId());
                    if (order == null || order.getDate() == null || !"paid".equalsIgnoreCase(order.getStatut())) {
                        return false;
                    }
                    
                    LocalDate orderDate = order.getDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    
                    return !orderDate.isBefore(firstDayOfMonth) && !orderDate.isAfter(now);
                })
                .mapToDouble(item -> {
                    Produit product = ownerProducts.stream()
                            .filter(p -> p.getId() == item.getProduitId())
                            .findFirst()
                            .orElse(null);
                    
                    return product != null ? item.getQuantite() * product.getPrix() : 0;
                })
                .sum();
    }

    private int calculatePreviousMonthUnits() {
        if (productOrders == null || productOrders.isEmpty()) return 0;
        
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = LocalDate.of(now.getYear(), now.getMonth(), 1);
        LocalDate firstDayOfPreviousMonth = firstDayOfMonth.minusMonths(1);
        LocalDate lastDayOfPreviousMonth = firstDayOfMonth.minusDays(1);
        
        return productOrders.stream()
                .filter(item -> {
                    Commande order = commandeService.getCommandeById(item.getCommandeId());
                    if (order == null || order.getDate() == null || !"paid".equalsIgnoreCase(order.getStatut())) {
                        return false;
                    }
                    
                    LocalDate orderDate = order.getDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    
                    return !orderDate.isBefore(firstDayOfPreviousMonth) && !orderDate.isAfter(lastDayOfPreviousMonth);
                })
                .mapToInt(CommandeProduit::getQuantite)
                .sum();
    }

    private int calculateCurrentMonthUnits() {
        if (productOrders == null || productOrders.isEmpty()) return 0;
        
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = LocalDate.of(now.getYear(), now.getMonth(), 1);
        
        return productOrders.stream()
                .filter(item -> {
                    Commande order = commandeService.getCommandeById(item.getCommandeId());
                    if (order == null || order.getDate() == null || !"paid".equalsIgnoreCase(order.getStatut())) {
                        return false;
                    }
                    
                    LocalDate orderDate = order.getDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    
                    return !orderDate.isBefore(firstDayOfMonth) && !orderDate.isAfter(now);
                })
                .mapToInt(CommandeProduit::getQuantite)
                .sum();
    }

    private void loadMonthlySalesChart() {
        // Get sales data for the last 6 months
        Map<String, Double> monthlySales = new LinkedHashMap<>();
        Map<String, Integer> monthlyUnits = new LinkedHashMap<>();
        
        LocalDate now = LocalDate.now();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");
        
        // Initialize with the last 6 months
        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            String monthName = monthDate.format(monthFormatter);
            monthlySales.put(monthName, 0.0);
            monthlyUnits.put(monthName, 0);
        }
        
        // Calculate sales for each month
        for (CommandeProduit item : productOrders) {
            Commande order = commandeService.getCommandeById(item.getCommandeId());
            if (order == null || order.getDate() == null || !"paid".equalsIgnoreCase(order.getStatut())) {
                continue;
            }
            
            LocalDate orderDate = order.getDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            
            // Only consider the last 6 months
            if (orderDate.isBefore(now.minusMonths(6))) {
                continue;
            }
            
            String monthName = orderDate.format(monthFormatter);
            
            if (monthlySales.containsKey(monthName)) {
                Produit product = ownerProducts.stream()
                        .filter(p -> p.getId() == item.getProduitId())
                        .findFirst()
                        .orElse(null);
                
                if (product != null) {
                    double currentSales = monthlySales.get(monthName);
                    monthlySales.put(monthName, currentSales + (item.getQuantite() * product.getPrix()));
                    
                    int currentUnits = monthlyUnits.get(monthName);
                    monthlyUnits.put(monthName, currentUnits + item.getQuantite());
                }
            }
        }
        
        // Create sales series
        XYChart.Series<String, Number> salesSeries = new XYChart.Series<>();
        salesSeries.setName("Sales (DT)");
        
        for (Map.Entry<String, Double> entry : monthlySales.entrySet()) {
            salesSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        // Create units series
        XYChart.Series<String, Number> unitsSeries = new XYChart.Series<>();
        unitsSeries.setName("Units Sold");
        
        for (Map.Entry<String, Integer> entry : monthlyUnits.entrySet()) {
            unitsSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        // Add series to chart
        monthlySalesChart.getData().clear();
        monthlySalesChart.getData().add(salesSeries);
        monthlySalesChart.getData().add(unitsSeries);
    }

    private void loadProductDistributionChart() {
        if (ownerProducts.isEmpty() || productOrders.isEmpty()) {
            // Add placeholder data
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("No Data Available", 1)
            );
            productDistributionChart.setData(pieChartData);
            return;
        }
        
        // Calculate sales distribution by product
        Map<String, Double> productSales = new HashMap<>();
        
        for (CommandeProduit item : productOrders) {
            Commande order = commandeService.getCommandeById(item.getCommandeId());
            if (order == null || !"paid".equalsIgnoreCase(order.getStatut())) {
                continue;
            }
            
            Produit product = ownerProducts.stream()
                    .filter(p -> p.getId() == item.getProduitId())
                    .findFirst()
                    .orElse(null);
            
            if (product != null) {
                String productName = product.getNom();
                double saleAmount = item.getQuantite() * product.getPrix();
                productSales.put(productName, productSales.getOrDefault(productName, 0.0) + saleAmount);
            }
        }
        
        // Create pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        productSales.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
                    pieChartData.add(slice);
                });
        
        productDistributionChart.setData(pieChartData);
        
        // Add tooltips
        Platform.runLater(() -> {
            for (PieChart.Data data : pieChartData) {
                Tooltip tooltip = new Tooltip(String.format(
                        "%s: %.2f DT (%.1f%%)",
                        data.getName(),
                        data.getPieValue(),
                        (data.getPieValue() / totalSales) * 100
                ));
                
                Tooltip.install(data.getNode(), tooltip);
            }
        });
    }

    private void loadProductPerformanceChart() {
        if (ownerProducts.isEmpty() || productOrders.isEmpty()) {
            return;
        }
        
        // Calculate units sold by product
        Map<String, Integer> productUnitsSold = new HashMap<>();
        
        for (CommandeProduit item : productOrders) {
            Commande order = commandeService.getCommandeById(item.getCommandeId());
            if (order == null || !"paid".equalsIgnoreCase(order.getStatut())) {
                continue;
            }
            
            Produit product = ownerProducts.stream()
                    .filter(p -> p.getId() == item.getProduitId())
                    .findFirst()
                    .orElse(null);
            
            if (product != null) {
                String productName = product.getNom();
                productUnitsSold.put(productName, productUnitsSold.getOrDefault(productName, 0) + item.getQuantite());
            }
        }
        
        // Create bar chart series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Units Sold");
        
        productUnitsSold.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                });
        
        productPerformanceChart.getData().clear();
        productPerformanceChart.getData().add(series);
        
        // Add tooltips
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Tooltip tooltip = new Tooltip(String.format(
                        "%s: %d units",
                        data.getXValue(),
                        data.getYValue().intValue()
                ));
                
                Tooltip.install(data.getNode(), tooltip);
            }
        });
    }

    private void loadCustomerDemographics() {
        // This would typically come from real customer data
        // For now, we'll use simulated data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Age 18-24", 15),
                new PieChart.Data("Age 25-34", 35),
                new PieChart.Data("Age 35-44", 25),
                new PieChart.Data("Age 45-54", 15),
                new PieChart.Data("Age 55+", 10)
        );
        
        customerDemographicsChart.setData(pieChartData);
    }

    /**
     * Loads the top customers section with pagination support.
     */
    private void loadTopCustomers() {
        if (customerPagination != null) {
            // Calculate total pages based on customer count
            Map<Integer, Double> customerSpending = calculateCustomerSpending();
            int totalPages = (int) Math.ceil((double) customerSpending.size() / ITEMS_PER_PAGE);
            customerPagination.setPageCount(Math.max(1, totalPages));
            
            // Load first page
            loadTopCustomersPage(0);
        } else {
            // Fallback to non-paginated version
            loadTopCustomersPage(0);
        }
    }
    
    /**
     * Calculates spending by customer.
     * @return Map of customer IDs to their total spending
     */
    private Map<Integer, Double> calculateCustomerSpending() {
        Map<Integer, Double> customerSpending = new HashMap<>();
        
        if (productOrders.isEmpty()) {
            return customerSpending;
        }
        
        for (CommandeProduit item : productOrders) {
            Commande order = getCachedOrder(item.getCommandeId());
            if (order == null || !"paid".equalsIgnoreCase(order.getStatut())) {
                continue;
            }
            
            // Apply date filter
            LocalDate orderDate = order.getDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            
            if (orderDate.isBefore(filterStartDate) || orderDate.isAfter(filterEndDate)) {
                continue;
            }
            
            Produit product = getCachedProduct(item.getProduitId());
            if (product != null) {
                int customerId = order.getClientId();
                double saleAmount = item.getQuantite() * product.getPrix();
                customerSpending.put(customerId, customerSpending.getOrDefault(customerId, 0.0) + saleAmount);
            }
        }
        
        return customerSpending;
    }
    
    /**
     * Loads a specific page of top customers.
     * @param pageIndex The page index to load
     */
    private void loadTopCustomersPage(int pageIndex) {
        topCustomersContainer.getChildren().clear();
        
        Map<Integer, Double> customerSpending = calculateCustomerSpending();
        
        if (customerSpending.isEmpty()) {
            Label noDataLabel = new Label("No customer data available");
            noDataLabel.getStyleClass().add("customer-info");
            topCustomersContainer.getChildren().add(noDataLabel);
            return;
        }
        
        // Get sorted customers
        List<Map.Entry<Integer, Double>> sortedCustomers = customerSpending.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        // Calculate start and end indices for current page
        int startIndex = pageIndex * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, sortedCustomers.size());
        
        // Get customers for current page
        List<Map.Entry<Integer, Double>> pageCustomers = sortedCustomers.subList(
                startIndex, endIndex);
        
        // Create customer rows
        for (Map.Entry<Integer, Double> entry : pageCustomers) {
            User customer = getCachedUser(entry.getKey());
            if (customer == null) continue;
            
            HBox customerRow = createCustomerRow(customer, entry.getValue());
            topCustomersContainer.getChildren().add(customerRow);
        }
    }
    
    /**
     * Creates a UI row for a customer.
     * @param customer The customer
     * @param totalSpent The total amount spent by the customer
     * @return An HBox containing the customer information
     */
    private HBox createCustomerRow(User customer, double totalSpent) {
        HBox customerRow = new HBox();
        customerRow.getStyleClass().add("customer-row");
        customerRow.setSpacing(10);
        
        VBox customerInfo = new VBox();
        customerInfo.setSpacing(2);
        HBox.setHgrow(customerInfo, Priority.ALWAYS);
        
        Label nameLabel = new Label(customer.getPrenom() + " " + customer.getNom());
        nameLabel.getStyleClass().add("customer-name");
        
        Label spendingLabel = new Label(String.format("Total Spent: %.2f DT", totalSpent));
        spendingLabel.getStyleClass().add("customer-info");
        
        customerInfo.getChildren().addAll(nameLabel, spendingLabel);
        customerRow.getChildren().add(customerInfo);
        
        return customerRow;
    }

    private void loadSalesForecast() {
        // Create forecast series
        XYChart.Series<String, Number> forecastSeries = new XYChart.Series<>();
        forecastSeries.setName("Projected Sales");
        
        // Get current month and the next 6 months
        LocalDate now = LocalDate.now();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");
        
        // Use last 3 months average growth to project future sales
        double lastMonthSales = calculateMonthSales(now.getMonth());
        double twoMonthsAgoSales = calculateMonthSales(now.getMonth().minus(1));
        double threeMonthsAgoSales = calculateMonthSales(now.getMonth().minus(2));
        
        // Calculate average growth rate
        double avgGrowthRate = 0.05; // Default 5% growth
        
        if (twoMonthsAgoSales > 0 && threeMonthsAgoSales > 0) {
            double growth1 = (lastMonthSales - twoMonthsAgoSales) / twoMonthsAgoSales;
            double growth2 = (twoMonthsAgoSales - threeMonthsAgoSales) / threeMonthsAgoSales;
            avgGrowthRate = (growth1 + growth2) / 2;
            
            // Cap growth rate to reasonable values
            avgGrowthRate = Math.max(-0.1, Math.min(0.2, avgGrowthRate));
        }
        
        // Project sales for next 6 months
        double projectedSales = lastMonthSales;
        
        for (int i = 0; i < 6; i++) {
            LocalDate futureMonth = now.plusMonths(i);
            String monthName = futureMonth.format(monthFormatter);
            
            // Apply growth rate
            if (i > 0) {
                projectedSales = projectedSales * (1 + avgGrowthRate);
            }
            
            forecastSeries.getData().add(new XYChart.Data<>(monthName, projectedSales));
        }
        
        // Add actual sales series for comparison
        XYChart.Series<String, Number> actualSeries = new XYChart.Series<>();
        actualSeries.setName("Actual Sales");
        
        // Add last 3 months of actual data
        for (int i = 2; i >= 0; i--) {
            LocalDate pastMonth = now.minusMonths(i);
            String monthName = pastMonth.format(monthFormatter);
            double monthlySales = calculateMonthSales(pastMonth.getMonth());
            
            actualSeries.getData().add(new XYChart.Data<>(monthName, monthlySales));
        }
        
        // Add series to chart
        salesForecastChart.getData().clear();
        salesForecastChart.getData().add(actualSeries);
        salesForecastChart.getData().add(forecastSeries);
    }
    
    /**
     * Gets a cached order or retrieves it from the database if not cached.
     * @param orderId The order ID
     * @return The order or null if not found
     */
    private Commande getCachedOrder(int orderId) {
        if (!orderCache.containsKey(orderId)) {
            Commande order = commandeService.getCommandeById(orderId);
            if (order != null) {
                orderCache.put(orderId, order);
            }
            return order;
        }
        return orderCache.get(orderId);
    }
    
    /**
     * Gets a cached product or retrieves it from the database if not cached.
     * @param productId The product ID
     * @return The product or null if not found
     */
    private Produit getCachedProduct(int productId) {
        if (!productCache.containsKey(productId)) {
            Produit product = ownerProducts.stream()
                    .filter(p -> p.getId() == productId)
                    .findFirst()
                    .orElse(null);
            if (product != null) {
                productCache.put(productId, product);
            }
            return product;
        }
        return productCache.get(productId);
    }
    
    /**
     * Gets a cached user or retrieves it from the database if not cached.
     * @param userId The user ID
     * @return The user or null if not found
     */
    private User getCachedUser(int userId) {
        if (!userCache.containsKey(userId)) {
            User user = userService.getById(userId);
            if (user != null) {
                userCache.put(userId, user);
            }
            return user;
        }
        return userCache.get(userId);
    }
    
    /**
     * Caches a user.
     * @param user The user to cache
     */
    private void cacheUser(User user) {
        if (user != null) {
            userCache.put(user.getId(), user);
        }
    }
    
    /**
     * Exports dashboard data to CSV.
     */
    @FXML
    public void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Dashboard Data");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("dashboard_data.csv");
        
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            Task<Void> exportTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    exportDashboardData(file);
                    return null;
                }
            };
            
            exportTask.setOnSucceeded(event -> {
                // Show success message
                if (loadingLabel != null) {
                    loadingLabel.setText("Export completed successfully");
                    loadingLabel.setVisible(true);
                    
                    // Hide message after 3 seconds
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> loadingLabel.setVisible(false));
                        }
                    }, 3000);
                }
            });
            
            exportTask.setOnFailed(event -> {
                // Show error message
                if (loadingLabel != null) {
                    loadingLabel.setText("Export failed: " + exportTask.getException().getMessage());
                    loadingLabel.setVisible(true);
                }
            });
            
            // Show loading message
            if (loadingLabel != null) {
                loadingLabel.setText("Exporting data...");
                loadingLabel.setVisible(true);
            }
            
            new Thread(exportTask).start();
        }
    }
    
    /**
     * Exports dashboard data to a CSV file.
     * @param file The file to export to
     * @throws IOException If an I/O error occurs
     */
    private void exportDashboardData(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.write("Dashboard Data Export\n");
            writer.write("Date Range: " + filterStartDate + " to " + filterEndDate + "\n\n");
            
            // Write summary metrics
            writer.write("Summary Metrics\n");
            writer.write("Total Sales," + totalSales + " DT\n");
            writer.write("Units Sold," + totalUnitsSold + "\n\n");
            
            // Write product data
            writer.write("Product Performance\n");
            writer.write("Product ID,Product Name,Units Sold,Total Sales\n");
            
            // Calculate sales by product
            Map<Integer, Integer> unitsByProduct = new HashMap<>();
            Map<Integer, Double> salesByProduct = new HashMap<>();
            
            for (CommandeProduit item : productOrders) {
                Commande order = getCachedOrder(item.getCommandeId());
                if (order == null || !"paid".equalsIgnoreCase(order.getStatut())) {
                    continue;
                }
                
                // Apply date filter
                LocalDate orderDate = order.getDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                
                if (orderDate.isBefore(filterStartDate) || orderDate.isAfter(filterEndDate)) {
                    continue;
                }
                
                Produit product = getCachedProduct(item.getProduitId());
                if (product != null) {
                    int productId = product.getId();
                    int quantity = item.getQuantite();
                    double saleAmount = quantity * product.getPrix();
                    
                    unitsByProduct.put(productId, unitsByProduct.getOrDefault(productId, 0) + quantity);
                    salesByProduct.put(productId, salesByProduct.getOrDefault(productId, 0.0) + saleAmount);
                }
            }
            
            // Write product data
            for (Produit product : ownerProducts) {
                int productId = product.getId();
                int units = unitsByProduct.getOrDefault(productId, 0);
                double sales = salesByProduct.getOrDefault(productId, 0.0);
                
                writer.write(productId + "," + product.getNom() + "," + units + "," + sales + "\n");
            }
            
            writer.write("\n");
            
            // Write customer data
            writer.write("Top Customers\n");
            writer.write("Customer ID,Customer Name,Total Spent\n");
            
            Map<Integer, Double> customerSpending = calculateCustomerSpending();
            customerSpending.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                    .forEach(entry -> {
                        try {
                            User customer = getCachedUser(entry.getKey());
                            if (customer != null) {
                                writer.write(customer.getId() + "," + 
                                        customer.getPrenom() + " " + customer.getNom() + "," + 
                                        entry.getValue() + "\n");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    private double calculateMonthSales(Month month) {
        if (productOrders.isEmpty()) return 0;
        
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        
        // If the month is after the current month, it must be from last year
        if (month.getValue() > now.getMonthValue()) {
            year--;
        }
        
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);
        
        return productOrders.stream()
                .filter(item -> {
                    Commande order = commandeService.getCommandeById(item.getCommandeId());
                    if (order == null || order.getDate() == null || !"paid".equalsIgnoreCase(order.getStatut())) {
                        return false;
                    }
                    
                    LocalDate orderDate = order.getDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    
                    return !orderDate.isBefore(firstDayOfMonth) && !orderDate.isAfter(lastDayOfMonth);
                })
                .mapToDouble(item -> {
                    Produit product = ownerProducts.stream()
                            .filter(p -> p.getId() == item.getProduitId())
                            .findFirst()
                            .orElse(null);
                    
                    return product != null ? item.getQuantite() * product.getPrix() : 0;
                })
                .sum();
    }
}
