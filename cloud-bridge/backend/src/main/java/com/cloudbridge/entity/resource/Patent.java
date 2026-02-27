package com.cloudbridge.entity.resource;

import lombok.Data;
import javax.persistence.*;
import java.util.List;

/**
 * @brief 专利实体，用于资源中心展示
 */
@Entity
@Table(name = "res_patents")
@Data
public class Patent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String patentNumber;
    private String assignee;
    private String inventor;
    private String applicationDate;
    private String publicationDate;

    @Column(columnDefinition = "TEXT")
    private String abstractText;

    private String status;

    @ElementCollection
    @CollectionTable(name = "res_patent_classifications", joinColumns = @JoinColumn(name = "patent_id"))
    @Column(name = "classification")
    private List<String> classifications;
}
