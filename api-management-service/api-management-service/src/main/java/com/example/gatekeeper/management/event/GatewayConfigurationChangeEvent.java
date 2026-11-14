package com.example.gatekeeper.management.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class GatewayConfigurationChangeEvent {

    private String eventId;
    private String eventType;
    private String changeType;
    private Long organizationId;
    private String routeId;
    private Long configurationVersion;
    private LocalDateTime occuredAt;
}
