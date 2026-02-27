
package com.cloudbridge.service;

import com.cloudbridge.entity.Achievement;
import com.cloudbridge.repository.AchievementRepository;
import com.cloudbridge.repository.graph.TechnologyRepository;
import com.cloudbridge.service.ai.AIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MatchingServiceTest {

    @Mock
    private TechnologyRepository technologyRepository;

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private AIService aiService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private MatchingService matchingService;

    @BeforeEach
    public void setup() {
        // Mock AI Service default behavior
        when(aiService.extractGraphData(anyString())).thenReturn("{}");
    }

    @Test
    public void testBioMedicineVsNewMaterials() throws Exception {
        String demand = "研发一种用于骨科植入的生物活性陶瓷材料";
        
        // Mock AI Profile Extraction
        com.cloudbridge.dto.MatchingProfile profile = new com.cloudbridge.dto.MatchingProfile();
        profile.setKeyword("生物活性陶瓷");
        profile.setField("生物医药");
        profile.setSubField("骨科植入物");
        profile.setApplicationScenario("骨科修复");
        profile.setTechnicalGoal("生物相容性");
        
        when(aiService.extractMatchingProfile(anyString())).thenReturn(profile);
        when(aiService.extractGraphData(anyString())).thenReturn("{\"nodes\": [], \"relationships\": []}");
        
        // Mock Repo
        Achievement bioAch = new Achievement();
        bioAch.setId(1L);
        bioAch.setTitle("3D打印生物活性陶瓷骨");
        bioAch.setDescription("具有优异生物相容性的多孔陶瓷支架");
        bioAch.setField("生物医药");
        bioAch.setPrice(new BigDecimal("500000"));
        
        Achievement matAch = new Achievement();
        matAch.setId(2L);
        matAch.setTitle("工业耐高温陶瓷");
        matAch.setDescription("用于航空发动机的高温结构陶瓷");
        matAch.setField("新材料"); // Different field
        matAch.setPrice(new BigDecimal("800000"));

        // Field match: "生物医药" -> returns bioAch
        when(achievementRepository.findByFieldContainingAndStatus(eq("生物医药"), eq(Achievement.Status.PUBLISHED)))
                .thenReturn(Collections.singletonList(bioAch));

        // Keyword match: "生物活性陶瓷" -> returns bioAch
        when(achievementRepository.findPublishedByKeyword(eq("生物活性陶瓷")))
                .thenReturn(Collections.singletonList(bioAch));
                
        // Execute match
        Map<String, Object> result = matchingService.match(demand);
        List<Map<String, Object>> matches = (List<Map<String, Object>>) result.get("matches");

        // Verification
        System.out.println("Matches found: " + matches.size());
        for (Map<String, Object> m : matches) {
            System.out.println(" - " + m.get("title") + " [" + m.get("field") + "]");
        }

        // Assertions
        boolean hasBio = matches.stream().anyMatch(m -> m.get("id").equals(1L));
        boolean hasMat = matches.stream().anyMatch(m -> m.get("id").equals(2L));

        assertTrue(hasBio, "Should contain bio-medicine achievement");
        assertFalse(hasMat, "Should NOT contain industrial material achievement (filtered by field)");
    }
}
