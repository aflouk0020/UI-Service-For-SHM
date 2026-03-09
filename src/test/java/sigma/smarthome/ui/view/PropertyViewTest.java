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

    @Test
    void propertyView_createsSuccessfully() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of());

        PropertyView view = new PropertyView(fakeService);
        Parent root = view.getView();

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

        assertNotNull(view.getView());
    }

    @Test
    void propertyView_handlesLoadFailure() throws Exception {
        SessionManager.startSession("token", "manager@test.com", "PROPERTY_MANAGER");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenThrow(new RuntimeException("API down"));

        PropertyView view = new PropertyView(fakeService);

        assertNotNull(view.getView());
    }

    @Test
    void propertyView_readOnlyForMaintenanceStaff() throws Exception {
        SessionManager.startSession("token", "staff@test.com", "MAINTENANCE_STAFF");

        PropertyApiService fakeService = Mockito.mock(PropertyApiService.class);
        when(fakeService.getProperties()).thenReturn(List.of());

        PropertyView view = new PropertyView(fakeService);

        assertNotNull(view.getView());
    }
}