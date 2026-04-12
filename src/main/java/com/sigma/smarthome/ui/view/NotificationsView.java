package com.sigma.smarthome.ui.view;

import com.sigma.smarthome.ui.model.NotificationItem;
import com.sigma.smarthome.ui.service.NotificationApiService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class NotificationsView {

    private final VBox root = new VBox(20);

    private final Label titleLabel = new Label("Notifications");
    private final Label subtitleLabel = new Label("Stay updated on recent system events and user-specific alerts.");
    private final Label messageLabel = new Label();

    private final Button refreshButton = new Button("Refresh");

    private final TableView<NotificationItem> table = new TableView<>();
    private final ObservableList<NotificationItem> notificationData = FXCollections.observableArrayList();

    private final NotificationApiService notificationApiService = new NotificationApiService();

    public NotificationsView() {
        initialise();
        loadNotifications();
    }

    private void initialise() {
        configureRoot();
        configureHeader();
        configureActions();
        configureTable();
    }

    private void configureRoot() {
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: transparent;");
    }

    private void configureHeader() {
        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        messageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);

        root.getChildren().addAll(titleLabel, subtitleLabel, messageLabel);
    }

    private void configureActions() {
        refreshButton.setOnAction(e -> loadNotifications());

        HBox actionRow = new HBox(refreshButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().add(actionRow);
    }

    private void configureTable() {
        TableColumn<NotificationItem, String> titleCol = createColumn("Title", 220, NotificationItem::getTitle);
        TableColumn<NotificationItem, String> messageCol = createColumn("Message", 420, NotificationItem::getMessage);
        TableColumn<NotificationItem, String> typeCol = createColumn("Type", 140, NotificationItem::getType);
        TableColumn<NotificationItem, String> readCol = createColumn("Read", 90, item ->
                Boolean.TRUE.equals(item.getIsRead()) ? "Yes" : "No");
        TableColumn<NotificationItem, String> createdAtCol = createColumn("Created At", 220, NotificationItem::getCreatedAt);

        table.getColumns().addAll(titleCol, messageCol, typeCol, readCol, createdAtCol);
        table.setItems(notificationData);
        table.setPrefHeight(560);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No notifications available."));

        VBox tableCard = new VBox(table);
        tableCard.setPadding(new Insets(18));
        tableCard.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 18;" +
                "-fx-border-width: 1;"
        );

        root.getChildren().add(tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);
    }

    private TableColumn<NotificationItem, String> createColumn(
            String title,
            double width,
            java.util.function.Function<NotificationItem, String> mapper
    ) {
        TableColumn<NotificationItem, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(mapper.apply(data.getValue()))));
        column.setPrefWidth(width);
        return column;
    }

    private void loadNotifications() {
        try {
            hideMessage();

            List<NotificationItem> notifications = notificationApiService.getNotifications(0, 10);
            notificationData.setAll(notifications);

            showMessage("Notifications loaded successfully.", false);
        } catch (RuntimeException ex) {
            notificationData.clear();
            showMessage("Failed to load notifications.", true);
        }
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

    private void hideMessage() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
        messageLabel.setText("");
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    public Parent getView() {
        return root;
    }
}