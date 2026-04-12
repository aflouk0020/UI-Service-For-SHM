package com.sigma.smarthome.ui.view;

import com.sigma.smarthome.ui.model.NotificationItem;
import com.sigma.smarthome.ui.service.NotificationPollingService;
import com.sigma.smarthome.ui.util.SessionManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

import com.sigma.smarthome.ui.model.MaintenanceRequest;
import com.sigma.smarthome.ui.model.NotificationItem;
import com.sigma.smarthome.ui.model.Property;
import com.sigma.smarthome.ui.service.MaintenanceApiService;
import com.sigma.smarthome.ui.service.NotificationApiService;
import com.sigma.smarthome.ui.service.PropertyApiService;
import javafx.scene.Parent;

public class DashboardView {

    private static final String ROLE_PROPERTY_MANAGER = "PROPERTY_MANAGER";
    private static final String ROLE_MAINTENANCE_STAFF = "MAINTENANCE_STAFF";

    private final BorderPane root = new BorderPane();
    private final StackPane contentArea = new StackPane();
    private final StackPane centerStack = new StackPane();
    private final VBox toastContainer = new VBox(12);

    private final Button dashboardButton = new Button("Dashboard");
    private final Button userManagementButton = new Button("User Management");
    private final Button propertyManagementButton = new Button("Property Management");
    private final Button maintenanceRequestsButton = new Button("Maintenance Requests");
    private final Button systemStatusButton = new Button("System Status");
    private final Button notificationsButton = new Button("Notifications");
    private final Button reportsButton = new Button("Reports");
    private final StackPane notificationsButtonStack = new StackPane();
    private final Label notificationsSidebarBadgeLabel = new Label();
    private final ScrollPane contentScrollPane = new ScrollPane();
    
    private final PropertyApiService propertyApiService = new PropertyApiService();
    private final MaintenanceApiService maintenanceApiService = new MaintenanceApiService();
    private final NotificationApiService notificationApiService = new NotificationApiService();
    
    private final StackPane notificationBellStack = new StackPane();
    private final Button notificationBellButton = new Button("🔔");
    private final Label unreadBadgeLabel = new Label();

    private final NotificationPollingService notificationPollingService = new NotificationPollingService();

    private final String role;

    public DashboardView(String email, String role, Runnable onLogout) {
        this.role = role == null ? "" : role.trim();

        configureRoot();
        configureNotificationsSidebarBadge();
        configureTopBar(email, this.role, () -> {
            notificationPollingService.stop();
            onLogout.run();
        });
        configureSidebar();
        configureContentArea();
        wireNavigation();
        configureNotificationPolling();
        showDashboardHome();
    }

    private void configureRoot() {
        root.setStyle("-fx-background-color: #f8fafc;");
    }

    private void configureNotificationPolling() {
        notificationPollingService.setOnNewNotification(this::showNotificationToast);
        notificationPollingService.setOnUnreadCountChanged(this::updateUnreadBadge);
        notificationPollingService.start();
    }

    private void configureTopBar(String email, String role, Runnable onLogout) {
        Label title = new Label("Smart Home Maintenance Platform");
        title.setStyle(
                "-fx-font-size: 24px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );

        Label subtitle = new Label("Secure, role-aware smart home operations");
        subtitle.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #64748b;"
        );

        VBox titleBox = new VBox(4, title, subtitle);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label emailLabel = new Label(email);
        emailLabel.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #334155;"
        );

        String userId = SessionManager.getUserId();

        Label userIdLabel = new Label(
                userId.isBlank() ? "User ID unavailable" : "ID: " + shortenUserId(userId)
        );
        userIdLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        Button copyIdButton = new Button("Copy ID");
        copyIdButton.setStyle(
                "-fx-background-color: #f1f5f9;" +
                "-fx-text-fill: #0f172a;" +
                "-fx-font-size: 11px;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 4 10 4 10;"
        );
        copyIdButton.setDisable(userId.isBlank());

