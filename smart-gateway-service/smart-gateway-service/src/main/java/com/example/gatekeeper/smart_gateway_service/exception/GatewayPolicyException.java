package com.example.gatekeeper.smart_gateway_service.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
public class GatewayPolicyException extends RuntimeException{

    private final HttpStatus status;
    private final String outcome;

    public GatewayPolicyException(HttpStatus status, String message, String outcome) {
        super(message);
        this.status = status;
        this.outcome = outcome;
    }
}
