package com.cloudbridge.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Sender ID is mandatory")
    private Long senderId;

    @NotNull(message = "Receiver ID is mandatory")
    private Long receiverId;

    @NotBlank(message = "Content is mandatory")
    @Column(columnDefinition = "TEXT")
    private String content;

    private String cooperationType;

    private String budget;

    private String attachmentUrl;

    private Long relatedEntityId; // e.g., Achievement ID or Demand ID
    private String relatedEntityType; // "ACHIEVEMENT" or "DEMAND"

    private boolean isRead = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
