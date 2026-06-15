package com.cloudbridge.repository;

import com.cloudbridge.entity.MatchFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchFeedbackRepository extends JpaRepository<MatchFeedback, Long> {
    
    // Find feedback for exact query match
    List<MatchFeedback> findByQueryAndScoreGreaterThan(String query, Integer score);

    // Find all high-quality feedback for training/testing
    List<MatchFeedback> findByScoreGreaterThan(Integer score);
}
