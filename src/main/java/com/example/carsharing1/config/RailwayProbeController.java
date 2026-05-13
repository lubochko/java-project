package com.example.carsharing1.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Railway UI often defaults health checks to {@code /livez}. Actuator liveness is at
 * {@code /actuator/health/liveness}; this endpoint keeps a simple 200 for the platform probe.
 */
@RestController
public class RailwayProbeController {

    @GetMapping("/livez")
    public ResponseEntity<String> livez() {
        return ResponseEntity.ok("OK");
    }
}
