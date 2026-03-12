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

    private static final String DEFAULT_BASE_URL = "http://localhost:8081";
    private static final String LOGIN_ENDPOINT = "/auth/login";
    private static final String REGISTER_ENDPOINT = "/auth/register";

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
                        "email", email,
                        "password", password
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + LOGIN_ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode json = objectMapper.readTree(response.body());
            String accessToken = json.path("accessToken").asText(null);

            if (accessToken == null || accessToken.isBlank()) {
                throw new RuntimeException("Login failed. Missing access token in response.");
            }

            JwtUserInfo userInfo = extractUserInfoFromJwt(accessToken);

            return new LoginResult(
                    accessToken,
                    userInfo.email(),
                    userInfo.role()
            );
        }

        throw new RuntimeException(
                "Login failed. HTTP " + response.statusCode() + " - " + response.body()
        );
    }

    public String register(String email, String password, String role) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(
                Map.of(
                        "email", email,
                        "password", password,
                        "role", role
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + REGISTER_ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode json = objectMapper.readTree(response.body());
            String registeredEmail = json.path("email").asText(null);

            if (registeredEmail == null || registeredEmail.isBlank()) {
                throw new RuntimeException("Registration failed. Missing email in response.");
            }

            return registeredEmail;
        }

        throw new RuntimeException(
                "Registration failed. HTTP " + response.statusCode() + " - " + response.body()
        );
    }

    private JwtUserInfo extractUserInfoFromJwt(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) {
                throw new RuntimeException("Invalid JWT format.");
            }

            byte[] decodedPayload = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(decodedPayload, StandardCharsets.UTF_8);

            JsonNode payload = objectMapper.readTree(payloadJson);

            String email = payload.path("email").asText("");
            String role = payload.path("role").asText("");

            return new JwtUserInfo(email, role);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to parse JWT token.", ex);
        }
    }

    private record JwtUserInfo(String email, String role) {
    }

    public record LoginResult(String accessToken, String email, String role) {
    }
}