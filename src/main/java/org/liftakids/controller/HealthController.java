package org.liftakids.controller;

import lombok.RequiredArgsConstructor;
import org.liftakids.repositories.DivisionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/health")
public class HealthController {
    @GetMapping("/wakeup")
    public ResponseEntity<Map<String, Object>> wakeup() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("timestamp", Instant.now().toString());
        response.put("message", "Server awakened successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "LiftAKids API");
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }
}
