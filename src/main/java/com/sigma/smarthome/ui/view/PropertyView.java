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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public class PropertyView {

    private static final String READ_ONLY_MESSAGE = "Maintenance staff can view property information only.";
    private static final String NO_PROPERTIES_TEXT = "No properties";
    private static final String EMPTY_VALUE = "";
    private static final String DASH = "-";
    private static final String WHITE = "white";

    private static final String COLOR_PRIMARY = "#2563eb";
    private static final String COLOR_PRIMARY_DARK = "#1d4ed8";
    private static final String COLOR_TEXT_MAIN = "#111827";
    private static final String COLOR_TEXT_DARK = "#374151";
    private static final String COLOR_TEXT_MUTED = "#6b7280";
    private static final String COLOR_TEXT_INFO = "#92400e";
    private static final String COLOR_TEXT_SUCCESS = "#16a34a";
    private static final String COLOR_TEXT_ERROR = "#dc2626";
    private static final String COLOR_TEXT_PROMPT = "#9ca3af";

    private static final String COLOR_BG_LIGHT_BLUE = "#dbeafe";
    private static final String COLOR_BG_WARNING = "#fef3c7";
    private static final String COLOR_BG_FIELD = "#f9fafb";
    private static final String COLOR_BG_DANGER = "#ef4444";
    private static final String COLOR_BG_SELECTED_ROW = "#eff6ff";

    private static final String COLOR_BORDER_LIGHT = "#d1d5db";
    private static final String COLOR_BORDER_CARD = "#e5e7eb";
    private static final String COLOR_TABLE_BORDER = "#eef2f7";

    private static final String FX_BACKGROUND_COLOR = "-fx-background-color: ";
    private static final String FX_TEXT_FILL = "-fx-text-fill: ";
    private static final String FX_BORDER_COLOR = "-fx-border-color: ";
    private static final String FX_FONT_SIZE = "-fx-font-size: ";
    private static final String FX_FONT_WEIGHT_BOLD = "-fx-font-weight: bold;";
    private static final String FX_BACKGROUND_RADIUS_10 = "-fx-background-radius: 10;";
    private static final String FX_BACKGROUND_RADIUS_14 = "-fx-background-radius: 14;";
    private static final String FX_BACKGROUND_RADIUS_16 = "-fx-background-radius: 16;";
    private static final String FX_BACKGROUND_RADIUS_18 = "-fx-background-radius: 18;";
    private static final String FX_BORDER_RADIUS_10 = "-fx-border-radius: 10;";
    private static final String FX_BORDER_RADIUS_16 = "-fx-border-radius: 16;";
    private static final String FX_BORDER_RADIUS_18 = "-fx-border-radius: 18;";
    private static final String FX_BORDER_WIDTH_1 = "-fx-border-width: 1;";
    private static final String FX_CURSOR_HAND = "-fx-cursor: hand;";
    private static final String PX = "px;";
    private static final String SEMICOLON = ";";

    private static final String BUTTON_PADDING = "-fx-padding: 0 18 0 18;";
    private static final String DIALOG_BUTTON_PADDING = "-fx-padding: 8 20 8 20;";
    private static final String WARNING_LABEL_PADDING = "-fx-padding: 8 12 8 12;";
    private static final String BADGE_PADDING = "-fx-padding: 6 12 6 12;";

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
    private final Label latestPropertyValue = new Label(NO_PROPERTIES_TEXT);
    private final Label activeRoleValue = new Label(DASH);
    private final Label tableFooterLabel = new Label("Showing 0 properties");

    private final TableView<Property> propertyTable = new TableView<>();
    private final ObservableList<Property> propertyData = FXCollections.observableArrayList();
    private final FilteredList<Property> filteredProperties = new FilteredList<>(propertyData, property -> true);
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
        root.setStyle(FX_BACKGROUND_COLOR + "transparent;");
    }

    private void configureHeaderSection() {
        sectionBadge.setStyle(
                badgeStyle(COLOR_BG_LIGHT_BLUE, COLOR_PRIMARY_DARK)
        );

        titleLabel.setStyle(textStyle(32, true, COLOR_TEXT_MAIN));
        subtitleLabel.setStyle(textStyle(15, false, COLOR_TEXT_MUTED));

        accessNoteLabel.setStyle(
                warningLabelStyle(COLOR_TEXT_INFO, COLOR_BG_WARNING)
        );

        if (readOnlyAccess) {
            accessNoteLabel.setText(READ_ONLY_MESSAGE);
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

        refreshButton.setOnAction(event -> loadProperties());
        addButton.setOnAction(event -> handleAddProperty());
        editButton.setOnAction(event -> handleEditProperty());
        deleteButton.setOnAction(event -> handleDeleteProperty());

        HBox buttonBar = new HBox(12, refreshButton, addButton, editButton, deleteButton);
        buttonBar.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().add(buttonBar);
    }

    private void configureSummaryCards() {
        activeRoleValue.setText(formatRole(SessionManager.getRole()));

        HBox cardsRow = new HBox(
                16,
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
        cardTitleLabel.setStyle(textStyle(13, true, COLOR_TEXT_MUTED));

        valueLabel.setStyle(textStyle(22, true, COLOR_TEXT_MAIN));

        VBox card = new VBox(10, cardTitleLabel, valueLabel);
        card.setPadding(new Insets(18));
        card.setPrefWidth(220);
        card.setMinHeight(96);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(cardStyle());
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
        clearFiltersButton.setOnAction(event -> {
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
        messageLabel.setStyle(messageStyle(COLOR_TEXT_ERROR));
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        TableColumn<Property, String> referenceColumn = createColumn("Reference", 150, property ->
                toReference(property.getId()));
        TableColumn<Property, String> idColumn = createColumn("Property ID", 255, Property::getId);
        TableColumn<Property, String> addressColumn = createColumn("Address", 260, Property::getAddress);
        TableColumn<Property, String> typeColumn = createColumn("Property Type", 170, Property::getPropertyType);
        TableColumn<Property, String> managerColumn = createColumn("Manager ID", 255, Property::getManagerId);

        propertyTable.getColumns().addAll(referenceColumn, idColumn, addressColumn, typeColumn, managerColumn);
        propertyTable.setItems(sortedProperties);
        sortedProperties.comparatorProperty().bind(propertyTable.comparatorProperty());

        propertyTable.setPrefHeight(560);
        propertyTable.setPlaceholder(createEmptyStateLabel());
        propertyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        propertyTable.setFixedCellSize(42);
        propertyTable.setStyle(
                FX_BACKGROUND_COLOR + "transparent;" +
                "-fx-control-inner-background: " + WHITE + SEMICOLON +
                "-fx-table-cell-border-color: " + COLOR_TABLE_BORDER + SEMICOLON +
                "-fx-padding: 0;"
        );

        propertyTable.setRowFactory(table -> {
            TableRow<Property> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> applyRowStyle(row));
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> applyRowStyle(row));
            return row;
        });

        VBox tableCard = new VBox(16, messageLabel, propertyTable, tableFooterLabel);
        tableCard.setPadding(new Insets(22));
        tableCard.setStyle(tableCardStyle());

        VBox.setVgrow(propertyTable, Priority.ALWAYS);
        root.getChildren().add(tableCard);
    }

    private TableColumn<Property, String> createColumn(String title, double width, Function<Property, String> mapper) {
        TableColumn<Property, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new ReadOnlyStringWrapper(safe(mapper.apply(data.getValue()))));
        column.setPrefWidth(width);
        styleTableHeader(column);
        return column;
    }

    private void applyRowStyle(TableRow<Property> row) {
        row.setStyle(FX_BACKGROUND_COLOR + (row.isSelected() ? COLOR_BG_SELECTED_ROW : WHITE) + SEMICOLON);
    }

    private Label createEmptyStateLabel() {
        Label emptyLabel = new Label("No properties match the current view.");
        emptyLabel.setStyle(textStyle(14, false, COLOR_TEXT_MUTED));
        return emptyLabel;
    }

    private void styleTableHeader(TableColumn<Property, String> column) {
        column.setStyle(
                "-fx-alignment: CENTER_LEFT;" +
                textSizeStyle(13) +
                FX_FONT_WEIGHT_BOLD
        );
    }

    private void configureFooter() {
        tableFooterLabel.setStyle(textStyle(13, false, COLOR_TEXT_MUTED));
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
        String searchText = searchField.getText() == null
                ? EMPTY_VALUE
                : searchField.getText().trim().toLowerCase(Locale.ROOT);

        String selectedType = propertyTypeFilter.getValue();

        filteredProperties.setPredicate(property -> {
            boolean matchesSearch = searchText.isBlank()
                    || containsIgnoreCase(property.getAddress(), searchText)
                    || containsIgnoreCase(property.getId(), searchText)
                    || containsIgnoreCase(property.getManagerId(), searchText)
                    || containsIgnoreCase(property.getPropertyType(), searchText)
                    || containsIgnoreCase(toReference(property.getId()), searchText);

            boolean matchesType = selectedType == null
                    || selectedType.isBlank()
                    || selectedType.equalsIgnoreCase(safe(property.getPropertyType()));

            return matchesSearch && matchesType;
        });

        updateSummaryCards();
        updateFooter();
    }

    private boolean containsIgnoreCase(String value, String searchText) {
        return safe(value).toLowerCase(Locale.ROOT).contains(searchText);
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
        } catch (RuntimeException ex) {
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
            showMessage(READ_ONLY_MESSAGE, true);
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
        } catch (PropertyApiService.PropertyApiException ex) {
            showMessage("Failed to create property.", true);
        }
    }

    private void handleEditProperty() {
        if (readOnlyAccess) {
            showMessage(READ_ONLY_MESSAGE, true);
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
            propertyApiService.updateProperty(selected.getId(), formData.address(), formData.propertyType());

            loadProperties();
            showMessage("Property updated successfully.", false);
        } catch (PropertyApiService.PropertyApiException ex) {
            showMessage("Failed to update property.", true);
        }
    }

    private void handleDeleteProperty() {
        if (readOnlyAccess) {
            showMessage(READ_ONLY_MESSAGE, true);
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
        confirmAlert.setContentText("Are you sure you want to delete property " + toReference(selected.getId()) + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            propertyApiService.deleteProperty(selected.getId());
            loadProperties();
            showMessage("Property deleted successfully.", false);
        } catch (PropertyApiService.PropertyApiException ex) {
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
        dialog.getDialogPane().setStyle(dialogPaneStyle());
        dialog.getDialogPane().setPrefWidth(460);

        Label dialogTitle = new Label(title);
        dialogTitle.setStyle(textStyle(24, true, COLOR_TEXT_MAIN));

        Label dialogSubtitle = new Label(header);
        dialogSubtitle.setWrapText(true);
        dialogSubtitle.setStyle(textStyle(14, false, COLOR_TEXT_MUTED));

        Label addressLabel = createFormLabel("Address");
        TextField addressField = createDialogField(defaultAddress, "Enter property address");

        Label typeLabel = createFormLabel("Property Type");
        TextField typeField = createDialogField(defaultPropertyType, "Enter property type");

        VBox content = new VBox(
                14,
                dialogTitle,
                dialogSubtitle,
                addressLabel,
                addressField,
                typeLabel,
                typeField
        );
        content.setPadding(new Insets(24));
        content.setStyle(FX_BACKGROUND_COLOR + WHITE + SEMICOLON);

        dialog.getDialogPane().setContent(content);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);

        saveButton.setStyle(dialogPrimaryButtonStyle());
        cancelButton.setStyle(dialogSecondaryButtonStyle());

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != saveButtonType) {
            return Optional.empty();
        }

        String address = addressField.getText() == null ? EMPTY_VALUE : addressField.getText().trim();
        String propertyType = typeField.getText() == null ? EMPTY_VALUE : typeField.getText().trim();

        if (address.isBlank() || propertyType.isBlank()) {
            showMessage("Address and property type are required.", true);
            return Optional.empty();
        }

        return Optional.of(new PropertyFormData(address, propertyType));
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle(textStyle(13, true, COLOR_TEXT_DARK));
        return label;
    }

    private TextField createDialogField(String defaultValue, String promptText) {
        TextField field = new TextField(defaultValue == null ? EMPTY_VALUE : defaultValue);
        field.setPromptText(promptText);
        field.setPrefHeight(42);
        field.setStyle(dialogFieldStyle());
        return field;
    }

    private void refreshFilterOptions() {
        String currentSelection = propertyTypeFilter.getValue();

        List<String> types = propertyData.stream()
                .map(Property::getPropertyType)
                .filter(type -> type != null && !type.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        propertyTypeFilter.setItems(FXCollections.observableArrayList(types));

        if (currentSelection != null && types.contains(currentSelection)) {
            propertyTypeFilter.setValue(currentSelection);
        } else {
            propertyTypeFilter.setValue(null);
            propertyTypeFilter.getSelectionModel().clearSelection();
        }
    }

    private void updateSummaryCards() {
        int total = filteredProperties.size();
        int uniqueTypes = (int) filteredProperties.stream()
                .map(Property::getPropertyType)
                .filter(type -> type != null && !type.isBlank())
                .map(String::trim)
                .map(type -> type.toLowerCase(Locale.ROOT))
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
                        .orElse(NO_PROPERTIES_TEXT)
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
            return DASH;
        }

        String shortened = propertyId.length() >= 8 ? propertyId.substring(0, 8) : propertyId;
        return "PROP-" + shortened.toUpperCase(Locale.ROOT);
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setStyle(messageStyle(isError ? COLOR_TEXT_ERROR : COLOR_TEXT_SUCCESS));
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void hideMessage() {
        messageLabel.setText(EMPTY_VALUE);
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    private void stylePrimaryButton(Button button) {
        button.setPrefHeight(42);
        button.setStyle(primaryButtonStyle());
    }

    private void styleSecondaryButton(Button button) {
        button.setPrefHeight(42);
        button.setStyle(secondaryButtonStyle());
    }

    private void styleDangerButton(Button button) {
        button.setPrefHeight(42);
        button.setStyle(dangerButtonStyle());
    }

    private String commonFieldStyle() {
        return fieldStyle(WHITE, 13, COLOR_TEXT_MAIN, COLOR_TEXT_PROMPT);
    }

    private String dialogFieldStyle() {
        return fieldStyle(COLOR_BG_FIELD, 13, COLOR_TEXT_MAIN, COLOR_TEXT_PROMPT);
    }

    private String fieldStyle(String backgroundColor, int fontSize, String textColor, String promptColor) {
        return FX_BACKGROUND_COLOR + backgroundColor + SEMICOLON +
               FX_BACKGROUND_RADIUS_10 +
               FX_BORDER_COLOR + COLOR_BORDER_LIGHT + SEMICOLON +
               FX_BORDER_RADIUS_10 +
               FX_BORDER_WIDTH_1 +
               textSizeStyle(fontSize) +
               FX_TEXT_FILL + textColor + SEMICOLON +
               "-fx-prompt-text-fill: " + promptColor + SEMICOLON;
    }

    private String messageStyle(String color) {
        return textSizeStyle(13) + FX_FONT_WEIGHT_BOLD + FX_TEXT_FILL + color + SEMICOLON;
    }

    private String textStyle(int fontSize, boolean bold, String color) {
        return textSizeStyle(fontSize) +
               (bold ? FX_FONT_WEIGHT_BOLD : EMPTY_VALUE) +
               FX_TEXT_FILL + color + SEMICOLON;
    }

    private String textSizeStyle(int fontSize) {
        return FX_FONT_SIZE + fontSize + PX;
    }

    private String badgeStyle(String backgroundColor, String textColor) {
        return FX_BACKGROUND_COLOR + backgroundColor + SEMICOLON +
               FX_TEXT_FILL + textColor + SEMICOLON +
               textSizeStyle(12) +
               FX_FONT_WEIGHT_BOLD +
               BADGE_PADDING +
               FX_BACKGROUND_RADIUS_14;
    }

    private String warningLabelStyle(String textColor, String backgroundColor) {
        return textSizeStyle(13) +
               FX_FONT_WEIGHT_BOLD +
               FX_TEXT_FILL + textColor + SEMICOLON +
               FX_BACKGROUND_COLOR + backgroundColor + SEMICOLON +
               WARNING_LABEL_PADDING +
               FX_BACKGROUND_RADIUS_10;
    }

    private String cardStyle() {
        return FX_BACKGROUND_COLOR + WHITE + SEMICOLON +
               FX_BACKGROUND_RADIUS_16 +
               FX_BORDER_COLOR + COLOR_BORDER_CARD + SEMICOLON +
               FX_BORDER_RADIUS_16 +
               FX_BORDER_WIDTH_1 +
               "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 14, 0.10, 0, 4);";
    }

    private String tableCardStyle() {
        return FX_BACKGROUND_COLOR + WHITE + SEMICOLON +
               FX_BACKGROUND_RADIUS_18 +
               FX_BORDER_COLOR + COLOR_BORDER_CARD + SEMICOLON +
               FX_BORDER_RADIUS_18 +
               FX_BORDER_WIDTH_1 +
               "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 16, 0.10, 0, 6);";
    }

    private String dialogPaneStyle() {
        return FX_BACKGROUND_COLOR + WHITE + SEMICOLON +
               FX_BACKGROUND_RADIUS_18 +
               FX_BORDER_COLOR + COLOR_BORDER_CARD + SEMICOLON +
               FX_BORDER_RADIUS_18 +
               FX_BORDER_WIDTH_1;
    }

    private String primaryButtonStyle() {
        return FX_BACKGROUND_COLOR + COLOR_PRIMARY + SEMICOLON +
               FX_TEXT_FILL + WHITE + SEMICOLON +
               textSizeStyle(13) +
               FX_FONT_WEIGHT_BOLD +
               FX_BACKGROUND_RADIUS_10 +
               BUTTON_PADDING +
               FX_CURSOR_HAND;
    }

    private String secondaryButtonStyle() {
        return FX_BACKGROUND_COLOR + WHITE + SEMICOLON +
               FX_TEXT_FILL + COLOR_TEXT_MAIN + SEMICOLON +
               textSizeStyle(13) +
               FX_FONT_WEIGHT_BOLD +
               FX_BORDER_COLOR + COLOR_BORDER_LIGHT + SEMICOLON +
               FX_BORDER_RADIUS_10 +
               FX_BACKGROUND_RADIUS_10 +
               BUTTON_PADDING +
               FX_CURSOR_HAND;
    }

    private String dangerButtonStyle() {
        return FX_BACKGROUND_COLOR + COLOR_BG_DANGER + SEMICOLON +
               FX_TEXT_FILL + WHITE + SEMICOLON +
               textSizeStyle(13) +
               FX_FONT_WEIGHT_BOLD +
               FX_BACKGROUND_RADIUS_10 +
               BUTTON_PADDING +
               FX_CURSOR_HAND;
    }

    private String dialogPrimaryButtonStyle() {
        return FX_BACKGROUND_COLOR + COLOR_PRIMARY + SEMICOLON +
               FX_TEXT_FILL + WHITE + SEMICOLON +
               textSizeStyle(13) +
               FX_FONT_WEIGHT_BOLD +
               FX_BACKGROUND_RADIUS_10 +
               DIALOG_BUTTON_PADDING +
               FX_CURSOR_HAND;
    }

    private String dialogSecondaryButtonStyle() {
        return FX_BACKGROUND_COLOR + WHITE + SEMICOLON +
               FX_TEXT_FILL + COLOR_TEXT_MAIN + SEMICOLON +
               textSizeStyle(13) +
               FX_FONT_WEIGHT_BOLD +
               FX_BORDER_COLOR + COLOR_BORDER_LIGHT + SEMICOLON +
               FX_BORDER_RADIUS_10 +
               FX_BACKGROUND_RADIUS_10 +
               DIALOG_BUTTON_PADDING +
               FX_CURSOR_HAND;
    }

    private String formatRole(String role) {
        if (role == null || role.isBlank()) {
            return DASH;
        }

        String formatted = role.replace("_", " ").toLowerCase(Locale.ROOT);
        return Character.toUpperCase(formatted.charAt(0)) + formatted.substring(1);
    }

    private String safe(String value) {
        return value == null ? EMPTY_VALUE : value;
    }

    public Parent getView() {
        return root;
    }

    private record PropertyFormData(String address, String propertyType) {
    }
}
 