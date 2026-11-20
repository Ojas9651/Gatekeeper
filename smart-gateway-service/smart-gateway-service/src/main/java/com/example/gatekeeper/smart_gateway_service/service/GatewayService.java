package com.example.gatekeeper.smart_gateway_service.service;

import com.example.gatekeeper.smart_gateway_service.model.CompiledRoute;
import com.example.gatekeeper.smart_gateway_service.dto.responseDto.GatewayErrorResponse;
import com.example.gatekeeper.smart_gateway_service.dto.responseDto.GatewayRoute;
import com.example.gatekeeper.smart_gateway_service.model.RateLimitResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class GatewayService {

    private final RouteRegistry routeRegistry;
    private final ObjectMapper objectMapper;
    private final RateLimitService rateLimitService;

    public GatewayService(RouteRegistry routeRegistry,
                          ObjectMapper objectMapper,
                          RateLimitService rateLimitService) {
        this.routeRegistry = routeRegistry;
        this.objectMapper = objectMapper;
        this.rateLimitService = rateLimitService;
    }

    public ResponseEntity<byte[]> handle(HttpServletRequest request){

        Long startedAt = System.nanoTime();

        String correlationId = getCorrelationId(request);

        String outcome = "TARGET_ERROR";
        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        boolean rateLimited = false;
        HttpHeaders policyHeaders = new HttpHeaders();

        //Find Compiled Route for Incoming request

        Optional<CompiledRoute> matchedRoute = routeRegistry.find(request.getRequestURI());

        //Validations

        if(!matchedRoute.isPresent()){
            outcome = "ROUTE_NOT_FOUND";
            statusCode = HttpStatus.NOT_FOUND.value();

            return error(HttpStatus.NOT_FOUND, "No Active Route Found", correlationId, null);
        }

        CompiledRoute compiledRoute = matchedRoute.get();
        GatewayRoute gatewayRoute = compiledRoute.getGatewayRoute();

        if(!gatewayRoute.getAllowedMethods().contains(request.getMethod())){
            outcome = "METHOD_NOT_ALLOWED";
            statusCode = HttpStatus.METHOD_NOT_ALLOWED.value();

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ALLOW, String.join(",", gatewayRoute.getAllowedMethods()));

            return error(HttpStatus.METHOD_NOT_ALLOWED, "Http method is not allowed for this route",
                    correlationId, null);
        }

        //Check rate limiting of route and apply it ; if rate limit achieved return

        RateLimitResult rateLimit = rateLimitService.check(gatewayRoute, request.getRemoteAddr());

        policyHeaders = rateLimitHeaders(rateLimit);

        if(!rateLimit.isAllowed()){
            rateLimited = true;
            outcome = "RATE_LIMITED";
            statusCode = HttpStatus.TOO_MANY_REQUESTS.value();

            policyHeaders.set(HttpHeaders.RETRY_AFTER, String.valueOf(rateLimit.getRetryAfterSeconds()));

            return error(HttpStatus.TOO_MANY_REQUESTS, "Rate Limit Exceeded",
                    correlationId, policyHeaders);
        }

        //3. Idempotency Check ; if Idempotency achieved return

        //4. Header Value

        //5. Redirect to target

        //6. Header Rules on Response

        //7. Store Response if Idempotency is enables

        //8. Return Response to Client
    }

    public String getCorrelationId(HttpServletRequest request){
        String correlationId = request.getHeader("X-Correlation-ID");
        if(correlationId == null || correlationId.trim().isEmpty()){
            return UUID.randomUUID().toString();
        }

        return correlationId;
    }

    public ResponseEntity<byte[]> error(HttpStatus status, String message,
                                        String correlationId,
                                        HttpHeaders additionalHeaders){

        GatewayErrorResponse response = new GatewayErrorResponse(status.value(), message,
                correlationId,
                LocalDateTime.now());

        HttpHeaders headers = additionalHeaders == null ? new HttpHeaders() : additionalHeaders;

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Correlation-ID", correlationId);

        return ResponseEntity.status(status).headers(headers).body(
                objectMapper.writeValueAsString(response).getBytes(StandardCharsets.UTF_8)
        );
    }

    public HttpHeaders rateLimitHeaders(RateLimitResult result){
        HttpHeaders headers = new HttpHeaders();
        if(result.getLimit() > 0){
            headers.set("X-RateLimit-Limit", String.valueOf(result.getLimit()));
            headers.set("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));
        }
        return headers;
    }
}
