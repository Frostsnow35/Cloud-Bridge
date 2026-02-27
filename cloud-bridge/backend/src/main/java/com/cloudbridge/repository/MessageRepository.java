package com.cloudbridge.repository;

import com.cloudbridge.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByReceiverId(Long receiverId);
    List<Message> findBySenderId(Long senderId);
    Page<Message> findByReceiverId(Long receiverId, Pageable pageable);
    
    // Find unread messages count
    long countByReceiverIdAndIsReadFalse(Long receiverId);
}
