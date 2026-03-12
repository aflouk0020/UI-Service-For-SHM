package com.sigma.smarthome.ui.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Property {

    @JsonProperty("propertyId")
    private String id;

    private String address;
    private String propertyType;
    private String managerId;

    public Property() {
    }

    public Property(String id, String address, String propertyType, String managerId) {
        this.id = id;
        this.address = address;
        this.propertyType = propertyType;
        this.managerId = managerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }
}