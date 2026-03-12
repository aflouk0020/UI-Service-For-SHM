package com.sigma.smarthome.ui.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Login screen for the Smart Home Maintenance Platform.
 * This class is responsible only for the UI layout and basic view behaviour.
 * Business logic and API calls should be wired from a controller/service layer.
 */
public class LoginView {

    private static final String COLOR_WHITE = "white";
    private static final String COLOR_PRIMARY = "#2563eb";
    private static final String COLOR_PRIMARY_HOVER = "#1d4ed8";
    private static final String COLOR_BORDER = "#d1d5db";
    private static final String COLOR_CARD_BORDER = "#e5e7eb";
    private static final String COLOR_TITLE = "#1f2937";
    private static final String COLOR_BODY = "#111827";
    private static final String COLOR_MUTED = "#6b7280";
    private static final String COLOR_PROMPT = "#9ca3af";
    private static final String COLOR_ERROR = "#dc2626";
    private static final String COLOR_SUCCESS = "#16a34a";
    private static final String COLOR_FIELD_BG = "#f9fafb";

    private static final String FONT_SIZE_12 = "-fx-font-size: 12px;";
    private static final String FONT_SIZE_13 = "-fx-font-size: 13px;";
    private static final String FONT_SIZE_14 = "-fx-font-size: 14px;";
    private static final String FONT_SIZE_28 = "-fx-font-size: 28px;";
    private static final String FONT_WEIGHT_BOLD = "-fx-font-weight: bold;";

    private static final String BACKGROUND_RADIUS_10 = "-fx-background-radius: 10;";
    private static final String BACKGROUND_RADIUS_18 = "-fx-background-radius: 18;";
    private static final String BORDER_RADIUS_10 = "-fx-border-radius: 10;";
    private static final String BORDER_RADIUS_18 = "-fx-border-radius: 18;";
    private static final String BORDER_WIDTH_1 = "-fx-border-width: 1;";
    private static final String CURSOR_HAND = "-fx-cursor: hand;";

    private static final String LOGIN_TEXT = "Login";
    private static final String SIGNING_IN_TEXT = "Signing in...";

    private final BorderPane root = new BorderPane();

    private final Label titleLabel = new Label("Smart Home Maintenance Platform");
    private final Label subtitleLabel = new Label("Sign in to continue");
    private final Button createAccountButton = new Button("Create Account");
    private final TextField emailField = new TextField();
    private final PasswordField passwordField = new PasswordField();

    private final Button loginButton = new Button(LOGIN_TEXT);
    private final Label messageLabel = new Label();
    private final ProgressIndicator progressIndicator = new ProgressIndicator();

    private final VBox loginCard = new VBox(16);

    public LoginView() {
        initialise();
    }

    private void initialise() {
        configureRoot();
        configureLabels();
        configureFields();
        configureButton();
        configureMessageLabel();
        configureProgressIndicator();
        configureCard();
        wireKeyboardBehaviour();
    }

    private void configureRoot() {
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8fafc, #eef2f7);");
    }

    private void configureLabels() {
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(340);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setStyle(
                FONT_SIZE_28 +
                FONT_WEIGHT_BOLD +
                textFill(COLOR_TITLE)
        );

        subtitleLabel.setStyle(
                FONT_SIZE_14 +
                textFill(COLOR_MUTED)
        );
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
    }

    private void configureButton() {
        loginButton.setPrefWidth(340);
        loginButton.setPrefHeight(46);
        loginButton.setDefaultButton(true);
        loginButton.setStyle(primaryButtonStyle(COLOR_PRIMARY));

        loginButton.setOnMouseEntered(event ->
                loginButton.setStyle(primaryButtonStyle(COLOR_PRIMARY_HOVER))
        );

        loginButton.setOnMouseExited(event ->
                loginButton.setStyle(primaryButtonStyle(COLOR_PRIMARY))
        );

        createAccountButton.setPrefWidth(340);
        createAccountButton.setPrefHeight(42);
        createAccountButton.setStyle(secondaryButtonStyle());
    }

