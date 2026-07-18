package com.cloudbridge.controller;

import com.cloudbridge.service.agent.MatchAgentOrchestrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @brief 智能匹配 Controller —— 委托给 MatchAgentOrchestrator（Agent 编排器）
 */
@RestController
@RequestMapping("/api/matching")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MatchingController {

    @Autowired
    private MatchAgentOrchestrator matchAgent;

    @PostMapping("/match")
    public Map<String, Object> matchDemand(@RequestBody Map<String, Object> request) {
        String description = (String) request.get("description");
        String field = (String) request.get("field");
        Double budget = null;
        Object budgetObj = request.get("budget");
        if (budgetObj != null) {
            try {
                budget = Double.parseDouble(budgetObj.toString());
            } catch (NumberFormatException e) {
                // Ignore invalid budget
            }
        }

        // Agent 编排器 orchestrates MatchingService pipeline
        return matchAgent.match(description, field, budget);
    }
}
