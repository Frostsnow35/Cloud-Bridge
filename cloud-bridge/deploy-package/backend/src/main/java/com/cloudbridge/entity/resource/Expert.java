package com.cloudbridge.entity.resource;

import lombok.Data;
import javax.persistence.*;
import java.util.List;

/**
 * @brief 专家实体，用于资源中心展示
 */
@Entity
@Table(name = "res_experts")
@Data
public class Expert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String title;
    private String institution;

    @ElementCollection
    @CollectionTable(name = "res_expert_research_areas", joinColumns = @JoinColumn(name = "expert_id"))
    @Column(name = "area")
    private List<String> researchArea;

    @ElementCollection
    @CollectionTable(name = "res_expert_achievements", joinColumns = @JoinColumn(name = "expert_id"))
    @Column(name = "achievement")
    private List<String> achievements;

    @Column(columnDefinition = "TEXT")
    private String collaborationHistory;
}
