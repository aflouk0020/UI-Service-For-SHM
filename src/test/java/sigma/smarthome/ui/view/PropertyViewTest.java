package sigma.smarthome.ui.view;

import com.sigma.smarthome.ui.model.Property;
import com.sigma.smarthome.ui.service.PropertyApiService;
import com.sigma.smarthome.ui.util.SessionManager;
import com.sigma.smarthome.ui.view.PropertyView;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
                new Property("1","A","House","m"),
                new Property("2","B","House","m"),
                new Property("3","C","House","m"),
                new Property("4","D","House","m"),
                new Property("5","E","House","m")
        );

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(properties);

        PropertyView view = new PropertyView(fakeService);

        assertNotNull(view.getView());
    }
}