package com.cloudbridge.repository;

import com.cloudbridge.entity.EvidenceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvidenceRepository extends JpaRepository<EvidenceRecord, Long> {
    Optional<EvidenceRecord> findByHash(String hash);
}
