package com.cloudbridge.entity;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "demands")
@Data
public class Demand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is mandatory")
    private String title;

    @NotBlank(message = "Description is mandatory")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Field is mandatory")
    private String field; // Domain

    @NotNull(message = "Budget is mandatory")
    @Min(value = 0, message = "Budget must be non-negative")
    @Column(precision = 19, scale = 2)
    private BigDecimal budget;

    @NotNull(message = "Deadline is mandatory")
    @Future(message = "Deadline must be in the future")
    private LocalDate deadline;

    private String contactName;
    private String phone;
    private String institution; // Maps to company/institution

    @NotNull(message = "Type is mandatory")
    @Enumerated(EnumType.STRING)
    private Type type; // NORMAL, REWARD (揭榜挂帅)

    // @NotNull(message = "Owner ID is mandatory") - Set by controller
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String txHash; // 存证哈希

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = Status.PENDING_REVIEW;
        }
    }

    public enum Type {
        NORMAL,
        REWARD, // 揭榜挂帅
        TECHNOLOGY_ATTACK // 技术攻关
    }

    public enum Status {
        PENDING_REVIEW,
        PUBLISHED,
        REJECTED,
        MATCHING,
        COMPLETED
    }
}
