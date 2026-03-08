package com.sigma.smarthome.ui.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sigma.smarthome.ui.model.Property;
import com.sigma.smarthome.ui.util.SessionManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class PropertyApiService {

    private static final String BASE_URL = "http://localhost:8082";
    private static final String PROPERTIES_ENDPOINT = "/properties";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PropertyApiService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public List<Property> getProperties() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PROPERTIES_ENDPOINT))
                .header("Authorization", "Bearer " + SessionManager.getAccessToken())
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Property>>() {});
        }

        throw new RuntimeException("Failed to load properties. HTTP " + response.statusCode() + " - " + response.body());
    }

    public Property createProperty(String address, String propertyType) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(
                Map.of(
                        "address", address,
                        "propertyType", propertyType
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PROPERTIES_ENDPOINT))
                .header("Authorization", "Bearer " + SessionManager.getAccessToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), Property.class);
        }

        throw new RuntimeException("Failed to create property. HTTP " + response.statusCode() + " - " + response.body());
    }

    public Property updateProperty(String propertyId, String address, String propertyType) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(
                Map.of(
                        "address", address,
                        "propertyType", propertyType
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PROPERTIES_ENDPOINT + "/" + propertyId))
                .header("Authorization", "Bearer " + SessionManager.getAccessToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), Property.class);
        }

        throw new RuntimeException("Failed to update property. HTTP " + response.statusCode() + " - " + response.body());
    }

    public void deleteProperty(String propertyId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PROPERTIES_ENDPOINT + "/" + propertyId))
                .header("Authorization", "Bearer " + SessionManager.getAccessToken())
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 204 || (response.statusCode() >= 200 && response.statusCode() < 300)) {
            return;
        }

        throw new RuntimeException("Failed to delete property. HTTP " + response.statusCode() + " - " + response.body());
    }
}