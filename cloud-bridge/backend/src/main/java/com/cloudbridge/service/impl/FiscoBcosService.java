package com.cloudbridge.service.impl;

import com.cloudbridge.service.BlockchainService;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Primary
@Profile("!mock")
public class FiscoBcosService implements BlockchainService {

    @Autowired(required = false)
    private Client client;

    @Value("${fisco.contract.evidenceAddress}")
    private String contractAddress;

    // ABI for the Evidence contract (Simplified for brevity, usually loaded from file)
    private static final String ABI = "[{\"constant\":false,\"inputs\":[{\"name\":\"hash\",\"type\":\"string\"},{\"name\":\"metadata\",\"type\":\"string\"},{\"name\":\"evidenceType\",\"type\":\"string\"},{\"name\":\"signerId\",\"type\":\"string\"},{\"name\":\"signature\",\"type\":\"string\"}],\"name\":\"storeEvidence\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"hash\",\"type\":\"string\"}],\"name\":\"getEvidence\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"},{\"name\":\"\",\"type\":\"uint256\"},{\"name\":\"\",\"type\":\"string\"},{\"name\":\"\",\"type\":\"string\"},{\"name\":\"\",\"type\":\"string\"},{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"hash\",\"type\":\"string\"}],\"name\":\"verifyEvidence\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]";

    @Override
    public String storeEvidence(String dataHash, String metadata, String evidenceType, String signerId) {
        if (client == null) {
            throw new RuntimeException("FISCO BCOS Client not initialized");
        }
        try {
            AssembleTransactionProcessor processor = TransactionProcessorFactory.createAssembleTransactionProcessor(client, client.getCryptoSuite().getCryptoKeyPair());
            
            List<Object> params = new ArrayList<>();
            params.add(dataHash);
            params.add(metadata);
            params.add(evidenceType != null ? evidenceType : "DEFAULT"); 
            params.add(signerId != null ? signerId : "SYSTEM");
            params.add("SIG_PLACEHOLDER"); // signature
            
            TransactionResponse response = processor.sendTransactionAndGetResponse(contractAddress, ABI, "storeEvidence", params);
            return response.getTransactionReceipt().getTransactionHash();
        } catch (Exception e) {
            throw new RuntimeException("Blockchain storage failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyEvidence(String dataHash) {
        if (client == null) return false;
        try {
            AssembleTransactionProcessor processor = TransactionProcessorFactory.createAssembleTransactionProcessor(client, client.getCryptoSuite().getCryptoKeyPair());
            
            List<Object> params = new ArrayList<>();
            params.add(dataHash);
            
            // Call verifyEvidence
            CallResponse response = processor.sendCall(client.getCryptoSuite().getCryptoKeyPair().getAddress(), contractAddress, ABI, "verifyEvidence", params);
            List<Object> result = response.getReturnObject();
            if (result == null || result.isEmpty()) return false;
            return (boolean) result.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Map<String, Object> getEvidence(String dataHash) {
        if (client == null) return null;
        try {
             AssembleTransactionProcessor processor = TransactionProcessorFactory.createAssembleTransactionProcessor(client, client.getCryptoSuite().getCryptoKeyPair());
            
            List<Object> params = new ArrayList<>();
            params.add(dataHash);
            
            // Returns: address, timestamp, metadata, evidenceType, signerId, signature
            CallResponse response = processor.sendCall(client.getCryptoSuite().getCryptoKeyPair().getAddress(), contractAddress, ABI, "getEvidence", params);
            List<Object> result = response.getReturnObject();
            
            if (result == null || result.isEmpty()) return null;
            
            Map<String, Object> map = new HashMap<>();
            map.put("owner", result.get(0));
            map.put("timestamp", result.get(1));
            map.put("metadata", result.get(2));
            map.put("evidenceType", result.get(3));
            map.put("signerId", result.get(4));
            map.put("signature", result.get(5));
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
