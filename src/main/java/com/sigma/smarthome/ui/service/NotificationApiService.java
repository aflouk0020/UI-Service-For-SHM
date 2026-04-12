package com.sigma.smarthome.ui.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sigma.smarthome.ui.model.NotificationItem;
import com.sigma.smarthome.ui.util.ApiConfig;
import com.sigma.smarthome.ui.util.SessionManager;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class NotificationApiService {

    private static final String DEFAULT_BASE_URL = ApiConfig.API_GATEWAY_BASE_URL;
    private static final String BASE_PATH = "/api/v1/notifications";

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public NotificationApiService() {
        this(DEFAULT_BASE_URL);
    }

    public NotificationApiService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public List<NotificationItem> getNotifications(int page, int size) {
        try {
            String userId = extractUserIdFromJwt();

            String path = BASE_PATH + "/" + encode(userId)
                    + "?page=" + page
                    + "&size=" + size;

            HttpRequest request = baseRequest(path)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (!isSuccess(response.statusCode())) {
                throw new NotificationApiException(
                        "Failed to load notifications. HTTP " + response.statusCode() + " - " + response.body()
                );
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode contentNode = root.get("content");

            List<NotificationItem> notifications = new ArrayList<>();

            if (contentNode != null && contentNode.isArray()) {
                for (JsonNode itemNode : contentNode) {
                    NotificationItem item = new NotificationItem();
                    item.setId(text(itemNode, "id"));
                    item.setUserId(text(itemNode, "userId"));
                    item.setTitle(text(itemNode, "title"));
                    item.setMessage(text(itemNode, "message"));
                    item.setType(text(itemNode, "type"));
                    item.setIsRead(itemNode.hasNonNull("isRead") ? itemNode.get("isRead").asBoolean() : null);
                    item.setCreatedAt(text(itemNode, "createdAt"));
                    notifications.add(item);
                }
            }

            return notifications;

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new NotificationApiException("Failed to load notifications.", ex);
        } catch (IOException ex) {
            throw new NotificationApiException("Failed to load notifications.", ex);
        }
    }
    
    public void markAsRead(String notificationId) {
        try {
            HttpRequest request = baseRequest(BASE_PATH + "/" + encode(notificationId) + "/read")
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (!isSuccess(response.statusCode())) {
                throw new NotificationApiException(
                        "Failed to mark notification as read. HTTP " + response.statusCode() + " - " + response.body()
                );
            }

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new NotificationApiException("Failed to mark notification as read.", ex);
        } catch (IOException ex) {
            throw new NotificationApiException("Failed to mark notification as read.", ex);
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
            throw new NotificationApiException("Missing access token.");
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new NotificationApiException("Invalid JWT format.");
            }

            byte[] decodedPayload = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(decodedPayload, StandardCharsets.UTF_8);
            JsonNode payload = objectMapper.readTree(payloadJson);

            String subject = text(payload, "sub");
            if (subject == null || subject.isBlank()) {
                throw new NotificationApiException("Missing user id in token.");
            }

            return subject;
        } catch (IllegalArgumentException | IOException ex) {
            throw new NotificationApiException("Failed to parse JWT token.", ex);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field == null || field.isNull() ? "" : field.asText();
    }

    public static class NotificationApiException extends RuntimeException {
        public NotificationApiException(String message) {
            super(message);
        }

        public NotificationApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}