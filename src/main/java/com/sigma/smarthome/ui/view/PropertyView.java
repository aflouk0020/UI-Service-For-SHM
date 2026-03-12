package com.sigma.smarthome.ui.view;

import com.sigma.smarthome.ui.model.Property;
import com.sigma.smarthome.ui.service.PropertyApiService;
import com.sigma.smarthome.ui.util.SessionManager;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PropertyView {

    private final VBox root = new VBox(22);

    private final Label sectionBadge = new Label("Property Service");
    private final Label titleLabel = new Label("Property Management");
    private final Label subtitleLabel = new Label("Manage residential properties connected to the platform.");
    private final Label accessNoteLabel = new Label();

    private final Button refreshButton = new Button("Refresh");
    private final Button addButton = new Button("Add Property");
    private final Button editButton = new Button("Edit Selected");
    private final Button deleteButton = new Button("Delete Selected");

    private final TextField searchField = new TextField();
    private final ComboBox<String> propertyTypeFilter = new ComboBox<>();
    private final Button clearFiltersButton = new Button("Clear Filters");

    private final Label messageLabel = new Label();

    private final Label totalPropertiesValue = new Label("0");
    private final Label uniqueTypesValue = new Label("0");
    private final Label latestPropertyValue = new Label("No properties");
    private final Label activeRoleValue = new Label("-");
    private final Label tableFooterLabel = new Label("Showing 0 properties");

    private final TableView<Property> propertyTable = new TableView<>();
    private final ObservableList<Property> propertyData = FXCollections.observableArrayList();
    private final FilteredList<Property> filteredProperties = new FilteredList<>(propertyData, p -> true);
    private final SortedList<Property> sortedProperties = new SortedList<>(filteredProperties);

    private final PropertyApiService propertyApiService;

    private final boolean readOnlyAccess;

    public PropertyView() {
        this(new PropertyApiService());
    }

    public PropertyView(PropertyApiService propertyApiService) {
        this.propertyApiService = propertyApiService;
        this.readOnlyAccess = isMaintenanceStaff();
        initialise();
        loadProperties();
    }

    private void initialise() {
        configureRoot();
        configureHeaderSection();
        configureActionBar();
        configureSummaryCards();
        configureFilterBar();
        configureMessageLabel();
        configureTable();
        configureFooter();
        wireFiltering();
        applyAccessControl();
    }

    private void configureRoot() {
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: transparent;");
    }

    private void configureHeaderSection() {
        sectionBadge.setStyle(
                "-fx-background-color: #dbeafe;" +
                "-fx-text-fill: #1d4ed8;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 6 12 6 12;" +
                "-fx-background-radius: 14;"
        );

        titleLabel.setStyle(
                "-fx-font-size: 32px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #111827;"
        );

        subtitleLabel.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-text-fill: #6b7280;"
        );

        accessNoteLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #92400e;" +
                "-fx-background-color: #fef3c7;" +
                "-fx-padding: 8 12 8 12;" +
                "-fx-background-radius: 10;"
        );

        if (readOnlyAccess) {
            accessNoteLabel.setText("Maintenance staff can view property information only.");
            accessNoteLabel.setVisible(true);
            accessNoteLabel.setManaged(true);
        } else {
            accessNoteLabel.setVisible(false);
            accessNoteLabel.setManaged(false);
        }

        VBox headerBox = new VBox(10, sectionBadge, titleLabel, subtitleLabel, accessNoteLabel);
        headerBox.setAlignment(Pos.TOP_LEFT);

        root.getChildren().add(headerBox);
    }

    private void configureActionBar() {
        styleSecondaryButton(refreshButton);
        stylePrimaryButton(addButton);
        styleSecondaryButton(editButton);
        styleDangerButton(deleteButton);

        refreshButton.setOnAction(e -> loadProperties());
        addButton.setOnAction(e -> handleAddProperty());
        editButton.setOnAction(e -> handleEditProperty());
        deleteButton.setOnAction(e -> handleDeleteProperty());

        HBox buttonBar = new HBox(12, refreshButton, addButton, editButton, deleteButton);
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().add(buttonBar);
    }

    private void configureSummaryCards() {
        activeRoleValue.setText(formatRole(SessionManager.getRole()));

        HBox cardsRow = new HBox(16,
                createSummaryCard("Total Properties", totalPropertiesValue),
                createSummaryCard("Property Types", uniqueTypesValue),
                createSummaryCard("Latest Property", latestPropertyValue),
                createSummaryCard("Active Role", activeRoleValue)
        );
        cardsRow.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().add(cardsRow);
    }

    private VBox createSummaryCard(String titleText, Label valueLabel) {
        Label cardTitleLabel = new Label(titleText);
        cardTitleLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #6b7280;"
        );

        valueLabel.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #111827;"
        );

        VBox card = new VBox(10, cardTitleLabel, valueLabel);
        card.setPadding(new Insets(18));
        card.setPrefWidth(220);
        card.setMinHeight(96);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #e5e7eb;" +
                "-fx-border-radius: 16;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 14, 0.10, 0, 4);"
        );
        return card;
    }

    private void configureFilterBar() {
        searchField.setPromptText("Search by address, property ID, manager ID, or type");
        searchField.setPrefWidth(420);
        searchField.setPrefHeight(42);
        searchField.setStyle(commonFieldStyle());

        propertyTypeFilter.setPromptText("Filter by property type");
        propertyTypeFilter.setPrefWidth(220);
        propertyTypeFilter.setPrefHeight(42);
        propertyTypeFilter.setStyle(commonFieldStyle());

        styleSecondaryButton(clearFiltersButton);
        clearFiltersButton.setOnAction(e -> {
            searchField.clear();
            propertyTypeFilter.getSelectionModel().clearSelection();
            applyFilters();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox filterBar = new HBox(12, searchField, propertyTypeFilter, clearFiltersButton, spacer);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().add(filterBar);
    }

    private void configureMessageLabel() {
        messageLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #dc2626;"
        );
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    private void configureTable() {
        TableColumn<Property, String> referenceColumn = new TableColumn<>("Reference");
        referenceColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(toReference(data.getValue().getId()))
        );
        referenceColumn.setPrefWidth(150);

        TableColumn<Property, String> idColumn = new TableColumn<>("Property ID");
        idColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(safe(data.getValue().getId()))
        );
        idColumn.setPrefWidth(255);

        TableColumn<Property, String> addressColumn = new TableColumn<>("Address");
        addressColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(safe(data.getValue().getAddress()))
        );
        addressColumn.setPrefWidth(260);

        TableColumn<Property, String> typeColumn = new TableColumn<>("Property Type");
        typeColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(safe(data.getValue().getPropertyType()))
        );
        typeColumn.setPrefWidth(170);

        TableColumn<Property, String> managerColumn = new TableColumn<>("Manager ID");
        managerColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(safe(data.getValue().getManagerId()))
        );
        managerColumn.setPrefWidth(255);

        propertyTable.getColumns().addAll(referenceColumn, idColumn, addressColumn, typeColumn, managerColumn);
        propertyTable.setItems(sortedProperties);
        sortedProperties.comparatorProperty().bind(propertyTable.comparatorProperty());

        propertyTable.setPrefHeight(560);
        propertyTable.setPlaceholder(createEmptyStateLabel());
        propertyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        propertyTable.setFixedCellSize(42);
        propertyTable.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-control-inner-background: white;" +
                "-fx-table-cell-border-color: #eef2f7;" +
                "-fx-padding: 0;"
        );

        styleTableHeader(referenceColumn);
        styleTableHeader(idColumn);
        styleTableHeader(addressColumn);
        styleTableHeader(typeColumn);
        styleTableHeader(managerColumn);

        propertyTable.setRowFactory(tv -> {
            TableRow<Property> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null || row.isEmpty()) {
                    row.setStyle("-fx-background-color: white;");
                } else {
                    row.setStyle("-fx-background-color: white;");
                }
            });

            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    row.setStyle("-fx-background-color: #eff6ff;");
                } else {
                    row.setStyle("-fx-background-color: white;");
                }
            });

            return row;
        });

        VBox tableCard = new VBox(16, messageLabel, propertyTable, tableFooterLabel);
        tableCard.setPadding(new Insets(22));
        tableCard.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: #e5e7eb;" +
                "-fx-border-radius: 18;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 16, 0.10, 0, 6);"
        );

        VBox.setVgrow(propertyTable, Priority.ALWAYS);
        root.getChildren().add(tableCard);
    }

    private Label createEmptyStateLabel() {
        Label emptyLabel = new Label("No properties match the current view.");
        emptyLabel.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #6b7280;"
        );
        return emptyLabel;
    }

    private void styleTableHeader(TableColumn<Property, String> column) {
        column.setStyle(
                "-fx-alignment: CENTER_LEFT;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;"
        );
    }

    private void configureFooter() {
        tableFooterLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #6b7280;"
        );
    }

    private void wireFiltering() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        propertyTypeFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    private void applyAccessControl() {
        if (readOnlyAccess) {
            addButton.setDisable(true);
            editButton.setDisable(true);
            deleteButton.setDisable(true);

            addButton.setOpacity(0.65);
            editButton.setOpacity(0.65);
            deleteButton.setOpacity(0.65);
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedType = propertyTypeFilter.getValue();

        filteredProperties.setPredicate(property -> {
            boolean matchesSearch = searchText.isBlank()
                    || safe(property.getAddress()).toLowerCase().contains(searchText)
                    || safe(property.getId()).toLowerCase().contains(searchText)
                    || safe(property.getManagerId()).toLowerCase().contains(searchText)
                    || safe(property.getPropertyType()).toLowerCase().contains(searchText)
                    || toReference(property.getId()).toLowerCase().contains(searchText);

            boolean matchesType = selectedType == null
                    || selectedType.isBlank()
                    || selectedType.equalsIgnoreCase(safe(property.getPropertyType()));

            return matchesSearch && matchesType;
        });

        updateSummaryCards();
        updateFooter();
    }

    private void loadProperties() {
        try {
            hideMessage();

            List<Property> properties = propertyApiService.getProperties();
            propertyData.setAll(properties);

            refreshFilterOptions();
            applyFilters();
            updateSummaryCards();
            updateFooter();

            showMessage("Properties loaded successfully.", false);
        } catch (Exception ex) {
            ex.printStackTrace();
            propertyData.clear();
            refreshFilterOptions();
            applyFilters();
            updateSummaryCards();
            updateFooter();
            showMessage("Failed to load properties. Check Property Service connection.", true);
        }
    }

    private void handleAddProperty() {
        if (readOnlyAccess) {
            showMessage("Maintenance staff can view property information only.", true);
            return;
        }

        try {
            Optional<PropertyFormData> result = showPropertyFormDialog(
                    "Create Property",
                    "Enter the new property details.",
                    null,
                    null
            );

            if (result.isEmpty()) {
                return;
            }

            PropertyFormData formData = result.get();
            propertyApiService.createProperty(formData.address(), formData.propertyType());

            loadProperties();
            showMessage("Property created successfully.", false);
        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Failed to create property.", true);
        }
    }

    private void handleEditProperty() {
        if (readOnlyAccess) {
            showMessage("Maintenance staff can view property information only.", true);
            return;
        }

        Property selected = propertyTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage("Please select a property to edit.", true);
            return;
        }

        try {
            Optional<PropertyFormData> result = showPropertyFormDialog(
                    "Edit Property",
                    "Update the selected property details.",
                    selected.getAddress(),
                    selected.getPropertyType()
            );

            if (result.isEmpty()) {
                return;
            }

            PropertyFormData formData = result.get();
            propertyApiService.updateProperty(
                    selected.getId(),
                    formData.address(),
                    formData.propertyType()
            );

            loadProperties();
            showMessage("Property updated successfully.", false);
        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Failed to update property.", true);
        }
    }

    private void handleDeleteProperty() {
        if (readOnlyAccess) {
            showMessage("Maintenance staff can view property information only.", true);
            return;
        }

        Property selected = propertyTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage("Please select a property to delete.", true);
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Property");
        confirmAlert.setHeaderText("Confirm property deletion");
        confirmAlert.setContentText(
                "Are you sure you want to delete property "
                        + toReference(selected.getId())
                        + "?"
        );

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            propertyApiService.deleteProperty(selected.getId());
            loadProperties();
            showMessage("Property deleted successfully.", false);
        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Failed to delete property.", true);
        }
    }
    private Optional<PropertyFormData> showPropertyFormDialog(
            String title,
            String header,
            String defaultAddress,
            String defaultPropertyType
    ) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.getDialogPane().setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: #e5e7eb;" +
                "-fx-border-radius: 18;" +
                "-fx-border-width: 1;"
        );
        dialog.getDialogPane().setPrefWidth(460);

        Label dialogTitle = new Label(title);
        dialogTitle.setStyle(
                "-fx-font-size: 24px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #111827;"
        );

        Label dialogSubtitle = new Label(header);
        dialogSubtitle.setWrapText(true);
        dialogSubtitle.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #6b7280;"
        );

        Label addressLabel = new Label("Address");
        addressLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #374151;"
        );

        TextField addressField = new TextField(defaultAddress == null ? "" : defaultAddress);
        addressField.setPromptText("Enter property address");
        addressField.setPrefHeight(42);
        addressField.setStyle(
                "-fx-background-color: #f9fafb;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #d1d5db;" +
                "-fx-border-radius: 10;" +
                "-fx-border-width: 1;" +
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #111827;" +
                "-fx-prompt-text-fill: #9ca3af;"
        );

        Label typeLabel = new Label("Property Type");
        typeLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #374151;"
        );

        TextField typeField = new TextField(defaultPropertyType == null ? "" : defaultPropertyType);
        typeField.setPromptText("Enter property type");
        typeField.setPrefHeight(42);
        typeField.setStyle(
                "-fx-background-color: #f9fafb;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #d1d5db;" +
                "-fx-border-radius: 10;" +
                "-fx-border-width: 1;" +
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #111827;" +
                "-fx-prompt-text-fill: #9ca3af;"
        );

        VBox content = new VBox(14,
                dialogTitle,
                dialogSubtitle,
                addressLabel,
                addressField,
                typeLabel,
                typeField
        );
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: white;");

        dialog.getDialogPane().setContent(content);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);

        saveButton.setStyle(
                "-fx-background-color: #2563eb;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 8 20 8 20;" +
                "-fx-cursor: hand;"
        );

        cancelButton.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #111827;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: #d1d5db;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 8 20 8 20;" +
                "-fx-cursor: hand;"
        );

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != saveButtonType) {
            return Optional.empty();
        }

        String address = addressField.getText() == null ? "" : addressField.getText().trim();
        String propertyType = typeField.getText() == null ? "" : typeField.getText().trim();

        if (address.isBlank() || propertyType.isBlank()) {
            showMessage("Address and property type are required.", true);
            return Optional.empty();
        }

        return Optional.of(new PropertyFormData(address, propertyType));
    }

    private void refreshFilterOptions() {
        String currentSelection = propertyTypeFilter.getValue();

        List<String> types = propertyData.stream()
                .map(Property::getPropertyType)
                .filter(type -> type != null && !type.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());

        propertyTypeFilter.setItems(FXCollections.observableArrayList(types));

        if (currentSelection != null && types.contains(currentSelection)) {
            propertyTypeFilter.setValue(currentSelection);
        } else {
            propertyTypeFilter.getSelectionModel().clearSelection();
        }
    }

    private void updateSummaryCards() {
        int total = filteredProperties.size();
        int uniqueTypes = (int) filteredProperties.stream()
                .map(Property::getPropertyType)
                .filter(type -> type != null && !type.isBlank())
                .map(String::trim)
                .map(String::toLowerCase)
                .distinct()
                .count();

        Optional<Property> latest = filteredProperties.stream()
                .filter(property -> property.getId() != null && !property.getId().isBlank())
                .max(Comparator.comparing(Property::getId));

        totalPropertiesValue.setText(String.valueOf(total));
        uniqueTypesValue.setText(String.valueOf(uniqueTypes));
        latestPropertyValue.setText(
                latest.map(property -> safe(property.getAddress()))
                        .filter(address -> !address.isBlank())
                        .orElse("No properties")
        );
        activeRoleValue.setText(formatRole(SessionManager.getRole()));
    }

    private void updateFooter() {
        int count = filteredProperties.size();
        tableFooterLabel.setText("Showing " + count + (count == 1 ? " property" : " properties"));
    }

    private boolean isMaintenanceStaff() {
        String role = SessionManager.getRole();
        return role != null && role.equalsIgnoreCase("MAINTENANCE_STAFF");
    }

    private String toReference(String propertyId) {
        if (propertyId == null || propertyId.isBlank()) {
            return "-";
        }

        String shortened = propertyId.length() >= 8 ? propertyId.substring(0, 8) : propertyId;
        return "PROP-" + shortened.toUpperCase();
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + (isError ? "#dc2626;" : "#16a34a;")
        );
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void hideMessage() {
        messageLabel.setText("");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    private void stylePrimaryButton(Button button) {
        button.setPrefHeight(42);
        button.setStyle(
                "-fx-background-color: #2563eb;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 0 18 0 18;" +
                "-fx-cursor: hand;"
        );
    }

    private void styleSecondaryButton(Button button) {
        button.setPrefHeight(42);
        button.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #111827;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: #d1d5db;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 0 18 0 18;" +
                "-fx-cursor: hand;"
        );
    }

    private void styleDangerButton(Button button) {
        button.setPrefHeight(42);
        button.setStyle(
                "-fx-background-color: #ef4444;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 0 18 0 18;" +
                "-fx-cursor: hand;"
        );
    }

    private String commonFieldStyle() {
        return "-fx-background-color: white;" +
               "-fx-background-radius: 10;" +
               "-fx-border-color: #d1d5db;" +
               "-fx-border-radius: 10;" +
               "-fx-border-width: 1;" +
               "-fx-font-size: 13px;" +
               "-fx-text-fill: #111827;" +
               "-fx-prompt-text-fill: #9ca3af;";
    }

    private String formatRole(String role) {
        if (role == null || role.isBlank()) {
            return "-";
        }

        String formatted = role.replace("_", " ").toLowerCase();
        return Character.toUpperCase(formatted.charAt(0)) + formatted.substring(1);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public Parent getView() {
        return root;
    }

    private record PropertyFormData(String address, String propertyType) {
    }
}