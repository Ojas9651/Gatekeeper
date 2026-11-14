package com.example.gatekeeper.management.controller;

import com.example.gatekeeper.management.dto.requestDto.RouteRequest;
import com.example.gatekeeper.management.dto.requestDto.RouteStatusRequest;
import com.example.gatekeeper.management.dto.responseDto.AuditRecordResponse;
import com.example.gatekeeper.management.dto.responseDto.RouteResponse;
import com.example.gatekeeper.management.dto.responseDto.RouteVersionResponse;
import com.example.gatekeeper.management.entity.APIRoute;
import com.example.gatekeeper.management.entity.RouteConfigurationVersion;
import com.example.gatekeeper.management.service.AuditService;
import com.example.gatekeeper.management.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenant")
public class TenantRouteController {

    private final RouteService routeService;

    public TenantRouteController(RouteService routeService) {
        this.routeService = routeService;
    }


    @PostMapping("/routes")
    public ResponseEntity<RouteResponse> create(@Valid @RequestBody RouteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.create(request));
    }

    @GetMapping("/routes")
    public ResponseEntity<List<RouteResponse>> findAll() {
        return ResponseEntity.ok(routeService.findAllForTenant());
    }

    @GetMapping("/routes/{id}")
    public ResponseEntity<RouteResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.findById(id));
    }

    @PutMapping("/routes/{id}")
    public ResponseEntity<RouteResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody RouteRequest request) {
        return ResponseEntity.ok(routeService.update(id, request));
    }

    @PatchMapping("/routes/{id}")
    public ResponseEntity<RouteResponse> changeStatus(@PathVariable Long id, @Valid @RequestBody RouteStatusRequest request) {
        return ResponseEntity.ok(routeService.changeStatus(id, request));
    }

    @DeleteMapping("/routes/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/route-versions/{routeId}")
    public ResponseEntity<List<RouteVersionResponse>> getVersion(@PathVariable String routeId){
        return ResponseEntity.ok(routeService.getVersion(routeId));
    }

    @GetMapping("/audit-records")
    public ResponseEntity<List<AuditRecordResponse>> getAuditRecords(){
        return ResponseEntity.ok(routeService.getAuditRecords());
    }
}
