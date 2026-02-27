package com.cloudbridge.dto;

import lombok.Data;

@Data
public class MatchingProfile {
    private String keyword;
    private String field;
    private String subField;
    private String applicationScenario;
    private String technicalGoal;
}
