package com.esprit.models;

import java.time.LocalDateTime;

/**
 * Represents a reply to a comment in the blog system.
 * This enables nested comments functionality.
 */
public class CommentReply {
    private int id;
    private String contenu;
    private LocalDateTime dateReply;
    private int auteurId;
    private int commentId; // Parent comment ID
    private String auteurName; // Cached author name for display

    // Constructors
    public CommentReply() {}
    
    public CommentReply(int id, String contenu, LocalDateTime dateReply, int auteurId, int commentId) {
        this.id = id;
        this.contenu = contenu;
        this.dateReply = dateReply;
        this.auteurId = auteurId;
        this.commentId = commentId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    
    public LocalDateTime getDateReply() { return dateReply; }
    public void setDateReply(LocalDateTime dateReply) { this.dateReply = dateReply; }
    
    public int getAuteurId() { return auteurId; }
    public void setAuteurId(int auteurId) { this.auteurId = auteurId; }
    
    public int getCommentId() { return commentId; }
    public void setCommentId(int commentId) { this.commentId = commentId; }
    
    public String getAuteurName() { return auteurName; }
    public void setAuteurName(String auteurName) { this.auteurName = auteurName; }
}
