package com.cloudbridge.controller;

import com.cloudbridge.entity.EvidenceRecord;
import com.cloudbridge.repository.EvidenceRepository;
import com.cloudbridge.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evidence")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EvidenceController {

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private EvidenceRepository evidenceRepository;

    @PostMapping("/store")
    public ResponseEntity<?> storeEvidence(@RequestBody Map<String, String> request) {
        String hash = request.get("hash");
        String metadata = request.get("metadata");
        String evidenceType = request.getOrDefault("type", "DEFAULT");
        // In a real app, get user ID from security context
        Long ownerId = 1L; 

        if (hash == null || hash.isEmpty()) {
            return ResponseEntity.badRequest().body("Hash is required");
        }

        try {
            String signerId = "USER_" + ownerId; // Mock signer ID
            String txHash = blockchainService.storeEvidence(hash, metadata, evidenceType, signerId);
            
            // Save local record
            EvidenceRecord record = new EvidenceRecord();
            record.setHash(hash);
            record.setTxHash(txHash);
            record.setMetadata(metadata);
            record.setEvidenceType(evidenceType);
            record.setOwnerId(ownerId);
            evidenceRepository.save(record);

            Map<String, String> response = new HashMap<>();
            response.put("txHash", txHash);
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to store evidence: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAllEvidence() {
        List<EvidenceRecord> records = evidenceRepository.findAll();
        return ResponseEntity.ok(records);
    }

    @GetMapping("/verify/{hash}")
    public ResponseEntity<?> verifyEvidence(@PathVariable String hash) {
        boolean exists = blockchainService.verifyEvidence(hash);
        if (exists) {
            Map<String, Object> data = blockchainService.getEvidence(hash);
            return ResponseEntity.ok(data);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
