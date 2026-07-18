package com.cloudbridge.repository.graph;

import com.cloudbridge.entity.graph.Technology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnologyRepository extends JpaRepository<Technology, Long> {

    Technology findByName(String name);

    @Query("SELECT t FROM Technology t WHERE t.name LIKE %:name%")
    List<Technology> findRelatedTechnologies(@Param("name") String name);

    List<Technology> findByCategory(String category);
}