        copyIdButton.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(userId);
            Clipboard.getSystemClipboard().setContent(content);
        });

        notificationBellButton.setStyle(
                "-fx-background-color: #f8fafc;" +
                "-fx-text-fill: #0f172a;" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 14;" +
                "-fx-padding: 8 12 8 12;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 14;" +
                "-fx-cursor: hand;"
        );

        notificationBellButton.setOnAction(e -> {
            setActiveButton(notificationsButton);
            contentArea.getChildren().setAll(new NotificationsView().getView());
        });

        unreadBadgeLabel.setStyle(
                "-fx-background-color: #ef4444;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 10px;" +
                "-fx-font-weight: bold;" +
                "-fx-min-width: 18;" +
                "-fx-min-height: 18;" +
                "-fx-alignment: center;" +
                "-fx-padding: 1 5 1 5;" +
                "-fx-background-radius: 20;"
        );
        unreadBadgeLabel.setVisible(false);
        unreadBadgeLabel.setManaged(false);

        notificationBellStack.getChildren().addAll(notificationBellButton, unreadBadgeLabel);
        StackPane.setAlignment(unreadBadgeLabel, Pos.TOP_RIGHT);
        StackPane.setMargin(unreadBadgeLabel, new Insets(-4, -4, 0, 0));

        Label roleLabel = new Label(formatRole(role));
        roleLabel.setStyle(
                "-fx-background-color: #dbeafe;" +
                "-fx-text-fill: #1d4ed8;" +
                "-fx-padding: 7 14 7 14;" +
                "-fx-background-radius: 18;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;"
        );

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle(
                "-fx-background-color: #ef4444;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 10 18 10 18;" +
                "-fx-cursor: hand;"
        );
        logoutButton.setOnAction(e -> onLogout.run());

        HBox idRow = new HBox(8, userIdLabel, copyIdButton);
        idRow.setAlignment(Pos.CENTER_RIGHT);

        VBox userTextBox = new VBox(6, emailLabel, idRow, roleLabel);
        userTextBox.setAlignment(Pos.CENTER_RIGHT);

        HBox rightBox = new HBox(14, notificationBellStack, userTextBox, logoutButton);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        BorderPane topBar = new BorderPane();
        topBar.setPadding(new Insets(22, 26, 22, 26));
        topBar.setLeft(titleBox);
        topBar.setRight(rightBox);
        topBar.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-width: 0 0 1 0;"
        );

        root.setTop(topBar);
    }

    private void updateUnreadBadge(int unreadCount) {
        if (unreadCount <= 0) {
            unreadBadgeLabel.setVisible(false);
            unreadBadgeLabel.setManaged(false);
            unreadBadgeLabel.setText("");

            notificationsSidebarBadgeLabel.setVisible(false);
            notificationsSidebarBadgeLabel.setManaged(false);
            notificationsSidebarBadgeLabel.setText("");
            return;
        }

        String badgeText = unreadCount > 99 ? "99+" : String.valueOf(unreadCount);

        unreadBadgeLabel.setText(badgeText);
        unreadBadgeLabel.setVisible(true);
        unreadBadgeLabel.setManaged(true);

        notificationsSidebarBadgeLabel.setText(badgeText);
        notificationsSidebarBadgeLabel.setVisible(true);
        notificationsSidebarBadgeLabel.setManaged(true);
    }
    
    private void configureNotificationsSidebarBadge() {
    	notificationsSidebarBadgeLabel.setStyle(
    	        "-fx-background-color: #ef4444;" +
    	        "-fx-text-fill: white;" +
    	        "-fx-font-size: 10px;" +
    	        "-fx-font-weight: bold;" +
    	        "-fx-min-width: 18;" +
    	        "-fx-min-height: 18;" +
    	        "-fx-alignment: center;" +
    	        "-fx-padding: 1 5 1 5;" +
    	        "-fx-background-radius: 20;"
    	);
        notificationsSidebarBadgeLabel.setVisible(false);
        notificationsSidebarBadgeLabel.setManaged(false);

        notificationsButtonStack.getChildren().addAll(notificationsButton, notificationsSidebarBadgeLabel);

        StackPane.setAlignment(notificationsButton, Pos.CENTER_LEFT);
        StackPane.setAlignment(notificationsSidebarBadgeLabel, Pos.TOP_RIGHT);
        StackPane.setMargin(notificationsSidebarBadgeLabel, new Insets(4, 8, 0, 0));

        notificationsButtonStack.setMaxWidth(Double.MAX_VALUE);
    }

    private String shortenUserId(String userId) {
        if (userId == null || userId.length() < 12) {
            return userId == null ? "" : userId;
        }
        return userId.substring(0, 8) + "..." + userId.substring(userId.length() - 4);
    }

    private void configureSidebar() {
        VBox sidebar = new VBox(12);
        sidebar.setPadding(new Insets(24));
        sidebar.setPrefWidth(270);
        sidebar.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-width: 0 1 0 0;"
        );

        Label navTitle = new Label("Navigation");
        navTitle.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #94a3b8;"
        );

        sidebar.getChildren().add(navTitle);

        addSidebarButton(sidebar, dashboardButton);

        if (isPropertyManager()) {
            addSidebarButton(sidebar, userManagementButton);
            addSidebarButton(sidebar, propertyManagementButton);
            addSidebarButton(sidebar, maintenanceRequestsButton);
            addSidebarButton(sidebar, systemStatusButton);
            addSidebarButtonStack(sidebar, notificationsButtonStack);
            addSidebarButton(sidebar, reportsButton);
        } else if (isMaintenanceStaff()) {
            addSidebarButton(sidebar, propertyManagementButton);
            addSidebarButton(sidebar, maintenanceRequestsButton);
            addSidebarButtonStack(sidebar, notificationsButtonStack);
        } else {
            addSidebarButton(sidebar, propertyManagementButton);
            addSidebarButton(sidebar, maintenanceRequestsButton);
        }

        root.setLeft(sidebar);
    }

    private void addSidebarButton(VBox sidebar, Button button) {
        styleSidebarButton(button);
        sidebar.getChildren().add(button);
    }

    private void configureContentArea() {
        contentArea.setPadding(new Insets(28));

        contentScrollPane.setContent(contentArea);
        contentScrollPane.setFitToWidth(true);
        contentScrollPane.setFitToHeight(false);
        contentScrollPane.setPannable(true);
        contentScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentScrollPane.setStyle(
                "-fx-background: transparent;" +
                "-fx-background-color: transparent;"
        );

        toastContainer.setMaxWidth(360);
        toastContainer.setAlignment(Pos.TOP_RIGHT);
        toastContainer.setPadding(new Insets(24));
        toastContainer.setMouseTransparent(true);
        toastContainer.setPickOnBounds(false);

        centerStack.getChildren().addAll(contentScrollPane, toastContainer);
        StackPane.setAlignment(toastContainer, Pos.TOP_RIGHT);

        root.setCenter(centerStack);
    }

    private void wireNavigation() {
        dashboardButton.setOnAction(e -> {
            setActiveButton(dashboardButton);
            showDashboardHome();
        });

        userManagementButton.setOnAction(e -> {
            setActiveButton(userManagementButton);
            contentArea.getChildren().setAll(new UserManagementView().getView());
        });

        propertyManagementButton.setOnAction(e -> {
            setActiveButton(propertyManagementButton);
            contentArea.getChildren().setAll(new PropertyView().getView());
        });

        maintenanceRequestsButton.setOnAction(e -> {
            setActiveButton(maintenanceRequestsButton);
            contentArea.getChildren().setAll(new MaintenanceRequestsView().getView());
        });

        systemStatusButton.setOnAction(e -> {
            setActiveButton(systemStatusButton);
            contentArea.getChildren().setAll(
                    new SystemStatusView(
                            () -> {
                                setActiveButton(notificationsButton);
                                contentArea.getChildren().setAll(new NotificationsView().getView());
                            },
                            () -> {
                                setActiveButton(reportsButton);
                                contentArea.getChildren().setAll(new ReportsView().getView());
                            },
                            () -> {
                                setActiveButton(maintenanceRequestsButton);
                                contentArea.getChildren().setAll(new MaintenanceRequestsView().getView());
                            }
                    ).getView()
            );
        });

        notificationsButton.setOnAction(e -> {
            setActiveButton(notificationsButton);
            contentArea.getChildren().setAll(new NotificationsView().getView());
        });

        reportsButton.setOnAction(e -> {
            setActiveButton(reportsButton);
            contentArea.getChildren().setAll(new ReportsView().getView());
        });
    }
    
    private void addSidebarButtonStack(VBox sidebar, StackPane buttonStack) {
        buttonStack.setMaxWidth(Double.MAX_VALUE);
        sidebar.getChildren().add(buttonStack);
    }

    private void showNotificationToast(NotificationItem item) {
        NotificationToast toast = new NotificationToast(item);
        toast.setMouseTransparent(false);
        toast.setOnMouseClicked(event -> {
            setActiveButton(notificationsButton);
            contentArea.getChildren().setAll(new NotificationsView().getView());

            toast.playHideAnimation(() -> toastContainer.getChildren().remove(toast));
        });

        toastContainer.getChildren().add(0, toast);
        toast.playShowAnimation();

        playBellAnimation();

        toast.autoDismiss(Duration.seconds(5), () ->
                Platform.runLater(() -> toastContainer.getChildren().remove(toast))
        );

        while (toastContainer.getChildren().size() > 3) {
            toastContainer.getChildren().remove(toastContainer.getChildren().size() - 1);
        }
    }
    
    private void playBellAnimation() {
        javafx.animation.ScaleTransition scaleUp =
                new javafx.animation.ScaleTransition(Duration.millis(120), notificationBellButton);
        scaleUp.setToX(1.18);
        scaleUp.setToY(1.18);

        javafx.animation.ScaleTransition scaleDown =
                new javafx.animation.ScaleTransition(Duration.millis(120), notificationBellButton);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        javafx.animation.SequentialTransition sequence =
                new javafx.animation.SequentialTransition(scaleUp, scaleDown);

        sequence.play();
    }

    private void showDashboardHome() {
        setActiveButton(dashboardButton);

        Label pageTitle = new Label("Dashboard");
        pageTitle.setStyle(
                "-fx-font-size: 34px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );

        Label pageSubtitle = new Label("Live overview of your platform activity, workload, and notifications.");
        pageSubtitle.setWrapText(true);
        pageSubtitle.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-text-fill: #64748b;"
        );

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_LEFT);

        try {
            List<Property> properties = propertyApiService.getProperties();
            List<MaintenanceRequest> allRequests = isMaintenanceStaff()
                    ? maintenanceApiService.getAssignedRequestsForStaff()
                    : maintenanceApiService.getRequests(null, null);
            List<NotificationItem> notifications = notificationApiService.getNotifications(0, 10);

            long unreadNotifications = notifications.stream()
                    .filter(item -> !Boolean.TRUE.equals(item.getIsRead()))
                    .count();

            long openRequests = allRequests.stream()
                    .filter(request -> "OPEN".equalsIgnoreCase(request.getStatus()))
                    .count();

            long inProgressRequests = allRequests.stream()
                    .filter(request -> "IN_PROGRESS".equalsIgnoreCase(request.getStatus()))
                    .count();

            long completedRequests = allRequests.stream()
                    .filter(request -> "COMPLETED".equalsIgnoreCase(request.getStatus()))
                    .count();

            HBox metricsRow = new HBox(16);
            metricsRow.setAlignment(Pos.CENTER_LEFT);

            if (isPropertyManager()) {
                metricsRow.getChildren().addAll(
                        createSummaryCard("Properties", String.valueOf(properties.size()), "Registered properties in platform"),
                        createSummaryCard("Open Requests", String.valueOf(openRequests), "Requests awaiting action"),
                        createSummaryCard("In Progress", String.valueOf(inProgressRequests), "Requests currently being handled"),
                        createSummaryCard("Unread Notifications", String.valueOf(unreadNotifications), "Notifications awaiting review")
                );
            } else {
                metricsRow.getChildren().addAll(
                        createSummaryCard("Assigned Tasks", String.valueOf(allRequests.size()), "Requests assigned to you"),
                        createSummaryCard("Open Tasks", String.valueOf(openRequests), "Tasks ready to begin"),
                        createSummaryCard("In Progress", String.valueOf(inProgressRequests), "Tasks you are actively handling"),
                        createSummaryCard("Unread Notifications", String.valueOf(unreadNotifications), "Notifications awaiting review")
                );
            }

            VBox attentionCard = createDashboardSectionCard(
                    "Needs Attention",
                    buildNeedsAttentionContent(openRequests, inProgressRequests, unreadNotifications, completedRequests)
            );

            VBox quickActionsCard = createDashboardSectionCard(
                    "Quick Actions",
                    buildQuickActionsContent()
            );

            HBox actionRow = new HBox(16, attentionCard, quickActionsCard);
            actionRow.setAlignment(Pos.TOP_LEFT);
            HBox.setHgrow(attentionCard, Priority.ALWAYS);
            HBox.setHgrow(quickActionsCard, Priority.ALWAYS);

            VBox recentActivityCard = createDashboardSectionCard(
                    "Recent Activity",
                    buildRecentActivityContent(notifications, allRequests)
            );

            content.getChildren().addAll(
                    pageTitle,
                    pageSubtitle,
                    metricsRow,
                    actionRow,
                    recentActivityCard
            );

        } catch (RuntimeException ex) {
            VBox fallbackCard = new VBox(12);
            fallbackCard.setPadding(new Insets(22));
            fallbackCard.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 18;" +
                    "-fx-border-color: #e2e8f0;" +
                    "-fx-border-radius: 18;" +
                    "-fx-border-width: 1;"
            );

            Label errorTitle = new Label("Dashboard data unavailable");
            errorTitle.setStyle(
                    "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #0f172a;"
            );

            Label errorBody = new Label(
                    "The dashboard could not load live platform information right now. Please refresh or check service connectivity."
            );
            errorBody.setWrapText(true);
            errorBody.setStyle(
                    "-fx-font-size: 14px;" +
                    "-fx-text-fill: #64748b;"
            );

            fallbackCard.getChildren().addAll(errorTitle, errorBody);

            content.getChildren().addAll(pageTitle, pageSubtitle, fallbackCard);
        }

        contentArea.getChildren().setAll(content);
    }
    
    private VBox createDashboardSectionCard(String title, Parent body) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );

        VBox card = new VBox(14, titleLabel, body);
        card.setPadding(new Insets(22));
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 18;" +
                "-fx-border-width: 1;"
        );
        card.setMaxWidth(Double.MAX_VALUE);

        return card;
    }
    
    private Parent buildNeedsAttentionContent(long openRequests,
            long inProgressRequests,
            long unreadNotifications,
            long completedRequests) {
			VBox box = new VBox(10);
			box.setAlignment(Pos.TOP_LEFT);
			
			box.getChildren().addAll(
			createAttentionLine("Open requests needing review: " + openRequests),
			createAttentionLine("Requests currently in progress: " + inProgressRequests),
			createAttentionLine("Unread notifications: " + unreadNotifications),
			createAttentionLine("Completed requests recorded: " + completedRequests)
			);

	return box;
	}
    
    private Label createAttentionLine(String text) {
        Label label = new Label("• " + text);
        label.setWrapText(true);
        label.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #475569;"
        );
        return label;
    }

    private Parent buildQuickActionsContent() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.TOP_LEFT);

        Button openMaintenanceButton = new Button("Open Maintenance Requests");
        Button openNotificationsButton = new Button("Open Notifications");
        Button openReportsButton = new Button("Open Reports");

        styleActionButton(openMaintenanceButton);
        styleActionButton(openNotificationsButton);
        styleActionButton(openReportsButton);

        openMaintenanceButton.setOnAction(e -> {
            setActiveButton(maintenanceRequestsButton);
            contentArea.getChildren().setAll(new MaintenanceRequestsView().getView());
        });

        openNotificationsButton.setOnAction(e -> {
            setActiveButton(notificationsButton);
            contentArea.getChildren().setAll(new NotificationsView().getView());
        });

        openReportsButton.setOnAction(e -> {
            setActiveButton(reportsButton);
            contentArea.getChildren().setAll(new ReportsView().getView());
        });

        if (isMaintenanceStaff()) {
            openReportsButton.setDisable(true);
            openReportsButton.setOpacity(0.6);
        }

        box.getChildren().addAll(
                openMaintenanceButton,
                openNotificationsButton,
                openReportsButton
        );

        return box;
    }
    private void styleActionButton(Button button) {
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(42);
        button.setStyle(
                "-fx-background-color: #f8fafc;" +
                "-fx-text-fill: #0f172a;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #d1d5db;" +
                "-fx-border-radius: 12;" +
                "-fx-padding: 10 16 10 16;" +
                "-fx-cursor: hand;"
        );
    }
    private Parent buildRecentActivityContent(List<NotificationItem> notifications,
            List<MaintenanceRequest> requests) {
			VBox box = new VBox(10);
			box.setAlignment(Pos.TOP_LEFT);
			
			int maxNotifications = Math.min(3, notifications.size());
			for (int i = 0; i < maxNotifications; i++) {
			NotificationItem item = notifications.get(i);
			Label label = new Label(
			"Notification: " + safeText(item.getTitle()) + " • " + safeText(item.getType())
			);
			label.setWrapText(true);
			label.setStyle(
			"-fx-font-size: 14px;" +
			"-fx-text-fill: #475569;"
			);
			box.getChildren().add(label);
			}
			
			int maxRequests = Math.min(2, requests.size());
			for (int i = 0; i < maxRequests; i++) {
			MaintenanceRequest request = requests.get(i);
			Label label = new Label(
			"Request: " + safeText(request.getDescription()) + " • " + safeText(request.getStatus())
			);
			label.setWrapText(true);
			label.setStyle(
			"-fx-font-size: 14px;" +
			"-fx-text-fill: #475569;"
			);
			box.getChildren().add(label);
			}
			
			if (box.getChildren().isEmpty()) {
			Label empty = new Label("No recent activity available.");
			empty.setStyle(
			"-fx-font-size: 14px;" +
			"-fx-text-fill: #64748b;"
			);
			box.getChildren().add(empty);
			}
			
			return box;
			}
    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
    private VBox createSummaryCard(String title, String value, String note) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #94a3b8;"
        );

        Label valueLabel = new Label(value);
        valueLabel.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );

        Label noteLabel = new Label(note);
        noteLabel.setWrapText(true);
        noteLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #64748b;"
        );

        VBox card = new VBox(8, titleLabel, valueLabel, noteLabel);
        card.setPadding(new Insets(18));
        card.setPrefWidth(220);
        card.setMinHeight(120);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 18;" +
                "-fx-border-width: 1;"
        );

        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private String getDashboardSubtitle() {
        if (isPropertyManager()) {
            return "Welcome. You can manage properties, review maintenance requests, and monitor platform activity.";
        }
        if (isMaintenanceStaff()) {
            return "Welcome. You can view assigned platform information, manage maintenance tasks, and check notifications.";
        }
        return "Welcome to the Smart Home Maintenance Platform dashboard.";
    }

    private String getOverviewBody() {
        if (isPropertyManager()) {
            return "Your dashboard is now connected to the main platform workflows. You can manage properties, create and assign maintenance requests, monitor notifications, and review summary reports.";
        }
        return "Your dashboard is now connected to assigned workflow tasks. You can view assigned maintenance requests, update request status, review notifications, and access property information.";
    }

    private boolean isPropertyManager() {
        return ROLE_PROPERTY_MANAGER.equalsIgnoreCase(role);
    }

    private boolean isMaintenanceStaff() {
        return ROLE_MAINTENANCE_STAFF.equalsIgnoreCase(role);
    }

    private String formatRole(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }

        String formatted = value.replace("_", " ").toLowerCase();
        return Character.toUpperCase(formatted.charAt(0)) + formatted.substring(1);
    }

    private void setActiveButton(Button activeButton) {
        Button[] allButtons = {
                dashboardButton,
                userManagementButton,
                propertyManagementButton,
                maintenanceRequestsButton,
                systemStatusButton,
                notificationsButton,
                reportsButton
        };

        for (Button button : allButtons) {
            if (button == activeButton) {
                button.setStyle(activeSidebarButtonStyle());
            } else {
                button.setStyle(defaultSidebarButtonStyle());
            }
        }
    }

    private void styleSidebarButton(Button button) {
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPrefHeight(44);
        button.setStyle(defaultSidebarButtonStyle());
    }

    private String defaultSidebarButtonStyle() {
        return "-fx-background-color: transparent;" +
               "-fx-text-fill: #1e293b;" +
               "-fx-font-size: 15px;" +
               "-fx-font-weight: bold;" +
               "-fx-padding: 12 14 12 14;" +
               "-fx-background-radius: 12;" +
               "-fx-cursor: hand;";
    }

    private String activeSidebarButtonStyle() {
        return "-fx-background-color: #eff6ff;" +
               "-fx-text-fill: #2563eb;" +
               "-fx-font-size: 15px;" +
               "-fx-font-weight: bold;" +
               "-fx-padding: 12 14 12 14;" +
               "-fx-background-radius: 12;" +
               "-fx-border-color: #bfdbfe;" +
               "-fx-border-radius: 12;" +
               "-fx-border-width: 1;" +
               "-fx-cursor: hand;";
    }

    public Parent getView() {
        return root;
    }
}
