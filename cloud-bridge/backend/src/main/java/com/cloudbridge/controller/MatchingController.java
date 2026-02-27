package com.cloudbridge.controller;

import com.cloudbridge.entity.Achievement;
import com.cloudbridge.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matching")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MatchingController {

    @Autowired
    private MatchingService matchingService;

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
        
        return matchingService.match(description, field, budget);
    }
}
