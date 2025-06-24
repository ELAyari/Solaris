package com.esprit.config;

/**
 * Central access to configuration secrets supplied through environment variables.
 * No credential values are stored in source control.
 */
public final class Secrets {
    private Secrets() {
    }

    public static String get(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            System.err.println("WARNING: environment variable " + name + " is not set.");
            return "";
        }
        return value;
    }
}