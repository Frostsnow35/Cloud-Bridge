package com.cloudbridge.config;

import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.model.ConfigProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

@Configuration
@Profile("!mock")
public class BcosConfig {

    @Value("${fisco.network.peers[0]}")
    private String peer;

    @Bean
    public BcosSDK bcosSDK() {
        // Force mock mode by returning null, so we fallback to MockBlockchainService
        // In a real scenario, we would check if config file exists.
        // Since we don't have a real FISCO BCOS node running in this environment,
        // we should not try to connect.
        System.out.println("No FISCO BCOS config found, skipping SDK initialization.");
        return null;
    }

    @Bean
    public Client client(@Autowired(required = false) BcosSDK bcosSDK) {
        if (bcosSDK == null) return null;
        return bcosSDK.getClient(1); // Group 1
    }
}
