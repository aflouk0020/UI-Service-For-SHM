package com.sigma.smarthome.ui.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DashboardView {

    private final BorderPane root = new BorderPane();
    private final StackPane contentArea = new StackPane();

    private final Button dashboardButton = new Button("Dashboard");
    private final Button userManagementButton = new Button("User Management");
    private final Button propertyManagementButton = new Button("Property Management");
    private final Button maintenanceRequestsButton = new Button("Maintenance Requests");
    private final Button systemStatusButton = new Button("System Status");
    private final Button notificationsButton = new Button("Notifications");
    private final Button reportsButton = new Button("Reports");

    public DashboardView(String email, String role, Runnable onLogout) {
        configureRoot();
        configureTopBar(email, role, onLogout);
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
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        Label emailLabel = new Label(email);
        emailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");

        Label roleLabel = new Label(role);
        roleLabel.setStyle(
                "-fx-background-color: #dbeafe;" +
                "-fx-text-fill: #2563eb;" +
                "-fx-padding: 6 12 6 12;" +
                "-fx-background-radius: 16;" +
                "-fx-font-size: 13px;"
        );

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle(
                "-fx-background-color: #ef4444;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 10 18 10 18;"
        );
        logoutButton.setOnAction(e -> onLogout.run());

        VBox userBox = new VBox(8, emailLabel, roleLabel);
        userBox.setAlignment(Pos.CENTER_RIGHT);

        HBox rightBox = new HBox(14, userBox, logoutButton);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        BorderPane topBar = new BorderPane();
        topBar.setPadding(new Insets(22));
        topBar.setLeft(title);
        topBar.setRight(rightBox);
        topBar.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #e5e7eb;" +
                "-fx-border-width: 0 0 1 0;"
        );

        root.setTop(topBar);
    }

    private void configureSidebar() {
        VBox sidebar = new VBox(18,
                dashboardButton,
                userManagementButton,
                propertyManagementButton,
                maintenanceRequestsButton,
                systemStatusButton,
                notificationsButton,
                reportsButton
        );

        sidebar.setPadding(new Insets(30));
        sidebar.setPrefWidth(250);
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

        root.setLeft(sidebar);
    }

    private void configureContentArea() {
        contentArea.setPadding(new Insets(28));
        root.setCenter(contentArea);
    }

    private void wireNavigation() {
        dashboardButton.setOnAction(e -> showDashboardHome());

        userManagementButton.setOnAction(e ->
                contentArea.getChildren().setAll(new PlaceholderView(
                        "User Management",
                        "User management features will appear here."
                ).getView())
        );

        propertyManagementButton.setOnAction(e ->
                contentArea.getChildren().setAll(new PropertyView().getView())
        );

        maintenanceRequestsButton.setOnAction(e ->
                contentArea.getChildren().setAll(new PlaceholderView(
                        "Maintenance Requests",
                        "Maintenance request management will appear here."
                ).getView())
        );

        systemStatusButton.setOnAction(e ->
                contentArea.getChildren().setAll(new PlaceholderView(
                        "System Status",
                        "System status and health details will appear here."
                ).getView())
        );

        notificationsButton.setOnAction(e ->
                contentArea.getChildren().setAll(new PlaceholderView(
                        "Notifications",
                        "Notifications module placeholder."
                ).getView())
        );

        reportsButton.setOnAction(e ->
                contentArea.getChildren().setAll(new PlaceholderView(
                        "Reports",
                        "Reports module placeholder."
                ).getView())
        );
    }

    private void showDashboardHome() {
        contentArea.getChildren().setAll(new PlaceholderView(
                "Dashboard",
                "Welcome to the Smart Home Maintenance Platform dashboard."
        ).getView());
    }

    private void styleSidebarButton(Button button) {
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #1f2937;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 12 10 12 10;"
        );
    }

    public Parent getView() {
        return root;
    }
}