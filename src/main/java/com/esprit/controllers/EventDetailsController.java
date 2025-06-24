package com.esprit.controllers;

import com.esprit.Services.EvenementService;
import com.esprit.Services.FeedbackService;
import com.esprit.Services.ReservationService;
import com.esprit.Services.TicketService;
import com.esprit.models.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.StageStyle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventDetailsController {

    @FXML private ImageView posterImage;
    @FXML private Label titleLabel, dateLabel, adresseLabel, descriptionLabel,priceLabel ,remainingLabel;
    @FXML private Button editButton, deleteButton, backButton, actionButton;
    @FXML private TextArea feedbackCommentField;
    @FXML private Spinner<Integer> noteSpinner;
    @FXML private Button submitFeedbackButton;
    @FXML private VBox feedbackListContainer;
    @FXML private HBox starsContainer;


    private BorderPane mainContainer;
    private Evenement evenement;
    private User currentUser;

    private int currentRating = 5;





    private final EvenementService    evenementService    = new EvenementService();
    private final ReservationService  reservationService  = new ReservationService();
    private final FeedbackService     feedbackService     = new FeedbackService();
    private EventController           eventController;

    public void setEventController(EventController controller) {
        this.eventController = controller;
    }

    public void setEvent(Evenement evt,
                         BorderPane mainContainer,
                         Node unused1,
                         Node unused2,
                         User currentUser) {
        this.evenement     = evt;
        this.mainContainer = mainContainer;
        this.currentUser   = currentUser;

        // --- Basic info ---
        titleLabel.setText(evt.getTitre());
        dateLabel.setText(evt.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        adresseLabel.setText( evt.getAdresse());
        descriptionLabel.setText(evt.getDescription());

        // --- Remaining places label ---
        refreshRemaining();

        // --- Poster image loading ---
        String url = evt.getPosterImageUrl();
        if (url != null && !url.isEmpty()) {
            try {
                posterImage.setImage(new Image(url, true));
            } catch (Exception ex) {
                posterImage.setImage(new Image(
                        getClass().getResource("/default_event.png").toExternalForm(), true
                ));
            }
        } else {
            posterImage.setImage(new Image(
                    getClass().getResource("/default_event.png").toExternalForm(), true
            ));
        }

        long cents = evt.getPriceCents();
        if (cents <= 0) {
            priceLabel.setText("Gratuit");
        } else {
            priceLabel.setText(String.format("%.2f TND", cents / 100.0));
        }

        // --- Reserve/Cancel button setup ---
        int count       = reservationService.getByEvent(evt.getId()).size();
        int limit       = evt.getReservationLimit();
        boolean soldOut = limit > 0 && count >= limit;
        boolean hasRes  = reservationService.exists(currentUser.getId(), evt.getId());
        configureActionButton(soldOut, hasRes);

        // --- Role-based visibility for event CRUD ---
        boolean isOrg = currentUser != null && UserRole.EVENT_PLANNER.equals(currentUser.getRole());
        editButton.setVisible(isOrg);
        editButton.setManaged(isOrg);
        deleteButton.setVisible(isOrg);
        deleteButton.setManaged(isOrg);

        // --- Handlers ---
        editButton.setOnAction(e -> eventController.openAjoutEvent(evenement, true));
        deleteButton.setOnAction(e -> handleDelete());
        backButton.setOnAction(e -> eventController.goToMainView());

        setupStarRating();
        submitFeedbackButton.setOnAction(e -> {
            handleSubmitFeedback();
            loadFeedbacks();
        });

        loadFeedbacks();
    }

    private void refreshRemaining() {
        // 1) Fetch current count and limit
        int count     = reservationService.getByEvent(evenement.getId()).size();
        int limit     = evenement.getReservationLimit();
        int remaining = limit > 0 ? Math.max(limit - count, 0) : -1;
        boolean soldOut = limit > 0 && remaining == 0;

        // 2) Update the remainingLabel text & style
        if (limit > 0) {
            // If there's a limit, show either “Complet” or “[N] places restantes”
            remainingLabel.setText(soldOut
                    ? "Complet"
                    : String.format("%d places restantes", remaining)
            );
            remainingLabel.setStyle(soldOut
                    ? "-fx-text-fill: #e74c3c; -fx-font-weight:bold;"
                    : "-fx-text-fill: #444;"
            );
        } else {
            // No limit → show total reservations
            remainingLabel.setText(String.format("%d réservations", count));
            remainingLabel.setStyle("-fx-text-fill: #27ae60;");
        }
    }

    private void handleCancelReservation() {
        // 1) Remove from database
        reservationService.remove(currentUser.getId(), evenement.getId());

        // 2) Send cancellation email
        TicketService ticketService = new TicketService();
        boolean emailSent = ticketService.sendCancellationEmail(currentUser, evenement);
        showAlert(emailSent
                        ? "Annulé"
                        : "Annulé (échec email)",
                emailSent
                        ? "Votre réservation a été annulée et un email de confirmation vous a été envoyé."
                        : "Votre réservation a été annulée, mais l'email de confirmation a échoué."
        );

        // 3) Refresh UI
        refreshRemaining();
        int limit = evenement.getReservationLimit();
        boolean soldOut = (limit > 0 && reservationService.getByEvent(evenement.getId()).size() >= limit);
        boolean hasReserved = reservationService.exists(currentUser.getId(), evenement.getId());
        configureActionButton(soldOut, hasReserved);
        eventController.reloadEvents();
    }

    private void configureActionButton(boolean soldOut, boolean hasReserved) {
        System.out.println("[DEBUG] configureActionButton(): currentUser=" + currentUser +
                ", role=" + (currentUser != null ? currentUser.getRole() : "null") +
                ", soldOut=" + soldOut +
                ", hasReserved=" + hasReserved);

        // 1) only a CLIENT may see it
        if (currentUser == null || !UserRole.CLIENT.equals(currentUser.getRole())) {
            actionButton.setVisible(false);
            actionButton.setManaged(false);   // ← hide from layout too
            return;
        }

        // show it
        actionButton.setVisible(true);
        actionButton.setManaged(true);        // ← re-enable in layout

        // 2) sold out & not reserved → disabled
        if (soldOut && !hasReserved) {
            actionButton.setText("Complet");
            actionButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #424242;");
            actionButton.setDisable(true);
            return;
        }

        // 3) already reserved → “Cancel”
        if (hasReserved) {
            actionButton.setText("Annuler");
            actionButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #424242;");
            actionButton.getStyleClass().setAll("cancel-button");
            actionButton.setDisable(false);
            actionButton.setOnAction(e -> handleCancelReservation());
        }
        // 4) otherwise → “Reserve”
        else {
            actionButton.setText("Réserver");
            actionButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: #ffffff;");
            actionButton.setDisable(false);
            actionButton.setOnAction(e -> {
                long price = evenement.getPriceCents();
                if (price > 0) openCheckout(price);
                else handleReservation();
            });
        }
    }

    private void openCheckout(long priceCents) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Checkout.fxml"));
            Parent checkoutView = loader.load();

            CheckoutController checkoutCtrl = loader.getController();
            // initialise le checkout avec l’événement courant, l’utilisateur et ce controller
            checkoutCtrl.init(evenement, currentUser, eventController);

            // Affiche la vue
            mainContainer.setCenter(checkoutView);
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la page de paiement.");
        }
    }

    private void handleReservation() {
        // 1) Only participants can reserve
       if (currentUser == null || !UserRole.CLIENT.equals(currentUser.getRole())) {
            showAlert("Erreur", "Vous devez être connecté en tant que participant pour réserver !");
            return;
        }
        // 2) Prevent double booking
        if (reservationService.exists( currentUser.getId(), evenement.getId())) {
            showAlert("Info", "Vous avez déjà réservé cet événement.");
            return;
        }

        // 3) Enforce limit
        int count = reservationService.getByEvent(evenement.getId()).size();
        int limit = evenement.getReservationLimit();
        if (limit > 0 && count >= limit) {
            showAlert("Complet", "Désolé, cet événement a atteint sa limite de réservations.");
            configureActionButton(true, false);  // sold out, user hasn’t reserved
            return;
        }

        // 4) Create reservation
        Reservation r = new Reservation(0,  currentUser, evenement, LocalDateTime.now(), evenement.getPriceCents());
        reservationService.add(r);

        // 5) Generate & send ticket
        try {
            actionButton.setText("Génération du ticket...");
            actionButton.setDisable(true);

            TicketService ticketService = new TicketService();
            String path = ticketService.generateTicket(r);
            boolean emailSent = ticketService.sendTicketByEmail(path,  currentUser, evenement);

            showAlert(emailSent
                            ? "Réservé !"
                            : "Réservé, mais échec d’envoi de l’email.",
                    emailSent
                            ? "Votre ticket a été envoyé à " +  currentUser.getEmail()
                            : "Veuillez vérifier votre email."
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Réservation créée, mais erreur lors de la génération du ticket.");
        } finally {
            // 6) Refresh UI
            refreshRemaining();
            boolean soldOut = (limit > 0 && reservationService.getByEvent(evenement.getId()).size() >= limit);
            configureActionButton(soldOut, true);
            eventController.reloadEvents();
        }
    }

    private void handleDelete() {
        var alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cet événement ?", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Suppression de l’événement");
        alert.showAndWait().ifPresent(resp-> {
            if (resp==ButtonType.OK) {
                evenementService.delete(evenement.getId());
                eventController.reloadEvents();
                handleBack();
            }
        });
    }

    private void handleBack() {
        // **Retour sans recharger**
        eventController.goToMainView();
    }

    private void handleSubmitFeedback() {
       if (currentUser==null
                || !UserRole.CLIENT.equals(currentUser.getRole())) {
            showAlert("Erreur",
                    "Seuls les participants connectés peuvent envoyer un feedback.");
            return;
        }
        var txt = feedbackCommentField.getText();
       // var note= noteSpinner.getValue();
        if (txt==null || txt.trim().isEmpty()) {
            showAlert("Erreur","Le commentaire ne peut pas être vide.");
            return;
        }
        Feedback fb = new Feedback();
        fb.setCommentaire(txt);
        fb.setNote(currentRating);
        fb.setDate(LocalDateTime.now());
        fb.setUtilisateur(currentUser);
        fb.setEvenement(evenement);
        feedbackService.add(fb);
        showAlert("Merci !","Votre feedback a été enregistré.");
        feedbackCommentField.clear();
        setupStarRating();
        loadFeedbacks();
    }


    private void loadFeedbacks() {
        feedbackListContainer.getChildren().clear();
        List<Feedback> list = feedbackService.getByEvent(evenement.getId());

        if (list.isEmpty()) {
            Label none = new Label("No reviews yet");
            none.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            feedbackListContainer.getChildren().add(none);
            return;
        }

        // Remove the isOrganizer permission to modify feedback
        // Only the feedback owner should be able to edit/delete their own feedback

        for (Feedback fb : list) {
            VBox card = new VBox(8);
            card.getStyleClass().add("feedback-card");

            // Author row with icon
            HBox authorRow = new HBox(10);
            authorRow.getStyleClass().add("feedback-author-row");

            // User icon
            SVGPath userIcon = new SVGPath();
            userIcon.setContent("M12,4A4,4 0 0,1 16,8A4,4 0 0,1 12,12A4,4 0 0,1 8,8A4,4 0 0,1 12,4M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z");
            userIcon.getStyleClass().add("user-icon");

            // Author name
            Label author = new Label(fb.getUtilisateur() != null
                    ? fb.getUtilisateur().getNom() + " " + fb.getUtilisateur().getPrenom()
                    : "Anonymous");
            author.getStyleClass().add("feedback-author");

            authorRow.getChildren().addAll(userIcon, author);

            // Date (now above rating)
            Label date = new Label(fb.getDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
            date.getStyleClass().add("feedback-date");

            // Stars
            HBox stars = new HBox(2);
            stars.getStyleClass().add("stars-row");

            for (int i = 1; i <= 5; i++) {
                Label star = new Label(i <= fb.getNote() ? "★" : "☆");
                star.getStyleClass().add(i <= fb.getNote() ? "star-filled" : "star-empty");
                stars.getChildren().add(star);
            }

            // Comment
            Label comment = new Label(fb.getCommentaire());
            comment.getStyleClass().add("feedback-comment");
            comment.setWrapText(true);

            // Actions row (edit/delete)
            HBox actionsRow = new HBox(10);
            actionsRow.getStyleClass().add("feedback-actions");

            // Only the owner of the feedback can edit or delete it
            boolean isOwner = currentUser != null && fb.getUtilisateur() != null
                    && fb.getUtilisateur().getId() == currentUser.getId();

            if (isOwner) {
                // Edit button as icon
                Button editFb = new Button();
                editFb.getStyleClass().add("icon-button");

                SVGPath editIcon = new SVGPath();
                editIcon.setContent("M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z");
                editIcon.getStyleClass().add("edit-icon");

                editFb.setGraphic(editIcon);
                editFb.setOnAction(e -> openFeedbackEditor(fb));
                actionsRow.getChildren().add(editFb);

                // Delete button as icon
                Button delFb = new Button();
                delFb.getStyleClass().add("icon-button");

                SVGPath deleteIcon = new SVGPath();
                deleteIcon.setContent("M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z");
                deleteIcon.getStyleClass().add("delete-icon");

                delFb.setGraphic(deleteIcon);
                delFb.setOnAction(e -> {
                    feedbackService.delete(fb.getId());
                    loadFeedbacks();
                });

                actionsRow.getChildren().add(delFb);
            }

            // Add all components to the card
            card.getChildren().addAll(authorRow, date, stars, comment);

            if (!actionsRow.getChildren().isEmpty()) {
                card.getChildren().add(actionsRow);
            }

            feedbackListContainer.getChildren().add(card);
        }
    }

    private void openFeedbackEditor(Feedback fb) {
        Dialog<Feedback> dialog = new Dialog<>();
        dialog.setTitle("Update Your Review");
        dialog.initStyle(StageStyle.UTILITY);

        // Create header
        Label headerLabel = new Label("Edit Your Review");
        headerLabel.getStyleClass().add("dialog-header-title");

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(15, 20, 15, 20));
        headerBox.getStyleClass().add("dialog-header");

        SVGPath editIcon = new SVGPath();
        editIcon.setContent("M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z");
        editIcon.getStyleClass().add("dialog-header-icon");

        headerBox.getChildren().addAll(editIcon, headerLabel);

        // Create comment area
        TextArea editedComment = new TextArea(fb.getCommentaire());
        editedComment.setWrapText(true);
        editedComment.getStyleClass().add("feedback-input");
        editedComment.setPrefRowCount(4);

        Label commentLabel = new Label("Your comments");
        commentLabel.getStyleClass().add("dialog-field-label");

        // Create star rating (initial value based on feedback)
        int initialRating = fb.getNote();

        Label ratingLabel = new Label("Your rating");
        ratingLabel.getStyleClass().add("dialog-field-label");

        HBox starsBox = new HBox(5);
        starsBox.setAlignment(Pos.CENTER_LEFT);
        starsBox.getStyleClass().add("stars-edit-row");

        // Current selected rating
        final int[] selectedRating = {initialRating};

        // Create 5 star buttons
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            Button star = new Button("★");
            star.getStyleClass().add("star-edit-button");

            // Set initial state
            if (i <= initialRating) {
                star.getStyleClass().add("selected");
            }

            // Add click event
            star.setOnAction(e -> {
                selectedRating[0] = rating;

                // Update all stars
                for (int j = 0; j < starsBox.getChildren().size(); j++) {
                    Button starBtn = (Button) starsBox.getChildren().get(j);
                    if (j < rating) {
                        if (!starBtn.getStyleClass().contains("selected")) {
                            starBtn.getStyleClass().add("selected");
                        }
                    } else {
                        starBtn.getStyleClass().remove("selected");
                    }
                }
            });

            starsBox.getChildren().add(star);
        }

        // Create dialog content layout
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
                commentLabel,
                editedComment,
                ratingLabel,
                starsBox
        );

        // Final dialog structure
        VBox dialogContent = new VBox();
        dialogContent.getChildren().addAll(headerBox, content);

        // Set up dialog
        dialog.getDialogPane().setContent(dialogContent);
        dialog.getDialogPane().getStylesheets().add("/styles/EventDetails.css");
        dialog.getDialogPane().getStyleClass().add("feedback-edit-dialog");

        ButtonType updateButtonType = new ButtonType("Update Review", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        // Style buttons
        Button updateButton = (Button) dialog.getDialogPane().lookupButton(updateButtonType);
        updateButton.getStyleClass().add("update-review-button");

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.getStyleClass().add("cancel-review-button");

        // Set the result converter
        dialog.setResultConverter(btn -> {
            if (btn == updateButtonType) {
                fb.setCommentaire(editedComment.getText().trim());
                fb.setNote(selectedRating[0]);
                fb.setDate(LocalDateTime.now());
                return fb;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            feedbackService.update(updated);
            loadFeedbacks();
        });
    }

    private void showAlert(String t, String c) {
        var a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(c);
        a.showAndWait();
    }

    private void setupStarRating() {
        starsContainer.getChildren().clear();

        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            Button star = new Button("★");
            star.getStyleClass().add("star-button");

            // Set initial state
            if (i <= currentRating) {
                star.getStyleClass().add("selected");
            }

            // Add click event
            star.setOnAction(e -> {
                currentRating = rating;
                updateStarRating();
            });

            starsContainer.getChildren().add(star);
        }
    }

    private void updateStarRating() {
        for (int i = 0; i < starsContainer.getChildren().size(); i++) {
            Button star = (Button) starsContainer.getChildren().get(i);

            if (i < currentRating) {
                if (!star.getStyleClass().contains("selected")) {
                    star.getStyleClass().add("selected");
                }
            } else {
                star.getStyleClass().remove("selected");
            }
        }
    }
}