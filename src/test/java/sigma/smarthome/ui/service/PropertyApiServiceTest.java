package sigma.smarthome.ui.service;

import com.sigma.smarthome.ui.service.PropertyApiService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PropertyApiServiceTest {

    @Test
    void service_instance_created() {

        PropertyApiService service = new PropertyApiService();

        assertNotNull(service);
    }

    @Test
    void multiple_instances_can_be_created() {

        PropertyApiService service1 = new PropertyApiService();
        PropertyApiService service2 = new PropertyApiService();

        assertNotNull(service1);
        assertNotNull(service2);
    }
}