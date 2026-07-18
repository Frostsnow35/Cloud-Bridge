package com.cloudbridge.repository.resource;

import com.cloudbridge.entity.resource.Patent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatentRepository extends JpaRepository<Patent, Long> {
}
