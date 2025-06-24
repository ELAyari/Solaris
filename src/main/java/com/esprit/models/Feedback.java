package com.esprit.models;

import java.time.LocalDateTime;

public class Feedback {
    private int id;
    private String commentaire;
    private int note; // Par exemple : de 1 à 5
    private LocalDateTime date;
    private User utilisateur;
    private Evenement evenement;

    public Feedback() {}

    public Feedback(int id, String commentaire, int note, LocalDateTime date, User utilisateur, Evenement evenement) {
        this.id = id;
        this.commentaire = commentaire;
        this.note = note;
        this.date = date;
        this.utilisateur = utilisateur;
        this.evenement = evenement;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public User getUtilisateur() { return utilisateur; }
    public void setUtilisateur(User utilisateur) { this.utilisateur = utilisateur; }

    public Evenement getEvenement() { return evenement; }
    public void setEvenement(Evenement evenement) { this.evenement = evenement; }
}
