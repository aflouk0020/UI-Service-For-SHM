package com.sigma.smarthome.ui.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class RegisterView {

    private final BorderPane root = new BorderPane();

    private final TextField emailField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final ComboBox<String> roleComboBox = new ComboBox<>();

    private final Button registerButton = new Button("Create Account");
    private final Button backToLoginButton = new Button("Back to Login");

    private final Label messageLabel = new Label();
    private final ProgressIndicator progressIndicator = new ProgressIndicator();

    public RegisterView() {
        initialise();
    }

    private void initialise() {
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8fafc, #eef2f7);");

        Label titleLabel = new Label("Create Your Account");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        Label subtitleLabel = new Label("Register to access the Smart Home Maintenance Platform");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");

        emailField.setPromptText("Email");
        emailField.setPrefWidth(340);
        emailField.setPrefHeight(44);
        emailField.setStyle(commonFieldStyle());

        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(340);
        passwordField.setPrefHeight(44);
        passwordField.setStyle(commonFieldStyle());

        roleComboBox.getItems().addAll("PROPERTY_MANAGER", "MAINTENANCE_STAFF");
        roleComboBox.setPromptText("Select role");
        roleComboBox.setPrefWidth(340);
        roleComboBox.setPrefHeight(44);
        roleComboBox.setStyle(commonFieldStyle());

        registerButton.setPrefWidth(340);
        registerButton.setPrefHeight(46);
        registerButton.setStyle(
                "-fx-background-color: #2563eb;" +
                "-fx-background-radius: 10;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;"
        );

        backToLoginButton.setPrefWidth(340);
        backToLoginButton.setPrefHeight(42);
        backToLoginButton.setStyle(
                "-fx-background-color: #ffffff;" +
                "-fx-border-color: #d1d5db;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-text-fill: #1f2937;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;"
        );

        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626;");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);

        progressIndicator.setVisible(false);
        progressIndicator.setManaged(false);
        progressIndicator.setPrefSize(24, 24);

        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36));
        card.setMaxWidth(460);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: #e5e7eb;" +
                "-fx-border-radius: 18;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(15, 23, 42, 0.08), 18, 0.12, 0, 6);"
        );

        card.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                emailField,
                passwordField,
                roleComboBox,
                registerButton,
                backToLoginButton,
                progressIndicator,
                messageLabel
        );

        root.setCenter(card);
        BorderPane.setAlignment(card, Pos.CENTER);
        BorderPane.setMargin(card, new Insets(40));
    }

    private String commonFieldStyle() {
        return "-fx-background-color: #f9fafb;" +
               "-fx-background-radius: 10;" +
               "-fx-border-color: #d1d5db;" +
               "-fx-border-radius: 10;" +
               "-fx-border-width: 1;" +
               "-fx-font-size: 14px;" +
               "-fx-text-fill: #111827;" +
               "-fx-prompt-text-fill: #9ca3af;";
    }

    public Parent getView() {
        return root;
    }

    public String getEmail() {
        return emailField.getText() == null ? "" : emailField.getText().trim();
    }

    public String getPassword() {
        return passwordField.getText() == null ? "" : passwordField.getText();
    }

    public String getRole() {
        return roleComboBox.getValue();
    }

    public void setMessage(String message, boolean isError) {
        messageLabel.setText(message == null ? "" : message);
        messageLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: " + (isError ? "#dc2626;" : "#16a34a;")
        );
        boolean hasMessage = message != null && !message.isBlank();
        messageLabel.setVisible(hasMessage);
        messageLabel.setManaged(hasMessage);
    }

    public void setLoading(boolean loading) {
        emailField.setDisable(loading);
        passwordField.setDisable(loading);
        roleComboBox.setDisable(loading);
        registerButton.setDisable(loading);
        backToLoginButton.setDisable(loading);

        progressIndicator.setVisible(loading);
        progressIndicator.setManaged(loading);

        registerButton.setText(loading ? "Creating..." : "Create Account");
    }

    public void setOnRegister(Runnable action) {
        registerButton.setOnAction(e -> action.run());
    }

    public void setOnBackToLogin(Runnable action) {
        backToLoginButton.setOnAction(e -> action.run());
    }
}