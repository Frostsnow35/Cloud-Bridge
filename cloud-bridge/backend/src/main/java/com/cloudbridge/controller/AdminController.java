package com.cloudbridge.controller;

import com.cloudbridge.repository.AchievementRepository;
import com.cloudbridge.repository.DemandRepository;
import com.cloudbridge.repository.UserRepository;
import com.cloudbridge.entity.Demand;
import com.cloudbridge.entity.Achievement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private DemandRepository demandRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied.");
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDemands", demandRepository.count());
        stats.put("totalAchievements", achievementRepository.count());
        stats.put("totalUsers", userRepository.count());
        stats.put("pendingDemands", demandRepository.findByStatus(Demand.Status.PENDING_REVIEW).size());
        stats.put("pendingAchievements", achievementRepository.findByStatus(Achievement.Status.PENDING_REVIEW).size());

        return ResponseEntity.ok(stats);
    }
}
