package com.esprit.models;

import java.time.LocalDateTime;

public class Reservation {
    private int id;
    private User utilisateur;
    private Evenement evenement;
    private LocalDateTime date;
    private long priceCents;
    private Ticket ticket;

    // Existing constructors
    public Reservation() {}

    public Reservation(int id, User utilisateur, Evenement evenement, LocalDateTime date ,long priceCents) {
        this.id = id;
        this.utilisateur = utilisateur;
        this.evenement = evenement;
        this.date = date;
        this.priceCents = priceCents;
    }

    // Existing getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public User getUtilisateur() { return utilisateur; }
    public void setUtilisateur(User utilisateur) { this.utilisateur = utilisateur; }

    public Evenement getEvenement() { return evenement; }
    public void setEvenement(Evenement evenement) { this.evenement = evenement; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    // New ticket-related methods
    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }

    public long getPriceCents() { return priceCents; }
    public void setPriceCents(long priceCents) { this.priceCents = priceCents; }

    public boolean hasTicket() {
        return ticket != null;
    }
}