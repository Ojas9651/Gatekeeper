package com.example.gatekeeper.analytics.dto.responseDto;

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

