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
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class ReportsView {

    private final VBox root = new VBox(20);

    private final Label titleLabel = new Label("Reports");
    private final Label subtitleLabel = new Label("Management reporting and operational insights for the platform.");
    private final Label messageLabel = new Label();

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

    private void configureDashboard() {
        VBox totalPropertiesCard = createCard("🏠 Total Properties", totalPropertiesValue);
        VBox propertyTypesCard = createCard("📂 Property Types", propertyTypesValue);
        VBox totalRequestsCard = createCard("📊 Total Requests", totalRequestsValue);
        VBox unreadNotificationsCard = createCard("🔔 Unread Notifications", unreadNotificationsValue);

        VBox openRequestsCard = createCard("🔴 Open Requests", openRequestsValue);
        VBox inProgressCard = createCard("🟡 In Progress", inProgressRequestsValue);
        VBox completedCard = createCard("🟢 Completed", completedRequestsValue);
        VBox latestNotificationCard = createCard("📝 Latest Notification", latestNotificationValue);

        HBox topCards = new HBox(16,
                totalPropertiesCard,
                propertyTypesCard,
                totalRequestsCard,
                unreadNotificationsCard
        );
        topCards.setAlignment(Pos.CENTER_LEFT);

        HBox bottomCards = new HBox(16,
                openRequestsCard,
                inProgressCard,
                completedCard,
                latestNotificationCard
        );
        bottomCards.setAlignment(Pos.CENTER_LEFT);

        VBox dashboardCard = new VBox(20, topCards, bottomCards);
        dashboardCard.setPadding(new Insets(20));
        dashboardCard.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 18;" +
                "-fx-border-width: 1;"
        );

        root.getChildren().add(dashboardCard);
        VBox.setVgrow(dashboardCard, Priority.ALWAYS);
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
            	latestNotificationValue.setText(notifications.get(0).getType());
            }

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

            showMessage("Failed to load report data.", true);
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

    public Parent getView() {
        return root;
    }
    
    private VBox createCard(String title, Label valueLabel) {

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        valueLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        valueLabel.setWrapText(true);

        VBox card = new VBox(8, titleLabel, valueLabel);
        card.setPadding(new Insets(16));
        card.setPrefWidth(220);
        card.setMinHeight(120);
        HBox.setHgrow(card, Priority.ALWAYS);

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 14;" +
                "-fx-border-width: 1;"
        );

        return card;
    }
}