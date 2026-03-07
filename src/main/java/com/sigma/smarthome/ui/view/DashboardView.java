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
import javafx.scene.layout.VBox;

public class DashboardView {

    private final BorderPane root = new BorderPane();

    private final Label appTitleLabel = new Label("Smart Home Maintenance Platform");
    private final Label userEmailLabel = new Label();
    private final Label roleLabel = new Label();
    private final Button logoutButton = new Button("Logout");

    private final VBox sidebar = new VBox(12);
    private final VBox contentArea = new VBox(16);

    private final Label pageTitleLabel = new Label("Dashboard");
    private final Label pageDescriptionLabel = new Label("Welcome to the Smart Home Maintenance Platform.");

    private final Button dashboardButton = new Button("Dashboard");
    private final Button userManagementButton = new Button("User Management");
    private final Button propertyManagementButton = new Button("Property Management");
    private final Button maintenanceRequestsButton = new Button("Maintenance Requests");
    private final Button systemStatusButton = new Button("System Status");
    private final Button notificationsButton = new Button("Notifications");
    private final Button reportsButton = new Button("Reports");

    public DashboardView(String email, String role) {
        initialise(email, role);
    }

    private void initialise(String email, String role) {
        configureRoot();
        configureTopBar(email, role);
        configureSidebar();
        configureContentArea();
    }

    private void configureRoot() {
        root.setStyle("-fx-background-color: #f8fafc;");
    }

    private void configureTopBar(String email, String role) {
        appTitleLabel.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #1f2937;"
        );

        userEmailLabel.setText(email == null ? "Unknown User" : email);
        userEmailLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #374151;"
        );

        roleLabel.setText(role == null ? "Unknown Role" : role);
        roleLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-background-color: #dbeafe;" +
                "-fx-text-fill: #1d4ed8;" +
                "-fx-padding: 6 10 6 10;" +
                "-fx-background-radius: 20;"
        );

        logoutButton.setStyle(
                "-fx-background-color: #ef4444;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
        );
        logoutButton.setPrefHeight(36);

        VBox userInfoBox = new VBox(6, userEmailLabel, roleLabel);
        userInfoBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(16, appTitleLabel, spacer, userInfoBox, logoutButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(18, 24, 18, 24));
        topBar.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #e5e7eb;" +
                "-fx-border-width: 0 0 1 0;"
        );

        root.setTop(topBar);
    }

    private void configureSidebar() {
        sidebar.setPadding(new Insets(24));
        sidebar.setPrefWidth(240);
        sidebar.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #e5e7eb;" +
                "-fx-border-width: 0 1 0 0;"
        );

        styleSidebarButton(dashboardButton);
        styleSidebarButton(userManagementButton);
        styleSidebarButton(propertyManagementButton);
        styleSidebarButton(maintenanceRequestsButton);
        styleSidebarButton(systemStatusButton);
        styleSidebarButton(notificationsButton);
        styleSidebarButton(reportsButton);

        dashboardButton.setOnAction(e -> setContent("Dashboard", "Overview of the Smart Home Maintenance Platform."));
        userManagementButton.setOnAction(e -> setContent("User Management", "User account and role management module placeholder."));
        propertyManagementButton.setOnAction(e -> setContent("Property Management", "Property creation, update, and deletion module placeholder."));
        maintenanceRequestsButton.setOnAction(e -> setContent("Maintenance Requests", "Maintenance request reporting and tracking module placeholder."));
        systemStatusButton.setOnAction(e -> setContent("System Status", "Service health and platform architecture module placeholder."));
        notificationsButton.setOnAction(e -> setContent("Notifications", "Future notifications module placeholder."));
        reportsButton.setOnAction(e -> setContent("Reports", "Future reports and analytics module placeholder."));

        sidebar.getChildren().addAll(
                dashboardButton,
                userManagementButton,
                propertyManagementButton,
                maintenanceRequestsButton,
                systemStatusButton,
                notificationsButton,
                reportsButton
        );

        root.setLeft(sidebar);
    }

    private void configureContentArea() {
        pageTitleLabel.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #1f2937;"
        );

        pageDescriptionLabel.setWrapText(true);
        pageDescriptionLabel.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-text-fill: #4b5563;"
        );

        VBox summaryCard = new VBox(10);
        summaryCard.setPadding(new Insets(24));
        summaryCard.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #e5e7eb;" +
                "-fx-border-radius: 16;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 12, 0.08, 0, 4);"
        );

        Label summaryHeading = new Label("Module Workspace");
        summaryHeading.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #111827;"
        );

        Label summaryText = new Label("This area is ready for live integration with your microservices.");
        summaryText.setWrapText(true);
        summaryText.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #6b7280;"
        );

        summaryCard.getChildren().addAll(summaryHeading, summaryText);

        contentArea.setPadding(new Insets(28));
        contentArea.getChildren().addAll(pageTitleLabel, pageDescriptionLabel, summaryCard);

        root.setCenter(contentArea);
    }

    private void styleSidebarButton(Button button) {
        button.setPrefWidth(190);
        button.setPrefHeight(42);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #1f2937;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: #eff6ff;" +
                "-fx-text-fill: #1d4ed8;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
        ));

        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #1f2937;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
        ));
    }

    private void setContent(String title, String description) {
        pageTitleLabel.setText(title);
        pageDescriptionLabel.setText(description);
    }

    public Parent getView() {
        return root;
    }

    public void setOnLogout(Runnable action) {
        logoutButton.setOnAction(e -> action.run());
    }
}