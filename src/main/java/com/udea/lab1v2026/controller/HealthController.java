package com.udea.lab1v2026.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de estado: la nube (Render) y el smoke test del pipeline consultan "/".
 */
@RestController
public class HealthController {

    @GetMapping("/")
    public String healthCheck() {
        return "HEALTH CHECK OK!";
    }

    @GetMapping("/version")
    public String version() {
        return "The actual version is 1.0.0";
    }
}
