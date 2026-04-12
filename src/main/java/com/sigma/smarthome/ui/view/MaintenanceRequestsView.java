package com.sigma.smarthome.ui.view;

import com.sigma.smarthome.ui.model.MaintenanceHistoryItem;
import com.sigma.smarthome.ui.model.MaintenanceRequest;
import com.sigma.smarthome.ui.model.Property;
import com.sigma.smarthome.ui.service.MaintenanceApiService;
import com.sigma.smarthome.ui.service.PropertyApiService;
import com.sigma.smarthome.ui.util.SessionManager;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;
import java.util.Optional;

public class MaintenanceRequestsView {

    private final VBox root = new VBox(20);

    private final Label titleLabel = new Label("Maintenance Requests");
    private final Label subtitleLabel = new Label("Create, review, assign, and track maintenance activity.");
    private final Label messageLabel = new Label();
    private final PropertyApiService propertyApiService = new PropertyApiService();
    private final ComboBox<Property> propertyComboBox = new ComboBox<>();
    
    private final Button refreshButton = new Button("Refresh");
    private final Button createButton = new Button("Create Request");
    private final Button assignButton = new Button("Assign Staff");
    private final Button updateStatusButton = new Button("Update Status");
    private final Button historyButton = new Button("View History");

    private final ComboBox<String> statusFilter = new ComboBox<>();
    private final ComboBox<String> priorityFilter = new ComboBox<>();

    private final TableView<MaintenanceRequest> table = new TableView<>();
    private final ObservableList<MaintenanceRequest> requestData = FXCollections.observableArrayList();

    private final MaintenanceApiService maintenanceApiService = new MaintenanceApiService();

    public MaintenanceRequestsView() {
        initialise();
        if (isPropertyManager()) {
            loadRequests();
        } else {
            showMessage("Maintenance staff request list endpoint is not yet connected.", false);
        }
    }

