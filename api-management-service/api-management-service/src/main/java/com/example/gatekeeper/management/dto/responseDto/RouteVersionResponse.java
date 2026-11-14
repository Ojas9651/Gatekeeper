package com.example.gatekeeper.management.dto.responseDto;

import com.example.gatekeeper.management.entity.Organization;
import com.example.gatekeeper.management.entity.RouteConfigurationVersion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class RouteVersionResponse {

    private Long id;
    private String routeId;
    private Long versionNumber;
    private String changeType;
    private String configurationSnapshot;
    private LocalDateTime createdAt;

    public static RouteVersionResponse from(RouteConfigurationVersion version){
        return new RouteVersionResponse(
                        version.getId(),
                        version.getRouteId(),
                        version.getVersionNumber(),
                        version.getChangeType(),
                        version.getConfigurationSnapshot(),
                        version.getCreatedAt()
                );
    }
}
