package com.udea.lab1v2026.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthControllerTest {

    private final HealthController controller = new HealthController();

    @Test
    void healthCheck() {
        assertEquals("HEALTH CHECK OK!", controller.healthCheck());
    }

    @Test
    void version() {
        assertEquals("The actual version is 1.0.0", controller.version());
    }
}
