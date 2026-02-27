package com.cloudbridge.service.impl;

import com.cloudbridge.service.BlockchainService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.annotation.Profile;

@Service
@Profile("mock")
public class MockBlockchainService implements BlockchainService {

    private final Map<String, Map<String, Object>> mockChain = new HashMap<>();

    @Override
    public String storeEvidence(String hash, String metadata, String evidenceType, String signerId) {
        if (mockChain.containsKey(hash)) {
            // throw new RuntimeException("Evidence already exists on chain");
            // Allow duplicate hash for testing different transactions with same content? No, hash is unique.
            // But for MVP demo, maybe just return existing hash?
            // Let's stick to unique hash rule.
            return (String) mockChain.get(hash).get("txHash");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("hash", hash);
        data.put("metadata", metadata);
        data.put("owner", "0xMockAddress1234567890abcdef");
        data.put("timestamp", Instant.now().getEpochSecond());
        data.put("txHash", "0x" + UUID.randomUUID().toString().replace("-", ""));
        data.put("evidenceType", evidenceType != null ? evidenceType : "DEFAULT");
        data.put("signerId", signerId != null ? signerId : "SYSTEM");

        mockChain.put(hash, data);

        return (String) data.get("txHash");
    }

    @Override
    public boolean verifyEvidence(String hash) {
        return mockChain.containsKey(hash);
    }

    @Override
    public Map<String, Object> getEvidence(String hash) {
        return mockChain.get(hash);
    }
}
