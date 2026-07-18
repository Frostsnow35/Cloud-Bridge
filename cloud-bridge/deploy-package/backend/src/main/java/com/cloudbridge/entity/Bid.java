package com.cloudbridge.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @brief 揭榜申请实体，代表用户对“揭榜挂帅”需求的投标/响应
 * @details 包含投标说明、预算报价、状态及区块链存证哈希
 */
@Data
@Entity
@Table(name = "bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demand_id", nullable = false)
    private Demand demand;

    @Column(nullable = false)
    private Long bidderId; // 投标者ID（成果持有者或专家）

    @Column(columnDefinition = "TEXT", nullable = false)
    private String proposal; // 投标方案说明

    private Double quote; // 报价（可选，对应需求预算）

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING; // 投标状态

    private String txHash; // 区块链存证交易哈希（用于固化投标行为）

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Status {
        PENDING,     // 待审核/待筛选
        ACCEPTED,    // 已采纳/中标
        REJECTED,    // 未采纳
        NEGOTIATING, // 洽谈中
        COMPLETED    // 已完成
    }
}
