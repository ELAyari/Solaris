package com.esprit.utils;

import javafx.animation.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Utility class for applying animations and visual effects to JavaFX nodes
 */
public class AnimationUtils {

    /**
     * Fade in a node with a smooth animation
     * @param node The node to animate
     * @param duration Duration in milliseconds
     * @param delay Delay before animation starts in milliseconds
     */
    public static void fadeIn(Node node, int duration, int delay) {
        node.setOpacity(0);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
        fadeTransition.setDelay(Duration.millis(delay));
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }

    /**
     * Fade out a node with a smooth animation
     * @param node The node to animate
     * @param duration Duration in milliseconds
     */
    public static void fadeOut(Node node, int duration) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);
        fadeTransition.play();
    }
    
    /**
     * Fade out a node with a smooth animation and delay
     * @param node The node to animate
     * @param duration Duration in milliseconds
     * @param delay Delay before animation starts in milliseconds
     */
    public static void fadeOut(Node node, int duration, int delay) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
        fadeTransition.setDelay(Duration.millis(delay));
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);
        fadeTransition.play();
    }

    /**
     * Slide a node in from the left
     * @param node The node to animate
     * @param duration Duration in milliseconds
     */
    public static void slideInFromLeft(Node node, int duration) {
        double startX = -20;
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(duration), node);
        translateTransition.setFromX(startX);
        translateTransition.setToX(0);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);

        ParallelTransition parallelTransition = new ParallelTransition(translateTransition, fadeTransition);
        parallelTransition.play();
    }

    /**
     * Slide a node in from the right
     * @param node The node to animate
     * @param duration Duration in milliseconds
     */
    public static void slideInFromRight(Node node, int duration) {
        double startX = 20;
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(duration), node);
        translateTransition.setFromX(startX);
        translateTransition.setToX(0);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);

        ParallelTransition parallelTransition = new ParallelTransition(translateTransition, fadeTransition);
        parallelTransition.play();
    }

    /**
     * Apply a pulse animation to a node (useful for notifications)
     * @param node The node to animate
     */
    public static void pulse(Node node) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), node);
        scaleTransition.setFromX(1);
        scaleTransition.setFromY(1);
        scaleTransition.setToX(1.1);
        scaleTransition.setToY(1.1);
        scaleTransition.setCycleCount(2);
        scaleTransition.setAutoReverse(true);
        scaleTransition.play();
    }
    
    /**
     * Apply a pulse animation to a node with custom scale and duration
     * @param node The node to animate
     * @param scale Maximum scale to pulse to
     * @param durationMillis Duration of animation in milliseconds
     */
    public static void pulse(Node node, double scale, int durationMillis) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(durationMillis), node);
        scaleTransition.setFromX(1);
        scaleTransition.setFromY(1);
        scaleTransition.setToX(scale);
        scaleTransition.setToY(scale);
        scaleTransition.setCycleCount(2);
        scaleTransition.setAutoReverse(true);
        scaleTransition.play();
    }

    /**
     * Apply a standardized shake animation for error feedback
     * @param node The node to animate
     */
    public static void shake(Node node) {
        TranslateTransition shakeLeft = new TranslateTransition(Duration.millis(50), node);
        shakeLeft.setFromX(0);
        shakeLeft.setToX(-10);

        TranslateTransition shakeRight = new TranslateTransition(Duration.millis(50), node);
        shakeRight.setFromX(-10);
        shakeRight.setToX(10);

        TranslateTransition shakeReset = new TranslateTransition(Duration.millis(50), node);
        shakeReset.setFromX(10);
        shakeReset.setToX(0);

        shakeLeft.play();
        shakeLeft.setOnFinished(e -> shakeRight.play());
        shakeRight.setOnFinished(e -> shakeReset.play());
    }

    /**
     * Apply a button press animation
     * @param button The button to animate
     */
    public static void buttonPress(Node button) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
        scaleDown.setFromX(1);
        scaleDown.setFromY(1);
        scaleDown.setToX(0.95);
        scaleDown.setToY(0.95);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
        scaleUp.setFromX(0.95);
        scaleUp.setFromY(0.95);
        scaleUp.setToX(1);
        scaleUp.setToY(1);

        scaleDown.play();
        scaleDown.setOnFinished(e -> scaleUp.play());
    }

    /**
     * Apply hover effect to a button or control
     * @param control The control to apply hover effect to
     */
    public static void applyHoverEffect(Control control) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(10);
        shadow.setSpread(0.2);

        control.setOnMouseEntered(e -> {
            control.setEffect(shadow);
            ScaleTransition st = new ScaleTransition(Duration.millis(150), control);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
        });

        control.setOnMouseExited(e -> {
            control.setEffect(null);
            ScaleTransition st = new ScaleTransition(Duration.millis(150), control);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    /**
     * Apply a bell shake animation (for notification icons)
     * @param node The node to animate
     */
    public static void bellShake(Node node) {
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(80), node);
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(15);
        rotateTransition.setCycleCount(6);
        rotateTransition.setAutoReverse(true);
        rotateTransition.play();
    }

    /**
     * Apply a card hover effect with shadow elevation
     * @param card The card node to enhance
     */
    public static void applyCardHoverEffect(Region card) {
        DropShadow initialShadow = new DropShadow();
        initialShadow.setColor(Color.rgb(0, 0, 0, 0.1));
        initialShadow.setRadius(5);
        initialShadow.setOffsetX(0);
        initialShadow.setOffsetY(2);
        card.setEffect(initialShadow);

        DropShadow hoverShadow = new DropShadow();
        hoverShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        hoverShadow.setRadius(10);
        hoverShadow.setOffsetX(0);
        hoverShadow.setOffsetY(5);

        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle() + "; -fx-background-color: derive(white, 2%);");

            Timeline shadowTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(initialShadow.radiusProperty(), initialShadow.getRadius()),
                            new KeyValue(initialShadow.offsetYProperty(), initialShadow.getOffsetY()),
                            new KeyValue(initialShadow.colorProperty(), initialShadow.getColor())
                    ),
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(initialShadow.radiusProperty(), hoverShadow.getRadius()),
                            new KeyValue(initialShadow.offsetYProperty(), hoverShadow.getOffsetY()),
                            new KeyValue(initialShadow.colorProperty(), hoverShadow.getColor())
                    )
            );
            shadowTimeline.play();
        });

        card.setOnMouseExited(e -> {
            card.setStyle(card.getStyle().replace("; -fx-background-color: derive(white, 2%);", ""));

            Timeline shadowTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(initialShadow.radiusProperty(), initialShadow.getRadius()),
                            new KeyValue(initialShadow.offsetYProperty(), initialShadow.getOffsetY()),
                            new KeyValue(initialShadow.colorProperty(), initialShadow.getColor())
                    ),
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(initialShadow.radiusProperty(), 5),
                            new KeyValue(initialShadow.offsetYProperty(), 2),
                            new KeyValue(initialShadow.colorProperty(), Color.rgb(0, 0, 0, 0.1))
                    )
            );
            shadowTimeline.play();
        });
    }

    /**
     * Apply a loading shimmer effect to a component
     * @param region The region to apply shimmer effect to
     * @param durationMillis Duration of the shimmer animation cycle
     */
    public static void applyShimmerEffect(Region region, int durationMillis) {
        region.getStyleClass().add("shimmer");

        // You can add additional animation if needed
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> region.setOpacity(0.7)),
                new KeyFrame(Duration.millis(durationMillis/2), e -> region.setOpacity(0.85)),
                new KeyFrame(Duration.millis(durationMillis), e -> region.setOpacity(0.7))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Remove shimmer effect from a component
     * @param region The region to remove shimmer effect from
     */
    public static void removeShimmerEffect(Region region) {
        region.getStyleClass().remove("shimmer");
        region.setOpacity(1.0);
    }

    /**
     * Add a ripple effect to a control (like buttons)
     * @param control The control to add ripple effect to
     */
    public static void addRippleEffect(Control control) {
        // Add ripple style
        control.getStyleClass().add("ripple-effect");
    }

    /**
     * Apply a counter animation effect to numeric labels
     * @param label The label to animate
     * @param startValue Starting value
     * @param endValue Ending value
     * @param durationMillis Duration in milliseconds
     */
    public static void animateCounter(Label label, int startValue, int endValue, int durationMillis) {
        IntegerProperty integerProperty = new SimpleIntegerProperty(startValue);

        integerProperty.addListener((observable, oldValue, newValue) ->
                label.setText(String.valueOf(newValue)));

        Timeline timeline = new Timeline();

        KeyValue keyValue = new KeyValue(integerProperty, endValue);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(durationMillis), keyValue);
        timeline.getKeyFrames().add(keyFrame);

        timeline.play();
    }

    /**
     * Apply staggered fade in for a set of children in a pane
     * @param pane The pane with children to animate
     * @param delayBetweenItems Delay between each child animation in milliseconds
     */
    public static void applyStaggeredFadeIn(Pane pane, int delayBetweenItems) {
        int index = 0;
        for (Node child : pane.getChildren()) {
            fadeIn(child, 300, index * delayBetweenItems);
            index++;
        }
    }

    public static void scaleUp(Node card, double v, int i) {
    }

    public static void scaleDown(Node card, double v, int i) {
    }

    public static void pulse(Label lblNotificationCount, double v, int i) {
    }
}