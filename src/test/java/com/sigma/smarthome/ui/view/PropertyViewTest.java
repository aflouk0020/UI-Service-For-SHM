package com.sigma.smarthome.ui.view;

import com.sigma.smarthome.ui.model.Property;
import com.sigma.smarthome.ui.service.PropertyApiService;
import com.sigma.smarthome.ui.util.SessionManager;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PropertyViewTest {

    @BeforeAll
    static void initJavaFx() {
        new JFXPanel();
    }

    @BeforeEach
    void clearSession() {
        SessionManager.clearSession();
    }

    private void invokePrivateMethod(Object target, String methodName) throws Exception {
        Class<?> current = target.getClass();

        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(methodName);
                method.setAccessible(true);
                method.invoke(target);
                return;
            } catch (NoSuchMethodException ex) {
                current = current.getSuperclass();
            }
        }

        throw new NoSuchMethodException(methodName);
    }

    @Test
    void propertyView_createsSuccessfully_forPropertyManager() {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");
        PropertyView view = createView(List.of());

        Parent root = view.getView();

        assertNotNull(view);
        assertNotNull(root);
    }

    @Test
    void propertyView_loadsPropertiesSuccessfully() {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyView view = createView(
                property("1", "Athlone", "House", "manager1"),
                property("2", "Dublin", "Apartment", "manager1")
        );

        assertNotNull(view);
        assertNotNull(view.getView());
    }

    @Test
    void propertyView_handlesLoadFailure() {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenThrow(new RuntimeException("API down"));

        PropertyView view = new PropertyView(fakeService);

        assertNotNull(view);
        assertNotNull(view.getView());
    }

    @Test
    void propertyView_isReadOnlyForMaintenanceStaff() {
        SessionManager.startSession("token", "staff@test.com", "MAINTENANCE_STAFF");
        PropertyView view = createView(List.of());

        assertNotNull(view);
        assertNotNull(view.getView());
    }

    @Test
    void propertyView_disablesWriteButtons_forMaintenanceStaff() throws Exception {
        SessionManager.startSession("token", "staff@test.com", "MAINTENANCE_STAFF");
        PropertyView view = createView(List.of());

        Button addButton = getPrivateField(view, "addButton", Button.class);
        Button editButton = getPrivateField(view, "editButton", Button.class);
        Button deleteButton = getPrivateField(view, "deleteButton", Button.class);
        Label accessNoteLabel = getPrivateField(view, "accessNoteLabel", Label.class);

        assertTrue(addButton.isDisable());
        assertTrue(editButton.isDisable());
        assertTrue(deleteButton.isDisable());
        assertTrue(accessNoteLabel.isVisible());
        assertEquals("Maintenance staff can view property information only.", accessNoteLabel.getText());
    }

    @Test
    void propertyView_managerButtonsRemainEnabled() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");
        PropertyView view = createView(List.of());

        Button addButton = getPrivateField(view, "addButton", Button.class);
        Button editButton = getPrivateField(view, "editButton", Button.class);
        Button deleteButton = getPrivateField(view, "deleteButton", Button.class);

        assertFalse(addButton.isDisable());
        assertFalse(editButton.isDisable());
        assertFalse(deleteButton.isDisable());
    }

    @Test
    void propertyView_updatesSummaryCardsAndFooter() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyView view = createView(
                property("1", "Athlone", "House", "manager1"),
                property("2", "Dublin", "Apartment", "manager1"),
                property("3", "Galway", "House", "manager2")
        );

        Label totalPropertiesValue = getPrivateField(view, "totalPropertiesValue", Label.class);
        Label uniqueTypesValue = getPrivateField(view, "uniqueTypesValue", Label.class);
        Label latestPropertyValue = getPrivateField(view, "latestPropertyValue", Label.class);
        Label activeRoleValue = getPrivateField(view, "activeRoleValue", Label.class);
        Label tableFooterLabel = getPrivateField(view, "tableFooterLabel", Label.class);
        Label messageLabel = getPrivateField(view, "messageLabel", Label.class);

        assertEquals("3", totalPropertiesValue.getText());
        assertEquals("2", uniqueTypesValue.getText());
        assertEquals("Galway", latestPropertyValue.getText());
        assertEquals("Property manager", activeRoleValue.getText());
        assertEquals("Showing 3 properties", tableFooterLabel.getText());
        assertEquals("Properties loaded successfully.", messageLabel.getText());
        assertTrue(messageLabel.isVisible());
    }

    @Test
    void propertyView_loadFailure_clearsDataAndShowsErrorMessage() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenThrow(new RuntimeException("API down"));

        PropertyView view = new PropertyView(fakeService);

        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);
        Label totalPropertiesValue = getPrivateField(view, "totalPropertiesValue", Label.class);
        Label uniqueTypesValue = getPrivateField(view, "uniqueTypesValue", Label.class);
        Label latestPropertyValue = getPrivateField(view, "latestPropertyValue", Label.class);
        Label tableFooterLabel = getPrivateField(view, "tableFooterLabel", Label.class);
        Label messageLabel = getPrivateField(view, "messageLabel", Label.class);

        assertEquals(0, propertyTable.getItems().size());
        assertEquals("0", totalPropertiesValue.getText());
        assertEquals("0", uniqueTypesValue.getText());
        assertEquals("No properties", latestPropertyValue.getText());
        assertEquals("Showing 0 properties", tableFooterLabel.getText());
        assertEquals("Failed to load properties. Check Property Service connection.", messageLabel.getText());
        assertTrue(messageLabel.isVisible());
    }

    @Test
    void propertyView_populatesTypeFilterAndSearchFilterWorks() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyView view = createView(
                property("1", "Athlone", "House", "m1"),
                property("2", "Dublin", "Apartment", "m2"),
                property("3", "Galway", "Studio", "m3")
        );

        ComboBox<String> propertyTypeFilter = getPrivateField(view, "propertyTypeFilter", ComboBox.class);
        TextField searchField = getPrivateField(view, "searchField", TextField.class);
        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);
        Label tableFooterLabel = getPrivateField(view, "tableFooterLabel", Label.class);

        assertEquals(3, propertyTypeFilter.getItems().size());
        assertTrue(propertyTypeFilter.getItems().contains("House"));
        assertTrue(propertyTypeFilter.getItems().contains("Apartment"));
        assertTrue(propertyTypeFilter.getItems().contains("Studio"));

        searchField.setText("Dublin");

        assertEquals(1, propertyTable.getItems().size());
        assertEquals("Dublin", propertyTable.getItems().get(0).getAddress());
        assertEquals("Showing 1 property", tableFooterLabel.getText());
    }

    @Test
    void propertyView_typeFilter_reducesVisibleRowsAndUpdatesSummary() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyView view = createView(
                property("1", "Athlone", "House", "m1"),
                property("2", "Dublin", "Apartment", "m2"),
                property("3", "Galway", "House", "m3")
        );

        ComboBox<String> propertyTypeFilter = getPrivateField(view, "propertyTypeFilter", ComboBox.class);
        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);
        Label totalPropertiesValue = getPrivateField(view, "totalPropertiesValue", Label.class);
        Label uniqueTypesValue = getPrivateField(view, "uniqueTypesValue", Label.class);
        Label tableFooterLabel = getPrivateField(view, "tableFooterLabel", Label.class);

        propertyTypeFilter.setValue("House");

        assertEquals(2, propertyTable.getItems().size());
        assertEquals("2", totalPropertiesValue.getText());
        assertEquals("1", uniqueTypesValue.getText());
        assertEquals("Showing 2 properties", tableFooterLabel.getText());
    }

    @Test
    void propertyView_handlesNullAndBlankValuesInSummary() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyView view = createView(
                property("", "", "", ""),
                property(null, null, null, null)
        );

        Label totalPropertiesValue = getPrivateField(view, "totalPropertiesValue", Label.class);
        Label uniqueTypesValue = getPrivateField(view, "uniqueTypesValue", Label.class);
        Label latestPropertyValue = getPrivateField(view, "latestPropertyValue", Label.class);
        Label tableFooterLabel = getPrivateField(view, "tableFooterLabel", Label.class);

        assertEquals("2", totalPropertiesValue.getText());
        assertEquals("0", uniqueTypesValue.getText());
        assertEquals("No properties", latestPropertyValue.getText());
        assertEquals("Showing 2 properties", tableFooterLabel.getText());
    }

    @Test
    void propertyView_searchWithNoMatch_showsEmptyTableAndUpdatesFooter() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyView view = createView(
                property("1", "Athlone", "House", "m1"),
                property("2", "Dublin", "Apartment", "m2")
        );

        TextField searchField = getPrivateField(view, "searchField", TextField.class);
        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);
        Label totalPropertiesValue = getPrivateField(view, "totalPropertiesValue", Label.class);
        Label uniqueTypesValue = getPrivateField(view, "uniqueTypesValue", Label.class);
        Label latestPropertyValue = getPrivateField(view, "latestPropertyValue", Label.class);
        Label tableFooterLabel = getPrivateField(view, "tableFooterLabel", Label.class);

        searchField.setText("Cork");

        assertEquals(0, propertyTable.getItems().size());
        assertEquals("0", totalPropertiesValue.getText());
        assertEquals("0", uniqueTypesValue.getText());
        assertEquals("No properties", latestPropertyValue.getText());
        assertEquals("Showing 0 properties", tableFooterLabel.getText());
    }

    @Test
    void propertyView_clearingSearch_restoresAllRows() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyView view = createView(
                property("1", "Athlone", "House", "m1"),
                property("2", "Dublin", "Apartment", "m2"),
                property("3", "Galway", "Studio", "m3")
        );

        TextField searchField = getPrivateField(view, "searchField", TextField.class);
        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);
        Label tableFooterLabel = getPrivateField(view, "tableFooterLabel", Label.class);

        searchField.setText("Dublin");
        assertEquals(1, propertyTable.getItems().size());
        assertEquals("Showing 1 property", tableFooterLabel.getText());

        searchField.setText("");
        assertEquals(3, propertyTable.getItems().size());
        assertEquals("Showing 3 properties", tableFooterLabel.getText());
    }

    @Test
    void propertyView_combinedSearchAndTypeFilter_appliesBothFilters() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyView view = createView(
                property("1", "Athlone", "House", "m1"),
                property("2", "Athlone West", "Apartment", "m2"),
                property("3", "Dublin", "House", "m3")
        );

        ComboBox<String> propertyTypeFilter = getPrivateField(view, "propertyTypeFilter", ComboBox.class);
        TextField searchField = getPrivateField(view, "searchField", TextField.class);
        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);
        Label totalPropertiesValue = getPrivateField(view, "totalPropertiesValue", Label.class);
        Label uniqueTypesValue = getPrivateField(view, "uniqueTypesValue", Label.class);
        Label tableFooterLabel = getPrivateField(view, "tableFooterLabel", Label.class);

        propertyTypeFilter.setValue("House");
        searchField.setText("Athlone");

        assertEquals(1, propertyTable.getItems().size());
        assertEquals("Athlone", propertyTable.getItems().get(0).getAddress());
        assertEquals("1", totalPropertiesValue.getText());
        assertEquals("1", uniqueTypesValue.getText());
        assertEquals("Showing 1 property", tableFooterLabel.getText());
    }

    @Test
    void propertyView_search_isCaseInsensitive() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyView view = createView(
                property("1", "Athlone", "House", "m1"),
                property("2", "Dublin", "Apartment", "m2")
        );

        TextField searchField = getPrivateField(view, "searchField", TextField.class);
        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);

        searchField.setText("dUbLiN");

        assertEquals(1, propertyTable.getItems().size());
        assertEquals("Dublin", propertyTable.getItems().get(0).getAddress());
    }

    @Test
    void propertyView_typeFilter_withSingleMatchingType_updatesLatestPropertyCorrectly() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyView view = createView(
                property("1", "Athlone", "House", "m1"),
                property("2", "Dublin", "Apartment", "m2"),
                property("3", "Galway", "House", "m3")
        );

        ComboBox<String> propertyTypeFilter = getPrivateField(view, "propertyTypeFilter", ComboBox.class);
        Label latestPropertyValue = getPrivateField(view, "latestPropertyValue", Label.class);

        propertyTypeFilter.setValue("Apartment");

        assertEquals("Dublin", latestPropertyValue.getText());
    }

    @Test
    void propertyView_maintenanceStaffStillSeesLoadedData() throws Exception {
        SessionManager.startSession("token", "staff@test.com", "MAINTENANCE_STAFF");

        PropertyView view = createView(
                property("1", "Athlone", "House", "m1"),
                property("2", "Dublin", "Apartment", "m2")
        );

        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);
        Label totalPropertiesValue = getPrivateField(view, "totalPropertiesValue", Label.class);
        Label activeRoleValue = getPrivateField(view, "activeRoleValue", Label.class);

        assertEquals(2, propertyTable.getItems().size());
        assertEquals("2", totalPropertiesValue.getText());
        assertEquals("Maintenance staff", activeRoleValue.getText());
    }

    @Test
    void propertyView_duplicatePropertyTypes_onlyAppearOnceInFilter() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyView view = createView(
                property("1", "Athlone", "House", "m1"),
                property("2", "Dublin", "House", "m2"),
                property("3", "Galway", "Apartment", "m3")
        );

        ComboBox<String> propertyTypeFilter = getPrivateField(view, "propertyTypeFilter", ComboBox.class);

        long houseCount = propertyTypeFilter.getItems().stream()
                .filter("House"::equals)
                .count();

        assertEquals(1, houseCount);
        assertTrue(propertyTypeFilter.getItems().contains("Apartment"));
    }

    @Test
    void propertyView_emptyDataset_setsExpectedSummaryValues() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");
        PropertyView view = createView(List.of());

        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);
        Label totalPropertiesValue = getPrivateField(view, "totalPropertiesValue", Label.class);
        Label uniqueTypesValue = getPrivateField(view, "uniqueTypesValue", Label.class);
        Label latestPropertyValue = getPrivateField(view, "latestPropertyValue", Label.class);
        Label tableFooterLabel = getPrivateField(view, "tableFooterLabel", Label.class);
        Label messageLabel = getPrivateField(view, "messageLabel", Label.class);

        assertEquals(0, propertyTable.getItems().size());
        assertEquals("0", totalPropertiesValue.getText());
        assertEquals("0", uniqueTypesValue.getText());
        assertEquals("No properties", latestPropertyValue.getText());
        assertEquals("Showing 0 properties", tableFooterLabel.getText());
        assertEquals("Properties loaded successfully.", messageLabel.getText());
    }

    @Test
    void propertyView_serviceIsCalledOnceOnInitialLoad() {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of());

        new PropertyView(fakeService);

        verify(fakeService, times(1)).getProperties();
    }

    @Test
    void handleAddProperty_readOnly_showsMessage() throws Exception {
        SessionManager.startSession("token", "staff@test.com", "MAINTENANCE_STAFF");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of());

        PropertyView view = new PropertyView(fakeService);
        Label messageLabel = getPrivateField(view, "messageLabel", Label.class);

        invokePrivateMethod(view, "handleAddProperty");

        assertTrue(messageLabel.isVisible());
        assertEquals("Maintenance staff can view property information only.", messageLabel.getText());
    }

    @Test
    void handleEditProperty_noSelection_showsMessage() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of(
                new Property("1", "Athlone", "House", "manager1")
        ));

        PropertyView view = new PropertyView(fakeService);
        Label messageLabel = getPrivateField(view, "messageLabel", Label.class);

        invokePrivateMethod(view, "handleEditProperty");

        assertTrue(messageLabel.isVisible());
        assertEquals("Please select a property to edit.", messageLabel.getText());
    }

    @Test
    void handleDeleteProperty_noSelection_showsMessage() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of(
                new Property("1", "Athlone", "House", "manager1")
        ));

        PropertyView view = new PropertyView(fakeService);
        Label messageLabel = getPrivateField(view, "messageLabel", Label.class);

        invokePrivateMethod(view, "handleDeleteProperty");

        assertTrue(messageLabel.isVisible());
        assertEquals("Please select a property to delete.", messageLabel.getText());
    }

    @Test
    void propertyView_createFormLabel_returnsStyledLabel() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of());

        PropertyView view = new PropertyView(fakeService);

        Method method = PropertyView.class.getDeclaredMethod("createFormLabel", String.class);
        method.setAccessible(true);

        Label label = (Label) method.invoke(view, "Address");

        assertEquals("Address", label.getText());
        assertNotNull(label.getStyle());
        assertTrue(label.getStyle().contains("-fx-font-size"));
    }

    @Test
    void propertyView_createDialogField_returnsConfiguredField() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of());

        PropertyView view = new PropertyView(fakeService);

        Method method = PropertyView.class.getDeclaredMethod("createDialogField", String.class, String.class);
        method.setAccessible(true);

        TextField field = (TextField) method.invoke(view, "Athlone", "Enter property address");

        assertEquals("Athlone", field.getText());
        assertEquals("Enter property address", field.getPromptText());
        assertEquals(42.0, field.getPrefHeight());
        assertNotNull(field.getStyle());
    }

    @Test
    void propertyView_dialogStyleHelpers_returnExpectedStyleStrings() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of());

        PropertyView view = new PropertyView(fakeService);

        Method paneStyle = PropertyView.class.getDeclaredMethod("dialogPaneStyle");
        Method primaryStyle = PropertyView.class.getDeclaredMethod("dialogPrimaryButtonStyle");
        Method secondaryStyle = PropertyView.class.getDeclaredMethod("dialogSecondaryButtonStyle");

        paneStyle.setAccessible(true);
        primaryStyle.setAccessible(true);
        secondaryStyle.setAccessible(true);

        String pane = (String) paneStyle.invoke(view);
        String primary = (String) primaryStyle.invoke(view);
        String secondary = (String) secondaryStyle.invoke(view);

        assertTrue(pane.contains("-fx-background-color"));
        assertTrue(primary.contains("-fx-background-color"));
        assertTrue(primary.contains("-fx-text-fill"));
        assertTrue(secondary.contains("-fx-border-color"));
    }

    @Test
    void propertyView_helperMethods_returnExpectedValues() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of());

        PropertyView view = new PropertyView(fakeService);

        Method safe = PropertyView.class.getDeclaredMethod("safe", String.class);
        Method formatRole = PropertyView.class.getDeclaredMethod("formatRole", String.class);
        Method toReference = PropertyView.class.getDeclaredMethod("toReference", String.class);

        safe.setAccessible(true);
        formatRole.setAccessible(true);
        toReference.setAccessible(true);

        assertEquals("", safe.invoke(view, new Object[]{null}));
        assertEquals("abc", safe.invoke(view, "abc"));

        assertEquals("-", formatRole.invoke(view, new Object[]{null}));
        assertEquals("Property manager", formatRole.invoke(view, "PROPERTY_MANAGER"));
        assertEquals("Maintenance staff", formatRole.invoke(view, "MAINTENANCE_STAFF"));

        assertEquals("-", toReference.invoke(view, new Object[]{null}));
        assertEquals("PROP-ABC", toReference.invoke(view, "abc"));
        assertEquals("PROP-12345678", toReference.invoke(view, "123456789"));
    }

    @Test
    void propertyView_refreshFilterOptions_preservesValidSelection() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        List<Property> properties = List.of(
                new Property("1", "Athlone", "House", "m1"),
                new Property("2", "Dublin", "Apartment", "m2"),
                new Property("3", "Galway", "House", "m3")
        );

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(properties);

        PropertyView view = new PropertyView(fakeService);

        ComboBox<String> propertyTypeFilter = getPrivateField(view, "propertyTypeFilter", ComboBox.class);
        propertyTypeFilter.setValue("House");

        Method method = PropertyView.class.getDeclaredMethod("refreshFilterOptions");
        method.setAccessible(true);
        method.invoke(view);

        assertEquals("House", propertyTypeFilter.getValue());
    }

    @Test
    void propertyView_showMessage_setsSuccessState() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");
        PropertyView view = createView(List.of());

        Method method = PropertyView.class.getDeclaredMethod("showMessage", String.class, boolean.class);
        method.setAccessible(true);
        method.invoke(view, "Saved successfully.", false);

        Label messageLabel = getPrivateField(view, "messageLabel", Label.class);

        assertEquals("Saved successfully.", messageLabel.getText());
        assertTrue(messageLabel.isVisible());
        assertTrue(messageLabel.isManaged());
        assertTrue(messageLabel.getStyle().contains("#16a34a"));
    }

    @Test
    void propertyView_showMessage_setsErrorState() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");
        PropertyView view = createView(List.of());

        Method method = PropertyView.class.getDeclaredMethod("showMessage", String.class, boolean.class);
        method.setAccessible(true);
        method.invoke(view, "Something went wrong.", true);

        Label messageLabel = getPrivateField(view, "messageLabel", Label.class);

        assertEquals("Something went wrong.", messageLabel.getText());
        assertTrue(messageLabel.isVisible());
        assertTrue(messageLabel.isManaged());
        assertTrue(messageLabel.getStyle().contains("#dc2626"));
    }

    @Test
    void propertyView_hideMessage_clearsMessageState() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");
        PropertyView view = createView(List.of());

        Method showMethod = PropertyView.class.getDeclaredMethod("showMessage", String.class, boolean.class);
        showMethod.setAccessible(true);
        showMethod.invoke(view, "Temporary message", false);

        Method hideMethod = PropertyView.class.getDeclaredMethod("hideMessage");
        hideMethod.setAccessible(true);
        hideMethod.invoke(view);

        Label messageLabel = getPrivateField(view, "messageLabel", Label.class);

        assertEquals("", messageLabel.getText());
        assertFalse(messageLabel.isVisible());
        assertFalse(messageLabel.isManaged());
    }

    @Test
    void propertyView_refreshFilterOptions_clearsInvalidSelection() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyView view = createView(
                property("1", "Athlone", "House", "m1"),
                property("2", "Dublin", "Apartment", "m2")
        );

        ComboBox<String> propertyTypeFilter = getPrivateField(view, "propertyTypeFilter", ComboBox.class);
        propertyTypeFilter.setValue("Studio");

        Method method = PropertyView.class.getDeclaredMethod("refreshFilterOptions");
        method.setAccessible(true);
        method.invoke(view);

        assertNull(propertyTypeFilter.getValue());
    }

    @Test
    void propertyView_createEmptyStateLabel_returnsExpectedText() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");
        PropertyView view = createView(List.of());

        Method method = PropertyView.class.getDeclaredMethod("createEmptyStateLabel");
        method.setAccessible(true);

        Label label = (Label) method.invoke(view);

        assertEquals("No properties match the current view.", label.getText());
        assertTrue(label.getStyle().contains("-fx-font-size"));
    }

    @Test
    void propertyView_styleHelpers_returnNonEmptyStyles() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");
        PropertyView view = createView(List.of());

        Method commonFieldStyle = PropertyView.class.getDeclaredMethod("commonFieldStyle");
        Method dialogFieldStyle = PropertyView.class.getDeclaredMethod("dialogFieldStyle");
        Method cardStyle = PropertyView.class.getDeclaredMethod("cardStyle");
        Method tableCardStyle = PropertyView.class.getDeclaredMethod("tableCardStyle");
        Method messageStyle = PropertyView.class.getDeclaredMethod("messageStyle", String.class);

        commonFieldStyle.setAccessible(true);
        dialogFieldStyle.setAccessible(true);
        cardStyle.setAccessible(true);
        tableCardStyle.setAccessible(true);
        messageStyle.setAccessible(true);

        String common = (String) commonFieldStyle.invoke(view);
        String dialog = (String) dialogFieldStyle.invoke(view);
        String card = (String) cardStyle.invoke(view);
        String tableCard = (String) tableCardStyle.invoke(view);
        String message = (String) messageStyle.invoke(view, "#000000");

        assertTrue(common.contains("-fx-background-color"));
        assertTrue(dialog.contains("-fx-border-color"));
        assertTrue(card.contains("-fx-effect"));
        assertTrue(tableCard.contains("-fx-effect"));
        assertTrue(message.contains("-fx-text-fill"));
    }

    @Test
    void propertyView_searchMatchesReferenceValue() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyView view = createView(
                property("abc12345", "Athlone", "House", "m1"),
                property("xyz99999", "Dublin", "Apartment", "m2")
        );

        TextField searchField = getPrivateField(view, "searchField", TextField.class);
        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);

        searchField.setText("PROP-ABC12345");

        assertEquals(1, propertyTable.getItems().size());
        assertEquals("Athlone", propertyTable.getItems().get(0).getAddress());
    }

    @Test
    void propertyView_searchMatchesManagerId() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyView view = createView(
                property("1", "Athlone", "House", "manager-a"),
                property("2", "Dublin", "Apartment", "manager-b")
        );

        TextField searchField = getPrivateField(view, "searchField", TextField.class);
        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);

        searchField.setText("manager-b");

        assertEquals(1, propertyTable.getItems().size());
        assertEquals("Dublin", propertyTable.getItems().get(0).getAddress());
    }

    @ParameterizedTest
    @CsvSource({
            "MAINTENANCE_STAFF,handleAddProperty,'Maintenance staff can view property information only.'",
            "PROPERTY_MANAGER,handleEditProperty,'Please select a property to edit.'",
            "PROPERTY_MANAGER,handleDeleteProperty,'Please select a property to delete.'"
    })
    void propertyView_actionWithoutRequiredState_showsExpectedMessage(
            String role,
            String methodName,
            String expectedMessage
    ) throws Exception {
        SessionManager.startSession("token", "user@test.com", role);

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of());

        PropertyView view = new PropertyView(fakeService);

        Method method = PropertyView.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(view);

        Label messageLabel = getPrivateField(view, "messageLabel", Label.class);

        assertEquals(expectedMessage, messageLabel.getText());
        assertTrue(messageLabel.isVisible());
    }

    @ParameterizedTest
    @CsvSource({
            "toReference,abc123,PROP-ABC123",
            "toReference,'   ',-",
            "formatRole,' ',-"
    })
    void propertyView_helperMethod_formatsExpectedValue(
            String methodName,
            String input,
            String expected
    ) throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");
        PropertyView view = createView(List.of());

        Method method = PropertyView.class.getDeclaredMethod(methodName, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(view, input);

        assertEquals(expected, result);
    }

    @Test
    void propertyView_isMaintenanceStaff_returnsTrueForStaff() throws Exception {
        SessionManager.startSession("token", "staff@test.com", "MAINTENANCE_STAFF");
        PropertyView view = createView(List.of());

        Method method = PropertyView.class.getDeclaredMethod("isMaintenanceStaff");
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(view);

        assertTrue(result);
    }

    @Test
    void propertyView_isMaintenanceStaff_returnsFalseForManager() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");
        PropertyView view = createView(List.of());

        Method method = PropertyView.class.getDeclaredMethod("isMaintenanceStaff");
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(view);

        assertFalse(result);
    }

    @Test
    void propertyView_searchMatchesPropertyType() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyView view = createView(
                property("1", "Athlone", "House", "m1"),
                property("2", "Dublin", "Apartment", "m2")
        );

        TextField searchField = getPrivateField(view, "searchField", TextField.class);
        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);

        searchField.setText("Apartment");

        assertEquals(1, propertyTable.getItems().size());
        assertEquals("Dublin", propertyTable.getItems().get(0).getAddress());
    }

    @Test
    void propertyView_searchMatchesPropertyId() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyView view = createView(
                property("prop-1", "Athlone", "House", "m1"),
                property("prop-2", "Dublin", "Apartment", "m2")
        );

        TextField searchField = getPrivateField(view, "searchField", TextField.class);
        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);

        searchField.setText("prop-2");

        assertEquals(1, propertyTable.getItems().size());
        assertEquals("Dublin", propertyTable.getItems().get(0).getAddress());
    }

    @Test
    void handleAddProperty_success_createsPropertyAndShowsSuccessMessage() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyApiService fakeService = mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of());

        TestablePropertyView view = new TestablePropertyView(fakeService);
        view.dialogResult = Optional.of(new PropertyView.PropertyFormData("Athlone", "House"));

        invokePrivateMethod(view, "handleAddProperty");

        verify(fakeService).createProperty("Athlone", "House");
        verify(fakeService, times(2)).getProperties();

        Label messageLabel = getPrivateField(view, "messageLabel", Label.class);
        assertEquals("Property created successfully.", messageLabel.getText());
    }

    @Test
    void handleAddProperty_serviceFailure_showsErrorMessage() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyApiService fakeService = mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of());
        when(fakeService.createProperty("Athlone", "House"))
                .thenThrow(new PropertyApiService.PropertyApiException("boom"));

        TestablePropertyView view = new TestablePropertyView(fakeService);
        view.dialogResult = Optional.of(new PropertyView.PropertyFormData("Athlone", "House"));

        invokePrivateMethod(view, "handleAddProperty");

        Label messageLabel = getPrivateField(view, "messageLabel", Label.class);
        assertEquals("Failed to create property.", messageLabel.getText());
    }

    @Test
    void handleAddProperty_cancelledDialog_doesNothing() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyApiService fakeService = mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of());

        TestablePropertyView view = new TestablePropertyView(fakeService);
        view.dialogResult = Optional.empty();

        invokePrivateMethod(view, "handleAddProperty");

        verify(fakeService, never()).createProperty(anyString(), anyString());
        verify(fakeService, times(1)).getProperties();
    }

    @Test
    void handleEditProperty_success_updatesPropertyAndShowsSuccessMessage() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        Property selected = property("1", "Athlone", "House", "m1");

        PropertyApiService fakeService = mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of(selected));

        TestablePropertyView view = new TestablePropertyView(fakeService);
        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);
        propertyTable.getSelectionModel().select(0);
        view.dialogResult = Optional.of(new PropertyView.PropertyFormData("Galway", "Apartment"));

        invokePrivateMethod(view, "handleEditProperty");

        verify(fakeService).updateProperty("1", "Galway", "Apartment");
        verify(fakeService, times(2)).getProperties();

        Label messageLabel = getPrivateField(view, "messageLabel", Label.class);
        assertEquals("Property updated successfully.", messageLabel.getText());
    }

    @Test
    void handleEditProperty_serviceFailure_showsErrorMessage() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        Property selected = property("1", "Athlone", "House", "m1");

        PropertyApiService fakeService = mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of(selected));
        doThrow(new PropertyApiService.PropertyApiException("boom"))
                .when(fakeService).updateProperty("1", "Galway", "Apartment");

        TestablePropertyView view = new TestablePropertyView(fakeService);
        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);
        propertyTable.getSelectionModel().select(0);
        view.dialogResult = Optional.of(new PropertyView.PropertyFormData("Galway", "Apartment"));

        invokePrivateMethod(view, "handleEditProperty");

        Label messageLabel = getPrivateField(view, "messageLabel", Label.class);
        assertEquals("Failed to update property.", messageLabel.getText());
    }

    @Test
    void handleEditProperty_cancelledDialog_doesNothing() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        Property selected = property("1", "Athlone", "House", "m1");

        PropertyApiService fakeService = mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of(selected));

        TestablePropertyView view = new TestablePropertyView(fakeService);
        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);
        propertyTable.getSelectionModel().select(0);
        view.dialogResult = Optional.empty();

        invokePrivateMethod(view, "handleEditProperty");

        verify(fakeService, never()).updateProperty(anyString(), anyString(), anyString());
        verify(fakeService, times(1)).getProperties();
    }

    @Test
    void handleDeleteProperty_confirmed_success_deletesPropertyAndShowsSuccessMessage() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        Property selected = property("1", "Athlone", "House", "m1");

        PropertyApiService fakeService = mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of(selected));

        TestablePropertyView view = new TestablePropertyView(fakeService);
        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);
        propertyTable.getSelectionModel().select(0);
        view.confirmDeleteResult = true;

        invokePrivateMethod(view, "handleDeleteProperty");

        verify(fakeService).deleteProperty("1");
        verify(fakeService, times(2)).getProperties();

        Label messageLabel = getPrivateField(view, "messageLabel", Label.class);
        assertEquals("Property deleted successfully.", messageLabel.getText());
    }

    @Test
    void handleDeleteProperty_confirmed_failure_showsErrorMessage() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        Property selected = property("1", "Athlone", "House", "m1");

        PropertyApiService fakeService = mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of(selected));
        doThrow(new PropertyApiService.PropertyApiException("boom"))
                .when(fakeService).deleteProperty("1");

        TestablePropertyView view = new TestablePropertyView(fakeService);
        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);
        propertyTable.getSelectionModel().select(0);
        view.confirmDeleteResult = true;

        invokePrivateMethod(view, "handleDeleteProperty");

        Label messageLabel = getPrivateField(view, "messageLabel", Label.class);
        assertEquals("Failed to delete property.", messageLabel.getText());
    }

    @Test
    void handleDeleteProperty_cancelled_doesNothing() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        Property selected = property("1", "Athlone", "House", "m1");

        PropertyApiService fakeService = mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of(selected));

        TestablePropertyView view = new TestablePropertyView(fakeService);
        TableView<Property> propertyTable = getPrivateField(view, "propertyTable", TableView.class);
        propertyTable.getSelectionModel().select(0);
        view.confirmDeleteResult = false;

        invokePrivateMethod(view, "handleDeleteProperty");

        verify(fakeService, never()).deleteProperty(anyString());
        verify(fakeService, times(1)).getProperties();
    }

    private PropertyView createView(List<Property> properties) {
        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(properties);
        return new PropertyView(fakeService);
    }

    private PropertyView createView(Property... properties) {
        return createView(List.of(properties));
    }

    private Property property(String id, String address, String propertyType, String managerId) {
        return new Property(id, address, propertyType, managerId);
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object target, String fieldName, Class<T> type) throws Exception {
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                return (T) field.get(target);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private static class TestablePropertyView extends PropertyView {
        Optional<PropertyFormData> dialogResult = Optional.empty();
        boolean confirmDeleteResult = false;

        TestablePropertyView(PropertyApiService propertyApiService) {
            super(propertyApiService);
        }

        @Override
        protected Optional<PropertyFormData> openPropertyFormDialog(
                String title,
                String header,
                String defaultAddress,
                String defaultPropertyType
        ) {
            return dialogResult;
        }

        @Override
        protected boolean confirmDelete(Property selected) {
            return confirmDeleteResult;
        }
    }
}