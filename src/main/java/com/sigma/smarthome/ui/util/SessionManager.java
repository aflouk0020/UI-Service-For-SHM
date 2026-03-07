package com.sigma.smarthome.ui.util;

public final class SessionManager {

    private static String accessToken;
    private static String email;
    private static String role;

    private SessionManager() {
    }

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
}