package sigma.smarthome.ui.service;

import com.sigma.smarthome.ui.service.UserApiService;
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
    void register_success_returnsRegisteredEmail() throws Exception {
        UserApiService service = new UserApiService(baseUrl);

        String email = service.register("newuser@example.com", "Password123!", "PROPERTY_MANAGER");

        assertEquals("newuser@example.com", email);
    }

    @Test
    void register_failure_throwsRuntimeException() {
        UserApiService service = new UserApiService(baseUrl);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.register("bad@example.com", "123", "PROPERTY_MANAGER")
        );

        assertTrue(ex.getMessage().contains("Registration failed"));
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