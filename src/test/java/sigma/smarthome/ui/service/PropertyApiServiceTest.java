package sigma.smarthome.ui.service;

import com.sigma.smarthome.ui.model.Property;
import com.sigma.smarthome.ui.service.PropertyApiService;
import com.sigma.smarthome.ui.util.SessionManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

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
            lastAuthHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            lastRequestPath.set(exchange.getRequestURI().getPath());

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
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

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
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
            lastAuthHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            lastRequestPath.set(exchange.getRequestURI().getPath());

            if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
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

            if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
        });

        server.createContext("/properties/prop-bad", exchange -> {
            lastAuthHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));

            if ("PUT".equalsIgnoreCase(exchange.getRequestMethod()) || "DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
                return;
            }

            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
        });

        server.start(); // only once
        int port = server.getAddress().getPort();
        baseUrl = "http://localhost:" + port;
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @BeforeEach
    void setUpSession() {
        SessionManager.startSession("token123", "user@test.com", "PROPERTY_MANAGER");
        lastAuthHeader.set(null);
        lastRequestBody.set(null);
        lastRequestPath.set(null);
    }



    private static void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    @Test
    void getProperties_success_returnsListAndSendsToken() throws Exception {
        PropertyApiService service = new PropertyApiService(baseUrl);

        List<Property> properties = service.getProperties();

        assertEquals(2, properties.size());
        assertEquals("prop-1", properties.get(0).getId());
        assertEquals("Athlone", properties.get(0).getAddress());
        assertEquals("Bearer token123", lastAuthHeader.get());
    }

    @Test
    void createProperty_success_returnsCreatedProperty() throws Exception {
        PropertyApiService service = new PropertyApiService(baseUrl);

        Property property = service.createProperty("Athlone", "House");

        assertNotNull(property);
        assertEquals("prop-10", property.getId());
        assertEquals("Athlone", property.getAddress());
        assertEquals("House", property.getPropertyType());
        assertTrue(lastRequestBody.get().contains("\"address\":\"Athlone\""));
        assertTrue(lastRequestBody.get().contains("\"propertyType\":\"House\""));
    }

    @Test
    void updateProperty_success_returnsUpdatedProperty() throws Exception {
        PropertyApiService service = new PropertyApiService(baseUrl);

        Property property = service.updateProperty("prop-1", "Galway", "Apartment");

        assertNotNull(property);
        assertEquals("prop-1", property.getId());
        assertEquals("Galway", property.getAddress());
        assertEquals("Apartment", property.getPropertyType());
        assertEquals("/properties/prop-1", lastRequestPath.get());
    }

    @Test
    void deleteProperty_success_doesNotThrow() {
        PropertyApiService service = new PropertyApiService(baseUrl);

        assertDoesNotThrow(() -> service.deleteProperty("prop-1"));
        assertEquals("/properties/prop-1", lastRequestPath.get());
    }

    @Test
    void updateProperty_failure_throwsRuntimeException() {
        PropertyApiService service = new PropertyApiService(baseUrl);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.updateProperty("prop-bad", "Nowhere", "Unknown")
        );

        assertTrue(ex.getMessage().contains("Failed to update property"));
    }

    @Test
    void deleteProperty_failure_throwsRuntimeException() {
        PropertyApiService service = new PropertyApiService(baseUrl);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.deleteProperty("prop-bad")
        );

        assertTrue(ex.getMessage().contains("Failed to delete property"));
    }
}