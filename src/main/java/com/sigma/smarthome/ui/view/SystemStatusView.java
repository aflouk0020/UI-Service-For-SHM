package com.sigma.smarthome.ui.view;

import com.sigma.smarthome.ui.model.MaintenanceRequest;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import com.sigma.smarthome.ui.model.NotificationItem;
import com.sigma.smarthome.ui.model.Property;
import com.sigma.smarthome.ui.service.MaintenanceApiService;
import com.sigma.smarthome.ui.service.NotificationApiService;
import com.sigma.smarthome.ui.service.PropertyApiService;
import com.sigma.smarthome.ui.util.ApiConfig;
import com.sigma.smarthome.ui.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import com.sigma.smarthome.ui.model.EdgeHeartbeat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
public class SystemStatusView {

	
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss");

    private static final String CARD_STYLE =
            "-fx-background-color: white;" +
            "-fx-background-radius: 18;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 18;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 16, 0.10, 0, 6);";

    private static final String METRIC_CARD_STYLE =
            "-fx-background-color: white;" +
            "-fx-background-radius: 18;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 18;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.04), 14, 0.08, 0, 5);";

    private final VBox root = new VBox(22);

    private final Runnable onOpenNotifications;
    private final Runnable onOpenReports;
    private final Runnable onOpenMaintenance;
    
    private final Label edgeDeviceIdValue = new Label("-");
    private final Label edgePropertyIdValue = new Label("-");
    private final Label edgeStatusValue = new Label("-");
    private final Label edgeTemperatureValue = new Label("-");
    private final Label edgeHumidityValue = new Label("-");
    private final Label edgeTimestampValue = new Label("-");
    
    private final Label sectionLabel = new Label("Platform Operations");
    private final Label titleLabel = new Label("System Status");
    private final Label subtitleLabel = new Label(
            "Live operational overview of platform connectivity, service readiness, and session health."
    );

    private final Label platformHealthBadge = new Label("Checking...");
    private final Label lastCheckedLabel = new Label("Last checked: -");
    private final Label messageLabel = new Label();

    private final Button refreshButton = new Button("Refresh Status");
    private final Button openNotificationsButton = new Button("Open Notifications");
    private final Button openReportsButton = new Button("Open Reports");
    private final Button openMaintenanceButton = new Button("Open Maintenance");

    private final Label connectedServicesValue = new Label("-");
    private final Label totalPropertiesValue = new Label("-");
    private final Label totalRequestsValue = new Label("-");
    private final Label unreadNotificationsValue = new Label("-");
    private final Label activeRoleValue = new Label("-");
    private final Label sessionStateValue = new Label("-");

    private final Label connectedServicesNote = new Label("Core platform dependencies");
    private final Label propertiesNote = new Label("Property service records");
    private final Label requestsNote = new Label("Live maintenance workload");
    private final Label unreadNote = new Label("Unread items awaiting action");
    private final Label roleNote = new Label("Current access level");
    private final Label sessionNote = new Label("Authentication state");

    private final Label gatewayStatusValue = new Label("-");
    private final Label propertyStatusValue = new Label("-");
    private final Label maintenanceStatusValue = new Label("-");
    private final Label notificationStatusValue = new Label("-");
    private final Label authStatusValue = new Label("-");

    private final Label gatewayNoteValue = new Label("-");
    private final Label propertyNoteValue = new Label("-");
    private final Label maintenanceNoteValue = new Label("-");
    private final Label notificationNoteValue = new Label("-");
    private final Label authNoteValue = new Label("-");

    private final Label gatewayUrlValue = new Label(ApiConfig.API_GATEWAY_BASE_URL);
    private final Label currentUserValue = new Label("-");
    private final Label currentUserIdValue = new Label("-");
    private final Label currentRoleValue = new Label("-");
    private final Label environmentValue = new Label("Local Development");
    private final Label insightValue = new Label("-");
    private final Label pollingValue = new Label("Active");
    private final Label operationalScoreValue = new Label("-");
    private Timeline autoRefreshTimeline;
    private final PropertyApiService propertyApiService = new PropertyApiService();
    private final MaintenanceApiService maintenanceApiService = new MaintenanceApiService();
    private final NotificationApiService notificationApiService = new NotificationApiService();

