package com.example.gatekeeper.management.service;

import com.example.gatekeeper.management.dto.requestDto.OrganizationRequest;
import com.example.gatekeeper.management.dto.requestDto.OrganizationStatusRequest;
import com.example.gatekeeper.management.dto.responseDto.OrganizationResponse;
import com.example.gatekeeper.management.entity.Organization;
import com.example.gatekeeper.management.entity.OrganizationStatus;
import com.example.gatekeeper.management.exception.ApiException;
import com.example.gatekeeper.management.repository.OrganizationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public OrganizationResponse create(OrganizationRequest request) {

        Long startedAt = System.nanoTime();

        checkUnique(request.getName(), request.getSlug());

        Organization newOrganization = new Organization(
                request.getName(),
                request.getSlug(),
                OrganizationStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Organization savedOrganization = organizationRepository.save(newOrganization);

        return OrganizationResponse.from(savedOrganization);
    }

    @Transactional
    public OrganizationResponse findById(Long id){
        Organization organization = organizationRepository.findByIdAndStatusNot(id, OrganizationStatus.DELETED).
                orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Organization not found"));

        return OrganizationResponse.from(organization);
    }

    @Transactional
    public List<OrganizationResponse> findAll(){

        List<Organization> organizations = organizationRepository.findAllByStatusNot(OrganizationStatus.DELETED);

        if(organizations.isEmpty()) throw new ApiException(HttpStatus.NOT_FOUND, "No Organization");

        List<OrganizationResponse> organizationResponses = new ArrayList<>();

        for(Organization organization : organizations){
            organizationResponses.add(OrganizationResponse.from(organization));
        }

        return organizationResponses;
    }

    @Transactional
    public OrganizationResponse update(Long id, OrganizationRequest request){

        String name = request.getName();
        String slug = request.getSlug();

        checkUnique(name, slug);

        if(name == null && slug == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Name and slug cannot be null");

        Organization organization = organizationRepository.findByIdAndStatusNot(id, OrganizationStatus.DELETED).
                orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Organization not found"));

        if(organization.getName() != null) organization.setName(request.getName());
        if(organization.getSlug() != null) organization.setSlug(request.getSlug());
        organization.setUpdatedAt(LocalDateTime.now());

        return OrganizationResponse.from(organization);
    }

    @Transactional
    public OrganizationResponse changeStatus(Long id,
                                             OrganizationStatusRequest request){

        if(request.getStatus() == OrganizationStatus.DELETED){
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please use delete endpoint to delete a organization");
        }

        Organization organization = organizationRepository.findByIdAndStatusNot(id, OrganizationStatus.DELETED).
                orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Organization not found"));

        organization.setStatus(request.getStatus());
        organization.setUpdatedAt(LocalDateTime.now());

        return OrganizationResponse.from(organization);
    }

    @Transactional
    public void delete(Long id){

        Organization organization = organizationRepository.findByIdAndStatusNot(id, OrganizationStatus.DELETED)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Organization not found"));

        organization.setStatus(OrganizationStatus.DELETED);
        organization.setUpdatedAt(LocalDateTime.now());
        organization.setDeletedAt(LocalDateTime.now());
    }

    private void checkUnique(String name, String slug){
        if(organizationRepository.existsByNameIgnoreCase(name)) throw new ApiException(HttpStatus.CONFLICT,
                "Organization with name " + name + " already exists");

        if(organizationRepository.existsBySlugIgnoreCase(slug)) throw new ApiException(HttpStatus.CONFLICT,
                "Organization with slug " + slug + " already exists");
    }
}
