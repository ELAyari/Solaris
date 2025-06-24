package com.esprit.models;

public class Produit {
    private int id;
    private String nom;
    private String description;
    private double prix;
    private int quantiteStock;
    private String image;
    private boolean recyclable;
    private int categorieId;
    private String companyName;
    private int ownerId;
    private String companyLogo;

    public Produit(int id, String nom, String description, double prix, int quantiteStock, String image, boolean recyclable, int categorieId) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.quantiteStock = quantiteStock;
        this.image = image;
        this.recyclable = recyclable;
        this.categorieId = categorieId;
        this.companyName = "";
        this.ownerId = 0;
        this.companyLogo = "";
    }
    
    public Produit(int id, String nom, String description, double prix, int quantiteStock, String image, boolean recyclable, int categorieId, String companyName, int ownerId) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.quantiteStock = quantiteStock;
        this.image = image;
        this.recyclable = recyclable;
        this.categorieId = categorieId;
        this.companyName = companyName;
        this.ownerId = ownerId;
        this.companyLogo = "";
    }
    
    public Produit(int id, String nom, String description, double prix, int quantiteStock, String image, boolean recyclable, int categorieId, String companyName, int ownerId, String companyLogo) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.quantiteStock = quantiteStock;
        this.image = image;
        this.recyclable = recyclable;
        this.categorieId = categorieId;
        this.companyName = companyName;
        this.ownerId = ownerId;
        this.companyLogo = companyLogo;
    }
    public Produit() {
       
    }


    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getQuantiteStock() {
        return quantiteStock;
    }

    public void setQuantiteStock(int quantiteStock) {
        this.quantiteStock = quantiteStock;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isRecyclable() {
        return recyclable;
    }

    public void setRecyclable(boolean recyclable) {
        this.recyclable = recyclable;
    }

    public int getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(int categorieId) {
        this.categorieId = categorieId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
    
    public String getCompanyLogo() {
        return companyLogo;
    }

    public void setCompanyLogo(String companyLogo) {
        this.companyLogo = companyLogo;
    }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", quantiteStock=" + quantiteStock +
                ", image='" + image + '\'' +
                ", recyclable=" + recyclable +
                ", categorieId=" + categorieId +
                ", companyName='" + companyName + '\'' +
                ", ownerId=" + ownerId +
                ", companyLogo='" + companyLogo + '\'' +
                '}';
    }
}