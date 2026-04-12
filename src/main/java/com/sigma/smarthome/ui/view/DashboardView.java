package com.sigma.smarthome.ui.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import com.sigma.smarthome.ui.view.NotificationsView;
import com.sigma.smarthome.ui.util.SessionManager;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class DashboardView {

    private static final String ROLE_PROPERTY_MANAGER = "PROPERTY_MANAGER";
    private static final String ROLE_MAINTENANCE_STAFF = "MAINTENANCE_STAFF";

    private final BorderPane root = new BorderPane();
    private final StackPane contentArea = new StackPane();

    private final Button dashboardButton = new Button("Dashboard");
    private final Button userManagementButton = new Button("User Management");
    private final Button propertyManagementButton = new Button("Property Management");
    private final Button maintenanceRequestsButton = new Button("Maintenance Requests");
    private final Button systemStatusButton = new Button("System Status");
    private final Button notificationsButton = new Button("Notifications");
    private final Button reportsButton = new Button("Reports");

    private final String role;

    public DashboardView(String email, String role, Runnable onLogout) {
        this.role = role == null ? "" : role.trim();

        configureRoot();
        configureTopBar(email, this.role, onLogout);
        configureSidebar();
        configureContentArea();
        wireNavigation();
        showDashboardHome();
    }

    private void configureRoot() {
        root.setStyle("-fx-background-color: #f8fafc;");
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

        HBox rightBox = new HBox(14, userTextBox, logoutButton);
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
            addSidebarButton(sidebar, notificationsButton);
            addSidebarButton(sidebar, reportsButton);
        } else if (isMaintenanceStaff()) {
            addSidebarButton(sidebar, propertyManagementButton);
            addSidebarButton(sidebar, maintenanceRequestsButton);
            addSidebarButton(sidebar, notificationsButton);
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
        root.setCenter(contentArea);
    }

    private void wireNavigation() {
        dashboardButton.setOnAction(e -> {
            setActiveButton(dashboardButton);
            showDashboardHome();
        });

        userManagementButton.setOnAction(e -> {
            setActiveButton(userManagementButton);
            contentArea.getChildren().setAll(new PlaceholderView(
                    "User Management",
                    "User administration tools will appear here for authorised managers."
            ).getView());
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
            contentArea.getChildren().setAll(new PlaceholderView(
                    "System Status",
                    "Platform health, service availability, and operational status will appear here."
            ).getView());
        });

        notificationsButton.setOnAction(e -> {
            setActiveButton(notificationsButton);
            contentArea.getChildren().setAll(new NotificationsView().getView());
        });

        reportsButton.setOnAction(e -> {
            setActiveButton(reportsButton);
            contentArea.getChildren().setAll(new PlaceholderView(
                    "Reports",
                    "Management reporting and operational insights will appear here."
            ).getView());
        });
    }

    private void showDashboardHome() {
        setActiveButton(dashboardButton);

        Label pageTitle = new Label("Dashboard");
        pageTitle.setStyle(
                "-fx-font-size: 34px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );

        Label pageSubtitle = new Label(getDashboardSubtitle());
        pageSubtitle.setWrapText(true);
        pageSubtitle.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-text-fill: #64748b;"
        );

        HBox cardsRow = new HBox(16);
        cardsRow.setAlignment(Pos.CENTER_LEFT);

        if (isPropertyManager()) {
            cardsRow.getChildren().addAll(
                    createSummaryCard("Role", "Property Manager", "Manager-level access enabled"),
                    createSummaryCard("Properties", "Ready", "Property module connected"),
                    createSummaryCard("Maintenance", "Pending UI", "Lifecycle screen is next"),
                    createSummaryCard("Notifications", "Pending UI", "Notification screen is next")
            );
        } else {
            cardsRow.getChildren().addAll(
                    createSummaryCard("Role", "Maintenance Staff", "Staff-level access enabled"),
                    createSummaryCard("Properties", "View Access", "Property module connected"),
                    createSummaryCard("Tasks", "Pending UI", "Maintenance task screen is next"),
                    createSummaryCard("Notifications", "Pending UI", "Notification screen is next")
            );
        }

        VBox overviewCard = new VBox(12);
        overviewCard.setPadding(new Insets(22));
        overviewCard.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 18;" +
                "-fx-border-width: 1;"
        );

        Label overviewTitle = new Label("Current Sprint 2 Focus");
        overviewTitle.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #0f172a;"
        );

        Label overviewBody = new Label(getOverviewBody());
        overviewBody.setWrapText(true);
        overviewBody.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #475569;"
        );

        overviewCard.getChildren().addAll(overviewTitle, overviewBody);

        VBox content = new VBox(20, pageTitle, pageSubtitle, cardsRow, overviewCard);
        content.setAlignment(Pos.TOP_LEFT);

        contentArea.getChildren().setAll(content);
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
            return "Your current dashboard is ready for manager workflows. Property management is already connected, and the next high-priority enhancement is the maintenance request interface through the API Gateway.";
        }
        return "Your current dashboard is ready for staff workflows. Property access is restricted appropriately, and the next high-priority enhancement is the maintenance request task interface for assignment and status tracking.";
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