    private void initialise() {
        configureRoot();
        configureHeader();
        configureFilters();
        configureButtons();
        configureTable();
        applyRoleAccess();
        configurePropertyDropdown();
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
    
    private void configurePropertyDropdown() {
        propertyComboBox.setPrefWidth(320);
        propertyComboBox.setPromptText("Select property");

        propertyComboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Property item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getAddress() + " (" + item.getId() + ")");
                }
            }
        });

        propertyComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Property item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getAddress() + " (" + item.getId() + ")");
                }
            }
        });
    }
    
    private void loadPropertiesForDropdown() {
        try {
            List<Property> properties = propertyApiService.getProperties();
            propertyComboBox.setItems(FXCollections.observableArrayList(properties));
            propertyComboBox.getSelectionModel().clearSelection();
        } catch (RuntimeException ex) {
            showMessage("Failed to load properties for request creation.", true);
        }
    }

    private void configureFilters() {
        statusFilter.getItems().addAll("", "OPEN", "IN_PROGRESS", "COMPLETED");
        statusFilter.setValue("");
        statusFilter.setPromptText("Filter by status");
        statusFilter.setPrefWidth(180);

        priorityFilter.getItems().addAll("", "LOW", "MEDIUM", "HIGH");
        priorityFilter.setValue("");
        priorityFilter.setPromptText("Filter by priority");
        priorityFilter.setPrefWidth(180);

        statusFilter.setOnAction(e -> loadRequests());
        priorityFilter.setOnAction(e -> loadRequests());

        refreshButton.setOnAction(e -> loadRequests());

        HBox filterRow = new HBox(12, statusFilter, priorityFilter, refreshButton);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().add(filterRow);
    }

    private void configureButtons() {
        createButton.setOnAction(e -> openCreateDialog());
        assignButton.setOnAction(e -> openAssignDialog());
        updateStatusButton.setOnAction(e -> openStatusDialog());
        historyButton.setOnAction(e -> showHistoryDialog());

        HBox actionRow = new HBox(12, createButton, assignButton, updateStatusButton, historyButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().add(actionRow);
    }

    private void configureTable() {
        TableColumn<MaintenanceRequest, String> idCol = createColumn("Request ID", 230, MaintenanceRequest::getId);
        TableColumn<MaintenanceRequest, String> propertyCol = createColumn("Property ID", 220, MaintenanceRequest::getPropertyId);
        TableColumn<MaintenanceRequest, String> descCol = createColumn("Description", 260, MaintenanceRequest::getDescription);
        TableColumn<MaintenanceRequest, String> priorityCol = createColumn("Priority", 120, MaintenanceRequest::getPriority);
        TableColumn<MaintenanceRequest, String> statusCol = createColumn("Status", 140, MaintenanceRequest::getStatus);
        TableColumn<MaintenanceRequest, String> assignedCol = createColumn("Assigned Staff", 220, MaintenanceRequest::getAssignedStaffId);

        table.getColumns().addAll(idCol, propertyCol, descCol, priorityCol, statusCol, assignedCol);
        table.setItems(requestData);
        table.setPrefHeight(520);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No maintenance requests found."));

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

    private TableColumn<MaintenanceRequest, String> createColumn(String title, double width, java.util.function.Function<MaintenanceRequest, String> mapper) {
        TableColumn<MaintenanceRequest, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(mapper.apply(data.getValue()))));
        column.setPrefWidth(width);
        return column;
    }

    private void applyRoleAccess() {
        if (isPropertyManager()) {
            updateStatusButton.setDisable(true);
        } else if (isMaintenanceStaff()) {
            createButton.setDisable(true);
            assignButton.setDisable(true);
            statusFilter.setDisable(true);
            priorityFilter.setDisable(true);
            refreshButton.setDisable(true);
        }
    }

    private void loadRequests() {
        try {
            hideMessage();

            String status = statusFilter.getValue();
            String priority = priorityFilter.getValue();

            if (status != null && status.isBlank()) {
                status = null;
            }
            if (priority != null && priority.isBlank()) {
                priority = null;
            }

            List<MaintenanceRequest> requests = maintenanceApiService.getRequests(status, priority);
            requestData.setAll(requests);

            showMessage("Maintenance requests loaded successfully.", false);
        } catch (RuntimeException ex) {
            requestData.clear();
            showMessage("Failed to load maintenance requests.", true);
        }
    }

    private void openCreateDialog() {
        loadPropertiesForDropdown();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Maintenance Request");

        ButtonType createType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createType, ButtonType.CANCEL);

        propertyComboBox.getSelectionModel().clearSelection();

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("LOW", "MEDIUM", "HIGH");
        priorityBox.setPromptText("Priority");

        VBox content = new VBox(12,
                new Label("Property"), propertyComboBox,
                new Label("Description"), descriptionField,
                new Label("Priority"), priorityBox
        );
        content.setPadding(new Insets(18));

        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == createType) {
            try {
                Property selectedProperty = propertyComboBox.getValue();

                if (selectedProperty == null
                        || descriptionField.getText() == null
                        || descriptionField.getText().isBlank()
                        || priorityBox.getValue() == null
                        || priorityBox.getValue().isBlank()) {
                    showMessage("Property, description, and priority are required.", true);
                    return;
                }

                maintenanceApiService.createRequest(
                        selectedProperty.getId(),
                        descriptionField.getText().trim(),
                        priorityBox.getValue()
                );

                loadRequests();
                showMessage("Maintenance request created successfully.", false);
            } catch (RuntimeException ex) {
                showMessage("Failed to create maintenance request.", true);
            }
        }
    }

    private void openAssignDialog() {
        MaintenanceRequest selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Please select a maintenance request first.", true);
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Assign Staff");
        dialog.setHeaderText("Assign maintenance staff");
        dialog.setContentText("Enter staff user ID:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(staffId -> {
            try {
                maintenanceApiService.assignStaff(selected.getId(), staffId.trim());
                loadRequests();
                showMessage("Maintenance staff assigned successfully.", false);
            } catch (RuntimeException ex) {
                showMessage("Failed to assign staff.", true);
            }
        });
    }

    private void openStatusDialog() {
        MaintenanceRequest selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Please select a maintenance request first.", true);
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>("IN_PROGRESS", "OPEN", "IN_PROGRESS", "COMPLETED");
        dialog.setTitle("Update Status");
        dialog.setHeaderText("Update maintenance status");
        dialog.setContentText("Select new status:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(status -> {
            try {
                maintenanceApiService.updateStatus(selected.getId(), status);
                loadRequests();
                showMessage("Maintenance status updated successfully.", false);
            } catch (RuntimeException ex) {
                showMessage("Failed to update maintenance status.", true);
            }
        });
    }

    private void showHistoryDialog() {
        MaintenanceRequest selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Please select a maintenance request first.", true);
            return;
        }

        try {
            List<MaintenanceHistoryItem> history = maintenanceApiService.getHistory(selected.getId());

            StringBuilder builder = new StringBuilder();
            for (MaintenanceHistoryItem item : history) {
                builder.append("Changed from ")
                        .append(safe(item.getOldStatus()))
                        .append(" to ")
                        .append(safe(item.getNewStatus()))
                        .append(" at ")
                        .append(safe(item.getChangedAt()))
                        .append("\n");
            }

            if (builder.isEmpty()) {
                builder.append("No history available.");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Maintenance History");
            alert.setHeaderText("History for request " + selected.getId());
            alert.setContentText(builder.toString());
            alert.showAndWait();

        } catch (RuntimeException ex) {
            showMessage("Failed to load maintenance history.", true);
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

    private boolean isPropertyManager() {
        return "PROPERTY_MANAGER".equalsIgnoreCase(SessionManager.getRole());
    }

    private boolean isMaintenanceStaff() {
        return "MAINTENANCE_STAFF".equalsIgnoreCase(SessionManager.getRole());
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }

    public Parent getView() {
        return root;
    }
}