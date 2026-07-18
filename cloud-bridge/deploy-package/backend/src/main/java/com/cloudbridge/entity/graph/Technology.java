package com.cloudbridge.entity.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "technologies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Technology {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String category;

    // 关联技术（逗号分隔的ID列表，模拟图关系）
    @Column(length = 1000)
    private String relatedTechnologyIds = "";

    @Transient
    private Set<Technology> relatedTechnologies = new HashSet<>();

    public Technology(String name, String category) {
        this.name = name;
        this.category = category;
    }
}
