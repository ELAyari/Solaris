package com.esprit.models;

import java.time.LocalDateTime;

public class Wishlist {
    private int id;
    private User user;
    private Produit produit;
    private LocalDateTime dateAdded;

    public Wishlist() {}

    public Wishlist(int id, User user, Produit produit, LocalDateTime dateAdded) {
        this.id = id;
        this.user = user;
        this.produit = produit;
        this.dateAdded = dateAdded;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
    }
}
