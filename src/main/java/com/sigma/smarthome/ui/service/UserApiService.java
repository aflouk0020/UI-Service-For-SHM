package com.sigma.smarthome.ui.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;

public class UserApiService {

    private static final String BASE_URL = "http://localhost:8081";
    private static final String LOGIN_ENDPOINT = "/auth/login";
    private static final String REGISTER_ENDPOINT = "/auth/register";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public UserApiService() {
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
                .uri(URI.create(BASE_URL + LOGIN_ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode jsonNode = objectMapper.readTree(response.body());

            if (!jsonNode.has("accessToken")) {
                throw new IllegalStateException("Login succeeded but accessToken was not found.");
            }

            String accessToken = jsonNode.get("accessToken").asText();
            JwtUserInfo jwtUserInfo = extractUserInfoFromToken(accessToken);

            return new LoginResult(accessToken, jwtUserInfo.email(), jwtUserInfo.role());
        }

        throw new RuntimeException("Login failed. HTTP " + response.statusCode() + " - " + response.body());
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
                .uri(URI.create(BASE_URL + REGISTER_ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode jsonNode = objectMapper.readTree(response.body());

            if (jsonNode.has("email")) {
                return jsonNode.get("email").asText();
            }

            return response.body();
        }

        throw new RuntimeException("Registration failed. HTTP " + response.statusCode() + " - " + response.body());
    }

    private JwtUserInfo extractUserInfoFromToken(String token) throws IOException {
        String[] tokenParts = token.split("\\.");

        if (tokenParts.length < 2) {
            throw new IllegalArgumentException("Invalid JWT token.");
        }

        String payloadJson = new String(Base64.getUrlDecoder().decode(tokenParts[1]));
        JsonNode payloadNode = objectMapper.readTree(payloadJson);

        String email = payloadNode.has("email") ? payloadNode.get("email").asText() : "Unknown";
        String role = payloadNode.has("role") ? payloadNode.get("role").asText() : "Unknown";

        return new JwtUserInfo(email, role);
    }

    private record JwtUserInfo(String email, String role) {
    }

    public record LoginResult(String accessToken, String email, String role) {
    }
}