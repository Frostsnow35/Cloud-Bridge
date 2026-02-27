package com.cloudbridge.repository;

import com.cloudbridge.entity.Achievement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByOwnerId(Long ownerId);
    List<Achievement> findByStatus(Achievement.Status status);
    
    // Basic status query
    Page<Achievement> findByStatus(Achievement.Status status, Pageable pageable);
    
    // Advanced search with Status, Keyword and Field
    @Query("SELECT a FROM Achievement a WHERE a.status = :status " +
           "AND (:keyword IS NULL OR :keyword = '' OR a.title LIKE CONCAT('%', :keyword, '%') OR a.description LIKE CONCAT('%', :keyword, '%')) " +
           "AND (:field IS NULL OR :field = 'All' OR :field = '' OR a.field = :field)")
    Page<Achievement> search(@Param("status") Achievement.Status status, 
                             @Param("keyword") String keyword, 
                             @Param("field") String field, 
                             Pageable pageable);
    
    // For MatchingService: Search published achievements by keyword (Title, Description, OR FIELD)
    @Query("SELECT a FROM Achievement a WHERE a.status = 'PUBLISHED' " +
           "AND (LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(a.field) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Achievement> findPublishedByKeyword(@Param("keyword") String keyword);

    // Fallback: Recommend by field (domain)
    List<Achievement> findByFieldContainingAndStatus(String field, Achievement.Status status);

    // Fallback: Latest achievements
    List<Achievement> findTop5ByOrderByCreatedAtDesc();
}
