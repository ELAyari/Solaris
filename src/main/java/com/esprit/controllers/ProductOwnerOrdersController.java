package com.esprit.controllers;

import com.esprit.Services.CommandeService;
import com.esprit.Services.ProduitService;
import com.esprit.Services.UserService;
import com.esprit.models.Commande;
import com.esprit.models.CommandeProduit;
import com.esprit.models.Produit;
import com.esprit.models.ProductOwner;
import com.esprit.models.User;
import com.esprit.models.UserRole;
import com.esprit.models.UserStatus;

import com.esprit.utils.UIUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Controller for the Product Owner Orders Dashboard view.
 * This dashboard allows product owners to view and manage orders containing their products.
 */
public class ProductOwnerOrdersController implements Initializable {

    // FXML components
    @FXML private Label companyNameLabel;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearButton;
    
    @FXML private Label pendingOrdersLabel;
    @FXML private Label processingOrdersLabel;
    @FXML private Label shippedOrdersLabel;
    @FXML private Label deliveredOrdersLabel;
    @FXML private Label totalRevenueLabel;
    
    @FXML private TableView<Commande> ordersTable;
    @FXML private TableColumn<Commande, Integer> orderIdColumn;
    @FXML private TableColumn<Commande, String> dateColumn;
    @FXML private TableColumn<Commande, String> customerColumn;
    @FXML private TableColumn<Commande, Double> totalColumn;
    @FXML private TableColumn<Commande, Integer> yourItemsColumn;
    @FXML private TableColumn<Commande, String> statusColumn;
    
    @FXML private Pagination ordersPagination;
    @FXML private VBox orderDetailsContainer;
    @FXML private PieChart orderStatusChart;
    @FXML private StackPane loadingOverlay;
    
    // Services
    private final CommandeService commandeService = new CommandeService();
    private final ProduitService produitService = new ProduitService();
    private UserService userService;
    
    // Data
    private User currentUser;
    private List<Integer> ownedProductIds = new ArrayList<>();
    private List<Commande> allOrders = new ArrayList<>();
    private List<Commande> filteredOrders = new ArrayList<>();
    private final Map<Integer, Produit> productCache = new ConcurrentHashMap<>();
    private final Map<Integer, User> userCache = new ConcurrentHashMap<>();
    private final int ITEMS_PER_PAGE = 10;
    
    // Filters
    private LocalDate filterStartDate;
    private LocalDate filterEndDate;
    private String filterStatus;
    private String searchQuery;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        // Initialize date filters to last 30 days by default
        filterEndDate = LocalDate.now();
        filterStartDate = filterEndDate.minusDays(30);
        startDatePicker.setValue(filterStartDate);
        endDatePicker.setValue(filterEndDate);
        
        // Initialize status filter
        List<String> statuses = Arrays.asList("All", "En attente", "En cours", "Expédiée", "Livrée", "Annulée");
        statusComboBox.setItems(FXCollections.observableArrayList(statuses));
        statusComboBox.setValue("All");
        
        // Initialize tables
        setupOrdersTable();
        
