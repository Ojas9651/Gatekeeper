package com.example.gatekeeper.management.service;

import com.example.gatekeeper.management.dto.requestDto.OrganizationRequest;
import com.example.gatekeeper.management.dto.requestDto.OrganizationStatusRequest;
import com.example.gatekeeper.management.dto.requestDto.TenantAdminRequest;
import com.example.gatekeeper.management.dto.requestDto.TenantEnableRequest;
import com.example.gatekeeper.management.dto.responseDto.OrganizationResponse;
import com.example.gatekeeper.management.dto.responseDto.TenantAdminResponse;
import com.example.gatekeeper.management.entity.ManagementUser;
import com.example.gatekeeper.management.entity.Organization;
import com.example.gatekeeper.management.entity.OrganizationStatus;
import com.example.gatekeeper.management.entity.Role;
import com.example.gatekeeper.management.exception.ApiException;
import com.example.gatekeeper.management.repository.ManagementUserRepository;
import com.example.gatekeeper.management.repository.OrganizationRepository;
import com.example.gatekeeper.management.repository.RoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrganizationService {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final OrganizationRepository organizationRepository;
    private final ManagementUserRepository managementUserRepository;
    private final RoleRepository roleRepository;

    public OrganizationService(OrganizationRepository organizationRepository,
                               ManagementUserRepository managementUserRepository,
                               RoleRepository roleRepository) {
        this.organizationRepository = organizationRepository;
        this.managementUserRepository = managementUserRepository;
        this.roleRepository = roleRepository;
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


    //Tenant Service Methods

    @Transactional
    public TenantAdminResponse createTenant(TenantAdminRequest request){

        Long organizationId = request.getOrganizationId();
        String username = request.getUsername();

        Organization organization = organizationRepository.findById(organizationId).
                orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Organization not found"));

        if(managementUserRepository.findByOrganization_id(organizationId).isPresent()){
            throw new ApiException(HttpStatus.CONFLICT, "Tenant Admin already exists for" + organizationId);
        }

        if(managementUserRepository.findByUsername(username).isPresent()){
            throw new ApiException(HttpStatus.CONFLICT, "Username already exists");
        }

        ManagementUser newUser = new ManagementUser();

        Role role = roleRepository.findByName("TENANT_ADMIN").
                orElseThrow(() ->  new ApiException(HttpStatus.NOT_FOUND, "Role not found"));

        newUser.setUsername(username);
        newUser.setPassword_hash(passwordEncoder.encode(request.getPassword()));
        newUser.setOrganization(organization);
        newUser.setRole(role);
        newUser.setEnabled(true);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        ManagementUser savedUser = managementUserRepository.save(newUser);

        return TenantAdminResponse.from(savedUser);
    }

    public TenantAdminResponse findTenant(Long id){
        ManagementUser user = managementUserRepository.findById(id).
                orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        return TenantAdminResponse.from(user);
    }

    @Transactional
    public TenantAdminResponse updateTenant(TenantAdminRequest request){
        Long organizationId = request.getOrganizationId();
        String username = request.getUsername();

        Organization organization = organizationRepository.findById(organizationId).
                orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Organization not found"));


        if(managementUserRepository.findByUsername(username).isPresent()){
            throw new ApiException(HttpStatus.CONFLICT, "Username already exists");
        }

        ManagementUser user = managementUserRepository.findByOrganization_id(organizationId)
                .orElseThrow(() ->  new ApiException(HttpStatus.NOT_FOUND, "User not found"));


        if(!user.isEnabled()) throw new ApiException(HttpStatus.NOT_FOUND, "User is not enabled");

        user.setUsername(username);
        user.setPassword_hash(passwordEncoder.encode(request.getPassword()));
        user.setOrganization(organization);
        user.setEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());

        return TenantAdminResponse.from(user);
    }

    @Transactional
    public void deleteTenant(Long id){

        ManagementUser user = managementUserRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        user.setEnabled(false);
    }
}
