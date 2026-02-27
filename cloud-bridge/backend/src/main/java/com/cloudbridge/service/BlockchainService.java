package com.cloudbridge.service;

import java.util.Map;

public interface BlockchainService {
    /**
     * Store evidence on blockchain.
     * @param hash SHA256 hash of the content
     * @param metadata JSON metadata string
     * @param evidenceType Type of evidence (e.g., "DISCLOSURE", "CONTRACT", "DELIVERY")
     * @param signerId ID of the signer
     * @return Transaction Hash
     */
    String storeEvidence(String hash, String metadata, String evidenceType, String signerId);

    /**
     * Store evidence on blockchain (Legacy overload).
     * @param hash SHA256 hash of the content
     * @param metadata JSON metadata string
     * @return Transaction Hash
     */
    default String storeEvidence(String hash, String metadata) {
        return storeEvidence(hash, metadata, "DEFAULT", "SYSTEM");
    }

    /**
     * Verify evidence existence.
     * @param hash SHA256 hash of the content
     * @return true if exists
     */
    boolean verifyEvidence(String hash);

    /**
     * Get evidence details.
     * @param hash SHA256 hash
     * @return Map with owner, timestamp, metadata
     */
    Map<String, Object> getEvidence(String hash);
}
