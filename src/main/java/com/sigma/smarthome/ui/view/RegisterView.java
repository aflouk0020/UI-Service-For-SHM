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

    private static final String FX_BACKGROUND_COLOR = "-fx-background-color: ";
    private static final String FX_TEXT_FILL = "-fx-text-fill: ";
    private static final String FX_BORDER_COLOR = "-fx-border-color: ";
    private static final String FX_FONT_SIZE = "-fx-font-size: ";
    private static final String FX_BORDER_WIDTH_1 = "-fx-border-width: 1;";
    private static final String FX_BACKGROUND_RADIUS_10 = "-fx-background-radius: 10;";
    private static final String FX_BACKGROUND_RADIUS_18 = "-fx-background-radius: 18;";
    private static final String FX_BORDER_RADIUS_10 = "-fx-border-radius: 10;";
    private static final String FX_BORDER_RADIUS_18 = "-fx-border-radius: 18;";
    private static final String FX_FONT_WEIGHT_BOLD = "-fx-font-weight: bold;";
    private static final String PX = "px;";
    private static final String SEMICOLON = ";";

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
        configureFields();
        configureButtons();
        configureMessageLabel();
        configureProgressIndicator();
        configureCard();
    }

    private void configureRoot() {
        root.setStyle(FX_BACKGROUND_COLOR + "linear-gradient(to bottom right, #f8fafc, #eef2f7);");
    }

    private void configureFields() {
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
        Label titleLabel = new Label("Create Your Account");
        titleLabel.setStyle(textStyle(28, true, TEXT_DARK));

        Label subtitleLabel = new Label("Register to access the Smart Home Maintenance Platform");
        subtitleLabel.setStyle(textStyle(14, false, TEXT_MUTED));

        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36));
        card.setMaxWidth(460);
        card.setStyle(cardStyle());

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
        return baseFieldStyle(FIELD_BACKGROUND, 14, TEXT_BODY, PROMPT_TEXT);
    }

    private String baseFieldStyle(String backgroundColor, int fontSize, String textColor, String promptColor) {
        return FX_BACKGROUND_COLOR + backgroundColor + SEMICOLON +
               FX_BACKGROUND_RADIUS_10 +
               FX_BORDER_COLOR + BORDER_LIGHT + SEMICOLON +
               FX_BORDER_RADIUS_10 +
               FX_BORDER_WIDTH_1 +
               FX_FONT_SIZE + fontSize + PX +
               FX_TEXT_FILL + textColor + SEMICOLON +
               "-fx-prompt-text-fill: " + promptColor + SEMICOLON;
    }

    private String primaryButtonStyle() {
        return FX_BACKGROUND_COLOR + PRIMARY_BLUE + SEMICOLON +
               FX_BACKGROUND_RADIUS_10 +
               FX_TEXT_FILL + WHITE + SEMICOLON +
               FX_FONT_SIZE + "14px;" +
               FX_FONT_WEIGHT_BOLD;
    }

    private String secondaryButtonStyle() {
        return FX_BACKGROUND_COLOR + WHITE + SEMICOLON +
               FX_BORDER_COLOR + BORDER_LIGHT + SEMICOLON +
               FX_BORDER_RADIUS_10 +
               FX_BACKGROUND_RADIUS_10 +
               FX_TEXT_FILL + TEXT_DARK + SEMICOLON +
               FX_FONT_SIZE + "13px;" +
               FX_FONT_WEIGHT_BOLD;
    }

    private String messageStyle(String color) {
        return FX_FONT_SIZE + "12px;" +
               FX_TEXT_FILL + color + SEMICOLON;
    }

    private String textStyle(int fontSize, boolean bold, String color) {
        return FX_FONT_SIZE + fontSize + PX +
               (bold ? FX_FONT_WEIGHT_BOLD : "") +
               FX_TEXT_FILL + color + SEMICOLON;
    }

    private String cardStyle() {
        return FX_BACKGROUND_COLOR + WHITE + SEMICOLON +
               FX_BACKGROUND_RADIUS_18 +
               FX_BORDER_COLOR + CARD_BORDER + SEMICOLON +
               FX_BORDER_RADIUS_18 +
               FX_BORDER_WIDTH_1 +
               "-fx-effect: dropshadow(gaussian, rgba(15, 23, 42, 0.08), 18, 0.12, 0, 6);";
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