package com.esprit.utils;

import com.esprit.models.User;

/**
 * Singleton class to hold the current logged-in user session.
 */
public class UserSession {

    private static UserSession instance;
    private User user;

    private UserSession() {
        // private constructor to prevent instantiation
    }

    /**
     * Get the singleton instance of UserSession.
     * @return UserSession instance
     */
    public static UserSession getInstance() {
        if (instance == null) {
            synchronized (UserSession.class) {
                if (instance == null) {
                    instance = new UserSession();
                }
            }
        }
        return instance;
    }

    /**
     * Initialize the session with the logged-in user.
     * @param user the logged-in User object
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Get the current logged-in user.
     * @return User object or null if no user logged in
     */
    public User getUser() {
        return user;
    }
    
    /**
     * Get the current logged-in user (alias for getUser).
     * @return User object or null if no user logged in
     */
    public User getCurrentUser() {
        return user;
    }

    /**
     * Clear the session data on logout.
     */
    public void clearSession() {
        user = null;
        instance = null;
    }
}
