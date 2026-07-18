package com.cloudbridge.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "evaluation_metrics")
public class EvaluationMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "achievement_id", unique = true)
    private Achievement achievement;

    // Dimensions (0-100 Score)
    private Integer technologyMaturity; // 行业成熟度
    private Integer innovationLevel;    // 创新性
    private Integer economicValue;      // 技术价值
    private Integer costEfficiency;     // 成本效益

    // Detailed AI Analysis (JSON or Text)
    @Column(columnDefinition = "TEXT")
    private String analysisReport; 

    private LocalDateTime analyzedAt;

    @PrePersist
    protected void onCreate() {
        analyzedAt = LocalDateTime.now();
    }
}
