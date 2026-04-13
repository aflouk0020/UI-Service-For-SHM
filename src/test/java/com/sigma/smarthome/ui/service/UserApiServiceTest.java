package com.sigma.smarthome.ui.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class UserApiServiceTest {

    private static HttpServer server;
    private static String baseUrl;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/auth/login", exchange -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            if (body.contains("\"email\":\"test@example.com\"")
                    && body.contains("\"password\":\"Password123!\"")) {
                String token = buildFakeJwt("test@example.com", "PROPERTY_MANAGER");
                sendResponse(exchange, 200, "{\"accessToken\":\"" + token + "\"}");
            } else {
                sendResponse(exchange, 401, "{\"error\":\"Invalid credentials\"}");
            }
        });

        server.createContext("/auth/register", exchange -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            if (body.contains("\"email\":\"newuser@example.com\"")) {
                sendResponse(exchange, 201, "{\"email\":\"newuser@example.com\"}");
            } else {
                sendResponse(exchange, 400, "{\"error\":\"Registration failed\"}");
            }
        });

        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void defaultConstructor_createsServiceSuccessfully() {
        UserApiService service = new UserApiService();
        assertNotNull(service);
    }

    @Test
    void login_success_returnsAccessTokenEmailAndRole() throws Exception {
        UserApiService service = new UserApiService(baseUrl);

        UserApiService.LoginResult result =
                service.login("test@example.com", "Password123!");

        assertNotNull(result);
        assertNotNull(result.accessToken());
        assertEquals("test@example.com", result.email());
        assertEquals("PROPERTY_MANAGER", result.role());
    }

    @Test
    void login_failure_throwsRuntimeException() {
        UserApiService service = new UserApiService(baseUrl);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.login("wrong@example.com", "wrongpass")
        );

        assertTrue(ex.getMessage().contains("Login failed"));
    }

    @Test
    void login_missingAccessToken_throwsRuntimeException() throws IOException {
        TempServer temp = startTempServer("/auth/login", 200, "{}");
        try {
            UserApiService service = new UserApiService(temp.baseUrl());

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    service.login("test@example.com", "Password123!")
            );

            assertTrue(ex.getMessage().contains("Missing access token"));
        } finally {
            temp.server().stop(0);
        }
    }

    @Test
    void login_blankAccessToken_throwsRuntimeException() throws IOException {
        TempServer temp = startTempServer("/auth/login", 200, "{\"accessToken\":\"   \"}");
        try {
            UserApiService service = new UserApiService(temp.baseUrl());

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    service.login("test@example.com", "Password123!")
            );

            assertTrue(ex.getMessage().contains("Missing access token"));
        } finally {
            temp.server().stop(0);
        }
    }

    @Test
    void login_invalidJwtFormat_throwsRuntimeException() throws IOException {
        TempServer temp = startTempServer("/auth/login", 200, "{\"accessToken\":\"not-a-jwt\"}");
        try {
            UserApiService service = new UserApiService(temp.baseUrl());

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    service.login("test@example.com", "Password123!")
            );

            assertTrue(ex.getMessage().contains("Invalid JWT format"));
        } finally {
            temp.server().stop(0);
        }
    }

    @Test
    void login_unparseableJwt_throwsRuntimeException() throws IOException {
        TempServer temp = startTempServer("/auth/login", 200, "{\"accessToken\":\"header.invalid-base64.signature\"}");
        try {
            UserApiService service = new UserApiService(temp.baseUrl());

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    service.login("test@example.com", "Password123!")
            );

            assertTrue(ex.getMessage().contains("Failed to parse JWT token"));
            assertNotNull(ex.getCause());
        } finally {
            temp.server().stop(0);
        }
    }

    @Test
    void register_failure_throwsRuntimeException() {
        UserApiService service = new UserApiService(baseUrl);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.register("bad@example.com", "123", "PROPERTY_MANAGER")
        );

        assertTrue(ex.getMessage().contains("Registration failed"));
    }

    @Test
    void register_missingEmail_throwsRuntimeException() throws IOException {
        TempServer temp = startTempServer("/auth/register", 201, "{}");
        try {
            UserApiService service = new UserApiService(temp.baseUrl());

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    service.register("newuser@example.com", "Password123!", "PROPERTY_MANAGER")
            );

            assertTrue(ex.getMessage().contains("Missing email"));
        } finally {
            temp.server().stop(0);
        }
    }

    @Test
    void register_blankEmail_throwsRuntimeException() throws IOException {
        TempServer temp = startTempServer("/auth/register", 201, "{\"email\":\"   \"}");
        try {
            UserApiService service = new UserApiService(temp.baseUrl());

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    service.register("newuser@example.com", "Password123!", "PROPERTY_MANAGER")
            );

            assertTrue(ex.getMessage().contains("Missing email"));
        } finally {
            temp.server().stop(0);
        }
    }

    @Test
    void login_connectionFailure_throwsConnectException() {
        UserApiService service = new UserApiService("http://localhost:1");

        Exception ex = assertThrows(Exception.class, () ->
                service.login("test@example.com", "Password123!")
        );

        assertNotNull(ex);
    }

    @Test
    void register_connectionFailure_throwsConnectException() {
        UserApiService service = new UserApiService("http://localhost:1");

        Exception ex = assertThrows(Exception.class, () ->
                service.register("newuser@example.com", "Password123!", "PROPERTY_MANAGER")
        );

        assertNotNull(ex);
    }

    private record TempServer(HttpServer server, String baseUrl) {
    }

    private TempServer startTempServer(String path, int status, String responseBody) throws IOException {
        HttpServer tempServer = HttpServer.create(new InetSocketAddress(0), 0);
        tempServer.createContext(path, exchange -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }
            sendResponse(exchange, status, responseBody);
        });
        tempServer.start();
        return new TempServer(tempServer, "http://localhost:" + tempServer.getAddress().getPort());
    }

    private static String buildFakeJwt(String email, String role) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("{\"email\":\"" + email + "\",\"role\":\"" + role + "\"}")
                        .getBytes(StandardCharsets.UTF_8));

        return header + "." + payload + ".signature";
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}