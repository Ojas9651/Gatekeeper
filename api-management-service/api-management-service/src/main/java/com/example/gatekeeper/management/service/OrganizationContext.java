package com.example.gatekeeper.management.service;

import com.example.gatekeeper.management.dto.responseDto.OrganizationResponse;
import com.example.gatekeeper.management.entity.Organization;
import com.example.gatekeeper.management.entity.OrganizationStatus;
import com.example.gatekeeper.management.exception.ApiException;
import com.example.gatekeeper.management.repository.OrganizationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class OrganizationContext {

    private OrganizationRepository organizationRepository;

    public OrganizationContext(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public Organization getOwnOrganization(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(!(authentication instanceof JwtAuthenticationToken)){
            throw new ApiException(HttpStatus.FORBIDDEN, "Teanant identity is missing");
        }
        System.out.println("instance Of");

        Long organizationId = ((JwtAuthenticationToken) authentication).getToken()
                .getClaim("organizationId");

        System.out.println(organizationId);

        if(organizationId == null){
            throw new ApiException(HttpStatus.FORBIDDEN, "Tenant identity is missing");
        }

        Organization organization = organizationRepository.findById(organizationId).
                orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Organization doesn't exist"));


        if(organization.getStatus() != OrganizationStatus.ACTIVE){
            throw new ApiException(HttpStatus.FORBIDDEN, "Organization is not ACTIVE");
        }

        return organization;
    }
}
