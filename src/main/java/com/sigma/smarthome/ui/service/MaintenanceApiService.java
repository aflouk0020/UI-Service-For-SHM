package com.sigma.smarthome.ui.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sigma.smarthome.ui.model.MaintenanceHistoryItem;
import com.sigma.smarthome.ui.model.MaintenanceRequest;
import com.sigma.smarthome.ui.util.ApiConfig;
import com.sigma.smarthome.ui.util.SessionManager;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class MaintenanceApiService {

    private static final String DEFAULT_BASE_URL = ApiConfig.API_GATEWAY_BASE_URL;
    private static final String BASE_PATH = "/api/v1/maintenance-requests";

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public MaintenanceApiService() {
        this(DEFAULT_BASE_URL);
    }

    public MaintenanceApiService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public List<MaintenanceRequest> getRequests(String status, String priority) {
        try {
            StringBuilder path = new StringBuilder(BASE_PATH);

            boolean hasStatus = status != null && !status.isBlank();
            boolean hasPriority = priority != null && !priority.isBlank();

            if (hasStatus || hasPriority) {
                path.append("?");
                if (hasStatus) {
                    path.append("status=").append(encode(status));
                }
                if (hasPriority) {
                    if (hasStatus) {
                        path.append("&");
                    }
                    path.append("priority=").append(encode(priority));
                }
            }

            HttpRequest request = baseRequest(path.toString())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (isSuccess(response.statusCode())) {
                return objectMapper.readValue(response.body(), new TypeReference<>() {});
            }

            throw new MaintenanceApiException("Failed to load maintenance requests. " + extractErrorMessage(response));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new MaintenanceApiException("Failed to load maintenance requests.", ex);
        } catch (IOException ex) {
            throw new MaintenanceApiException("Failed to load maintenance requests.", ex);
        }
    }

    public MaintenanceRequest createRequest(String propertyId, String description, String priority) {
        try {
            String requestBody = objectMapper.writeValueAsString(
                    Map.of(
                            "propertyId", propertyId,
                            "createdByUserId", extractUserIdFromJwt(),
                            "description", description,
                            "priority", priority
                    )
            );

            HttpRequest request = baseRequest(BASE_PATH)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (isSuccess(response.statusCode())) {
                if (response.body() == null || response.body().isBlank()) {
                    return null;
                }
                return objectMapper.readValue(response.body(), MaintenanceRequest.class);
            }

            throw new MaintenanceApiException("Failed to create maintenance request. HTTP " + response.statusCode() + " - " + response.body());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new MaintenanceApiException("Failed to create maintenance request.", ex);
        } catch (IOException ex) {
            throw new MaintenanceApiException("Failed to create maintenance request.", ex);
        }
    }
    
    private String extractErrorMessage(HttpResponse<String> response) {
        try {
            if (response.body() == null || response.body().isBlank()) {
                return "HTTP " + response.statusCode();
            }
            return response.body();
        } catch (Exception ex) {
            return "HTTP " + response.statusCode();
        }
    }

    public void assignStaff(String requestId, String assignedStaffId) {
        try {
            String requestBody = objectMapper.writeValueAsString(
                    Map.of("assignedStaffId", assignedStaffId)
            );

            HttpRequest request = baseRequest(BASE_PATH + "/" + requestId + "/assign")
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (isSuccess(response.statusCode())) {
                return;
            }

            throw new MaintenanceApiException("Failed to assign maintenance staff. HTTP " + response.statusCode() + " - " + response.body());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new MaintenanceApiException("Failed to assign maintenance staff.", ex);
        } catch (IOException ex) {
            throw new MaintenanceApiException("Failed to assign maintenance staff.", ex);
        }
    }

    public void updateStatus(String requestId, String status) {
        try {
            String requestBody = objectMapper.writeValueAsString(
                    Map.of("status", status)
            );

            HttpRequest request = baseRequest(BASE_PATH + "/" + requestId + "/status")
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (isSuccess(response.statusCode())) {
                return;
            }

            throw new MaintenanceApiException("Failed to update maintenance status. HTTP " + response.statusCode() + " - " + response.body());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new MaintenanceApiException("Failed to update maintenance status.", ex);
        } catch (IOException ex) {
            throw new MaintenanceApiException("Failed to update maintenance status.", ex);
        }
    }

    public List<MaintenanceHistoryItem> getHistory(String requestId) {
        try {
            HttpRequest request = baseRequest(BASE_PATH + "/" + requestId + "/history")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (isSuccess(response.statusCode())) {
                return objectMapper.readValue(response.body(), new TypeReference<>() {});
            }

            throw new MaintenanceApiException("Failed to load maintenance history. HTTP " + response.statusCode());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new MaintenanceApiException("Failed to load maintenance history.", ex);
        } catch (IOException ex) {
            throw new MaintenanceApiException("Failed to load maintenance history.", ex);
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

    private String extractUserIdFromJwt() {
        String token = SessionManager.getAccessToken();
        if (token == null || token.isBlank()) {
            throw new MaintenanceApiException("Missing access token.");
        }

        try {
            String[] parts = token.split("\\.");
            String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            com.fasterxml.jackson.databind.JsonNode payload = objectMapper.readTree(payloadJson);

            String subject = payload.get("sub").asText();
            if (subject == null || subject.isBlank()) {
                throw new MaintenanceApiException("Missing user id in JWT subject.");
            }
            return subject;
        } catch (Exception ex) {
            throw new MaintenanceApiException("Failed to extract user id from JWT.", ex);
        }
    }

    private boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static class MaintenanceApiException extends RuntimeException {
        public MaintenanceApiException(String message) {
            super(message);
        }

        public MaintenanceApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}