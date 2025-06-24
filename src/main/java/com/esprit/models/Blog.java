package com.esprit.models;

import java.util.ArrayList;
import java.util.List;

public class Blog {
    private int id;
    private String titre;
    private String resume;
    private String contenu;
    private String status;
    private String image;
    private int likes;
    private List<String> comments;
    private int views;
    private java.time.LocalDateTime datePublication;
    private int auteurId;

    public Blog() {
        comments = new ArrayList<>();
        likes = 0;
        views = 0;
        datePublication = null;
        auteurId = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public List<String> getComments() {
        return comments;
    }

    public void addComment(String comment) {
        comments.add(comment);
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public java.time.LocalDateTime getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(java.time.LocalDateTime datePublication) {
        this.datePublication = datePublication;
    }

    public int getAuteurId() {
        return auteurId;
    }

    public void setAuteurId(int auteurId) {
        this.auteurId = auteurId;
    }
}
