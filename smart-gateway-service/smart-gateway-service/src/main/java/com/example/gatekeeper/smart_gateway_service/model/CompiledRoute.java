package com.example.gatekeeper.smart_gateway_service.model;

import com.example.gatekeeper.smart_gateway_service.dto.responseDto.GatewayRoute;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.util.pattern.PathPattern;

@Getter
@AllArgsConstructor
public class CompiledRoute {

    private GatewayRoute gatewayRoute;
    private PathPattern pathPattern;
    private String sourcePathPrefix;
}
