package com.esprit.utils;

import com.esprit.models.User;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Enhanced ActivityLogger for application-wide logging
 * Supports multiple log levels and context-based logging
 */
public class ActivityLogger {
    private static final Logger LOGGER = Logger.getLogger(ActivityLogger.class.getName());
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static boolean initialized = false;
    
    static {
        try {
            initializeLogger();
        } catch (Exception e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }
    
    /**
     * Initialize the logger with file handler
     */
    private static void initializeLogger() throws IOException {
        if (initialized) return;
        
        // Create logs directory if it doesn't exist
        File logsDir = new File("logs");
        if (!logsDir.exists()) {
            logsDir.mkdir();
        }
        
        // Set up file handler
        FileHandler fileHandler = new FileHandler("logs/app.log", true);
        fileHandler.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(fileHandler);
        
        // Create custom formatter
        Formatter customFormatter = new Formatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("[%s] [%s] %s: %s%n",
                        LocalDateTime.now().format(DATE_FORMAT),
                        record.getLevel(),
                        record.getLoggerName(),
                        record.getMessage());
            }
        };
        
        fileHandler.setFormatter(customFormatter);
        initialized = true;
    }
    
    /**
     * Log an info message
     * @param context The context or component generating the log
     * @param message The message to log
     */
    public static void logInfo(String context, String message) {
        String logMessage = formatLogMessage(context, message);
        LOGGER.log(Level.INFO, logMessage);
    }
    
    /**
     * Log a warning message
     * @param context The context or component generating the log
     * @param message The message to log
     */
    public static void logWarning(String context, String message) {
        String logMessage = formatLogMessage(context, message);
        LOGGER.log(Level.WARNING, logMessage);
    }
    
    /**
     * Log an error message
     * @param context The context or component generating the log
     * @param message The message to log
     */
    public static void logError(String context, String message) {
        String logMessage = formatLogMessage(context, message);
        LOGGER.log(Level.SEVERE, logMessage);
    }
    
    /**
     * Log an error message with exception
     * @param context The context or component generating the log
     * @param message The message to log
     * @param exception The exception to log
     */
    public static void logError(String context, String message, Exception exception) {
        String logMessage = formatLogMessage(context, message + " - Exception: " + exception.getMessage());
        LOGGER.log(Level.SEVERE, logMessage, exception);
    }
    
    /**
     * Log a user activity
     * @param user The user performing the action
     * @param activity Description of the activity
     */
    public static void logUserActivity(User user, String activity) {
        String userName = user != null ? user.getNom() + " " + user.getPrenom() : "Unknown";
        String logMessage = formatLogMessage("UserActivity", 
                "User " + userName + " (ID: " + (user != null ? user.getId() : "N/A") + "): " + activity);
        LOGGER.log(Level.INFO, logMessage);
    }
    
    /**
     * Format log message with context
     */
    private static String formatLogMessage(String context, String message) {
        return String.format("[%s] %s", context, message);
    }
}
