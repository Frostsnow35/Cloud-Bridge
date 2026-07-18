package com.cloudbridge.entity.resource;

import lombok.Data;
import javax.persistence.*;

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

    private String amount;
    private String provider;
    @Column(name = "fund_field")
    private String field;
    @Column(length = 2000)
    private String description;
    private String amountRange;

    @Column(length = 2000)
    private String industryFocus;

    @Column(length = 2000)
    private String requirements;

    private String interestRate;
    private String contactInfo;
}
