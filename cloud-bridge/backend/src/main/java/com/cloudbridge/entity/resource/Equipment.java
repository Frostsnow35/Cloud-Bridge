package com.cloudbridge.entity.resource;

import lombok.Data;
import javax.persistence.*;

/**
 * @brief 设备实体，用于资源中心展示
 */
@Entity
@Table(name = "res_equipments")
@Data
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String facilityName;
    private String owner;
    private String category;
    
    @Column(columnDefinition = "TEXT")
    private String specs;
    
    private String availability;
    private String location;
    
    @Column(columnDefinition = "TEXT")
    private String serviceContent;
    
    private String bookingUrl;
}
