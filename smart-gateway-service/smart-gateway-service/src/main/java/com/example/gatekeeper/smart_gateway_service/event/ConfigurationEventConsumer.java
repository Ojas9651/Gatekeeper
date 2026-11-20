package com.example.gatekeeper.smart_gateway_service.event;

import com.example.gatekeeper.smart_gateway_service.service.GatewayConfigurationLoader;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationEventConsumer {

    private final GatewayConfigurationLoader gatewayConfigurationLoader;

    public ConfigurationEventConsumer(GatewayConfigurationLoader gatewayConfigurationLoader) {
        this.gatewayConfigurationLoader = gatewayConfigurationLoader;
    }

    @KafkaListener(topics = "${gateway.kafka.configuration-topic}",
                groupId = "${gateway.kafka.configuration-group}")
    public void configurationChanged(String event){

        System.out.println("Gateway configuration notification received: " + event);

        gatewayConfigurationLoader.refresh();

        System.out.println("Gateway configuration UPDATED: " + event);
    }
}
