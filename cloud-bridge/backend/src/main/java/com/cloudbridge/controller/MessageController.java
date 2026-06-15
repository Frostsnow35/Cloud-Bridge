package com.cloudbridge.controller;

import com.cloudbridge.entity.Message;
import com.cloudbridge.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @PostMapping
    public ResponseEntity<?> sendMessage(@Valid @RequestBody Message message, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        if (message.getReceiverId() == null) {
            return ResponseEntity.badRequest().body("Receiver ID is required");
        }

        if (message.getSenderId() != null && !currentUserId.equals(message.getSenderId())) {
            return ResponseEntity.status(403).body("Sender ID does not match current user");
        }

        // Basic sanitization
        if (message.getContent() != null) {
            message.setContent(Jsoup.clean(message.getContent(), Safelist.none()));
        }
        if (message.getCooperationType() != null) {
            message.setCooperationType(Jsoup.clean(message.getCooperationType(), Safelist.none()));
        }
        if (message.getBudget() != null) {
            message.setBudget(Jsoup.clean(message.getBudget(), Safelist.none()));
        }
        if (message.getAttachmentUrl() != null) {
            message.setAttachmentUrl(Jsoup.clean(message.getAttachmentUrl(), Safelist.none()));
        }

        message.setSenderId(currentUserId);
        message.setCreatedAt(LocalDateTime.now());
        message.setRead(false);
        
        Message savedMessage = messageRepository.save(message);
        return ResponseEntity.ok(savedMessage);
    }

    @GetMapping("/received")
    public ResponseEntity<?> getCurrentUserReceivedMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        Page<Message> messages = messageRepository.findByReceiverId(
                currentUserId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/received/{userId}")
    public ResponseEntity<?> getReceivedMessages(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        if (!currentUserId.equals(userId)) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        Page<Message> messages = messageRepository.findByReceiverId(
                currentUserId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/sent")
    public ResponseEntity<?> getCurrentUserSentMessages(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return ResponseEntity.ok(messageRepository.findBySenderId(currentUserId));
    }

    @GetMapping("/sent/{userId}")
    public ResponseEntity<?> getSentMessages(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        if (!currentUserId.equals(userId)) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        return ResponseEntity.ok(messageRepository.findBySenderId(currentUserId));
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getCurrentUserUnreadCount(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        Map<String, Long> response = new HashMap<>();
        response.put("count", messageRepository.countByReceiverIdAndIsReadFalse(currentUserId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread/{userId}")
    public ResponseEntity<?> getUnreadCount(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        if (!currentUserId.equals(userId)) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        Map<String, Long> response = new HashMap<>();
        response.put("count", messageRepository.countByReceiverIdAndIsReadFalse(currentUserId));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return messageRepository.findById(id)
                .map(message -> {
                    if (!currentUserId.equals(message.getReceiverId())) {
                        return ResponseEntity.status(403).body("Forbidden");
                    }
                    message.setRead(true);
                    return ResponseEntity.ok(messageRepository.save(message));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }
}
