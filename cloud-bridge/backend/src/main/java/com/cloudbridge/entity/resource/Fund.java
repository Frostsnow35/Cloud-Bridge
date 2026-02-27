package com.cloudbridge.entity.resource;

import lombok.Data;
import javax.persistence.*;
import java.util.List;

/**
 * @brief 资金实体，用于资源中心展示
 */
@Entity
@Table(name = "res_funds")
@Data
public class Fund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String provider;
    private String fundType;
    private String amountRange;

    @ElementCollection
    @CollectionTable(name = "res_fund_industries", joinColumns = @JoinColumn(name = "fund_id"))
    @Column(name = "industry")
    private List<String> industryFocus;

    @ElementCollection
    @CollectionTable(name = "res_fund_requirements", joinColumns = @JoinColumn(name = "fund_id"))
    @Column(name = "requirement")
    private List<String> requirements;

    private String interestRate;
    private String contactInfo;
}
