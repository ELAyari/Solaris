package com.esprit.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("⚠️  Unable to find config.properties on the classpath");
            } else {
                properties.load(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getSendGridApiKey() {
        return getProperty("sendgrid.api.key");
    }

    public static String getEmailSender() {
        return getProperty("email.sender");
    }

    public static String getEmailSenderName() {
        return getProperty("email.sender.name");
    }

    public static String getStripeSecretKey() {
        return getProperty("stripe.secret");
    }

    public static String getStripePublishableKey() {
        return getProperty("stripe.publishable");
    }
}