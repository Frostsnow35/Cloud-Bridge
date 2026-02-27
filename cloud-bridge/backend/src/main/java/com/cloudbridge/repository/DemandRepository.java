package com.cloudbridge.repository;

import com.cloudbridge.entity.Demand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandRepository extends JpaRepository<Demand, Long> {
    List<Demand> findByOwnerId(Long ownerId);
    List<Demand> findByStatus(Demand.Status status);
    Page<Demand> findByStatus(Demand.Status status, Pageable pageable);
    
    // Add search method
    Page<Demand> findByTitleContainingOrDescriptionContaining(String title, String description, Pageable pageable);
}
