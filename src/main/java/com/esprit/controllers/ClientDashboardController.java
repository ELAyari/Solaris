package com.esprit.controllers;

import com.esprit.Services.EvenementService;
import com.esprit.Services.ReservationService;
import com.esprit.models.Evenement;
import com.esprit.models.Reservation;
import com.esprit.models.User;
import com.esprit.tests.MainprogGUI;
import com.esprit.utils.AnimationUtils;
import com.esprit.utils.AvatarHelper;
import com.esprit.utils.NotificationManager;
import com.esprit.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ClientDashboardController implements Initializable {

    private MainprogGUI mainApp;
    private EvenementService evenementService;
    private ReservationService reservationService;
    
    // Track current active tab
    private Button currentActiveTab = null;

    @FXML private HBox profileSection;
    @FXML private Label lblUsername;
    @FXML private Button btnNotificationBell;
    @FXML private Label lblNotificationCount;
    @FXML private Circle sidebarAvatar;
    
    // Sidebar buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnEvents;
    @FXML private Button btnProducts;
    @FXML private Button btnBlog;
    @FXML private Button btnProfile;
    @FXML private Button btnLogout;
    
    // Content elements
    @FXML private StackPane contentPane;
    @FXML private Label lblWelcome;
    @FXML private Label lblAvailableEvents;
    @FXML private Label lblMyReservations;
    @FXML private Label lblUpcomingEvents;
    @FXML private Button btnViewAllEvents;
    @FXML private Label lblTopEvent;
    @FXML private Label lblReservationsMonth;
    @FXML private VBox eventsContainer;
    @FXML private VBox noEventsContainer;
    @FXML private VBox recentActivityContainer;

    public void setMainApp(MainprogGUI mainApp) {
        this.mainApp = mainApp;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialize services
            evenementService = new EvenementService();
            reservationService = new ReservationService();

            // Load user information
            loadUserInfo();
            
            // Load dashboard data
            loadDashboardStats();
            loadDashboardInsights();
            loadLatestEvents();
            loadRecentActivity();

            // Setup avatar with automatic refresh
            if (sidebarAvatar != null) {
                AvatarHelper.setupAvatar(sidebarAvatar);
            }

            // Apply animations
            animateDashboardElements();
            
            // Set dashboard as initial active tab only if no other tab is active
            if (currentActiveTab == null) {
                currentActiveTab = btnDashboard;
                styleActiveButton(btnDashboard);
            }
            
            // Store this controller in the scene's user data for tab state restoration
            if (btnDashboard != null && btnDashboard.getScene() != null) {
                btnDashboard.getScene().setUserData(this);
            }
        } catch (Exception e) {
            System.err.println("Error initializing ClientDashboardController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadUserInfo() {
        User currentUser = UserSession.getInstance().getUser();
        if (currentUser != null) {
            lblUsername.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            lblWelcome.setText("Welcome back, " + currentUser.getNom() + "!");
        }
    }

    private void loadDashboardStats() {
        try {
            User currentUser = UserSession.getInstance().getUser();
            if (currentUser == null) return;

            // Get all events
            List<Evenement> allEvents = evenementService.get();
            lblAvailableEvents.setText(String.valueOf(allEvents.size()));

            // Get user's reservations
            int userReservations = 0;
            for (Evenement event : allEvents) {
                List<Reservation> eventReservations = reservationService.getByEvent(event.getId());
                for (Reservation reservation : eventReservations) {
                    if (reservation.getUtilisateur().getId() == currentUser.getId()) {
                        userReservations++;
                        break;
                    }
                }
            }
            lblMyReservations.setText(String.valueOf(userReservations));

            // Count upcoming events (this week)
            long upcomingCount = allEvents.stream()
                .filter(event -> event.getDate() != null)
                .filter(event -> event.getDate().isAfter(java.time.LocalDateTime.now()))
                .filter(event -> event.getDate().isBefore(java.time.LocalDateTime.now().plusWeeks(1)))
                .count();
            lblUpcomingEvents.setText(String.valueOf(upcomingCount));

        } catch (Exception e) {
            System.err.println("Error loading dashboard stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadLatestEvents() {
        try {
            List<Evenement> events = evenementService.get();
            
            if (events.isEmpty()) {
                eventsContainer.setVisible(false);
                eventsContainer.setManaged(false);
                noEventsContainer.setVisible(true);
                noEventsContainer.setManaged(true);
                return;
            }

            eventsContainer.getChildren().clear();
            
            // Show latest 3 events
            events.stream()
                .filter(event -> event.getDate() != null)
                .filter(event -> event.getDate().isAfter(java.time.LocalDateTime.now()))
                .sorted((e1, e2) -> e1.getDate().compareTo(e2.getDate()))
                .limit(3)
                .forEach(this::createEventCard);

        } catch (Exception e) {
            System.err.println("Error loading latest events: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createEventCard(Evenement event) {
        HBox eventCard = new HBox(15);
        eventCard.getStyleClass().add("event-card");
        eventCard.setAlignment(Pos.CENTER_LEFT);
        eventCard.setPadding(new Insets(15));

        // Event image
        ImageView eventImage = new ImageView();
        eventImage.setFitWidth(80);
        eventImage.setFitHeight(60);
        eventImage.setPreserveRatio(true);
        eventImage.getStyleClass().add("event-image");

        String imageUrl = event.getPosterImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                eventImage.setImage(new Image(imageUrl, true));
            } catch (Exception e) {
                eventImage.setImage(new Image(getClass().getResource("/default_event.png").toExternalForm(), true));
            }
        } else {
            eventImage.setImage(new Image(getClass().getResource("/default_event.png").toExternalForm(), true));
        }

        // Event details
        VBox eventDetails = new VBox(5);
        eventDetails.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(eventDetails, javafx.scene.layout.Priority.ALWAYS);

        Label eventTitle = new Label(event.getTitre());
        eventTitle.getStyleClass().add("event-title");

        Label eventDate = new Label(event.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm")));
        eventDate.getStyleClass().add("event-date");

        Label eventLocation = new Label(event.getAdresse());
        eventLocation.getStyleClass().add("event-location");

        eventDetails.getChildren().addAll(eventTitle, eventDate, eventLocation);

        // Price and status
        VBox eventMeta = new VBox(5);
        eventMeta.setAlignment(Pos.CENTER_RIGHT);

        Label eventPrice = new Label();
        long cents = event.getPriceCents();
        if (cents <= 0) {
            eventPrice.setText("Free");
            eventPrice.getStyleClass().add("event-price-free");
        } else {
            eventPrice.setText(String.format("%.2f TND", cents / 100.0));
            eventPrice.getStyleClass().add("event-price");
        }

        // Reservation status
        Label reservationStatus = new Label();
        int currentReservations = reservationService.getByEvent(event.getId()).size();
        int limit = event.getReservationLimit();
        
        if (limit > 0) {
            int remaining = limit - currentReservations;
            if (remaining <= 0) {
                reservationStatus.setText("Sold Out");
                reservationStatus.getStyleClass().add("status-sold-out");
            } else {
                reservationStatus.setText(remaining + " spots left");
                reservationStatus.getStyleClass().add("status-available");
            }
        } else {
            reservationStatus.setText(currentReservations + " attendees");
            reservationStatus.getStyleClass().add("status-unlimited");
        }

        eventMeta.getChildren().addAll(eventPrice, reservationStatus);

        // View button
        Button viewButton = new Button("View");
        viewButton.getStyleClass().add("view-event-button");
        viewButton.setOnAction(e -> handleViewEvent(event));

        eventCard.getChildren().addAll(eventImage, eventDetails, eventMeta, viewButton);
        eventsContainer.getChildren().add(eventCard);
    }

    private void loadRecentActivity() {
        try {
            User currentUser = UserSession.getInstance().getUser();
            if (currentUser == null) return;

            recentActivityContainer.getChildren().clear();

            // Get user's recent reservations
            List<Evenement> allEvents = evenementService.get();
            int activityCount = 0;

            for (Evenement event : allEvents) {
                if (activityCount >= 5) break; // Limit to 5 recent activities

                List<Reservation> eventReservations = reservationService.getByEvent(event.getId());
                for (Reservation reservation : eventReservations) {
                    if (reservation.getUtilisateur().getId() == currentUser.getId()) {
                        createActivityItem("You joined \"" + event.getTitre() + "\"", 
                                         reservation.getDate().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")));
                        activityCount++;
                        break;
                    }
                }
            }

            if (activityCount == 0) {
                Label noActivity = new Label("No recent activity");
                noActivity.getStyleClass().add("no-activity-label");
                recentActivityContainer.getChildren().add(noActivity);
            }

        } catch (Exception e) {
            System.err.println("Error loading recent activity: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Compute simple insights like the most popular event and the user's reservation count
     * for the current month.
     */
    private void loadDashboardInsights() {
        try {
            List<Evenement> events = evenementService.get();

            // Most popular event by reservation count
            if (lblTopEvent != null && !events.isEmpty()) {
                Evenement top = events.stream()
                        .max(java.util.Comparator.comparingInt(e -> reservationService.getByEvent(e.getId()).size()))
                        .orElse(null);
                if (top != null) {
                    lblTopEvent.setText(top.getTitre());
                }
            }

            // Reservations this month for current user
            if (lblReservationsMonth != null) {
                User currentUser = UserSession.getInstance().getUser();
                if (currentUser != null) {
                    java.time.Month currentMonth = java.time.LocalDate.now().getMonth();
                    long count = events.stream()
                            .flatMap(e -> reservationService.getByEvent(e.getId()).stream())
                            .filter(r -> r.getUtilisateur().getId() == currentUser.getId())
                            .filter(r -> r.getDate() != null && r.getDate().getMonth() == currentMonth)
                            .count();
                    lblReservationsMonth.setText(String.valueOf(count));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading dashboard insights: " + e.getMessage());
        }
    }

    private void createActivityItem(String activity, String time) {
        HBox activityItem = new HBox(15);
        activityItem.getStyleClass().add("activity-item");
        activityItem.setAlignment(Pos.CENTER_LEFT);
        activityItem.setPadding(new Insets(10));

        // Activity icon
        SVGPath activityIcon = new SVGPath();
        activityIcon.setContent("M9,12L11,14L15,10L13.5,8.5L11,11L9.5,9.5L9,12M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2M12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4Z");
        activityIcon.getStyleClass().add("activity-icon");

        // Activity details
        VBox activityDetails = new VBox(2);
        HBox.setHgrow(activityDetails, javafx.scene.layout.Priority.ALWAYS);

        Label activityText = new Label(activity);
        activityText.getStyleClass().add("activity-text");

        Label activityTime = new Label(time);
        activityTime.getStyleClass().add("activity-time");

        activityDetails.getChildren().addAll(activityText, activityTime);

        activityItem.getChildren().addAll(activityIcon, activityDetails);
        recentActivityContainer.getChildren().add(activityItem);
    }

    private void animateDashboardElements() {
        // Animate sidebar avatar
        if (sidebarAvatar != null) {
            AnimationUtils.fadeIn(sidebarAvatar, 800, 300);
        }

        // Animate dashboard cards
        if (lblAvailableEvents.getParent() != null) {
            AnimationUtils.fadeIn(lblAvailableEvents.getParent(), 600, 400);
        }
        if (lblMyReservations.getParent() != null) {
            AnimationUtils.fadeIn(lblMyReservations.getParent(), 600, 500);
        }
        if (lblUpcomingEvents.getParent() != null) {
            AnimationUtils.fadeIn(lblUpcomingEvents.getParent(), 600, 600);
        }

        // Animate events section
        if (eventsContainer != null) {
            AnimationUtils.fadeIn(eventsContainer, 600, 700);
        }
    }

    @FXML
    private void handleDashboard() {
        currentActiveTab = btnDashboard;
        styleActiveButton(btnDashboard);
        // Already on dashboard
    }

    @FXML
    private void handleEvents() {
        currentActiveTab = btnEvents;
        styleActiveButton(btnEvents);
        if (mainApp != null) {
            mainApp.loadContentFragment("/EventsView.fxml",
                    (EventController controller) -> controller.setCurrentUser(UserSession.getInstance().getUser()));
        }
    }

    @FXML
    private void handleProducts() {
        currentActiveTab = btnProducts;
        styleActiveButton(btnProducts);
        if (mainApp != null) {
            mainApp.loadContentFragment("/ProductCardView.fxml", controller -> {});
        }
    }

    @FXML
    private void handleBlog() {
        currentActiveTab = btnBlog;
        styleActiveButton(btnBlog);
        if (mainApp != null) {
            mainApp.loadContentFragment("/BlogView.fxml", controller -> {});
        }
    }

    @FXML
    private void handleProfile() {
        currentActiveTab = btnProfile;
        styleActiveButton(btnProfile);
        if (mainApp != null) {
            mainApp.showProfileScene();
        }
    }

    @FXML
    private void handleViewAllEvents() {
        if (mainApp != null) {
            mainApp.loadContentFragment("/EventsView.fxml",
                    (EventController controller) -> controller.setCurrentUser(UserSession.getInstance().getUser()));
        }
    }

    @FXML
    private void handleLogout() {
        if (mainApp != null) {
            NotificationManager.showConfirmation(
                    (Stage) btnLogout.getScene().getWindow(),
                    "Confirm Logout",
                    "Are you sure you want to logout?",
                    () -> {
                        UserSession.getInstance().setUser(null);
                        mainApp.showLoginScene();
                    },
                    null
            );
        }
    }

    @FXML
    private void handleNotificationBellClick() {
        NotificationManager.showInfo("Notifications", "No new notifications");
    }

    private void handleViewEvent(Evenement event) {
        if (mainApp != null) {
            mainApp.showEventPlannerDashboardScene();
        }
    }

    private void styleActiveButton(Button activeButton) {
        System.out.println("=== TAB SELECTION DEBUG ===");
        System.out.println("Setting active button: " + (activeButton == btnDashboard ? "Dashboard" : 
                                                       activeButton == btnEvents ? "Events" :
                                                       activeButton == btnProducts ? "Products" :
                                                       activeButton == btnBlog ? "Blog" :
                                                       activeButton == btnProfile ? "Profile" : "Unknown"));
        
        // Remove active style from all buttons
        btnDashboard.getStyleClass().remove("selected");
        btnEvents.getStyleClass().remove("selected");
        if (btnProducts != null) btnProducts.getStyleClass().remove("selected");
        if (btnBlog != null) btnBlog.getStyleClass().remove("selected");
        btnProfile.getStyleClass().remove("selected");

        // Add active style to the clicked button
        activeButton.getStyleClass().add("selected");
        
        // Debug: Check which buttons have the selected class
        System.out.println("Dashboard has selected: " + btnDashboard.getStyleClass().contains("selected"));
        System.out.println("Events has selected: " + btnEvents.getStyleClass().contains("selected"));
        System.out.println("Products has selected: " + (btnProducts != null ? btnProducts.getStyleClass().contains("selected") : "null"));
        System.out.println("Blog has selected: " + (btnBlog != null ? btnBlog.getStyleClass().contains("selected") : "null"));
        System.out.println("Profile has selected: " + btnProfile.getStyleClass().contains("selected"));
        System.out.println("=== END TAB SELECTION DEBUG ===");
    }
    
    /**
     * Check if multiple tabs are selected and fix it
     */
    private void fixMultipleTabSelection() {
        int selectedCount = 0;
        Button selectedButton = null;
        
        if (btnDashboard.getStyleClass().contains("selected")) {
            selectedCount++;
            selectedButton = btnDashboard;
        }
        if (btnEvents.getStyleClass().contains("selected")) {
            selectedCount++;
            selectedButton = btnEvents;
        }
        if (btnProducts != null && btnProducts.getStyleClass().contains("selected")) {
            selectedCount++;
            selectedButton = btnProducts;
        }
        if (btnBlog != null && btnBlog.getStyleClass().contains("selected")) {
            selectedCount++;
            selectedButton = btnBlog;
        }
        if (btnProfile.getStyleClass().contains("selected")) {
            selectedCount++;
            selectedButton = btnProfile;
        }
        
        System.out.println("Multiple tab selection check: " + selectedCount + " tabs selected");
        
        if (selectedCount > 1) {
            System.out.println("Multiple tabs selected! Fixing...");
            // Clear all selections and set only the current active tab
            styleActiveButton(currentActiveTab != null ? currentActiveTab : btnDashboard);
        }
    }
    
    /**
     * Restore the active tab state - useful when returning from fragments
     */
    public void restoreActiveTab() {
        System.out.println("=== RESTORE ACTIVE TAB DEBUG ===");
        System.out.println("Current active tab: " + (currentActiveTab == btnDashboard ? "Dashboard" : 
                                                    currentActiveTab == btnEvents ? "Events" :
                                                    currentActiveTab == btnProducts ? "Products" :
                                                    currentActiveTab == btnBlog ? "Blog" :
                                                    currentActiveTab == btnProfile ? "Profile" : "Unknown"));
        
        // First check if multiple tabs are selected and fix it
        fixMultipleTabSelection();
        
        if (currentActiveTab != null) {
            // Check if the current active tab already has the selected class
            boolean alreadySelected = currentActiveTab.getStyleClass().contains("selected");
            System.out.println("Current active tab already selected: " + alreadySelected);
            
            // Only restore if the current active tab is not already selected
            if (!alreadySelected) {
                System.out.println("Restoring tab selection...");
                styleActiveButton(currentActiveTab);
            } else {
                System.out.println("Tab already selected, skipping restoration");
            }
        } else {
            System.out.println("Warning: currentActiveTab is null!");
        }
        System.out.println("=== END RESTORE ACTIVE TAB DEBUG ===");
    }
    
    /**
     * Get the currently active tab
     */
    public Button getCurrentActiveTab() {
        return currentActiveTab;
    }
}
