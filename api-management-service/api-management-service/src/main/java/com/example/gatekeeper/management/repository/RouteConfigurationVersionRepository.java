package com.example.gatekeeper.management.repository;

import com.example.gatekeeper.management.entity.RouteConfigurationVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteConfigurationVersionRepository extends JpaRepository<RouteConfigurationVersion, Long> {

    List<RouteConfigurationVersion> findByOrganizationIdAndRouteIdOrderByVersionNumberDesc(
            Long organizationId, String routeId);

}
