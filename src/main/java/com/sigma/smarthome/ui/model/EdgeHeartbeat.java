package com.sigma.smarthome.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EdgeHeartbeat {

    private String deviceId;
    private String propertyId;
    private String status;
    private double temperature;
    private double humidity;
    private String timestamp;
    private Long secondsSinceLastHeartbeat;
    private String statusNote;
    private String message;

    public String getDeviceId() {
        return deviceId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public String getStatus() {
        return status;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Long getSecondsSinceLastHeartbeat() {
        return secondsSinceLastHeartbeat;
    }

    public String getStatusNote() {
        return statusNote;
    }

    public String getMessage() {
        return message;
    }
}