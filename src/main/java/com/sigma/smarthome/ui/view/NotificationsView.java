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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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
	 // Summary cards
    private final Label totalCountLabel = new Label("0");
    private final Label unreadCountLabel = new Label("0");
    private final Label readCountLabel = new Label("0");
    private final Label latestTypeLabel = new Label("-");

    // Actions
    private final Button markSelectedReadButton = new Button("Mark Selected Read");
    private final Button markAllReadButton = new Button("Mark All Read");

    // Filters
    private final CheckBox unreadOnlyCheckBox = new CheckBox("Unread only");
    private final ComboBox<String> typeFilter = new ComboBox<>();

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
        configureSummaryCards();   // NEW
        configureActions();        // UPDATED
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
    
    private void configureSummaryCards() {
        HBox cardsRow = new HBox(16);
        cardsRow.setAlignment(Pos.CENTER_LEFT);

        cardsRow.getChildren().addAll(
                createSummaryCard("Total", totalCountLabel),
                createSummaryCard("Unread", unreadCountLabel),
                createSummaryCard("Read", readCountLabel),
                createSummaryCard("Latest Type", latestTypeLabel)
        );

        root.getChildren().add(cardsRow);
    }

    private VBox createSummaryCard(String title, Label valueLabel) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");

        valueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        VBox card = new VBox(8, titleLabel, valueLabel);
        card.setPadding(new Insets(16));
        card.setPrefWidth(180);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 16;" +
                "-fx-border-width: 1;"
        );

        return card;
    }

    private void configureActions() {
        refreshButton.setOnAction(e -> loadNotifications());

        markSelectedReadButton.setOnAction(e -> markSelectedAsRead());
        markAllReadButton.setOnAction(e -> markAllAsRead());

        typeFilter.getItems().addAll("", "ASSIGNMENT", "STATUS_UPDATE", "SUCCESS", "WARNING", "ERROR");
        typeFilter.setPromptText("Filter by type");

        unreadOnlyCheckBox.setOnAction(e -> applyFilters());
        typeFilter.setOnAction(e -> applyFilters());

        HBox actionRow = new HBox(12,
                refreshButton,
                markSelectedReadButton,
                markAllReadButton,
                unreadOnlyCheckBox,
                typeFilter
        );

        actionRow.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().add(actionRow);
    }
    
    private void applyFilters() {
        List<NotificationItem> all = notificationApiService.getNotifications(0, PAGE_SIZE);

        boolean unreadOnly = unreadOnlyCheckBox.isSelected();
        String selectedType = typeFilter.getValue();

        List<NotificationItem> filtered = all.stream()
                .filter(n -> !unreadOnly || !Boolean.TRUE.equals(n.getIsRead()))
                .filter(n -> selectedType == null || selectedType.isBlank()
                        || selectedType.equalsIgnoreCase(n.getType()))
                .toList();

        notificationData.setAll(filtered);
        updateSummary(filtered);
    }
    
    private void updateSummary(List<NotificationItem> list) {
        int total = list.size();
        int unread = (int) list.stream().filter(n -> !Boolean.TRUE.equals(n.getIsRead())).count();
        int read = total - unread;

        totalCountLabel.setText(String.valueOf(total));
        unreadCountLabel.setText(String.valueOf(unread));
        readCountLabel.setText(String.valueOf(read));

        latestTypeLabel.setText(
                list.isEmpty() ? "-" : safe(list.get(0).getType())
        );
    }
    
    private void markSelectedAsRead() {
        NotificationItem selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) return;
        if (Boolean.TRUE.equals(selected.getIsRead())) return;

        try {
            notificationApiService.markAsRead(selected.getId());
            selected.setIsRead(true);

            table.refresh();
            updateSummary(notificationData);

        } catch (RuntimeException ex) {
            showError("Failed to mark notification as read.");
        }
    }
    
    private void markAllAsRead() {
        try {
            for (NotificationItem item : notificationData) {
                if (!Boolean.TRUE.equals(item.getIsRead())) {
                    notificationApiService.markAsRead(item.getId());
                    item.setIsRead(true);
                }
            }

            table.refresh();
            updateSummary(notificationData);

        } catch (RuntimeException ex) {
            showError("Failed to mark all notifications as read.");
        }
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

            List<NotificationItem> notifications =
                    notificationApiService.getNotifications(0, PAGE_SIZE);

            notificationData.setAll(notifications);
            updateSummary(notifications);

            statusLabel.setText("Showing " + notifications.size() + " notifications.");

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