package com.sigma.smarthome.ui.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    public String login(String email, String password) throws IOException, InterruptedException {
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

            if (jsonNode.has("accessToken")) {
                return jsonNode.get("accessToken").asText();
            }

            throw new IllegalStateException("Login succeeded but accessToken was not found.");
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
}