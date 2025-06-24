package com.esprit.models;

import java.time.LocalDateTime;

/**
 * Represents a ticket generated after event reservation
 */
public class Ticket {
    private String id;                // Unique ticket identifier (UUID)
    private Reservation reservation;  // Associated reservation
    private User user;                // User who made the reservation
    private Evenement evenement;      // Event the ticket is for
    private String pdfPath;           // Path to the generated PDF file
    private String qrCodeData;        // Data encoded in the QR code
    private LocalDateTime generatedAt; // When the ticket was generated
    private boolean used;             // Whether ticket has been used

    // Default constructor
    public Ticket() {
    }

    // Constructor with required fields
    public Ticket(String id, Reservation reservation, String pdfPath, String qrCodeData) {
        this.id = id;
        this.reservation = reservation;
        this.user = reservation.getUtilisateur();
        this.evenement = reservation.getEvenement();
        this.pdfPath = pdfPath;
        this.qrCodeData = qrCodeData;
        this.generatedAt = LocalDateTime.now();
        this.used = false;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
        if (reservation != null) {
            this.user = reservation.getUtilisateur();
            this.evenement = reservation.getEvenement();
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Evenement getEvenement() {
        return evenement;
    }

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    /**
     * Mark this ticket as used
     */
    public void markAsUsed() {
        this.used = true;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id='" + id + '\'' +
                ", event='" + (evenement != null ? evenement.getTitre() : "null") + '\'' +
                ", user='" + (user != null ? user.getNom() + " " + user.getPrenom() : "null") + '\'' +
                ", generated=" + generatedAt +
                ", used=" + used +
                '}';
    }
}