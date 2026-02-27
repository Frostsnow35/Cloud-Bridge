package com.cloudbridge.controller;

import com.cloudbridge.repository.EvidenceRepository;
import com.cloudbridge.service.BlockchainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EvidenceControllerTest {

    @Mock
    private BlockchainService blockchainService;

    @Mock
    private EvidenceRepository evidenceRepository;

    @InjectMocks
    private EvidenceController evidenceController;

    @Test
    public void testStoreEvidence() {
        Map<String, String> request = new HashMap<>();
        request.put("hash", "0x1234567890abcdef");
        request.put("metadata", "{\"title\": \"Test Contract\"}");
        request.put("type", "CONTRACT");

        when(blockchainService.storeEvidence(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("0xTxHash123");

        ResponseEntity<?> response = evidenceController.storeEvidence(request);

        assertEquals(200, response.getStatusCodeValue());
        
        // Verify evidenceType is passed correctly
        verify(blockchainService).storeEvidence(
                eq("0x1234567890abcdef"), 
                eq("{\"title\": \"Test Contract\"}"), 
                eq("CONTRACT"), 
                anyString() // signerId is generated internally
        );
    }
}
