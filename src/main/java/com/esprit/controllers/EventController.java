package com.esprit.controllers;

import com.esprit.Services.EvenementService;
import com.esprit.Services.ReservationService;
import com.esprit.models.Evenement;
import com.esprit.models.User;
import com.esprit.models.UserRole;
import com.esprit.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventController {

    @FXML private BorderPane mainContainer;
    @FXML private FlowPane eventsFlowPane;
    @FXML private HBox dayFilterBar;
    @FXML private VBox dayFilterContainer;
    @FXML private Button ajoutEventButton;
    @FXML private TextField searchField;
    @FXML private Button clearSearchButton;
    @FXML private HBox searchContainer;



    private List<Evenement> allEvents;
    private User currentUser;
    private final EvenementService service = new EvenementService();

    @FXML
    public void initialize() {
        // Logo
      //  logoImage.setImage(new Image(getClass().getResource("/logoPi.jpeg.png").toExternalForm()));
        User sessionUser = UserSession.getInstance().getUser();

        setCurrentUser(sessionUser);
        // Charger et trier
        allEvents = service.get().stream()
                .sorted((e1,e2)->e1.getDate().compareTo(e2.getDate()))
                .toList();

        setupDayFilterBar();
        afficherEvenements(allEvents);


        // Ajout / Burger
        ajoutEventButton.setOnAction(e -> openAjoutEventForm());


        // Click hors sidebar


        // Recherche
        searchField.textProperty().addListener((obs,oldV,newV)->{
            var filt = allEvents.stream()
                    .filter(evt->evt.getTitre().toLowerCase().contains(newV.toLowerCase()))
                    .toList();
            afficherEvenements(filt);
        });
        clearSearchButton.setOnAction(e -> {
            searchField.clear();
            afficherEvenements(allEvents);
        });

    }

    private void setupDayFilterBar() {
        Button allButton = new Button("All Events");
        allButton.getStyleClass().add("day-button");
        allButton.setOnAction(e -> toggleDayFilter("All", allButton));
        dayFilterBar.getChildren().add(allButton);

        // Day buttons - no icons
        String[] jours = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String j : jours) {
            Button b = new Button(j);
            b.getStyleClass().add("day-button");
            b.setOnAction(e -> toggleDayFilter(j, b));
            dayFilterBar.getChildren().add(b);
        }
    }

    private void toggleDayFilter(String jour, Button btn) {
        dayFilterBar.getChildren().forEach(n -> n.getStyleClass().remove("active"));

        if ("All".equals(jour) || "All Events".equals(jour)) {
            btn.getStyleClass().add("active");
            afficherEvenements(allEvents);
        } else {
            btn.getStyleClass().add("active");
            int idx = switch(jour) {
                case "Monday"    -> 1;
                case "Tuesday"   -> 2;
                case "Wednesday" -> 3;
                case "Thursday"  -> 4;
                case "Friday"    -> 5;
                case "Saturday"  -> 6;
                case "Sunday"    -> 7;
                default          -> -1;
            };
            var filt = allEvents.stream()
                    .filter(evt -> evt.getDate().getDayOfWeek().getValue() == idx)
                    .toList();
            afficherEvenements(filt);
        }
    }

    private void afficherEvenements(List<Evenement> list) {
        eventsFlowPane.getChildren().clear();
        for (Evenement evt: list) {
            eventsFlowPane.getChildren().add(createEventCard(evt));
        }
    }

    private VBox createEventCard(Evenement evt) {
        VBox card = new VBox();
        card.getStyleClass().add("card");

        // --- image pane (260×160) with clipping ---
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(260, 160);
        imageContainer.setMaxSize(260, 160);
        imageContainer.setMinSize(260, 160);

        // clip so anything outside is hidden
        Rectangle clip = new Rectangle(260, 160);
        clip.setArcWidth(10);  // match the card radius at top corners
        clip.setArcHeight(10);
        imageContainer.setClip(clip);

        // --- image view that WILL stretch to fill its container ---
        ImageView iv = new ImageView();
        iv.setPreserveRatio(false);
        iv.setSmooth(true);
        iv.fitWidthProperty().bind(imageContainer.widthProperty());
        iv.fitHeightProperty().bind(imageContainer.heightProperty());

        // load the actual image (or default)
        String imageUrl = evt.getPosterImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            iv.setImage(new Image(imageUrl, true));
        } else {
            iv.setImage(new Image(
                    getClass().getResource("/default_event.png").toExternalForm(),
                    true
            ));
        }

        imageContainer.getChildren().add(iv);

        // Content container for all text elements
        VBox content = new VBox(8);
        content.getStyleClass().add("content");

        // --- title & date ---
        Label title = new Label(evt.getTitre());
        title.getStyleClass().add("title");

        // Date with calendar icon
        HBox dateBox = new HBox(5);
        dateBox.setAlignment(Pos.CENTER_LEFT);

        SVGPath calendarIcon = new SVGPath();
        calendarIcon.setContent("M19,19H5V8H19M16,1V3H8V1H6V3H5C3.89,3 3,3.89 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V5C21,3.89 20.1,3 19,3H18V1");
        calendarIcon.getStyleClass().add("card-icon");

        Label date = new Label(evt.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        date.getStyleClass().add("date");

        dateBox.getChildren().addAll(calendarIcon, date);

        // Price with tag icon
        HBox priceBox = new HBox(5);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        SVGPath priceIcon = new SVGPath();
        priceIcon.setContent("M21.41,11.58L12.41,2.58C12.05,2.22 11.55,2 11,2H4C2.89,2 2,2.89 2,4V11C2,11.55 2.22,12.05 2.59,12.41L11.58,21.41C11.95,21.78 12.45,22 13,22C13.55,22 14.05,21.78 14.41,21.41L21.41,14.41C21.78,14.05 22,13.55 22,13C22,12.45 21.77,11.94 21.41,11.58M5.5,7C4.67,7 4,6.33 4,5.5C4,4.67 4.67,4 5.5,4C6.33,4 7,4.67 7,5.5C7,6.33 6.33,7 5.5,7Z");
        priceIcon.getStyleClass().add("card-icon");

        Label price = new Label();
        long cents = evt.getPriceCents();
        if (cents <= 0) {
            price.setText("Free");
        } else {
            price.setText(String.format("%.2f TND", cents / 100.0));
        }
        price.getStyleClass().add("price");

        priceBox.getChildren().addAll(priceIcon, price);

        // --- reservations badge with remaining places ---
        HBox badgeBox = new HBox(5);
        badgeBox.setAlignment(Pos.CENTER_LEFT);

        SVGPath personIcon = new SVGPath();
        personIcon.setContent("M16,13C15.71,13 15.38,13 15.03,13.05C16.19,13.89 17,15 17,16.5V19H23V16.5C23,14.17 18.33,13 16,13M8,13C5.67,13 1,14.17 1,16.5V19H15V16.5C15,14.17 10.33,13 8,13M8,11A3,3 0 0,0 11,8A3,3 0 0,0 8,5A3,3 0 0,0 5,8A3,3 0 0,0 8,11M16,11A3,3 0 0,0 19,8A3,3 0 0,0 16,5A3,3 0 0,0 13,8A3,3 0 0,0 16,11Z");
        personIcon.getStyleClass().add("card-icon");

        int count = new ReservationService().getByEvent(evt.getId()).size();
        int limit = evt.getReservationLimit();

        Label badge = new Label();
        if (limit > 0) {
            int remaining = limit - count;
            if (remaining <= 0) {
                badge.setText("Sold out");
                badge.getStyleClass().addAll("badge", "badge-full");
            } else if ((double) count / limit >= 0.7) {
                badge.setText(String.format("%d spots left", remaining));
                badge.getStyleClass().addAll("badge", "badge-almost-full");
            } else {
                badge.setText(String.format("%d/%d reserved", count, limit));
                badge.getStyleClass().addAll("badge", "badge-available");
            }
        } else {
            badge.setText(String.format("%d attendees", count));
            badge.getStyleClass().add("badge");
        }

        badgeBox.getChildren().addAll(personIcon, badge);

        // --- assemble card ---
        content.getChildren().addAll(title, dateBox, priceBox, badgeBox);
        card.getChildren().addAll(imageContainer, content);
        card.setOnMouseClicked(e -> openEventDetails(evt));

        return card;
    }


    private void openEventDetails(Evenement evt) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
            Parent view = loader.load();

            EventDetailsController ctrl = loader.getController();

            ctrl.setEvent(
                    evt,
                    mainContainer,
                    dayFilterContainer,
                    eventsFlowPane,
                    UserSession.getInstance().getUser()
            );
            ctrl.setEventController(this);   // keep the reference you had

            // Hide both day filter container AND search container
            dayFilterContainer.setVisible(false);
            searchContainer.setVisible(false);

            mainContainer.setCenter(view);   // show the details pane
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openAjoutEventForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutEvent.fxml"));
            Parent view = loader.load();
            AjoutEvent ctrl = loader.getController();
            ctrl.setMode("add");
            ctrl.setEventController(this);
            ctrl.setMainContainer(mainContainer);
           // searchContainer.setVisible(false);
            dayFilterContainer.setVisible(false);
            mainContainer.setCenter(view);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void reloadEvents() {
        allEvents = service.get().stream()
                .sorted((e1,e2)->e1.getDate().compareTo(e2.getDate()))
                .toList();
        afficherEvenements(allEvents);
    }

    /**
     * Affiche la vue principale sans recharger le FXML
     */
    public void goToMainView() {
        // Make search container visible
        searchContainer.setVisible(true);
        searchContainer.setManaged(true);

        // Make day filter container visible
        dayFilterContainer.setVisible(true);
        dayFilterContainer.setManaged(true);

        try {
            // Recreate the scroll pane
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.getStyleClass().add("main-scroll-pane");
            scrollPane.setContent(dayFilterContainer);

            // Set the scroll pane as the center content
            mainContainer.setCenter(scrollPane);

            // Restore the floating action button in the bottom region
            AnchorPane buttonWrapper = new AnchorPane();
            buttonWrapper.getStyleClass().add("floating-button-wrapper");

            // Set the proper anchors for the button
            AnchorPane.setRightAnchor(ajoutEventButton, 30.0);
            AnchorPane.setBottomAnchor(ajoutEventButton, 30.0);

            // Add the button to the wrapper and set it as the bottom content
            buttonWrapper.getChildren().add(ajoutEventButton);
            mainContainer.setBottom(buttonWrapper);

            // Show the button if the user has the appropriate role
            ajoutEventButton.setVisible(true);

            // Clear search field and reload events
            searchField.clear();
            reloadEvents();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;

        // Use the parameter passed to the method, not the session
        boolean isOrg = user != null
                && (UserRole.EVENT_PLANNER.equals(user.getRole())
                || UserRole.ADMIN.equals(user.getRole()));

        // Add debugging to see what's happening
        System.out.println("User role: " + (user != null ? user.getRole() : "null"));
        System.out.println("Is event planner: " + isOrg);

        ajoutEventButton.setVisible(isOrg);
        ajoutEventButton.setManaged(isOrg);
    }

    public void setMainContainer(BorderPane main) {
        this.mainContainer = main;
    }

    public void openAjoutEvent(Evenement evenement, boolean isEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutEvent.fxml"));
            Parent view = loader.load();

            AjoutEvent ctrl = loader.getController();
            ctrl.setMode("edit");
            ctrl.setEvenementToEdit(evenement);
            ctrl.setEventController(this);
            ctrl.setMainContainer(mainContainer);
            ctrl.prefillFieldsIfEdit();

          //  searchContainer.setVisible(false);
            dayFilterContainer.setVisible(false);
            mainContainer.setCenter(view);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}
