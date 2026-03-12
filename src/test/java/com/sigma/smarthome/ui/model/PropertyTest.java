package com.sigma.smarthome.ui.model;

import com.sigma.smarthome.ui.model.Property;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PropertyTest {

    @Test
    void property_constructor_setsValues() {

        Property property = new Property(
                "1",
                "Dublin City",
                "HOUSE",
                "manager123"
        );

        assertEquals("1", property.getId());
        assertEquals("Dublin City", property.getAddress());
        assertEquals("HOUSE", property.getPropertyType());
        assertEquals("manager123", property.getManagerId());
    }

    @Test
    void property_setters_updateValues() {

        Property property = new Property();

        property.setId("10");
        property.setAddress("Galway");
        property.setPropertyType("APARTMENT");
        property.setManagerId("manager999");

        assertEquals("10", property.getId());
        assertEquals("Galway", property.getAddress());
        assertEquals("APARTMENT", property.getPropertyType());
        assertEquals("manager999", property.getManagerId());
    }
    @Test
    void propertyFieldsWorkCorrectly() {

        Property property = new Property();

        property.setId("123");
        property.setAddress("1 Main Street");
        property.setPropertyType("House");

        assertEquals("123", property.getId());
        assertEquals("1 Main Street", property.getAddress());
        assertEquals("House", property.getPropertyType());
    }
}