package com.sigma.smarthome.ui.view;

import com.sigma.smarthome.ui.model.MaintenanceRequest;
import com.sigma.smarthome.ui.model.NotificationItem;
import com.sigma.smarthome.ui.model.Property;
import com.sigma.smarthome.ui.service.MaintenanceApiService;
import com.sigma.smarthome.ui.service.NotificationApiService;
import com.sigma.smarthome.ui.service.PropertyApiService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsView {

    private final VBox root = new VBox(22);

    private final Label sectionLabel = new Label("MANAGEMENT ANALYTICS");
    private final Label titleLabel = new Label("Reports & Insights");
    private final Label subtitleLabel = new Label("Operational reporting, workload visibility, and platform health summary.");
    private final Label messageLabel = new Label();
    private final Label lastUpdatedLabel = new Label("Last updated: -");

    private final PropertyApiService propertyApiService = new PropertyApiService();
    private final MaintenanceApiService maintenanceApiService = new MaintenanceApiService();
    private final NotificationApiService notificationApiService = new NotificationApiService();

    private final Label totalPropertiesValue = new Label("-");
    private final Label propertyTypesValue = new Label("-");
    private final Label totalRequestsValue = new Label("-");
    private final Label unreadNotificationsValue = new Label("-");

    private final Label openRequestsValue = new Label("-");
    private final Label inProgressRequestsValue = new Label("-");
    private final Label completedRequestsValue = new Label("-");
    private final Label latestNotificationValue = new Label("-");

    private final Label insightLineOne = new Label("No insight available yet.");
    private final Label insightLineTwo = new Label("");
    private final Label insightLineThree = new Label("");

    private final Label healthPropertyService = new Label("Property Service: -");
    private final Label healthMaintenanceService = new Label("Maintenance Service: -");
    private final Label healthNotificationService = new Label("Notification Service: -");
    private final Label healthRefreshStatus = new Label("Refresh Status: -");

    public ReportsView() {
        initialise();
        loadReportData();
    }

    private void initialise() {
        configureRoot();
        configureHeader();
        configureDashboard();
    }

    private void configureRoot() {
        root.setPadding(new Insets(24, 28, 28, 28));
        root.setMaxWidth(1180);
        root.setFillWidth(true);
        root.setStyle("-fx-background-color: transparent;");
    }

    private void configureHeader() {
        sectionLabel.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2563eb;" +
                "-fx-letter-spacing: 1.2px;"
        );

        titleLabel.setStyle(
                "-fx-font-size: 34px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );

        subtitleLabel.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #64748b;"
        );

        messageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);

        lastUpdatedLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #64748b;"
        );

        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle(
                "-fx-background-color: #eff6ff;" +
                "-fx-text-fill: #2563eb;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 9 16 9 16;" +
                "-fx-cursor: hand;"
        );
        refreshButton.setOnAction(e -> loadReportData());

        Button exportButton = new Button("Export Summary");
        exportButton.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #0f172a;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #dbe3ee;" +
                "-fx-border-radius: 12;" +
                "-fx-padding: 9 16 9 16;" +
                "-fx-cursor: hand;"
        );
        exportButton.setDisable(true);
        exportButton.setOpacity(0.75);

        VBox headerText = new VBox(6, sectionLabel, titleLabel, subtitleLabel);
        headerText.setAlignment(Pos.CENTER_LEFT);

        HBox actionsRow = new HBox(10, refreshButton, exportButton);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);

        VBox headerActions = new VBox(8, lastUpdatedLabel, actionsRow);
        headerActions.setMinWidth(220);
        headerActions.setMaxWidth(260);
        headerActions.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerRow = new HBox(16, headerText, spacer, headerActions);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().add(headerRow);
        VBox.setMargin(messageLabel, new Insets(2, 0, 0, 0));
        root.getChildren().add(messageLabel);
    }

    private void configureDashboard() {
        HBox primaryMetricsRow = new HBox(14,
                createMetricCard("🏠", "Total Properties", totalPropertiesValue, "Registered properties across the platform", "#2563eb"),
                createMetricCard("📊", "Total Requests", totalRequestsValue, "Total maintenance demand currently recorded", "#7c3aed"),
                createMetricCard("🔴", "Open Requests", openRequestsValue, "Requests needing attention right now", "#dc2626"),
                createMetricCard("🔔", "Unread Notifications", unreadNotificationsValue, "Items still awaiting review", "#d97706")
        );
        primaryMetricsRow.setAlignment(Pos.CENTER_LEFT);

        HBox secondaryMetricsRow = new HBox(14,
                createMetricCard("📂", "Property Types", propertyTypesValue, "Distinct property categories currently registered", "#0f766e"),
                createMetricCard("🟡", "In Progress", inProgressRequestsValue, "Requests actively being handled", "#ca8a04"),
                createMetricCard("🟢", "Completed", completedRequestsValue, "Requests successfully completed", "#16a34a"),
                createTextMetricCard(
                        "📝",
                        "Latest Notification",
                        latestNotificationValue,
                        "Most recent notification snapshot",
                        "#475569"
                )
                );
        secondaryMetricsRow.setAlignment(Pos.CENTER_LEFT);

        VBox overviewBody = new VBox(14, primaryMetricsRow, secondaryMetricsRow);
        VBox overviewSection = createSectionCard("Overview", overviewBody);

        VBox insightSection = createSectionCard(
                "Insight Summary",
                new VBox(10, createInsightLabel(insightLineOne), createInsightLabel(insightLineTwo), createInsightLabel(insightLineThree))
        );

        VBox healthSection = createSectionCard(
                "Platform Health Snapshot",
                new VBox(10,
                        createHealthLabel(healthPropertyService),
                        createHealthLabel(healthMaintenanceService),
                        createHealthLabel(healthNotificationService),
                        createHealthLabel(healthRefreshStatus)
                )
        );

        HBox bottomRow = new HBox(16, insightSection, healthSection);
        bottomRow.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(insightSection, Priority.ALWAYS);
        HBox.setHgrow(healthSection, Priority.ALWAYS);

        root.getChildren().addAll(overviewSection, bottomRow);
        VBox.setVgrow(overviewSection, Priority.ALWAYS);
    }
    
    private VBox createTextMetricCard(String icon, String title, Label valueLabel, String note, String accentColor) {
        Label iconLabel = new Label(icon);
        iconLabel.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-background-color: #f8fafc;" +
                "-fx-background-radius: 14;" +
                "-fx-padding: 8;"
        );

        Label titleText = new Label(title);
        titleText.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #64748b;"
        );

        valueLabel.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );
        valueLabel.setWrapText(true);
        valueLabel.setMaxWidth(180);

        Label noteLabel = new Label(note);
        noteLabel.setWrapText(true);
        noteLabel.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: #64748b;"
        );

        HBox titleRow = new HBox(10, iconLabel, titleText);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(12, titleRow, valueLabel, noteLabel);
        card.setPadding(new Insets(18));
        card.setPrefWidth(220);
        card.setMinHeight(135);
        card.setMaxHeight(135);
        HBox.setHgrow(card, Priority.ALWAYS);

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 18;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 16, 0.2, 0, 4);" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 1 1 1 5;" +
                "-fx-border-color: #e2e8f0 #e2e8f0 #e2e8f0 " + accentColor + ";"
        );

        return card;
    }

    private void loadReportData() {
        try {
            List<Property> properties = propertyApiService.getProperties();
            List<MaintenanceRequest> requests = maintenanceApiService.getRequests(null, null);
            List<NotificationItem> notifications = notificationApiService.getNotifications(0, 10);

            long propertyTypesCount = properties.stream()
                    .map(Property::getPropertyType)
                    .filter(type -> type != null && !type.isBlank())
                    .distinct()
                    .count();

            long openCount = requests.stream()
                    .filter(request -> "OPEN".equalsIgnoreCase(request.getStatus()))
                    .count();

            long inProgressCount = requests.stream()
                    .filter(request -> "IN_PROGRESS".equalsIgnoreCase(request.getStatus()))
                    .count();

            long completedCount = requests.stream()
                    .filter(request -> "COMPLETED".equalsIgnoreCase(request.getStatus()))
                    .count();

            long unreadCount = notifications.stream()
                    .filter(notification -> !Boolean.TRUE.equals(notification.getIsRead()))
                    .count();

            totalPropertiesValue.setText(String.valueOf(properties.size()));
            propertyTypesValue.setText(String.valueOf(propertyTypesCount));
            totalRequestsValue.setText(String.valueOf(requests.size()));
            unreadNotificationsValue.setText(String.valueOf(unreadCount));

            openRequestsValue.setText(String.valueOf(openCount));
            inProgressRequestsValue.setText(String.valueOf(inProgressCount));
            completedRequestsValue.setText(String.valueOf(completedCount));

            if (notifications.isEmpty()) {
                latestNotificationValue.setText("No recent notifications");
            } else {
                NotificationItem latest = notifications.get(0);
                String latestType = safeText(latest.getType());
                latestNotificationValue.setText(latestType.length() > 28 ? latestType.substring(0, 28) + "..." : latestType);
            }

            updateInsights(openCount, inProgressCount, completedCount, unreadCount, notifications);
            updateHealthLabels(true);
            updateLastUpdated();
            showMessage("Reports loaded successfully.", false);

        } catch (RuntimeException ex) {
            totalPropertiesValue.setText("-");
            propertyTypesValue.setText("-");
            totalRequestsValue.setText("-");
            unreadNotificationsValue.setText("-");
            openRequestsValue.setText("-");
            inProgressRequestsValue.setText("-");
            completedRequestsValue.setText("-");
            latestNotificationValue.setText("-");

            insightLineOne.setText("Report insight data is currently unavailable.");
            insightLineTwo.setText("Please refresh the page or check service connectivity.");
            insightLineThree.setText("");

            updateHealthLabels(false);
            lastUpdatedLabel.setText("Last updated: unavailable");
            showMessage("Failed to load report data.", true);
        }
    }

    private void updateInsights(long openCount,
                                long inProgressCount,
                                long completedCount,
                                long unreadCount,
                                List<NotificationItem> notifications) {

        insightLineOne.setText("• " + openCount + " open request(s) currently need review.");
        insightLineTwo.setText("• " + inProgressCount + " request(s) are actively being handled.");
        if (notifications.isEmpty()) {
            insightLineThree.setText("• No recent notifications were recorded in the latest refresh.");
        } else {
            insightLineThree.setText("• " + unreadCount + " notification(s) remain unread, with recent platform activity detected.");
        }
    }

    private void updateHealthLabels(boolean success) {
        if (success) {
            healthPropertyService.setText("Property Service: Connected");
            healthMaintenanceService.setText("Maintenance Service: Connected");
            healthNotificationService.setText("Notification Service: Connected");
            healthRefreshStatus.setText("Refresh Status: Successful");
        } else {
            healthPropertyService.setText("Property Service: Unavailable");
            healthMaintenanceService.setText("Maintenance Service: Unavailable");
            healthNotificationService.setText("Notification Service: Unavailable");
            healthRefreshStatus.setText("Refresh Status: Failed");
        }
    }

    private void updateLastUpdated() {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        lastUpdatedLabel.setText("Last updated: " + time);
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

    private VBox createMetricCard(String icon, String title, Label valueLabel, String note, String accentColor) {
        Label iconLabel = new Label(icon);
        iconLabel.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-background-color: #f8fafc;" +
                "-fx-background-radius: 14;" +
                "-fx-padding: 8;"
        );

        Label titleText = new Label(title);
        titleText.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #64748b;"
        );

        valueLabel.setStyle(
                "-fx-font-size: 30px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );
        valueLabel.setWrapText(true);

        Label noteLabel = new Label(note);
        noteLabel.setWrapText(true);
        noteLabel.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: #64748b;"
        );

        HBox titleRow = new HBox(10, iconLabel, titleText);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, titleRow, valueLabel, noteLabel);
        card.setPadding(new Insets(18));
        card.setPrefWidth(220);
        card.setMinHeight(135);
        card.setMaxHeight(135);
        HBox.setHgrow(card, Priority.ALWAYS);

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 18;" +
                "-fx-border-width: 1;" +
                "-fx-border-insets: 0;" +
                "-fx-background-insets: 0;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 16, 0.2, 0, 4);" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 1 1 1 5;" +
                "-fx-border-color: #e2e8f0 #e2e8f0 #e2e8f0 " + accentColor + ";"
        );

        return card;
    }

    private VBox createSectionCard(String title, Parent body) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 19px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );

        VBox card = new VBox(18, titleLabel, body);
        card.setPadding(new Insets(22));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 20;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 18, 0.2, 0, 6);"
        );
        return card;
    }

    private Label createInsightLabel(Label label) {
        label.setWrapText(true);
        label.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #475569;"
        );
        return label;
    }

    private Label createHealthLabel(Label label) {
        label.setWrapText(true);
        label.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #334155;"
        );
        return label;
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    public Parent getView() {
        return root;
    }
}