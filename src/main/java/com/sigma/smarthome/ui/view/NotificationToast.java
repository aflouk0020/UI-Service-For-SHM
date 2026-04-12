package com.sigma.smarthome.ui.view;

import com.sigma.smarthome.ui.model.NotificationItem;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class NotificationToast extends VBox {

    public NotificationToast(NotificationItem item) {
        String type = safe(item.getType(), "GENERAL");
        String accentColor = accentColor(type);
        String badgeBackground = badgeBackground(type);
        String badgeText = badgeText(type);

        setSpacing(10);
        setPadding(new Insets(16));
        setMaxWidth(360);
        setPrefWidth(360);
        setAlignment(Pos.TOP_LEFT);

        setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 16;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.18), 20, 0.15, 0, 8);" +
                "-fx-border-insets: 0;" +
                "-fx-background-insets: 0;"
        );

        Region accentBar = new Region();
        accentBar.setPrefWidth(5);
        accentBar.setMinWidth(5);
        accentBar.setMaxWidth(5);
        accentBar.setStyle(
                "-fx-background-color: " + accentColor + ";" +
                "-fx-background-radius: 10;"
        );

        Label badge = new Label(formatTypeLabel(type));
        badge.setStyle(
                "-fx-background-color: " + badgeBackground + ";" +
                "-fx-text-fill: " + badgeText + ";" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 5 10 5 10;" +
                "-fx-background-radius: 12;"
        );

        Label title = new Label(safe(item.getTitle(), "Notification"));
        title.setWrapText(true);
        title.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );

        Label message = new Label(safe(item.getMessage(), "You have received a new notification."));
        message.setWrapText(true);
        message.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #475569;"
        );

        Label footer = new Label("Just now");
        footer.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #94a3b8;"
        );

        VBox textBox = new VBox(8, badge, title, message, footer);
        textBox.setAlignment(Pos.TOP_LEFT);

        HBox container = new HBox(12, accentBar, textBox);
        container.setAlignment(Pos.TOP_LEFT);

        getChildren().add(container);
    }

    public void playShowAnimation() {
        setOpacity(0);
        setTranslateX(46);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(260), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(260), this);
        slideIn.setFromX(46);
        slideIn.setToX(0);

        fadeIn.play();
        slideIn.play();
    }

    public void playHideAnimation(Runnable onFinished) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(220), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(220), this);
        slideOut.setFromX(0);
        slideOut.setToX(34);

        fadeOut.setOnFinished(event -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });

        fadeOut.play();
        slideOut.play();
    }

    public void autoDismiss(Duration delay, Runnable onFinished) {
        PauseTransition pause = new PauseTransition(delay);
        pause.setOnFinished(event -> playHideAnimation(onFinished));
        pause.play();
    }

    private String formatTypeLabel(String type) {
        return switch (type.toUpperCase()) {
            case "ASSIGNMENT" -> "Assignment";
            case "STATUS_UPDATE" -> "Status Update";
            case "SUCCESS" -> "Success";
            case "WARNING" -> "Warning";
            case "ERROR" -> "Error";
            default -> "Notification";
        };
    }

    private String accentColor(String type) {
        return switch (type.toUpperCase()) {
            case "ASSIGNMENT" -> "#2563eb";
            case "STATUS_UPDATE" -> "#f59e0b";
            case "SUCCESS" -> "#16a34a";
            case "WARNING" -> "#f59e0b";
            case "ERROR" -> "#dc2626";
            default -> "#2563eb";
        };
    }

    private String badgeBackground(String type) {
        return switch (type.toUpperCase()) {
            case "ASSIGNMENT" -> "#dbeafe";
            case "STATUS_UPDATE" -> "#fef3c7";
            case "SUCCESS" -> "#dcfce7";
            case "WARNING" -> "#fef3c7";
            case "ERROR" -> "#fee2e2";
            default -> "#e2e8f0";
        };
    }

    private String badgeText(String type) {
        return switch (type.toUpperCase()) {
            case "ASSIGNMENT" -> "#1d4ed8";
            case "STATUS_UPDATE" -> "#92400e";
            case "SUCCESS" -> "#166534";
            case "WARNING" -> "#92400e";
            case "ERROR" -> "#991b1b";
            default -> "#334155";
        };
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}