package com.sigma.smarthome.ui.util;

public final class ApiConfig {

    public static final String API_GATEWAY_BASE_URL =
            System.getProperty(
                    "api.baseUrl",
                    System.getenv().getOrDefault(
                            "API_BASE_URL",
                            "http://localhost:32527"
                    )
            );

    private ApiConfig() {
    }
}