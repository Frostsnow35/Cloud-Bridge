package com.cloudbridge.service;

import com.cloudbridge.entity.Achievement;
import com.cloudbridge.repository.AchievementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class MatchingServiceTest {

    @Autowired
    private MatchingService matchingService;

    @Autowired
    private AchievementRepository achievementRepository;

    @Test
    public void testKeywordMatchReturnsPublishedAchievement() {
        Achievement a = new Achievement();
        a.setTitle("深度学习工业缺陷检测系统");
        a.setDescription("基于深度学习的表面缺陷检测方案，适用于工厂流水线质检。");
        a.setField("人工智能");
        a.setMaturity("Product");
        a.setPrice(BigDecimal.TEN);
        a.setStatus(Achievement.Status.PUBLISHED);
        a = achievementRepository.save(a);

        String query = "我们需要一个深度学习视觉方案用于工业缺陷检测";
        Map<String, Object> result = matchingService.match(query);
        List<Map<String, Object>> matches = (List<Map<String, Object>>) result.get("matches");

        assertTrue(matches.size() > 0, "Should have matches");
        assertEquals(a.getId(), matches.get(0).get("id"), "First match should contain the created achievement");
        
        System.out.println("Test Passed: Keyword/field matching works correctly.");
    }
}
