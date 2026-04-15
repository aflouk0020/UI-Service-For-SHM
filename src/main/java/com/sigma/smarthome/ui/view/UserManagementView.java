package com.sigma.smarthome.ui.view;

import com.sigma.smarthome.ui.service.UserApiService;
import com.sigma.smarthome.ui.service.UserApiService.RegisterResult;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserManagementView {

    private static final DateTimeFormatter DISPLAY_TIME =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final VBox root = new VBox(22);

    private final Label titleLabel = new Label("User Management");
    private final Label subtitleLabel = new Label("Create and manage platform user accounts for operational workflows.");
    private final Label messageLabel = new Label();
    private final Label helperLabel = new Label(
            "Manager note: the table below shows users created from this UI session. "
                    + "A full user listing endpoint has not been connected yet."
    );

    private final Label totalUsersValue = new Label("0");
    private final Label managersValue = new Label("0");
    private final Label staffValue = new Label("0");
    private final Label latestCreatedValue = new Label("-");

    private final TextField emailField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final ComboBox<String> roleBox = new ComboBox<>();
    private final Button createUserButton = new Button("Create User");
    private final Button clearButton = new Button("Clear Form");

    private final TableView<UserRow> table = new TableView<>();
    private final ObservableList<UserRow> userRows = FXCollections.observableArrayList();

    private final UserApiService userApiService = new UserApiService();

    public UserManagementView() {
        initialise();
    }

    private void initialise() {
        configureRoot();
        configureHeader();
        configureSummaryCards();
        configureForm();
        configureTable();
    }

    private void configureRoot() {
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: transparent;");
    }

    private void configureHeader() {
        titleLabel.setStyle(
                "-fx-font-size: 32px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );

        subtitleLabel.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-text-fill: #64748b;"
        );

        messageLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;"
        );
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);

        helperLabel.setWrapText(true);
        helperLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #92400e;" +
                "-fx-background-color: #fef3c7;" +
                "-fx-background-radius: 14;" +
                "-fx-padding: 12 16 12 16;" +
                "-fx-border-color: #fde68a;" +
                "-fx-border-radius: 14;" +
                "-fx-border-width: 1;"
        );

        root.getChildren().addAll(titleLabel, subtitleLabel, messageLabel, helperLabel);
    }

    private void configureSummaryCards() {
        HBox summaryRow = new HBox(16,
                createSummaryCard("Total Created", totalUsersValue),
                createSummaryCard("Property Managers", managersValue),
                createSummaryCard("Maintenance Staff", staffValue),
                createSummaryCard("Latest Created", latestCreatedValue)
        );
        summaryRow.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().add(summaryRow);
    }

    private VBox createSummaryCard(String title, Label valueLabel) {
        Label titleCard = new Label(title);
        titleCard.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #94a3b8;"
        );

        valueLabel.setWrapText(true);
        valueLabel.setStyle(
                "-fx-font-size: 24px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );

        VBox card = new VBox(10, titleCard, valueLabel);
        card.setPadding(new Insets(20));
        card.setPrefWidth(220);
        card.setMinHeight(118);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #ffffff, #f8fbff);" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: #dbeafe;" +
                "-fx-border-radius: 20;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.08), 18, 0.12, 0, 6);"
        );

        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private void configureForm() {
        emailField.setPromptText("Enter user email");
        emailField.setPrefWidth(320);
        emailField.setPrefHeight(42);
        emailField.setStyle(commonFieldStyle());

        passwordField.setPromptText("Enter temporary password");
        passwordField.setPrefWidth(320);
        passwordField.setPrefHeight(42);
        passwordField.setStyle(commonFieldStyle());

        roleBox.getItems().addAll("PROPERTY_MANAGER", "MAINTENANCE_STAFF");
        roleBox.setPromptText("Select role");
        roleBox.setPrefWidth(320);
        roleBox.setPrefHeight(42);
        roleBox.setStyle(commonFieldStyle());

        stylePrimaryButton(createUserButton);
        styleSecondaryButton(clearButton);

        createUserButton.setOnAction(e -> createUser());
        clearButton.setOnAction(e -> clearForm());

        GridPane formGrid = new GridPane();
        formGrid.setHgap(18);
        formGrid.setVgap(14);

        formGrid.add(createFieldLabel("Email"), 0, 0);
        formGrid.add(emailField, 0, 1);

        formGrid.add(createFieldLabel("Password"), 1, 0);
        formGrid.add(passwordField, 1, 1);

        formGrid.add(createFieldLabel("Role"), 2, 0);
        formGrid.add(roleBox, 2, 1);

        HBox actionRow = new HBox(12, createUserButton, clearButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        VBox formCard = new VBox(18, formGrid, actionRow);
        formCard.setPadding(new Insets(22));
        formCard.setStyle(
                "-fx-background-color: rgba(255,255,255,0.98);" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 20;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 18, 0.12, 0, 6);"
        );

        root.getChildren().add(formCard);
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #64748b;"
        );
        return label;
    }

    private void configureTable() {
        TableColumn<UserRow, String> emailCol = createColumn("Email", 280, UserRow::email);
        TableColumn<UserRow, String> roleCol = createColumn("Role", 180, row -> formatRole(row.role()));
        TableColumn<UserRow, String> statusCol = createColumn("Status", 140, UserRow::status);
        TableColumn<UserRow, String> createdAtCol = createColumn("Created At", 190, UserRow::createdAt);

        table.getColumns().addAll(emailCol, roleCol, statusCol, createdAtCol);
        table.setItems(userRows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(430);
        table.setPlaceholder(new Label("No users created from this UI session yet."));
        table.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-control-inner-background: white;" +
                "-fx-table-cell-border-color: #eef2f7;" +
                "-fx-padding: 0;"
        );

        VBox tableCard = new VBox(table);
        tableCard.setPadding(new Insets(18));
        tableCard.setStyle(
                "-fx-background-color: rgba(255,255,255,0.98);" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 20;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 18, 0.12, 0, 6);"
        );

        root.getChildren().add(tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);
    }

    private TableColumn<UserRow, String> createColumn(
            String title,
            double width,
            java.util.function.Function<UserRow, String> mapper
    ) {
        TableColumn<UserRow, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(mapper.apply(data.getValue()))));
        column.setPrefWidth(width);
        column.setStyle(
                "-fx-alignment: CENTER_LEFT;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;"
        );
        return column;
    }

    private void createUser() {
        String email = safeTrim(emailField.getText());
        String password = safeTrim(passwordField.getText());
        String role = roleBox.getValue();

        if (email.isBlank() || password.isBlank() || role == null || role.isBlank()) {
            showMessage("Email, password, and role are required.", true);
            return;
        }

        try {
            RegisterResult result = userApiService.register(email, password, role);

            UserRow row = new UserRow(
                    result.email(),
                    result.role(),
                    "Active",
                    LocalDateTime.now().format(DISPLAY_TIME)
            );

            userRows.add(0, row);
            updateSummaryCards();
            clearForm();

            showMessage(
                    "User created successfully: " + result.email() + " (" + formatRole(result.role()) + ").",
                    false
            );

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            showMessage("User creation was interrupted. Please try again.", true);
        } catch (IOException ex) {
            showMessage("Failed to create user. Service communication error.", true);
        } catch (RuntimeException ex) {
            showMessage(ex.getMessage() == null || ex.getMessage().isBlank()
                    ? "Failed to create user."
                    : ex.getMessage(), true);
        }
    }

    private void clearForm() {
        emailField.clear();
        passwordField.clear();
        roleBox.getSelectionModel().clearSelection();
    }

    private void updateSummaryCards() {
        totalUsersValue.setText(String.valueOf(userRows.size()));

        long managers = userRows.stream()
                .filter(row -> "PROPERTY_MANAGER".equalsIgnoreCase(row.role()))
                .count();

        long staff = userRows.stream()
                .filter(row -> "MAINTENANCE_STAFF".equalsIgnoreCase(row.role()))
                .count();

        managersValue.setText(String.valueOf(managers));
        staffValue.setText(String.valueOf(staff));

        if (userRows.isEmpty()) {
            latestCreatedValue.setText("-");
        } else {
            latestCreatedValue.setText(userRows.get(0).email());
        }
    }

    private String commonFieldStyle() {
        return "-fx-background-color: #f8fafc;" +
               "-fx-text-fill: #0f172a;" +
               "-fx-font-size: 13px;" +
               "-fx-background-radius: 12;" +
               "-fx-border-color: #d1d5db;" +
               "-fx-border-radius: 12;" +
               "-fx-border-width: 1;" +
               "-fx-prompt-text-fill: #9ca3af;";
    }

    private void stylePrimaryButton(Button button) {
        button.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #2563eb, #1d4ed8);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 14;" +
                "-fx-padding: 10 18 10 18;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.20), 14, 0.15, 0, 4);"
        );
    }

    private void styleSecondaryButton(Button button) {
        button.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #ffffff, #f8fafc);" +
                "-fx-text-fill: #0f172a;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: #cbd5e1;" +
                "-fx-border-radius: 14;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 10 18 10 18;" +
                "-fx-cursor: hand;"
        );
    }

    private void showMessage(String message, boolean error) {
        messageLabel.setText(message);
        messageLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + (error ? "#dc2626;" : "#16a34a;")
        );
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private String formatRole(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }

        String formatted = value.replace("_", " ").toLowerCase();
        return Character.toUpperCase(formatted.charAt(0)) + formatted.substring(1);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    public Parent getView() {
        return root;
    }

    private record UserRow(String email, String role, String status, String createdAt) {
    }
}