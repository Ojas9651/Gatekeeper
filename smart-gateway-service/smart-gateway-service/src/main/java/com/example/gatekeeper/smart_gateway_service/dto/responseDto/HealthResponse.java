package com.example.gatekeeper.smart_gateway_service.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class HealthResponse {

    private String serviceName;
    private String serviceDescription;
    private String status;
}
