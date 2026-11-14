package com.example.gatekeeper.management.repository;

import com.example.gatekeeper.management.entity.APIRoute;
import com.example.gatekeeper.management.entity.Organization;
import com.example.gatekeeper.management.entity.OrganizationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface APIRouteRepository extends JpaRepository<APIRoute, Long> {

    Boolean existsByOrganizationIdAndRouteId(Long organizationId, String routeId);

    List<APIRoute> findAllByOrganizationId(Long id);

    Optional<APIRoute> findByIdAndOrganizationId(Long routeId, Long organizationId);

    List<APIRoute> findAllByActiveTrueAndOrganizationStatus(OrganizationStatus status);
}
