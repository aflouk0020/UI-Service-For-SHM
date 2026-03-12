package sigma.smarthome.ui.view;

import com.sigma.smarthome.ui.model.Property;
import com.sigma.smarthome.ui.service.PropertyApiService;
import com.sigma.smarthome.ui.util.SessionManager;
import com.sigma.smarthome.ui.view.PropertyView;
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
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PropertyViewTest {

    @BeforeAll
    static void initJavaFx() {
        new JFXPanel();
    }

    @BeforeEach
    void clearSession() {
        SessionManager.clearSession();
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object target, String fieldName, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    @Test
    void propertyView_createsSuccessfully_forPropertyManager() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of());

        PropertyView view = new PropertyView(fakeService);
        Parent root = view.getView();

        assertNotNull(view);
        assertNotNull(root);
    }

    @Test
    void propertyView_loadsPropertiesSuccessfully() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        Property p1 = new Property("1", "Athlone", "House", "manager1");
        Property p2 = new Property("2", "Dublin", "Apartment", "manager1");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of(p1, p2));

        PropertyView view = new PropertyView(fakeService);

        assertNotNull(view);
        assertNotNull(view.getView());
    }

    @Test
    void propertyView_handlesLoadFailure() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenThrow(new RuntimeException("API down"));

        PropertyView view = new PropertyView(fakeService);

        assertNotNull(view);
        assertNotNull(view.getView());
    }

    @Test
    void propertyView_isReadOnlyForMaintenanceStaff() throws Exception {
        SessionManager.startSession("token", "staff@test.com", "MAINTENANCE_STAFF");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of());

        PropertyView view = new PropertyView(fakeService);

        assertNotNull(view);
        assertNotNull(view.getView());
    }

    @Test
    void propertyView_handlesMultipleReturnedProperties() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        List<Property> properties = List.of(
                new Property("1", "Athlone", "House", "manager1"),
                new Property("2", "Galway", "Apartment", "manager1"),
                new Property("3", "Dublin", "Detached", "manager1")
        );

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(properties);

        PropertyView view = new PropertyView(fakeService);

        assertNotNull(view);
        assertNotNull(view.getView());
    }

    @Test
    void propertyView_createsSuccessfully_whenNoPropertiesExist() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of());

        PropertyView view = new PropertyView(fakeService);

        assertNotNull(view);
        assertNotNull(view.getView());
    }

    @Test
    void propertyView_handlesDifferentPropertyTypes() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        List<Property> properties = List.of(
                new Property("1", "Athlone", "House", "manager1"),
                new Property("2", "Athlone", "Apartment", "manager1"),
                new Property("3", "Athlone", "Studio", "manager1")
        );

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(properties);

        PropertyView view = new PropertyView(fakeService);

        assertNotNull(view.getView());
    }

    @Test
    void propertyView_handlesEmptyPropertyFields() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        List<Property> properties = List.of(
                new Property("", "", "", "")
        );

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(properties);

        PropertyView view = new PropertyView(fakeService);

        assertNotNull(view.getView());
    }

    @Test
    void propertyView_handlesLargePropertyList() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        List<Property> properties = List.of(
                new Property("1", "A", "House", "m"),
                new Property("2", "B", "House", "m"),
                new Property("3", "C", "House", "m"),
                new Property("4", "D", "House", "m"),
                new Property("5", "E", "House", "m")
        );

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(properties);

        PropertyView view = new PropertyView(fakeService);

        assertNotNull(view.getView());
    }

    @Test
    void propertyView_disablesWriteButtons_forMaintenanceStaff() throws Exception {
        SessionManager.startSession("token", "staff@test.com", "MAINTENANCE_STAFF");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of());

        PropertyView view = new PropertyView(fakeService);

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

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of());

        PropertyView view = new PropertyView(fakeService);

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

        List<Property> properties = List.of(
                new Property("1", "Athlone", "House", "manager1"),
                new Property("2", "Dublin", "Apartment", "manager1"),
                new Property("3", "Galway", "House", "manager2")
        );

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(properties);

        PropertyView view = new PropertyView(fakeService);

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

        List<Property> properties = List.of(
                new Property("1", "Athlone", "House", "m1"),
                new Property("2", "Dublin", "Apartment", "m2"),
                new Property("3", "Galway", "Studio", "m3")
        );

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(properties);

        PropertyView view = new PropertyView(fakeService);

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

        List<Property> properties = List.of(
                new Property("1", "Athlone", "House", "m1"),
                new Property("2", "Dublin", "Apartment", "m2"),
                new Property("3", "Galway", "House", "m3")
        );

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(properties);

        PropertyView view = new PropertyView(fakeService);

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

        List<Property> properties = List.of(
                new Property("", "", "", ""),
                new Property(null, null, null, null)
        );

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(properties);

        PropertyView view = new PropertyView(fakeService);

        Label totalPropertiesValue = getPrivateField(view, "totalPropertiesValue", Label.class);
        Label uniqueTypesValue = getPrivateField(view, "uniqueTypesValue", Label.class);
        Label latestPropertyValue = getPrivateField(view, "latestPropertyValue", Label.class);
        Label tableFooterLabel = getPrivateField(view, "tableFooterLabel", Label.class);

        assertEquals("2", totalPropertiesValue.getText());
        assertEquals("0", uniqueTypesValue.getText());
        assertEquals("No properties", latestPropertyValue.getText());
        assertEquals("Showing 2 properties", tableFooterLabel.getText());
    }
}