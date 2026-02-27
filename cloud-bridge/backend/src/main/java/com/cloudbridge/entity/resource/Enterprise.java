package com.cloudbridge.entity.resource;

import lombok.Data;
import javax.persistence.*;
import java.util.List;

/**
 * @brief 企业实体，用于资源中心展示
 */
@Entity
@Table(name = "res_enterprises")
@Data
public class Enterprise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String industry;
    private String scale;
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "res_enterprise_products", joinColumns = @JoinColumn(name = "enterprise_id"))
    @Column(name = "product")
    private List<String> products;

    private String website;
    private String contact;
}
