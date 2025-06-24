package com.esprit.utils;

import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

/**
 * Utility class for sidebar styling and behavior across controllers
 */
public class SidebarUtils {
    
    /**
     * Highlights the active button with a green background and resets other buttons
     * This method provides consistent sidebar styling across all controllers
     * 
     * @param activeButton The button to highlight as active
     * @param sidebarButtons Array of all sidebar buttons
     */
    public static void styleActiveButton(Button activeButton, Button[] sidebarButtons) {
        // Reset all buttons to default style
        for (Button btn : sidebarButtons) {
            if (btn != null) {
                // Remove active style
                btn.getStyleClass().remove("sidebar-button-active");
                // Apply default style if not present
                if (!btn.getStyleClass().contains("sidebar-button")) {
                    btn.getStyleClass().add("sidebar-button");
                }
            }
        }
        
        // Apply active style to the current button
        if (activeButton != null) {
            activeButton.getStyleClass().add("sidebar-button-active");
            
            // Add subtle glow effect to active button
            DropShadow glow = new DropShadow();
            glow.setColor(Color.valueOf("#2ecc71"));
            glow.setRadius(10);
            glow.setSpread(0.1);
            activeButton.setEffect(glow);
        }
    }
    
    /**
     * Applies hover effects to all sidebar buttons
     * 
     * @param sidebarButtons Array of all sidebar buttons
     */
    public static void applyHoverEffects(Button[] sidebarButtons) {
        for (Button btn : sidebarButtons) {
            if (btn != null) {
                AnimationUtils.applyHoverEffect(btn);
            }
        }
    }
}
