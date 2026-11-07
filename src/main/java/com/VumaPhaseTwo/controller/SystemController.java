package com.VumaPhaseTwo.controller;

import com.VumaPhaseTwo.dto.SystemStatusResponse;
import com.VumaPhaseTwo.service.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200", "http://localhost:8080", "http://127.0.0.1:8080"}, allowCredentials = "true")
public class SystemController {

    private final SystemService systemService;

    @GetMapping("/status")
    public ResponseEntity<SystemStatusResponse> getSystemStatus() {
        return ResponseEntity.ok(systemService.getSystemStatus());
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
