package com.cloudbridge.dto;

import lombok.Data;

@Data
public class FeedbackRequest {
    private String query;
    private Long achievementId;
    private String achievementTitle; // Optional fallback
    private Integer score; // 0-100
    private String source; // "manual", "user"
}
