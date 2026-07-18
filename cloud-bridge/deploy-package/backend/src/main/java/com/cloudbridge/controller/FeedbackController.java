package com.cloudbridge.controller;

import com.cloudbridge.dto.FeedbackRequest;
import com.cloudbridge.entity.Achievement;
import com.cloudbridge.entity.MatchFeedback;
import com.cloudbridge.repository.AchievementRepository;
import com.cloudbridge.repository.MatchFeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
public class FeedbackController {

    @Autowired
    private MatchFeedbackRepository feedbackRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @PostMapping("/match")
    public ResponseEntity<?> submitMatchFeedback(@RequestBody FeedbackRequest request) {
        if (request.getQuery() == null) {
            return ResponseEntity.badRequest().body("Query is required");
        }

        Optional<Achievement> achievement = Optional.empty();
        
        if (request.getAchievementId() != null) {
            achievement = achievementRepository.findById(request.getAchievementId());
        } else if (request.getAchievementTitle() != null) {
            // Find by title (assuming exact match or first match)
            // Need a repository method for this, or just use keyword search and filter
            List<Achievement> matches = achievementRepository.findPublishedByKeyword(request.getAchievementTitle());
            // Filter exact title match if possible
            achievement = matches.stream()
                .filter(a -> a.getTitle().equals(request.getAchievementTitle()))
                .findFirst();
        }

        if (!achievement.isPresent()) {
            return ResponseEntity.badRequest().body("Achievement not found by ID or Title");
        }

        MatchFeedback feedback = new MatchFeedback();
        feedback.setQuery(request.getQuery());
        feedback.setAchievement(achievement.get());
        feedback.setScore(request.getScore() != null ? request.getScore() : 100);
        feedback.setSource(request.getSource() != null ? request.getSource() : "manual");
        
        feedbackRepository.save(feedback);
        
        return ResponseEntity.ok("Feedback saved successfully");
    }
}
