package com.esprit.controllers;

import com.esprit.Services.ReservationService;
import com.esprit.Services.StripeService;
import com.esprit.Services.TicketService;
import com.esprit.models.Evenement;
import com.esprit.models.Reservation;
import com.esprit.models.User;
import com.stripe.model.checkout.Session;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.time.LocalDateTime;

public class CheckoutController {

    @FXML private WebView webView;

    private Evenement evenement;
    private User user;
    private EventController parent;

    public void init(Evenement evt, User u, EventController parent) throws Exception {
        this.evenement = evt;
        this.user = u;
        this.parent = parent;

        // build URLs
        String base = "https://book.stripe.com/test_bJe28q8tUeFh7vz2Fs00000";
        String success = base + "/success";
        String cancel  = base + "/cancel";

        // create Stripe session
        String url = new StripeService()
                .createCheckoutSession(evt.getPriceCents(), success, cancel);

        WebEngine eng = webView.getEngine();
        eng.locationProperty().addListener((obs, oldLoc, newLoc) -> {
            if (newLoc.startsWith(success)) {
                // extract session_id
                String sessionId = newLoc.split("session_id=")[1];
                finalizeReservation(sessionId);
            }
        });
        eng.load(url);
    }

    private void finalizeReservation(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            if ("paid".equals(session.getPaymentStatus())) {
                // 1) record reservation
                Reservation r = new Reservation(0, user, evenement, LocalDateTime.now(), evenement.getPriceCents());
                new ReservationService().add(r);

                // 2) send ticket
                String path = new TicketService().generateTicket(r);
                new TicketService().sendTicketByEmail(path, user, evenement);

                // 3) back to details
                parent.goToMainView();
                showAlert("Merci !", "Paiement validé, ticket envoyé !");
            } else {
                showAlert("Erreur", "Paiement non confirmé : " + session.getPaymentStatus());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Impossible de confirmer le paiement.");
        }
    }


    private void showAlert(String title, String msg) {
        javafx.application.Platform.runLater(() -> {
            var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            a.setTitle(title);
            a.setHeaderText(null);
            a.setContentText(msg);
            a.showAndWait();
        });
    }
}
