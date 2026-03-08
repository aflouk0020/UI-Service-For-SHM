package sigma.smarthome.ui.service;

import org.junit.jupiter.api.Test;

import com.sigma.smarthome.ui.service.PropertyApiService;

import static org.junit.jupiter.api.Assertions.*;

class PropertyApiServiceTest {

    @Test
    void service_instance_created() {

        PropertyApiService service = new PropertyApiService();

        assertNotNull(service);
    }

}