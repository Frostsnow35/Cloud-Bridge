package com.cloudbridge.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "match_feedback")
public class MatchFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String query; // The user's search query

    @ManyToOne
    @JoinColumn(name = "achievement_id")
    private Achievement achievement; // The correct match

    private Integer score; // 100 = Perfect Match, 0 = Bad Match

    private String source; // "MANUAL", "USER_CLICK", "ADMIN"

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
