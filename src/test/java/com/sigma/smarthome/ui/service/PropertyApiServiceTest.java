package com.sigma.smarthome.ui.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sigma.smarthome.ui.model.Property;
import com.sigma.smarthome.ui.util.SessionManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class PropertyApiServiceTest {

    private static HttpServer server;
    private static String baseUrl;

    private static final AtomicReference<String> lastAuthHeader = new AtomicReference<>();
    private static final AtomicReference<String> lastRequestBody = new AtomicReference<>();
    private static final AtomicReference<String> lastRequestPath = new AtomicReference<>();

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/properties", exchange -> {
            captureCommonRequestData(exchange);

            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                sendResponse(exchange, 200, """
                        [
                          {
                            "propertyId": "prop-1",
                            "address": "Athlone",
                            "propertyType": "House",
                            "managerId": "manager-1"
                          },
                          {
                            "propertyId": "prop-2",
                            "address": "Dublin",
                            "propertyType": "Apartment",
                            "managerId": "manager-2"
                          }
                        ]
                        """);
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                String body = readBody(exchange);
                lastRequestBody.set(body);

                if (body.contains("\"address\":\"Athlone\"") && body.contains("\"propertyType\":\"House\"")) {
                    sendResponse(exchange, 201, """
                            {
                              "propertyId": "prop-10",
                              "address": "Athlone",
                              "propertyType": "House",
                              "managerId": "manager-1"
                            }
                            """);
                } else {
                    sendResponse(exchange, 400, "{\"error\":\"Bad request\"}");
                }
                return;
            }

            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
        });

        server.createContext("/properties/prop-1", exchange -> {
            captureCommonRequestData(exchange);

            String method = exchange.getRequestMethod();

            if ("PUT".equalsIgnoreCase(method)) {
                String body = readBody(exchange);
                lastRequestBody.set(body);

                sendResponse(exchange, 200, """
                        {
                          "propertyId": "prop-1",
                          "address": "Galway",
                          "propertyType": "Apartment",
                          "managerId": "manager-1"
                        }
                        """);
                return;
            }

            if ("DELETE".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
        });

        server.createContext("/properties/prop-bad", exchange -> {
            captureCommonRequestData(exchange);

            String method = exchange.getRequestMethod();
            if ("PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
                sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
                return;
            }

            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
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

    @BeforeEach
    void setUp() {
        SessionManager.startSession("token123", "user@test.com", "PROPERTY_MANAGER");
        lastAuthHeader.set(null);
        lastRequestBody.set(null);
        lastRequestPath.set(null);
    }

    @Test
    void getProperties_success_returnsListAndSendsToken() {
        PropertyApiService service = new PropertyApiService(baseUrl);

        List<Property> properties = service.getProperties();

        assertEquals(2, properties.size());
        assertEquals("prop-1", properties.get(0).getId());
        assertEquals("Athlone", properties.get(0).getAddress());
        assertEquals("Bearer token123", lastAuthHeader.get());
        assertEquals("/properties", lastRequestPath.get());
    }

    @Test
    void createProperty_success_returnsCreatedProperty() {
        PropertyApiService service = new PropertyApiService(baseUrl);

        Property property = service.createProperty("Athlone", "House");

        assertNotNull(property);
        assertEquals("prop-10", property.getId());
        assertEquals("Athlone", property.getAddress());
        assertEquals("House", property.getPropertyType());
        assertTrue(lastRequestBody.get().contains("\"address\":\"Athlone\""));
        assertTrue(lastRequestBody.get().contains("\"propertyType\":\"House\""));
        assertEquals("Bearer token123", lastAuthHeader.get());
    }

    @Test
    void updateProperty_success_returnsUpdatedProperty() {
        PropertyApiService service = new PropertyApiService(baseUrl);

        Property property = service.updateProperty("prop-1", "Galway", "Apartment");

        assertNotNull(property);
        assertEquals("prop-1", property.getId());
        assertEquals("Galway", property.getAddress());
        assertEquals("Apartment", property.getPropertyType());
        assertEquals("/properties/prop-1", lastRequestPath.get());
        assertTrue(lastRequestBody.get().contains("\"address\":\"Galway\""));
        assertTrue(lastRequestBody.get().contains("\"propertyType\":\"Apartment\""));
    }

    @Test
    void deleteProperty_success_doesNotThrow() {
        PropertyApiService service = new PropertyApiService(baseUrl);

        assertDoesNotThrow(() -> service.deleteProperty("prop-1"));
        assertEquals("/properties/prop-1", lastRequestPath.get());
        assertEquals("Bearer token123", lastAuthHeader.get());
    }

    @Test
    void updateProperty_failure_throwsPropertyApiException() {
        PropertyApiService service = new PropertyApiService(baseUrl);

        PropertyApiService.PropertyApiException ex =
                assertThrows(PropertyApiService.PropertyApiException.class,
                        () -> service.updateProperty("prop-bad", "Nowhere", "Unknown"));

        assertTrue(ex.getMessage().contains("Failed to update property"));
    }

    @Test
    void deleteProperty_failure_throwsPropertyApiException() {
        PropertyApiService service = new PropertyApiService(baseUrl);

        PropertyApiService.PropertyApiException ex =
                assertThrows(PropertyApiService.PropertyApiException.class,
                        () -> service.deleteProperty("prop-bad"));

        assertTrue(ex.getMessage().contains("Failed to delete property"));
    }

    @Test
    void createProperty_badRequest_throwsPropertyApiException() {
        PropertyApiService service = new PropertyApiService(baseUrl);

        PropertyApiService.PropertyApiException ex =
                assertThrows(PropertyApiService.PropertyApiException.class,
                        () -> service.createProperty("BadAddress", "BadType"));

        assertTrue(ex.getMessage().contains("Failed to create property"));
    }

    @Test
    void request_withoutToken_doesNotSendAuthorizationHeader() {
        SessionManager.clearSession();
        PropertyApiService service = new PropertyApiService(baseUrl);

        service.getProperties();

        assertNull(lastAuthHeader.get());
    }

    private static void captureCommonRequestData(HttpExchange exchange) {
        lastAuthHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
        lastRequestPath.set(exchange.getRequestURI().getPath());
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }
    
    @Test
    void getProperties_httpError_throwsException() {
        PropertyApiService service = new PropertyApiService(baseUrl + "/invalid-base");

        RuntimeException ex = assertThrows(RuntimeException.class, service::getProperties);

        assertTrue(ex.getMessage().contains("Failed to load properties"));
    }

    @Test
    void createProperty_httpError_throwsException() {
        PropertyApiService service = new PropertyApiService(baseUrl);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.createProperty("Bad Address", "Bad Type")
        );

        assertTrue(ex.getMessage().contains("Failed to create property"));
    }

    @Test
    void getProperties_connectionFailure_throwsException() {
        PropertyApiService service = new PropertyApiService("http://localhost:1");

        RuntimeException ex = assertThrows(RuntimeException.class, service::getProperties);

        assertTrue(ex.getMessage().contains("Failed to load properties"));
    }

    @Test
    void createProperty_connectionFailure_throwsException() {
        PropertyApiService service = new PropertyApiService("http://localhost:1");

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.createProperty("Athlone", "House")
        );

        assertTrue(ex.getMessage().contains("Failed to create property"));
    }
    
    @Test
    void defaultConstructor_createsService() {
        PropertyApiService service = new PropertyApiService();
        assertNotNull(service);
    }
    
    @Test
    void getProperties_invalidJson_throwsPropertyApiException() throws IOException {
        HttpServer invalidJsonServer = HttpServer.create(new InetSocketAddress(0), 0);
        invalidJsonServer.createContext("/properties", exchange -> {
            sendResponse(exchange, 200, "{ invalid json");
        });
        invalidJsonServer.start();

        String invalidBaseUrl = "http://localhost:" + invalidJsonServer.getAddress().getPort();
        PropertyApiService service = new PropertyApiService(invalidBaseUrl);

        PropertyApiService.PropertyApiException ex =
                assertThrows(PropertyApiService.PropertyApiException.class, service::getProperties);

        assertTrue(ex.getMessage().contains("Failed to load properties"));

        invalidJsonServer.stop(0);
    }
    
    @Test
    void createProperty_invalidJson_throwsPropertyApiException() throws IOException {
        HttpServer invalidJsonServer = HttpServer.create(new InetSocketAddress(0), 0);
        invalidJsonServer.createContext("/properties", exchange -> {
            sendResponse(exchange, 201, "{ invalid json");
        });
        invalidJsonServer.start();

        String invalidBaseUrl = "http://localhost:" + invalidJsonServer.getAddress().getPort();
        PropertyApiService service = new PropertyApiService(invalidBaseUrl);

        PropertyApiService.PropertyApiException ex =
                assertThrows(PropertyApiService.PropertyApiException.class,
                        () -> service.createProperty("Athlone", "House"));

        assertTrue(ex.getMessage().contains("Failed to create property"));

        invalidJsonServer.stop(0);
    }
    
    @Test
    void defaultConstructor_createsServiceSuccessfully() {
        PropertyApiService service = new PropertyApiService();
        assertNotNull(service);
    }
    
    @Test
    void updateProperty_connectionFailure_throwsException() {
        PropertyApiService service = new PropertyApiService("http://localhost:1");

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.updateProperty("prop-1", "Athlone", "House")
        );

        assertTrue(ex.getMessage().contains("Failed to update property"));
    }
    
    @Test
    void deleteProperty_connectionFailure_throwsException() {
        PropertyApiService service = new PropertyApiService("http://localhost:1");

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.deleteProperty("prop-1")
        );

        assertTrue(ex.getMessage().contains("Failed to delete property"));
    }
}