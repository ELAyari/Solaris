package com.esprit.models;

import java.util.Date;

/**
 * Represents a user activity record for the admin dashboard
 */
public class UserActivity {
    private String username;
    private String action;
    private Date timestamp;
    
    /**
     * Creates a new user activity record
     * @param username The username of the user who performed the activity
     * @param action The action that was performed
     * @param timestamp The time when the action was performed
     */
    public UserActivity(String username, String action, Date timestamp) {
        this.username = username;
        this.action = action;
        this.timestamp = timestamp;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return username + " " + action + " at " + timestamp;
    }
}
