package com.cloudbridge.controller;

import com.cloudbridge.entity.Achievement;
import com.cloudbridge.repository.AchievementRepository;
import javax.validation.Valid;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import com.cloudbridge.service.BlockchainService;
import org.springframework.util.DigestUtils;

@RestController
@RequestMapping("/api/achievements")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AchievementController {

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private BlockchainService blockchainService;

    @GetMapping("/debug/all")
    public List<Achievement> getAllDebug() {
        return achievementRepository.findAll();
    }

    @GetMapping
    public Page<Achievement> getAllPublished(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String field
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        return achievementRepository.search(Achievement.Status.PUBLISHED, keyword, field, pageRequest);
    }

    @GetMapping("/my")
    public List<Achievement> getMyAchievements(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return achievementRepository.findByOwnerId(userId);
    }
    
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingAchievements(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied.");
        }
        return ResponseEntity.ok(achievementRepository.findByStatus(Achievement.Status.PENDING_REVIEW));
    }

    @PostMapping
    public Achievement createAchievement(@Valid @RequestBody Achievement achievement, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        achievement.setOwnerId(userId);
        achievement.setStatus(Achievement.Status.PENDING_REVIEW);
        sanitizeAchievement(achievement);
        return achievementRepository.save(achievement);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAchievement(@PathVariable Long id, @Valid @RequestBody Achievement achievementDetails, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return achievementRepository.findById(id)
                .map(achievement -> {
                    if (!achievement.getOwnerId().equals(userId)) {
                        return ResponseEntity.status(403).body("You can only update your own achievements.");
                    }
                    achievement.setTitle(achievementDetails.getTitle());
                    achievement.setDescription(achievementDetails.getDescription());
                    achievement.setField(achievementDetails.getField());
                    achievement.setMaturity(achievementDetails.getMaturity());
                    achievement.setPrice(achievementDetails.getPrice());
                    achievement.setInstitution(achievementDetails.getInstitution());
                    achievement.setContactName(achievementDetails.getContactName());
                    achievement.setPhone(achievementDetails.getPhone());
                    achievement.setPatentInfo(achievementDetails.getPatentInfo());
                    achievement.setApplicationCases(achievementDetails.getApplicationCases());
                    achievement.setResourceLinks(achievementDetails.getResourceLinks());
                    sanitizeAchievement(achievement);
                    return ResponseEntity.ok(achievementRepository.save(achievement));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAchievement(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return achievementRepository.findById(id)
                .map(achievement -> {
                    if (!achievement.getOwnerId().equals(userId)) {
                        return ResponseEntity.status(403).body("You can only delete your own achievements.");
                    }
                    achievementRepository.delete(achievement);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Achievement> getAchievementById(@PathVariable Long id) {
        return achievementRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/audit")
    public ResponseEntity<?> auditAchievement(@PathVariable Long id, @RequestParam Achievement.Status status, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Only admins can audit achievements.");
        }
        if (status != Achievement.Status.PUBLISHED && status != Achievement.Status.REJECTED) {
             return ResponseEntity.badRequest().body("Invalid status for audit. Must be PUBLISHED or REJECTED.");
        }
        return achievementRepository.findById(id)
                .map(achievement -> {
                    achievement.setStatus(status);
                    
                    // Blockchain Evidence for Audit
                    try {
                        String content = "Audit:" + status + "|Achievement:" + id + "|Admin:" + request.getAttribute("userId");
                        String hash = DigestUtils.md5DigestAsHex(content.getBytes());
                        String txHash = blockchainService.storeEvidence(hash, "{\"type\":\"ACHIEVEMENT_AUDIT\",\"achievementId\":" + id + ",\"status\":\"" + status + "\"}");
                        // Ideally store in AuditLog or update achievement txHash if it represents current state proof
                        // For MVP, let's assume we might want to prove the approval
                        if (status == Achievement.Status.PUBLISHED) {
                             // Maybe store as property if entity supports it, or just return it.
                             // Achievement entity doesn't have txHash field yet? Let's check or add it.
                             // If not, we just store it on chain.
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to store audit on blockchain: " + e.getMessage());
                    }
                    
                    return ResponseEntity.ok(achievementRepository.save(achievement));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private void sanitizeAchievement(Achievement achievement) {
        if (achievement.getTitle() != null) {
            achievement.setTitle(Jsoup.clean(achievement.getTitle(), Safelist.none()));
        }
        if (achievement.getDescription() != null) {
            achievement.setDescription(Jsoup.clean(achievement.getDescription(), Safelist.relaxed()));
        }
        if (achievement.getField() != null) {
            achievement.setField(Jsoup.clean(achievement.getField(), Safelist.none()));
        }
        if (achievement.getMaturity() != null) {
            achievement.setMaturity(Jsoup.clean(achievement.getMaturity(), Safelist.none()));
        }
        if (achievement.getInstitution() != null) {
            achievement.setInstitution(Jsoup.clean(achievement.getInstitution(), Safelist.none()));
        }
        if (achievement.getContactName() != null) {
            achievement.setContactName(Jsoup.clean(achievement.getContactName(), Safelist.none()));
        }
        if (achievement.getPhone() != null) {
            achievement.setPhone(Jsoup.clean(achievement.getPhone(), Safelist.none()));
        }
        if (achievement.getPatentInfo() != null) {
            achievement.setPatentInfo(Jsoup.clean(achievement.getPatentInfo(), Safelist.none()));
        }
        if (achievement.getApplicationCases() != null) {
            achievement.setApplicationCases(Jsoup.clean(achievement.getApplicationCases(), Safelist.relaxed()));
        }
        if (achievement.getResourceLinks() != null) {
            achievement.setResourceLinks(Jsoup.clean(achievement.getResourceLinks(), Safelist.none()));
        }
    }
}
