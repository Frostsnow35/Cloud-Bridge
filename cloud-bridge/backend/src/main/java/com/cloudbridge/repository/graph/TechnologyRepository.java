package com.cloudbridge.repository.graph;

import com.cloudbridge.entity.graph.Technology;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnologyRepository extends Neo4jRepository<Technology, Long> {

    Technology findByName(String name);

    @Query("MATCH (t:Technology {name: $name})-[:RELATED_TO|BELONGS_TO*1..2]-(r:Technology) RETURN r")
    List<Technology> findRelatedTechnologies(String name);
}
