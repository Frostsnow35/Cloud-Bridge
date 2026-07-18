package com.cloudbridge.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @brief 合同实体，记录需求方与揭榜方的合作协议
 * @details 包含合同内容、签署状态及区块链存证哈希
 */
@Data
@Entity
@Table(name = "contracts")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demand_id", nullable = false)
    private Demand demand;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_id", nullable = false)
    private Bid bid;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 合同条款内容

    @Enumerated(EnumType.STRING)
    private Status status = Status.DRAFT;

    private LocalDateTime ownerSignedAt;
    private LocalDateTime bidderSignedAt;

    private String txHash; // 合同存证哈希

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Status {
        DRAFT,          // 草稿
        PENDING_SIGN,   // 待签署
        SIGNED,         // 已签署（生效）
        COMPLETED       // 已履行
    }
}
