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

public class PropertyApiService {

    private static final String DEFAULT_BASE_URL = "http://localhost:8082";
    private final String baseUrl;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PropertyApiService() {
        this(DEFAULT_BASE_URL);
    }

    public PropertyApiService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public List<Property> getProperties() {
        try {
            HttpRequest request = baseRequest("/properties")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), new TypeReference<>() {});
            }

            throw new RuntimeException("Failed to load properties. HTTP " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to load properties", e);
        }
    }

    public Property createProperty(String address, String propertyType) {
        try {
            String requestBody = objectMapper.writeValueAsString(
                    new CreateOrUpdatePropertyRequest(address, propertyType)
            );

            HttpRequest request = baseRequest("/properties")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), Property.class);
            }

            throw new RuntimeException("Failed to create property. HTTP " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to create property", e);
        }
    }

    public Property updateProperty(String propertyId, String address, String propertyType) {
        try {
            String requestBody = objectMapper.writeValueAsString(
                    new CreateOrUpdatePropertyRequest(address, propertyType)
            );

            HttpRequest request = baseRequest("/properties/" + propertyId)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), Property.class);
            }

            throw new RuntimeException("Failed to update property. HTTP " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to update property", e);
        }
    }

    public void deleteProperty(String propertyId) {
        try {
            HttpRequest request = baseRequest("/properties/" + propertyId)
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return;
            }

            throw new RuntimeException("Failed to delete property. HTTP " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to delete property", e);
        }
    }

    private HttpRequest.Builder baseRequest(String path) {
    	String token = SessionManager.getAccessToken();

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path));

        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }

        return builder;
    }

    private record CreateOrUpdatePropertyRequest(String address, String propertyType) {
    }
}