package com.cloudbridge.controller;

import com.cloudbridge.entity.Achievement;
import com.cloudbridge.entity.MatchFeedback;
import com.cloudbridge.repository.AchievementRepository;
import com.cloudbridge.repository.MatchFeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
public class FeedbackImportController {

    @Autowired
    private MatchFeedbackRepository feedbackRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @PostMapping("/import/csv")
    public ResponseEntity<?> importFeedbackCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        List<String> errors = new ArrayList<>();
        int successCount = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (isFirstLine) { // Skip header: Query,AchievementTitle,Score
                    isFirstLine = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length < 2) continue;

                String query = parts[0].trim();
                String achievementTitle = parts[1].trim();
                int score = 100; // Default
                if (parts.length >= 3) {
                    try {
                        score = Integer.parseInt(parts[2].trim());
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }

                // Find Achievement
                List<Achievement> matches = achievementRepository.findPublishedByKeyword(achievementTitle);
                Optional<Achievement> achievement = matches.stream()
                    .filter(a -> a.getTitle().equals(achievementTitle))
                    .findFirst();

                if (achievement.isPresent()) {
                    MatchFeedback feedback = new MatchFeedback();
                    feedback.setQuery(query);
                    feedback.setAchievement(achievement.get());
                    feedback.setScore(score);
                    feedback.setSource("import");
                    feedbackRepository.save(feedback);
                    successCount++;
                } else {
                    errors.add("Achievement not found for title: " + achievementTitle);
                }
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Import failed: " + e.getMessage());
        }

        return ResponseEntity.ok("Imported " + successCount + " feedbacks. Errors: " + errors.size());
    }
}
