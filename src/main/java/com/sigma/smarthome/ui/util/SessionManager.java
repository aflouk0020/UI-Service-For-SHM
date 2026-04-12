package com.sigma.smarthome.ui.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SessionManager {

    private static String accessToken;
    private static String email;
    private static String role;

    public static void startSession(String token, String userEmail, String userRole) {
        accessToken = token;
        email = userEmail;
        role = userRole;
    }

    public static void clearSession() {
        accessToken = null;
        email = null;
        role = null;
    }

    public static boolean isLoggedIn() {
        return accessToken != null && !accessToken.isBlank();
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static String getEmail() {
        return email;
    }

    public static String getRole() {
        return role;
    }

    public static String getUserId() {
        if (accessToken == null || accessToken.isBlank()) {
            return "";
        }

        try {
            String[] parts = accessToken.split("\\.");
            if (parts.length < 2) {
                return "";
            }

            String payloadJson = new String(
                    Base64.getUrlDecoder().decode(parts[1]),
                    StandardCharsets.UTF_8
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode payload = mapper.readTree(payloadJson);

            JsonNode subNode = payload.get("sub");
            return subNode != null ? subNode.asText("") : "";
        } catch (Exception ex) {
            return "";
        }
    }
}