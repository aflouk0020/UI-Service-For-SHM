package com.sigma.smarthome.ui.view;

import com.sigma.smarthome.ui.model.NotificationItem;
import com.sigma.smarthome.ui.service.NotificationApiService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationsView {

    private static final int PAGE_SIZE = 10;
    private static final DateTimeFormatter INPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS]");
    private static final DateTimeFormatter OUTPUT_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final VBox root = new VBox(20);

    private final Label titleLabel = new Label("Notifications");
    private final Label subtitleLabel = new Label("Stay updated on recent system events and user-specific alerts.");
    private final Label messageLabel = new Label();
    private final Label statusLabel = new Label("Loading notifications...");

    private final Button refreshButton = new Button("Refresh");

    private final TableView<NotificationItem> table = new TableView<>();
    private final ObservableList<NotificationItem> notificationData = FXCollections.observableArrayList();

    private final NotificationApiService notificationApiService = new NotificationApiService();

    private Timeline autoRefreshTimeline;

    public NotificationsView() {
        initialise();
        loadNotifications();
        startAutoRefresh();
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

        messageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);

        statusLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");

        root.getChildren().addAll(titleLabel, subtitleLabel, messageLabel, statusLabel);
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
        TableColumn<NotificationItem, String> readCol = createColumn("Status", 110, item ->
                Boolean.TRUE.equals(item.getIsRead()) ? "Read" : "Unread");
        TableColumn<NotificationItem, String> createdAtCol = createColumn("Created At", 220, item ->
                formatTimestamp(item.getCreatedAt()));

        table.getColumns().addAll(titleCol, messageCol, typeCol, readCol, createdAtCol);
        table.setItems(notificationData);
        table.setPrefHeight(560);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(createEmptyStateLabel());
        
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                NotificationItem selected = table.getSelectionModel().getSelectedItem();

                if (selected != null && !Boolean.TRUE.equals(selected.getIsRead())) {
                    try {
                        notificationApiService.markAsRead(selected.getId());
                        loadNotifications();
                    } catch (RuntimeException ex) {
                        showError("Failed to mark notification as read.");
                    }
                }
            }
        });
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(NotificationItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("");
                } else if (!Boolean.TRUE.equals(item.getIsRead())) {
                	setStyle("-fx-background-color: #eef6ff; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });

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

    private Label createEmptyStateLabel() {
        Label label = new Label("No notifications yet. New updates will appear here.");
        label.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
        return label;
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
            hideError();

            List<NotificationItem> notifications = notificationApiService.getNotifications(0, PAGE_SIZE);
            notificationData.setAll(notifications);

            if (notifications.isEmpty()) {
                statusLabel.setText("No notifications found for this user.");
            } else if (notifications.size() == 1) {
                statusLabel.setText("Showing 1 notification.");
            } else {
                statusLabel.setText("Showing " + notifications.size() + " notifications.");
            }

        } catch (RuntimeException ex) {
            notificationData.clear();
            statusLabel.setText("");
            showError("Failed to load notifications.");
        }
    }

    private void startAutoRefresh() {
        autoRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(15), event -> loadNotifications())
        );
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void hideError() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
        messageLabel.setText("");
    }

    private String formatTimestamp(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }

        try {
            return LocalDateTime.parse(value, INPUT_FORMAT).format(OUTPUT_FORMAT);
        } catch (Exception ex) {
            return value;
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    public Parent getView() {
        return root;
    }
}