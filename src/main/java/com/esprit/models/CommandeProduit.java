package com.esprit.models;

public class CommandeProduit {
    private int id;
    private int commandeId;
    private int produitId;
    private int quantite;
    private double sousTotal;
    private Produit produit; // Add this field

    public CommandeProduit() {
    }

    public CommandeProduit(int id, int commandeId, int produitId, int quantite, double sousTotal) {
        this.id = id;
        this.commandeId = commandeId;
        this.produitId = produitId;
        this.quantite = quantite;
        this.sousTotal = sousTotal;
    }

    // Getters and setters for all fields including produit

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(int commandeId) {
        this.commandeId = commandeId;
    }

    public int getProduitId() {
        return produitId;
    }

    public void setProduitId(int produitId) {
        this.produitId = produitId;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getSousTotal() {
        return sousTotal;
    }

    public void setSousTotal(double sousTotal) {
        this.sousTotal = sousTotal;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }
}
