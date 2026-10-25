package com.example.gatekeeper.management.controller;

import com.example.gatekeeper.management.dto.responseDto.HealthResponse;
import com.example.gatekeeper.management.service.HealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/health")
public class HealthController {

    private HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    public ResponseEntity<HealthResponse> serviceHealth() {

        String dbStatus = healthService.healthCheck();

        return ResponseEntity.ok(new HealthResponse("UP" , dbStatus, LocalDateTime.now()));
    }

}
