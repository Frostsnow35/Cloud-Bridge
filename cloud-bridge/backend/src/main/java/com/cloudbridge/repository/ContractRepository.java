package com.cloudbridge.repository;

import com.cloudbridge.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    Optional<Contract> findByDemandId(Long demandId);
    Optional<Contract> findByBidId(Long bidId);
}
