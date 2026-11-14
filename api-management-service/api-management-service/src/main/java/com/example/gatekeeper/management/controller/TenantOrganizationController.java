package com.example.gatekeeper.management.controller;

import com.example.gatekeeper.management.dto.requestDto.OrganizationRequest;
import com.example.gatekeeper.management.dto.requestDto.TenantOrganizationRequest;
import com.example.gatekeeper.management.dto.responseDto.OrganizationResponse;
import com.example.gatekeeper.management.service.OrganizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenant/organization")
public class TenantOrganizationController {

    private OrganizationService organizationService;

    public TenantOrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    public ResponseEntity<OrganizationResponse> getOwnOrganization(){
        return ResponseEntity.ok(organizationService.getOwnOrganization());
    }

    @PutMapping
    public ResponseEntity<OrganizationResponse> updateOwnOrganization(
            @RequestBody TenantOrganizationRequest tenantOrganizationRequest){
        return ResponseEntity.ok(organizationService.updateOwnOrganization(tenantOrganizationRequest));
    }
}