    public SystemStatusView() {
        this(() -> {}, () -> {}, () -> {});
    }

    public SystemStatusView(Runnable onOpenNotifications,
                            Runnable onOpenReports,
                            Runnable onOpenMaintenance) {
        this.onOpenNotifications = onOpenNotifications == null ? () -> {} : onOpenNotifications;
        this.onOpenReports = onOpenReports == null ? () -> {} : onOpenReports;
        this.onOpenMaintenance = onOpenMaintenance == null ? () -> {} : onOpenMaintenance;

        initialise();
        loadSystemStatus();
    }

    private void initialise() {
        configureRoot();
        configureHeader();
        configureSummaryCards();
        configureServiceGrid();
        configureBottomSection();
        configureAutoRefresh();
    }

    private void configureRoot() {
        root.setPadding(new Insets(28));
        root.setFillWidth(true);
        root.setStyle("-fx-background-color: transparent;");
    }
    
    private void configureAutoRefresh() {
        autoRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(5), event -> loadSystemStatus())
        );
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }
    
    private String formatHeartbeatTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return "-";
        }

        try {
            return OffsetDateTime.parse(timestamp).format(TIME_FORMAT);
        } catch (Exception ex) {
            return timestamp;
        }
    }

    private void configureHeader() {
        sectionLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2563eb;" +
                "-fx-background-color: #dbeafe;" +
                "-fx-padding: 6 12 6 12;" +
                "-fx-background-radius: 14;"
        );

        titleLabel.setStyle(
                "-fx-font-size: 34px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );

        subtitleLabel.setWrapText(true);
        subtitleLabel.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-text-fill: #64748b;"
        );

        platformHealthBadge.setStyle(badgeStyle("#e2e8f0", "#334155"));

        lastCheckedLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #64748b;"
        );

        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
        messageLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #16a34a;"
        );

        stylePrimaryButton(refreshButton);
        refreshButton.setOnAction(e -> loadSystemStatus());

        VBox leftBox = new VBox(10, sectionLabel, titleLabel, subtitleLabel);
        leftBox.setAlignment(Pos.TOP_LEFT);

        HBox badgeRow = new HBox(12, platformHealthBadge, refreshButton);
        badgeRow.setAlignment(Pos.CENTER_RIGHT);

        VBox rightBox = new VBox(10, badgeRow, lastCheckedLabel);
        rightBox.setAlignment(Pos.TOP_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(18, leftBox, spacer, rightBox);
        topRow.setAlignment(Pos.TOP_LEFT);

        root.getChildren().addAll(topRow, messageLabel);
    }

    private void configureSummaryCards() {
        HBox row = new HBox(
                16,
                createMetricCard("Connected Services", connectedServicesValue, connectedServicesNote),
                createMetricCard("Properties", totalPropertiesValue, propertiesNote),
                createMetricCard("Maintenance Requests", totalRequestsValue, requestsNote),
                createMetricCard("Unread Notifications", unreadNotificationsValue, unreadNote),
                createMetricCard("Active Role", activeRoleValue, roleNote),
                createMetricCard("Session", sessionStateValue, sessionNote)
        );
        row.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().add(row);
    }

    private VBox createMetricCard(String title, Label valueLabel, Label noteLabel) {
        Label titleLabel = new Label(title);
        titleLabel.setWrapText(true);
        titleLabel.setStyle(
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

        noteLabel.setWrapText(true);
        noteLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #64748b;"
        );

        VBox card = new VBox(8, titleLabel, valueLabel, noteLabel);
        card.setPadding(new Insets(18));
        card.setPrefWidth(190);
        card.setMinHeight(112);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle(METRIC_CARD_STYLE);

        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private void configureServiceGrid() {
        Label gridTitle = new Label("Service Health");
        gridTitle.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        grid.add(createServiceCard("API Gateway", gatewayStatusValue, gatewayNoteValue), 0, 0);
        grid.add(createServiceCard("Property Service", propertyStatusValue, propertyNoteValue), 1, 0);
        grid.add(createServiceCard("Maintenance Service", maintenanceStatusValue, maintenanceNoteValue), 2, 0);
        grid.add(createServiceCard("Notification Service", notificationStatusValue, notificationNoteValue), 0, 1);
        grid.add(createServiceCard("Authentication Session", authStatusValue, authNoteValue), 1, 1);

        VBox wrapper = new VBox(14, gridTitle, grid);
        root.getChildren().add(wrapper);
    }

    private VBox createServiceCard(String title, Label statusValue, Label noteValue) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );

        statusValue.setStyle(badgeStyle("#e2e8f0", "#334155"));

        noteValue.setWrapText(true);
        noteValue.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #64748b;"
        );

        Label technicalNote = new Label("Last evaluated using live platform calls.");
        technicalNote.setWrapText(true);
        technicalNote.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #94a3b8;"
        );

        VBox card = new VBox(12, titleLabel, statusValue, noteValue, technicalNote);
        card.setPadding(new Insets(18));
        card.setPrefWidth(320);
        card.setMinHeight(150);
        card.setStyle(CARD_STYLE);

        return card;
    }

    private void configureBottomSection() {
        VBox insightsCard = createInsightsCard();
        VBox actionsCard = createQuickActionsCard();
        VBox edgeCard = createEdgeDeviceCard();

        HBox topBottomRow = new HBox(16, insightsCard, actionsCard);
        topBottomRow.setAlignment(Pos.TOP_LEFT);

        HBox.setHgrow(insightsCard, Priority.ALWAYS);
        HBox.setHgrow(actionsCard, Priority.ALWAYS);

        HBox edgeRow = new HBox(edgeCard);
        edgeRow.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(edgeCard, Priority.ALWAYS);

        root.getChildren().addAll(topBottomRow, edgeRow);
    }
    
    private VBox createEdgeDeviceCard() {
        Label edgeTitle = new Label("Edge Device Telemetry");
        edgeTitle.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );

        VBox panel = new VBox(
                12,
                createInsightRow("Device ID", edgeDeviceIdValue),
                createInsightRow("Property ID", edgePropertyIdValue),
                createInsightRow("Status", edgeStatusValue),
                createInsightRow("Temperature", edgeTemperatureValue),
                createInsightRow("Humidity", edgeHumidityValue),
                createInsightRow("Last Heartbeat", edgeTimestampValue)
        );
        panel.setPadding(new Insets(20));
        panel.setStyle(CARD_STYLE);

        return new VBox(14, edgeTitle, panel);
    }

    private VBox createInsightsCard() {
        Label insightsTitle = new Label("Operational Insights");
        insightsTitle.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );

        VBox panel = new VBox(
                12,
                createInsightRow("Gateway URL", gatewayUrlValue),
                createInsightRow("Current User", currentUserValue),
                createInsightRow("User ID", currentUserIdValue),
                createInsightRow("Role", currentRoleValue),
                createInsightRow("Environment", environmentValue),
                createInsightRow("Notification Polling", pollingValue),
                createInsightRow("Operational Score", operationalScoreValue),
                createInsightRow("Operational Note", insightValue)
        );
        panel.setPadding(new Insets(20));
        panel.setStyle(CARD_STYLE);

        return new VBox(14, insightsTitle, panel);
    }

    private VBox createQuickActionsCard() {
        Label actionsTitle = new Label("Quick Actions");
        actionsTitle.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );

        styleSecondaryButton(openNotificationsButton);
        styleSecondaryButton(openReportsButton);
        styleSecondaryButton(openMaintenanceButton);

        openNotificationsButton.setPrefWidth(220);
        openReportsButton.setPrefWidth(220);
        openMaintenanceButton.setPrefWidth(220);

        openNotificationsButton.setOnAction(e -> onOpenNotifications.run());
        openReportsButton.setOnAction(e -> onOpenReports.run());
        openMaintenanceButton.setOnAction(e -> onOpenMaintenance.run());

        Label helperNote = new Label(
                "Use these shortcuts to move quickly between operational pages."
        );
        helperNote.setWrapText(true);
        helperNote.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #64748b;"
        );

        VBox buttonsBox = new VBox(10,
                openNotificationsButton,
                openReportsButton,
                openMaintenanceButton,
                helperNote
        );
        buttonsBox.setPadding(new Insets(20));
        buttonsBox.setStyle(CARD_STYLE);

        return new VBox(14, actionsTitle, buttonsBox);
    }

    private HBox createInsightRow(String labelText, Label valueLabel) {
        Label label = new Label(labelText);
        label.setPrefWidth(150);
        label.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #475569;"
        );

        valueLabel.setWrapText(true);
        valueLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #0f172a;"
        );

        HBox row = new HBox(12, label, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void loadSystemStatus() {
        hideMessage();

        int healthyCount = 0;
        int totalServiceChecks = 5;

        currentUserValue.setText(safeValue(SessionManager.getEmail(), "No active user"));
        currentUserIdValue.setText(safeValue(SessionManager.getUserId(), "Unavailable"));
        currentRoleValue.setText(formatRole(SessionManager.getRole()));
        sessionStateValue.setText(SessionManager.isLoggedIn() ? "Active" : "Unavailable");
        pollingValue.setText("Active");

        if (SessionManager.isLoggedIn()) {
            authStatusValue.setText("Healthy");
            authStatusValue.setStyle(badgeStyle("#dcfce7", "#166534"));
            authNoteValue.setText("JWT token is present and session is active.");
            healthyCount++;
        } else {
            authStatusValue.setText("Unavailable");
            authStatusValue.setStyle(badgeStyle("#fee2e2", "#991b1b"));
            authNoteValue.setText("No active session token found.");
        }

        try {
            List<Property> properties = propertyApiService.getProperties();
            totalPropertiesValue.setText(String.valueOf(properties.size()));
            propertyStatusValue.setText("Healthy");
            propertyStatusValue.setStyle(badgeStyle("#dcfce7", "#166534"));
            propertyNoteValue.setText("Property data loaded successfully.");
            healthyCount++;
        } catch (RuntimeException ex) {
            totalPropertiesValue.setText("-");
            propertyStatusValue.setText("Issue");
            propertyStatusValue.setStyle(badgeStyle("#fee2e2", "#991b1b"));
            propertyNoteValue.setText("Unable to load property data.");
        }

        try {
            List<MaintenanceRequest> requests = maintenanceApiService.getRequests(null, null);
            totalRequestsValue.setText(String.valueOf(requests.size()));
            maintenanceStatusValue.setText("Healthy");
            maintenanceStatusValue.setStyle(badgeStyle("#dcfce7", "#166534"));
            maintenanceNoteValue.setText("Maintenance workflows are reachable.");
            healthyCount++;
        } catch (RuntimeException ex) {
            totalRequestsValue.setText("-");
            maintenanceStatusValue.setText("Issue");
            maintenanceStatusValue.setStyle(badgeStyle("#fee2e2", "#991b1b"));
            maintenanceNoteValue.setText("Unable to load maintenance request data.");
        }

        try {
            List<NotificationItem> notifications = notificationApiService.getNotifications(0, 10);
            long unread = notifications.stream()
                    .filter(item -> !Boolean.TRUE.equals(item.getIsRead()))
                    .count();

            unreadNotificationsValue.setText(String.valueOf(unread));
            notificationStatusValue.setText("Healthy");
            notificationStatusValue.setStyle(badgeStyle("#dcfce7", "#166534"));
            notificationNoteValue.setText("Notification feed is reachable and returning data.");
            healthyCount++;
        } catch (RuntimeException ex) {
            unreadNotificationsValue.setText("-");
            notificationStatusValue.setText("Issue");
            notificationStatusValue.setStyle(badgeStyle("#fee2e2", "#991b1b"));
            notificationNoteValue.setText("Unable to load notifications.");
        }

        if (healthyCount >= 3) {
            gatewayStatusValue.setText("Healthy");
            gatewayStatusValue.setStyle(badgeStyle("#dcfce7", "#166534"));
            gatewayNoteValue.setText("Gateway routing appears operational based on downstream service responses.");
            healthyCount++;
        } else {
            gatewayStatusValue.setText("Degraded");
            gatewayStatusValue.setStyle(badgeStyle("#fef3c7", "#92400e"));
            gatewayNoteValue.setText("Gateway may be partially degraded because multiple downstream checks failed.");
        }

        connectedServicesValue.setText(healthyCount + " / " + totalServiceChecks);
        activeRoleValue.setText(formatRole(SessionManager.getRole()));
        operationalScoreValue.setText((healthyCount * 100 / totalServiceChecks) + "%");
        lastCheckedLabel.setText("Last checked: " + LocalDateTime.now().format(TIME_FORMAT));
        
        try {
            EdgeHeartbeat heartbeat = maintenanceApiService.getLatestHeartbeat();

            if (heartbeat != null && heartbeat.getDeviceId() != null && !heartbeat.getDeviceId().isBlank()) {
                edgeDeviceIdValue.setText(heartbeat.getDeviceId());
                edgePropertyIdValue.setText(safeValue(heartbeat.getPropertyId(), "-"));
                edgeStatusValue.setText(safeValue(heartbeat.getStatus(), "-"));
                edgeTemperatureValue.setText(String.format("%.2f °C", heartbeat.getTemperature()));
                edgeHumidityValue.setText(String.format("%.2f %%", heartbeat.getHumidity()));
                edgeTimestampValue.setText(formatHeartbeatTimestamp(heartbeat.getTimestamp()));
            } else {
                edgeDeviceIdValue.setText("No heartbeat yet");
                edgePropertyIdValue.setText("-");
                edgeStatusValue.setText("-");
                edgeTemperatureValue.setText("-");
                edgeHumidityValue.setText("-");
                edgeTimestampValue.setText("-");
            }
        } catch (RuntimeException ex) {
            edgeDeviceIdValue.setText("Unavailable");
            edgePropertyIdValue.setText("-");
            edgeStatusValue.setText("Unavailable");
            edgeTemperatureValue.setText("-");
            edgeHumidityValue.setText("-");
            edgeTimestampValue.setText("Unable to load edge heartbeat");
        }

        if (healthyCount == totalServiceChecks) {
            platformHealthBadge.setText("Platform Healthy");
            platformHealthBadge.setStyle(badgeStyle("#dcfce7", "#166534"));
            insightValue.setText("All major platform checks passed successfully. The platform is operating normally.");
            showMessage("System status loaded successfully.", false);
        } else if (healthyCount >= 3) {
            platformHealthBadge.setText("Partially Degraded");
            platformHealthBadge.setStyle(badgeStyle("#fef3c7", "#92400e"));
            insightValue.setText("Core platform is partly available, but one or more integrations need attention.");
            showMessage("System status loaded with warnings.", false);
        } else {
            platformHealthBadge.setText("Service Issues Detected");
            platformHealthBadge.setStyle(badgeStyle("#fee2e2", "#991b1b"));
            insightValue.setText("Multiple platform checks failed. Investigate service connectivity and authentication.");
            showMessage("System status detected service issues.", true);
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
        messageLabel.setText("");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String formatRole(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        String formatted = value.replace("_", " ").toLowerCase();
        return Character.toUpperCase(formatted.charAt(0)) + formatted.substring(1);
    }

    private String badgeStyle(String background, String text) {
        return "-fx-background-color: " + background + ";" +
               "-fx-text-fill: " + text + ";" +
               "-fx-font-size: 12px;" +
               "-fx-font-weight: bold;" +
               "-fx-padding: 7 14 7 14;" +
               "-fx-background-radius: 18;";
    }

    private void stylePrimaryButton(Button button) {
        button.setStyle(
                "-fx-background-color: #2563eb;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 10 18 10 18;" +
                "-fx-cursor: hand;"
        );
    }

    private void styleSecondaryButton(Button button) {
        button.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #0f172a;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #d1d5db;" +
                "-fx-border-radius: 12;" +
                "-fx-padding: 10 18 10 18;" +
                "-fx-cursor: hand;"
        );
    }

    public Parent getView() {
        return root;
    }
}