    private void configureMessageLabel() {
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(340);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setStyle(
                FONT_SIZE_12 +
                textFill(COLOR_ERROR)
        );
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    private void configureProgressIndicator() {
        progressIndicator.setVisible(false);
        progressIndicator.setManaged(false);
        progressIndicator.setPrefSize(24, 24);
    }

    private void configureCard() {
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.NEVER);

        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(36));
        loginCard.setMaxWidth(430);
        loginCard.setPrefWidth(430);
        loginCard.setStyle(
                backgroundColor(COLOR_WHITE) +
                BACKGROUND_RADIUS_18 +
                borderColor(COLOR_CARD_BORDER) +
                BORDER_RADIUS_18 +
                BORDER_WIDTH_1 +
                "-fx-effect: dropshadow(gaussian, rgba(15, 23, 42, 0.08), 18, 0.12, 0, 6);"
        );

        loginCard.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                spacer,
                emailField,
                passwordField,
                loginButton,
                createAccountButton,
                progressIndicator,
                messageLabel
        );

        root.setCenter(loginCard);
        BorderPane.setAlignment(loginCard, Pos.CENTER);
    }

    private void wireKeyboardBehaviour() {
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !loginButton.isDisabled()) {
                loginButton.fire();
            }
        });
    }

    private String commonFieldStyle() {
        return backgroundColor(COLOR_FIELD_BG) +
               BACKGROUND_RADIUS_10 +
               borderColor(COLOR_BORDER) +
               BORDER_RADIUS_10 +
               BORDER_WIDTH_1 +
               FONT_SIZE_14 +
               textFill(COLOR_BODY) +
               promptTextFill(COLOR_PROMPT);
    }

    private String primaryButtonStyle(String backgroundColor) {
        return backgroundColor(backgroundColor) +
               BACKGROUND_RADIUS_10 +
               CURSOR_HAND +
               textFill(COLOR_WHITE) +
               FONT_SIZE_14 +
               FONT_WEIGHT_BOLD;
    }

    private String secondaryButtonStyle() {
        return backgroundColor(COLOR_WHITE) +
               borderColor(COLOR_BORDER) +
               BORDER_RADIUS_10 +
               BACKGROUND_RADIUS_10 +
               textFill(COLOR_TITLE) +
               FONT_SIZE_13 +
               FONT_WEIGHT_BOLD;
    }

    private String backgroundColor(String color) {
        return "-fx-background-color: " + color + ";";
    }

    private String borderColor(String color) {
        return "-fx-border-color: " + color + ";";
    }

    private String textFill(String color) {
        return "-fx-text-fill: " + color + ";";
    }

    private String promptTextFill(String color) {
        return "-fx-prompt-text-fill: " + color + ";";
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

    public void clearPassword() {
        passwordField.clear();
    }

    public void clearFields() {
        emailField.clear();
        passwordField.clear();
    }

    public void setMessage(String message, boolean isError) {
        messageLabel.setText(message == null ? "" : message);
        messageLabel.setStyle(
                FONT_SIZE_12 +
                textFill(isError ? COLOR_ERROR : COLOR_SUCCESS)
        );

        boolean hasMessage = message != null && !message.isBlank();
        messageLabel.setVisible(hasMessage);
        messageLabel.setManaged(hasMessage);
    }

    public void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        emailField.setDisable(loading);
        passwordField.setDisable(loading);

        progressIndicator.setVisible(loading);
        progressIndicator.setManaged(loading);

        loginButton.setText(loading ? SIGNING_IN_TEXT : LOGIN_TEXT);
    }

    public void setOnLogin(Runnable action) {
        loginButton.setOnAction(event -> {
            hideMessage();
            action.run();
        });
    }

    public void hideMessage() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
        messageLabel.setText("");
    }

    public void focusEmailField() {
        emailField.requestFocus();
    }

    public void setOnCreateAccount(Runnable action) {
        createAccountButton.setOnAction(event -> {
            hideMessage();
            action.run();
        });
    }
}