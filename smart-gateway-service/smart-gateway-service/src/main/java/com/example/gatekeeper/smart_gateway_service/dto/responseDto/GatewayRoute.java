package com.example.gatekeeper.smart_gateway_service.dto.responseDto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class GatewayRoute {

    private Long id;
    private Long organizationId;
    private String routeId;
    private String name;
    private String pathPattern;
    private String targetBaseURL;
    private String targetPathPrefix;
    private Integer requestPerMinutes;
    private Integer responseTimeoutMs;
    private boolean idempotencyEnabled;
    private boolean active;
    private Long configurationVersion;
    private Set<String> allowedMethods;
    private List<HeaderRule> headerRules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
