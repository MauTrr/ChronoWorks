package com.example.chronoworks.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok().body(Map.of(
                "status", "OK",
                "service", "Chronoworks",
                "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }
}