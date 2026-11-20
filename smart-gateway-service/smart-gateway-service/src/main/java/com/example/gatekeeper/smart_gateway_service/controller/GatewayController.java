package com.example.gatekeeper.smart_gateway_service.controller;

import com.example.gatekeeper.smart_gateway_service.service.GatewayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayController {

    private final GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService){
        this.gatewayService = gatewayService;
    }

    public ResponseEntity<byte[]> proxy(HttpServletRequest request) {
        gatewayService.handle(request);
    }
}
