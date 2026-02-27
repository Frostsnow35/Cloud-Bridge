package com.cloudbridge.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @brief 专家评审实体，记录专家对揭榜方案的评分与意见
 * @details 包含评分、评语、评审人及区块链存证哈希
 */
@Data
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_id", nullable = false)
    private Bid bid;

    @Column(nullable = false)
    private Long reviewerId; // 评审专家ID

    private Integer score; // 评分 (1-100)

    @Column(columnDefinition = "TEXT")
    private String comment; // 评审意见

    private String txHash; // 评审记录存证哈希

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
