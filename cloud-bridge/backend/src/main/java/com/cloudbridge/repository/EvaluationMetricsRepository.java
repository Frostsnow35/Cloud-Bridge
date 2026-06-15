package com.cloudbridge.repository;

import com.cloudbridge.entity.EvaluationMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvaluationMetricsRepository extends JpaRepository<EvaluationMetrics, Long> {
    Optional<EvaluationMetrics> findByAchievementId(Long achievementId);
}
