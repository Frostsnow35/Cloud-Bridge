package com.cloudbridge.entity.resource;

import lombok.Data;
import javax.persistence.*;
import java.util.List;

/**
 * @brief 政策实体，用于资源中心展示
 */
@Entity
@Table(name = "res_policies")
@Data
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String publishDate;
    private String department;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String policyType;

    @ElementCollection
    @CollectionTable(name = "res_policy_industries", joinColumns = @JoinColumn(name = "policy_id"))
    @Column(name = "industry")
    private List<String> industry;

    private String region;

    @ElementCollection
    @CollectionTable(name = "res_policy_conditions", joinColumns = @JoinColumn(name = "policy_id"))
    @Column(name = "condition_text")
    private List<String> conditions;

    @Column(columnDefinition = "TEXT")
    private String supportContent;

    private String deadline;
    private String sourceUrl;
}
