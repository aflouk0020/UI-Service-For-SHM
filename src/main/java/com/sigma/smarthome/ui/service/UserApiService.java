package com.sigma.smarthome.ui.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class UserApiService {

	private static final String DEFAULT_BASE_URL = com.sigma.smarthome.ui.util.ApiConfig.API_GATEWAY_BASE_URL;
    private static final String LOGIN_ENDPOINT = "/auth/login";
    private static final String REGISTER_ENDPOINT = "/auth/register";

    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_PASSWORD = "password";
    private static final String FIELD_ROLE = "role";
    private static final String FIELD_ACCESS_TOKEN = "accessToken";

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public UserApiService() {
        this(DEFAULT_BASE_URL);
    }

    public UserApiService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public LoginResult login(String email, String password) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(
                Map.of(
                        FIELD_EMAIL, email,
                        FIELD_PASSWORD, password
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + LOGIN_ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (!isSuccess(response.statusCode())) {
            throw new UserApiException(
                    "Login failed. HTTP " + response.statusCode() + " - " + response.body()
            );
        }

        JsonNode json = objectMapper.readTree(response.body());
        String accessToken = getNullableText(json, FIELD_ACCESS_TOKEN);

        if (accessToken == null || accessToken.isBlank()) {
            throw new UserApiException("Login failed. Missing access token in response.");
        }

        JwtUserInfo userInfo = extractUserInfoFromJwt(accessToken);

        return new LoginResult(
                accessToken,
                userInfo.email(),
                userInfo.role()
        );
    }

    public RegisterResult register(String email, String password, String role) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(
                Map.of(
                        FIELD_EMAIL, email,
                        FIELD_PASSWORD, password,
                        FIELD_ROLE, role
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + REGISTER_ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (!isSuccess(response.statusCode())) {
            throw new UserApiException(
                    "Registration failed. HTTP " + response.statusCode() + " - " + response.body()
            );
        }

        JsonNode json = objectMapper.readTree(response.body());
        String registeredEmail = getNullableText(json, FIELD_EMAIL);
        String registeredRole = getNullableText(json, FIELD_ROLE);

        if (registeredEmail == null || registeredEmail.isBlank()) {
            throw new UserApiException("Registration failed. Missing email in response.");
        }

        if (registeredRole == null || registeredRole.isBlank()) {
            registeredRole = role;
        }

        return new RegisterResult(registeredEmail, registeredRole);
    }

    private JwtUserInfo extractUserInfoFromJwt(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) {
                throw new UserApiException("Invalid JWT format.");
            }

            byte[] decodedPayload = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(decodedPayload, StandardCharsets.UTF_8);
            JsonNode payload = objectMapper.readTree(payloadJson);

            String email = getTextOrEmpty(payload, FIELD_EMAIL);
            String role = getTextOrEmpty(payload, FIELD_ROLE);

            return new JwtUserInfo(email, role);
        } catch (IllegalArgumentException | IOException ex) {
            throw new UserApiException("Failed to parse JWT token.", ex);
        }
    }

    private boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private String getNullableText(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode == null || fieldNode.isNull() ? null : fieldNode.asText();
    }

    private String getTextOrEmpty(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode == null || fieldNode.isNull() ? "" : fieldNode.asText();
    }

    private record JwtUserInfo(String email, String role) {
    }

    public record LoginResult(String accessToken, String email, String role) {
    }

    public record RegisterResult(String email, String role) {
    }

    public static class UserApiException extends RuntimeException {
        public UserApiException(String message) {
            super(message);
        }

        public UserApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}