        // Set up event handlers
        setupEventHandlers();
    }
    
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
     * Sets the current user and loads their data.
     * @param user The current user (product owner)
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null && 
            ((user.getRole() == UserRole.PRODUCT_OWNER && user.getStatus() == UserStatus.CONFIRMED) ||
             (user instanceof ProductOwner && user.getStatus() == UserStatus.CONFIRMED))) {
            companyNameLabel.setText("Company: " + user.getNom() + "'s Products");
            loadData();
        }
    }
    
    /**
     * Sets up the orders table with appropriate cell factories.
     */
    private void setupOrdersTable() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        dateColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return new SimpleStringProperty(
                cellData.getValue().getDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate().format(formatter));
        });
        
        customerColumn.setCellValueFactory(cellData -> {
            int userId = cellData.getValue().getClientId();
            User user = getUserById(userId);
            return new SimpleStringProperty(user != null ? user.getNom() + " " + user.getPrenom() : "Unknown");
        });
        
        totalColumn.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getTotal();
            return new SimpleDoubleProperty(total).asObject();
        });
        
        yourItemsColumn.setCellValueFactory(cellData -> {
            Commande order = cellData.getValue();
            int count = countOwnedItemsInOrder(order);
            return new SimpleIntegerProperty(count).asObject();
        });
        
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        
        // Setup pagination
        ordersPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            updateOrdersTablePage(newIndex.intValue());
        });
    }
    
    /**
     * Sets up event handlers for UI components.
     */
    private void setupEventHandlers() {
        // Date filter change events
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterStartDate = newVal;
            applyFilters();
        });
        
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterEndDate = newVal;
            applyFilters();
        });
        
        // Status filter change event
        statusComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterStatus = "All".equals(newVal) ? null : newVal;
            applyFilters();
        });
        
        // Set placeholder text for search field
        searchField.setPromptText("Search by Order ID or Customer Name");
        
        // Add enter key event handler for search field
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                searchQuery = searchField.getText().trim();
                applyFilters();
            }
        });
        
        // Search button click event
        searchButton.setOnAction(event -> {
            searchQuery = searchField.getText().trim();
            applyFilters();
        });
        
        // Clear filters button click event
        clearButton.setOnAction(event -> {
            startDatePicker.setValue(LocalDate.now().minusDays(30));
            endDatePicker.setValue(LocalDate.now());
            statusComboBox.setValue("All");
            searchField.clear();
            searchQuery = null;
            applyFilters();
        });
    }
    
    /**
     * Loads all necessary data for the dashboard.
     */
    private void loadData() {
        showLoading(true, "Loading data...");
        
        // Load owned products
        CompletableFuture.supplyAsync(() -> {
            List<Produit> ownedProducts = produitService.recupererParOwnerId(currentUser.getId());
            ownedProductIds = ownedProducts.stream()
                    .map(Produit::getId)
                    .collect(Collectors.toList());
            
            // Cache products
            ownedProducts.forEach(product -> productCache.put(product.getId(), product));
            
            return ownedProducts;
        }).thenCompose(products -> {
            // Load all orders that contain owned products
            return CompletableFuture.supplyAsync(() -> {
                List<Commande> orders = commandeService.recuperer();
                return orders.stream()
                        .filter(this::orderContainsOwnedProducts)
                        .collect(Collectors.toList());
            });
        }).thenAccept(orders -> {
            Platform.runLater(() -> {
                allOrders = orders;
                applyFilters();
                updateDashboardSummary();
                updateOrderStatusChart();
                showLoading(false, null);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                showLoading(false, null);
                UIUtils.showAlert("Error", null, "Failed to load data: " + ex.getMessage(), "ERROR");
                ex.printStackTrace();
            });
            return null;
        });
    }
    
    /**
     * Applies filters to the orders list and updates the UI.
     */
    private void applyFilters() {
        showLoading(true, "Filtering orders...");
        
        CompletableFuture.supplyAsync(() -> {
            // Apply all filters
            return allOrders.stream()
                    .filter(order -> {
                        // Date filter
                        boolean dateMatch = true;
                        if (filterStartDate != null && filterEndDate != null) {
                            LocalDate orderDate = order.getDate().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                            dateMatch = !orderDate.isBefore(filterStartDate) && !orderDate.isAfter(filterEndDate);
                        }
                        
                        // Status filter
                        boolean statusMatch = true;
                        if (filterStatus != null && !filterStatus.isEmpty()) {
                            statusMatch = order.getStatut().equalsIgnoreCase(filterStatus);
                        }
                        
                        // Search query filter
                        boolean searchMatch = true;
                        if (searchQuery != null && !searchQuery.isEmpty()) {
                            // Check if search query is numeric (likely an order ID search)
                            boolean isNumeric = searchQuery.matches("\\d+");
                            
                            // Match by order ID - exact match if numeric, contains otherwise
                            boolean idMatch;
                            if (isNumeric) {
                                try {
                                    int searchId = Integer.parseInt(searchQuery);
                                    idMatch = (order.getId() == searchId);
                                } catch (NumberFormatException e) {
                                    idMatch = String.valueOf(order.getId()).contains(searchQuery);
                                }
                            } else {
                                idMatch = String.valueOf(order.getId()).contains(searchQuery);
                            }
                            
                            // Match by customer name
                            User customer = getUserById(order.getClientId());
                            boolean customerMatch = false;
                            if (customer != null) {
                                String customerName = customer.getNom() + " " + customer.getPrenom();
                                customerMatch = customerName.toLowerCase().contains(searchQuery.toLowerCase());
                            }
                            
                            searchMatch = idMatch || customerMatch;
                        }
                        
                        return dateMatch && statusMatch && searchMatch;
                    })
                    .collect(Collectors.toList());
        }).thenAccept(orders -> {
            Platform.runLater(() -> {
                filteredOrders = orders;
                updateOrdersPagination();
                updateDashboardSummary();
                showLoading(false, null);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                showLoading(false, null);
                UIUtils.showAlert("Error", null, "Failed to apply filters: " + ex.getMessage(), "ERROR");
                ex.printStackTrace();
            });
            return null;
        });
    }
    
    /**
     * Updates the pagination control based on the filtered orders.
     */
    private void updateOrdersPagination() {
        int pageCount = (filteredOrders.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        ordersPagination.setPageCount(Math.max(1, pageCount));
        ordersPagination.setCurrentPageIndex(0);
        updateOrdersTablePage(0);
    }
    
    /**
     * Updates the orders table with data for the specified page.
     * @param pageIndex The page index to display
     */
    private void updateOrdersTablePage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredOrders.size());
        
        if (fromIndex > toIndex) {
            ordersTable.setItems(FXCollections.observableArrayList());
            return;
        }
        
        List<Commande> pageItems = filteredOrders.subList(fromIndex, toIndex);
        ordersTable.setItems(FXCollections.observableArrayList(pageItems));
    }
    
    /**
     * Updates the dashboard summary with counts and totals.
     */
    private void updateDashboardSummary() {
        // Count orders by status
        long pendingCount = countOrdersByStatus("En attente");
        long processingCount = countOrdersByStatus("En cours");
        long shippedCount = countOrdersByStatus("Expédiée");
        long deliveredCount = countOrdersByStatus("Livrée");
        
        // Calculate total revenue from owned products
        double totalRevenue = calculateTotalRevenue();
        
        // Update UI
        pendingOrdersLabel.setText(String.valueOf(pendingCount));
        processingOrdersLabel.setText(String.valueOf(processingCount));
        shippedOrdersLabel.setText(String.valueOf(shippedCount));
        deliveredOrdersLabel.setText(String.valueOf(deliveredCount));
        totalRevenueLabel.setText(String.format("%.2f DT", totalRevenue));
    }
    
    /**
     * Updates the order status pie chart with interactive features.
     */
    private void updateOrderStatusChart() {
        Map<String, Integer> statusCounts = new HashMap<>();
        Map<String, Double> statusRevenue = new HashMap<>();
        
        // Count orders by status and calculate revenue per status
        for (Commande order : allOrders) {
            String status = order.getStatut();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
            
            // Calculate revenue for this order if it's not cancelled
            if (!"Annulée".equals(status)) {
                List<CommandeProduit> orderItems = getOrderItems(order.getId());
                double orderRevenue = 0.0;
                for (CommandeProduit item : orderItems) {
                    if (ownedProductIds.contains(item.getProduitId())) {
                        orderRevenue += item.getSousTotal();
                    }
                }
                statusRevenue.put(status, statusRevenue.getOrDefault(status, 0.0) + orderRevenue);
            }
        }
        
        // Create pie chart data with custom colors
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        // Add data points in specific order for consistency
        addStatusDataIfExists(pieChartData, statusCounts, "En attente");
        addStatusDataIfExists(pieChartData, statusCounts, "En cours");
        addStatusDataIfExists(pieChartData, statusCounts, "Expédiée");
        addStatusDataIfExists(pieChartData, statusCounts, "Livrée");
        addStatusDataIfExists(pieChartData, statusCounts, "Annulée");
        
        // Add any other statuses that might exist
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            if (!entry.getKey().equals("En attente") && 
                !entry.getKey().equals("En cours") && 
                !entry.getKey().equals("Expédiée") && 
                !entry.getKey().equals("Livrée") && 
                !entry.getKey().equals("Annulée")) {
                pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            }
        }
        
        orderStatusChart.setData(pieChartData);
        orderStatusChart.setTitle("Order Status Distribution");
        orderStatusChart.setLegendVisible(true);
        orderStatusChart.setLegendSide(javafx.geometry.Side.RIGHT);
        
        // Apply custom colors to the pie chart slices
        applyCustomColors(pieChartData);
        
        // Add tooltips and click handlers to pie chart slices
        addInteractivity(pieChartData, statusRevenue);
    }
    
    /**
     * Adds a status data point to the pie chart if it exists in the status counts.
     * @param pieChartData The pie chart data collection
     * @param statusCounts The map of status counts
     * @param status The status to add
     */
    private void addStatusDataIfExists(ObservableList<PieChart.Data> pieChartData, Map<String, Integer> statusCounts, String status) {
        if (statusCounts.containsKey(status)) {
            int count = statusCounts.get(status);
            pieChartData.add(new PieChart.Data(status + " (" + count + ")", count));
        }
    }
    
    /**
     * Applies custom colors to the pie chart slices.
     * @param pieChartData The pie chart data collection
     */
    private void applyCustomColors(ObservableList<PieChart.Data> pieChartData) {
        // Apply colors when the chart is shown
        orderStatusChart.setAnimated(true);
        
        // Apply custom colors after the chart has been laid out
        Platform.runLater(() -> {
            for (PieChart.Data data : pieChartData) {
                String status = data.getName().split(" \\(")[0]; // Extract status name without count
                
                String color;
                switch (status) {
                    case "En attente":
                        color = "#FFA500"; // Orange
                        break;
                    case "En cours":
                        color = "#1E90FF"; // Dodger blue
                        break;
                    case "Expédiée":
                        color = "#9370DB"; // Medium purple
                        break;
                    case "Livrée":
                        color = "#32CD32"; // Lime green
                        break;
                    case "Annulée":
                        color = "#DC143C"; // Crimson
                        break;
                    default:
                        color = "#808080"; // Gray
                        break;
                }
                
                // Get the node representing this slice
                Node node = data.getNode();
                if (node != null) {
                    node.setStyle("-fx-pie-color: " + color + ";");
                }
            }
        });
    }
    
    /**
     * Adds tooltips and click handlers to pie chart slices.
     * @param pieChartData The pie chart data collection
     * @param statusRevenue The map of revenue by status
     */
    private void addInteractivity(ObservableList<PieChart.Data> pieChartData, Map<String, Double> statusRevenue) {
        // Add tooltips and click handlers after the chart has been laid out
        Platform.runLater(() -> {
            for (PieChart.Data data : pieChartData) {
                String status = data.getName().split(" \\(")[0]; // Extract status name without count
                double revenue = statusRevenue.getOrDefault(status, 0.0);
                
                // Create tooltip with detailed information
                Tooltip tooltip = new Tooltip(
                    "Status: " + status + "\n" +
                    "Count: " + (int)data.getPieValue() + "\n" +
                    "Revenue: " + String.format("%.2f DT", revenue) + "\n" +
                    "Click to filter by this status"
                );
                tooltip.setStyle("-fx-font-size: 12px;");
                
                // Get the node representing this slice
                Node node = data.getNode();
                if (node != null) {
                    // Add tooltip
                    Tooltip.install(node, tooltip);
                    
                    // Add hover effect
                    node.setOnMouseEntered(event -> {
                        node.setStyle(node.getStyle() + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
                        node.setScaleX(1.1);
                        node.setScaleY(1.1);
                    });
                    
                    node.setOnMouseExited(event -> {
                        node.setStyle(node.getStyle().replace("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);", ""));
                        node.setScaleX(1.0);
                        node.setScaleY(1.0);
                    });
                    
                    // Add click handler to filter by this status
                    node.setOnMouseClicked(event -> {
                        statusComboBox.setValue(status);
                    });
                }
            }
        });
    }
    
    /**
     * Checks if an order contains any products owned by the current user.
     * @param order The order to check
     * @return true if the order contains owned products, false otherwise
     */
    private boolean orderContainsOwnedProducts(Commande order) {
        if (order == null) return false;
        
        List<CommandeProduit> orderItems = getOrderItems(order.getId());
        for (CommandeProduit item : orderItems) {
            if (ownedProductIds.contains(item.getProduitId())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Counts the number of owned items in an order.
     * @param order The order to check
     * @return The number of owned items
     */
    private int countOwnedItemsInOrder(Commande order) {
        if (order == null) return 0;
        
        List<CommandeProduit> orderItems = getOrderItems(order.getId());
        int count = 0;
        for (CommandeProduit item : orderItems) {
            if (ownedProductIds.contains(item.getProduitId())) {
                count += item.getQuantite();
            }
        }
        
        return count;
    }
    
    /**
     * Counts orders by status.
     * @param status The status to count
     * @return The number of orders with the specified status
     */
    private long countOrdersByStatus(String status) {
        return filteredOrders.stream()
                .filter(order -> order.getStatut().equalsIgnoreCase(status))
                .count();
    }
    
    /**
     * Calculates the total revenue from owned products.
     * @return The total revenue
     */
    private double calculateTotalRevenue() {
        double total = 0.0;
        
        for (Commande order : filteredOrders) {
            if (!"Annulée".equals(order.getStatut())) {
                List<CommandeProduit> orderItems = getOrderItems(order.getId());
                for (CommandeProduit item : orderItems) {
                    if (ownedProductIds.contains(item.getProduitId())) {
                        total += item.getSousTotal();
                    }
                }
            }
        }
        
        return total;
    }
    
    /**
     * Gets the order items for a specific order.
     * @param orderId The order ID
     * @return List of order items
     */
    private List<CommandeProduit> getOrderItems(int orderId) {
        List<CommandeProduit> items = new ArrayList<>();
        
        try {
            // Get order items from the database
            String sql = "SELECT * FROM commande_produit WHERE commande_id = ?";
            Connection conn = com.esprit.utils.DataSource.getInstance().getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, orderId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                CommandeProduit item = new CommandeProduit();
                item.setId(rs.getInt("id"));
                item.setCommandeId(rs.getInt("commande_id"));
                item.setProduitId(rs.getInt("produit_id"));
                item.setQuantite(rs.getInt("quantite"));
                item.setSousTotal(rs.getDouble("sous_total"));
                
                // Get product details
                Produit produit = getProductById(item.getProduitId());
                item.setProduit(produit);
                
                items.add(item);
            }
            
            rs.close();
            pst.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return items;
    }
    
    /**
     * Gets a product by ID from the cache or database.
     * @param productId The product ID
     * @return The product, or null if not found
     */
    private Produit getProductById(int productId) {
        if (productCache.containsKey(productId)) {
            return productCache.get(productId);
        }
        
        Produit product = produitService.getProduitById(productId);
        if (product != null) {
            productCache.put(productId, product);
        }
        
        return product;
    }
    
    /**
     * Gets a user by ID from the cache or database.
     * @param userId The user ID
     * @return The user, or null if not found
     */
    private User getUserById(int userId) {
        if (userCache.containsKey(userId)) {
            return userCache.get(userId);
        }
        
        User user = userService.getById(userId);
        if (user != null) {
            userCache.put(userId, user);
        }
        
        return user;
    }
    
    /**
     * Shows or hides the loading overlay with a message.
     * @param show Whether to show or hide the overlay
     * @param message The message to display
     */
    private void showLoading(boolean show, String message) {
        Platform.runLater(() -> {
            if (message != null) {
                Label loadingLabel = (Label) loadingOverlay.getChildren().get(0);
                loadingLabel.setText(message);
            }
            loadingOverlay.setVisible(show);
        });
    }
}
