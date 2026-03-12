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

    private static final String PRIMARY_BLUE = "#2563eb";
    private static final String TEXT_DARK = "#1f2937";
    private static final String TEXT_BODY = "#111827";
    private static final String TEXT_MUTED = "#6b7280";
    private static final String TEXT_ERROR = "#dc2626";
    private static final String TEXT_SUCCESS = "#16a34a";
    private static final String BORDER_LIGHT = "#d1d5db";
    private static final String CARD_BORDER = "#e5e7eb";
    private static final String FIELD_BACKGROUND = "#f9fafb";
    private static final String PROMPT_TEXT = "#9ca3af";
    private static final String WHITE = "white";

    private static final String FONT_WEIGHT_BOLD = "-fx-font-weight: bold;";
    private static final String BORDER_RADIUS_10 = "-fx-border-radius: 10;";
    private static final String BACKGROUND_RADIUS_10 = "-fx-background-radius: 10;";

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
        configureRoot();
        configureLabelsAndFields();
        configureButtons();
        configureMessageLabel();
        configureProgressIndicator();
        configureCard();
    }

    private void configureRoot() {
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8fafc, #eef2f7);");
    }

    private Label createTitleLabel() {
        Label titleLabel = new Label("Create Your Account");
        titleLabel.setStyle(buildTextStyle(28, true, TEXT_DARK));
        return titleLabel;
    }

    private Label createSubtitleLabel() {
        Label subtitleLabel = new Label("Register to access the Smart Home Maintenance Platform");
        subtitleLabel.setStyle(buildTextStyle(14, false, TEXT_MUTED));
        return subtitleLabel;
    }

    private void configureLabelsAndFields() {
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
    }

    private void configureButtons() {
        registerButton.setPrefWidth(340);
        registerButton.setPrefHeight(46);
        registerButton.setStyle(primaryButtonStyle());

        backToLoginButton.setPrefWidth(340);
        backToLoginButton.setPrefHeight(42);
        backToLoginButton.setStyle(secondaryButtonStyle());
    }

    private void configureMessageLabel() {
        messageLabel.setStyle(messageStyle(TEXT_ERROR));
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    private void configureProgressIndicator() {
        progressIndicator.setVisible(false);
        progressIndicator.setManaged(false);
        progressIndicator.setPrefSize(24, 24);
    }

    private void configureCard() {
        Label titleLabel = createTitleLabel();
        Label subtitleLabel = createSubtitleLabel();

        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36));
        card.setMaxWidth(460);
        card.setStyle(
                "-fx-background-color: " + WHITE + ";" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: " + CARD_BORDER + ";" +
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
        return "-fx-background-color: " + FIELD_BACKGROUND + ";" +
               BACKGROUND_RADIUS_10 +
               "-fx-border-color: " + BORDER_LIGHT + ";" +
               BORDER_RADIUS_10 +
               "-fx-border-width: 1;" +
               "-fx-font-size: 14px;" +
               "-fx-text-fill: " + TEXT_BODY + ";" +
               "-fx-prompt-text-fill: " + PROMPT_TEXT + ";";
    }

    private String primaryButtonStyle() {
        return "-fx-background-color: " + PRIMARY_BLUE + ";" +
               BACKGROUND_RADIUS_10 +
               "-fx-text-fill: " + WHITE + ";" +
               "-fx-font-size: 14px;" +
               FONT_WEIGHT_BOLD;
    }

    private String secondaryButtonStyle() {
        return "-fx-background-color: #ffffff;" +
               "-fx-border-color: " + BORDER_LIGHT + ";" +
               BORDER_RADIUS_10 +
               BACKGROUND_RADIUS_10 +
               "-fx-text-fill: " + TEXT_DARK + ";" +
               "-fx-font-size: 13px;" +
               FONT_WEIGHT_BOLD;
    }

    private String messageStyle(String color) {
        return "-fx-font-size: 12px;" +
               "-fx-text-fill: " + color + ";";
    }

    private String buildTextStyle(int fontSize, boolean bold, String color) {
        return "-fx-font-size: " + fontSize + "px;" +
               (bold ? FONT_WEIGHT_BOLD : "") +
               "-fx-text-fill: " + color + ";";
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
        messageLabel.setStyle(messageStyle(isError ? TEXT_ERROR : TEXT_SUCCESS));

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
        registerButton.setOnAction(event -> action.run());
    }

    public void setOnBackToLogin(Runnable action) {
        backToLoginButton.setOnAction(event -> action.run());
    }
}