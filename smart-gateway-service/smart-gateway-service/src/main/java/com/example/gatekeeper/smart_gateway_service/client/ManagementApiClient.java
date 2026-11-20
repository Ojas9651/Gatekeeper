package com.example.gatekeeper.smart_gateway_service.client;

import com.example.gatekeeper.smart_gateway_service.dto.requestDto.LoginRequest;
import com.example.gatekeeper.smart_gateway_service.dto.responseDto.GatewayRoute;
import com.example.gatekeeper.smart_gateway_service.dto.responseDto.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class ManagementApiClient {

    private final RestClient restClient;
    private final String username;
    private final String password;
    private String token;
    private Instant expiresAt;

    //Using RestClient builder to reach our API Management Service

    public ManagementApiClient(@Value("${management.api.username}") String username,
                               @Value("${management.api.password}") String password,
                               @Value("${management.api.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.username = username;
        this.password = password;
    }

    public List<GatewayRoute> fetchActiveRoutes(){
        ensureToken();

        try{
            return requestRoutes();
        }
        catch(HttpClientErrorException exception){
            if(exception.getStatusCode() == HttpStatus.UNAUTHORIZED){
                System.out.println(exception.getMessage());
                throw exception;
            }

            authenticate(); //retry authentication
            return requestRoutes();
        }
    }

    private synchronized void ensureToken(){
        if(token == null || expiresAt == null ||
                Instant.now().plusSeconds(30).isAfter(expiresAt)){
            authenticate();
        }
    }

    private synchronized void authenticate(){

        LoginResponse response = restClient.post()
                .uri("/api/auth/login")
                .body(new LoginRequest(username, password))
                .retrieve()
                .body(LoginResponse.class);
        if(response == null || response.getToken() == null ||
                !response.getRole().equals("GATEWAY_SERVICE")){
            throw new IllegalStateException("API Management didn't return a GATEWAY_SERVICE token");
        }
        token = response.getToken();
        expiresAt = Instant.now().plusSeconds(response.getExpiresInSeconds());
    }

    public List<GatewayRoute> requestRoutes(){

        return restClient.get()
                .uri("/api/gateway/routes")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<GatewayRoute>>() {});
    }
}
