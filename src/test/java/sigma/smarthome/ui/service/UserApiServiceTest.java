package com.sigma.smarthome.ui.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserApiServiceTest {

    @Test
    void serviceCanBeCreated() {

        UserApiService service = new UserApiService();

        assertNotNull(service);
    }
}