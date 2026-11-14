package com.example.gatekeeper.management.service;

import com.example.gatekeeper.management.dto.requestDto.HeaderRuleRequest;
import com.example.gatekeeper.management.dto.requestDto.RouteRequest;
import com.example.gatekeeper.management.dto.requestDto.RouteStatusRequest;
import com.example.gatekeeper.management.dto.responseDto.AuditRecordResponse;
import com.example.gatekeeper.management.dto.responseDto.RouteResponse;
import com.example.gatekeeper.management.dto.responseDto.RouteVersionResponse;
import com.example.gatekeeper.management.entity.*;
import com.example.gatekeeper.management.event.GatewayConfigurationEventService;
import com.example.gatekeeper.management.exception.ApiException;
import com.example.gatekeeper.management.repository.APIRouteRepository;
import com.example.gatekeeper.management.repository.ControlPlaneAuditRepository;
import com.example.gatekeeper.management.repository.RouteConfigurationVersionRepository;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class RouteService {

    private OrganizationContext organizationContext;
    private APIRouteRepository apiRouteRepository;
    private RouteConfigurationVersionRepository routeConfigurationVersionRepository;
    private ObjectMapper objectMapper;
    private AuditService auditService;
    private ControlPlaneAuditRepository auditRepository;
    private GatewayConfigurationEventService eventService;

    public RouteService(OrganizationContext organizationContext,
                        APIRouteRepository apiRouteRepository,
                        RouteConfigurationVersionRepository routeConfigurationVersionRepository,
                        ObjectMapper objectMapper,
                        AuditService auditService,
                        ControlPlaneAuditRepository auditRepository,
                        GatewayConfigurationEventService eventService) {
        this.organizationContext = organizationContext;
        this.apiRouteRepository = apiRouteRepository;
        this.routeConfigurationVersionRepository = routeConfigurationVersionRepository;
        this.objectMapper = objectMapper;
        this.auditService = auditService;
        this.auditRepository = auditRepository;
        this.eventService = eventService;
    }

    @Transactional
    public RouteResponse create(RouteRequest request){

        Long startedAt = System.nanoTime();

        Organization organization = organizationContext.getOwnOrganization();

        validate(request, organization);

        if(apiRouteRepository.existsByOrganizationIdAndRouteId(
                organization.getId(), request.getRouteId())){
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Route ID already exists for the organization");
        }

        APIRoute route = new APIRoute();

        route.setOrganization(organization);
        route.setConfigurationVersion(1L);
        route.setCreatedAt(LocalDateTime.now());

        apply(route, request);

        apiRouteRepository.save(route);

        saveVersion(route, "CREATED");

        auditService.record(organization, "CREATE_ROUTE", "API_ROUTE",
                route.getRouteId(), startedAt, "Route configuration version 1 created");

        eventService.publish("CREATED", organization.getId(),
                route.getRouteId(),
                route.getConfigurationVersion());

        return RouteResponse.from(route);
    }

    @Transactional
    public List<RouteResponse> findAllForTenant(){
        Organization organization = organizationContext.getOwnOrganization();

        return toResponses(apiRouteRepository.findAllByOrganizationId(organization.getId()));
    }

    @Transactional
    public RouteResponse findById(Long id){
        Organization organization = organizationContext.getOwnOrganization();
        APIRoute route = apiRouteRepository.findByIdAndOrganizationId(id, organization.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Organization not found"));
        return RouteResponse.from(route);
    }

    @Transactional
    public RouteResponse update(Long id, RouteRequest request){

        Long startedAt = System.nanoTime();

        Organization organization = organizationContext.getOwnOrganization();

        APIRoute route = apiRouteRepository.findByIdAndOrganizationId(id, organization.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Organization not found"));

        if(!route.getRouteId().equals(request.getRouteId())){
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "RouteID cannot be changed");
        }

        validate(request, organization);

        route.setConfigurationVersion(route.getConfigurationVersion() + 1);

        apply(route, request);

        saveVersion(route, "UPDATED");

        auditService.record(organization, "UPDATE_ROUTE", "API_ROUTE",
                route.getRouteId(), startedAt, "Route configuration version " + route.getConfigurationVersion() + " created");

        eventService.publish("UPDATED", organization.getId(),
                route.getRouteId(),
                route.getConfigurationVersion());

        return RouteResponse.from(route);
    }

    @Transactional
    public RouteResponse changeStatus(Long id, RouteStatusRequest request){

        Long startedAt = System.nanoTime();

        Organization organization = organizationContext.getOwnOrganization();

        APIRoute route = apiRouteRepository.findByIdAndOrganizationId(id, organization.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Organization not found"));


        route.setActive(request.getActive());

        route.setConfigurationVersion(route.getConfigurationVersion() + 1);

        route.setUpdatedAt(LocalDateTime.now());

        String changeType = "";

        if(request.getActive()){
            changeType = "ACTIVATED";
            saveVersion(route, changeType);
        }
        else {
            changeType = "DEACTIVATED";
            saveVersion(route, changeType);
        }

        String status = request.getActive() ? "ACTIVATED" : "DEACTIVATED";

        auditService.record(organization, "CHANGE_ROUTE_STATUS", "API_ROUTE",
                route.getRouteId(), startedAt, "Route configuration " + status);

        eventService.publish(changeType, organization.getId(),
                route.getRouteId(),
                route.getConfigurationVersion());

        return RouteResponse.from(route);
    }

    @Transactional
    public void deleteRoute(Long id){

        Long startedAt = System.nanoTime();

        Organization organization = organizationContext.getOwnOrganization();

        APIRoute route = apiRouteRepository.findByIdAndOrganizationId(id, organization.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Organization not found"));

        route.setConfigurationVersion(route.getConfigurationVersion() + 1);

        route.setUpdatedAt(LocalDateTime.now());

        saveVersion(route, "DELETED");

        auditService.record(organization, "DELETE_ROUTE", "API_ROUTE",
                route.getRouteId(), startedAt, "Route deleted");

        eventService.publish("Deleted", organization.getId(),
                route.getRouteId(),
                route.getConfigurationVersion());

        apiRouteRepository.delete(route);
    }

    //Gateway Methods

    @Transactional
    public List<RouteResponse> findAllActiveForGateway(){
        return toResponses(apiRouteRepository.findAllByActiveTrueAndOrganizationStatus(OrganizationStatus.ACTIVE));
    }

    public void validate(RouteRequest request, Organization organization){
        String tenantPrefix = "/" + organization.getSlug() + "/";

        if(!(request.getPathPattern().startsWith(tenantPrefix))){
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "pathPattern should start with " + tenantPrefix);
        }

        if(!request.getTargetPathPrefix().startsWith("/")){
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "targetPathPrefix should start with /");
        }

        try{
            URI uri = URI.create(request.getTargetBaseURL());
            if(!"http".equalsIgnoreCase(uri.getScheme()) &&
            !"https".equalsIgnoreCase(uri.getScheme())){
                throw new IllegalArgumentException();
            }
        }catch(IllegalArgumentException exception){
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "targetBaseUrl must be a valid HTTP or HTTPS URL");
        }

        normalizeMethods(request.getAllowedMethods());

        for(HeaderRuleRequest rule : request.getHeaderRules()){
            String direction = rule.getDirection().toUpperCase(Locale.ENGLISH);
            String action = rule.getAction().toUpperCase(Locale.ENGLISH);
            if(!"REQUEST".equals(direction) && !"RESPONSE".equals(direction)){
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Header direction must me REQUEST or RESPONSE");
            }
            if(!"ADD".equals(action) && !"REMOVE".equals(action)){
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Header action must me ADD or REMOVE");
            }
            if("ADD".equals(action) &&
                    (rule.getHeaderValue() == null || rule.getHeaderValue().trim().isEmpty())){
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "ADD header value headerValue");
            }
        }
    }

    public Set<String> normalizeMethods(Set<String> methods){
        Set<String> normalized = new HashSet<>();

        for(String method : methods){
            String value = method.toUpperCase(Locale.ENGLISH);
            try{
                HttpMethod.valueOf(value);
            }catch(IllegalArgumentException exception){
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Unsupported HTTP method: " + method);
            }
            normalized.add(value);
        }
        return normalized;
    }

    public void apply(APIRoute route, RouteRequest request){
        route.setRouteId(request.getRouteId());
        route.setName(request.getName());
        route.setPathPattern(request.getPathPattern());
        route.setTargetBaseUrl(request.getTargetBaseURL());
        route.setTargetPathPrefix(request.getTargetPathPrefix());
        route.setRequestsPerMinute(request.getRequestsPerMinute());
        route.setResponseTimeoutMs(request.getResponseTimeoutMs());
        route.setIdempotencyEnabled(request.isIdempotencyEnabled());
        route.setActive(request.isActive());
        route.setUpdatedAt(LocalDateTime.now());
        route.setAllowedMethods(normalizeMethods(request.getAllowedMethods()));

        route.getHeaderRules().clear();

        for(HeaderRuleRequest ruleRequest : request.getHeaderRules()){
            RouteHeaderRule rule = new RouteHeaderRule();

            rule.setRoute(route);
            rule.setDirection(ruleRequest.getDirection().toUpperCase(Locale.ENGLISH));
            rule.setAction(ruleRequest.getAction().toUpperCase(Locale.ENGLISH));
            rule.setHeaderName(ruleRequest.getHeaderName());
            rule.setHeaderValue(ruleRequest.getHeaderValue());
            rule.setCreatedAt(LocalDateTime.now());
            rule.setUpdatedAt(LocalDateTime.now());
            route.getHeaderRules().add(rule);
        }
    }

    public List<RouteResponse> toResponses(List<APIRoute> routes){
        List<RouteResponse> response = new ArrayList<>();

        for(APIRoute route : routes){
            response.add(RouteResponse.from(route));
        }

        return response;
    }

    private void saveVersion(APIRoute route, String changeType){
        RouteConfigurationVersion version = new RouteConfigurationVersion();

        version.setOrganization(route.getOrganization());
        version.setRouteId(route.getRouteId());
        version.setVersionNumber(route.getConfigurationVersion());
        version.setChangeType(changeType);
        version.setConfigurationSnapshot(objectMapper.writeValueAsString(RouteResponse.from(route)));
        version.setCreatedAt(LocalDateTime.now());
        routeConfigurationVersionRepository.save(version);
    }

    public List<RouteVersionResponse> getVersion(String routeId){

        Organization organization = organizationContext.getOwnOrganization();

        List<RouteConfigurationVersion> versions = routeConfigurationVersionRepository
                .findByOrganizationIdAndRouteIdOrderByVersionNumberDesc(
                        organization.getId(), routeId
                );

        return toRouteVersionResponse(versions);
    }

    public List<RouteVersionResponse> toRouteVersionResponse(List<RouteConfigurationVersion> versions){
        List<RouteVersionResponse> response = new ArrayList<>();

        for(RouteConfigurationVersion version : versions){
            response.add(RouteVersionResponse.from(version));
        }

        return response;
    }

    public List<AuditRecordResponse> getAuditRecords(){

        Organization organization = organizationContext.getOwnOrganization();

        List<AuditRecordResponse> response = new ArrayList<>();

        List<ControlPlaneAuditRecord> records = auditRepository.
                findAllByOrganizationIdOrderByCreatedAtDesc(organization.getId());

        for(ControlPlaneAuditRecord record : records){
            response.add(AuditRecordResponse.from(record));
        }

        return response;
    }
}
