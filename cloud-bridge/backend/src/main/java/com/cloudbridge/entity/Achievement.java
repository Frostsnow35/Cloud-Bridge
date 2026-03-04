package com.cloudbridge.entity;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
@Data
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is mandatory")
    private String title;

    @NotBlank(message = "Description is mandatory")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Field is mandatory")
    private String field; // Domain: e.g., Bio, Material

    @NotBlank(message = "Maturity level is mandatory")
    private String maturity; // TRL level

    @NotNull(message = "Price is mandatory")
    @Min(value = 0, message = "Price must be non-negative")
    @Column(precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "institution")
    private String institution;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "patent_info", columnDefinition = "TEXT")
    private String patentInfo;

    @Column(name = "application_cases", columnDefinition = "TEXT")
    private String applicationCases;

    @Column(name = "resource_links", columnDefinition = "TEXT")
    private String resourceLinks;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags; // Comma separated tags

    @Column(name = "image")
    private String image;

    // @NotNull(message = "Owner ID is mandatory") - Set by controller
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = Status.PENDING_REVIEW;
        }
    }

    public enum Status {
        PENDING_REVIEW,
        PUBLISHED,
        REJECTED,
        TRADED
    }
}
