package com.udea.lab1v2026.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de estado. La ruta "/" queda libre para que Spring Boot sirva la
 * interfaz web (static/index.html); el health check ahora vive en "/health".
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public String healthCheck() {
        return "HEALTH CHECK OK!";
    }

    @GetMapping("/version")
    public String version() {
        return "The actual version is 1.0.0";
    }
}
