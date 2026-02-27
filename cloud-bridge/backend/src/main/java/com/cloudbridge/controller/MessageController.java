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
    public ResponseEntity<?> sendMessage(@Valid @RequestBody Message message) {
        if (message.getSenderId() == null || message.getReceiverId() == null) {
            return ResponseEntity.badRequest().body("Sender and Receiver IDs are required");
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

        message.setCreatedAt(LocalDateTime.now());
        message.setRead(false);
        
        Message savedMessage = messageRepository.save(message);
        return ResponseEntity.ok(savedMessage);
    }

    @GetMapping("/received/{userId}")
    public Page<Message> getReceivedMessages(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return messageRepository.findByReceiverId(userId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @GetMapping("/sent/{userId}")
    public List<Message> getSentMessages(@PathVariable Long userId) {
        return messageRepository.findBySenderId(userId);
    }

    @GetMapping("/unread/{userId}")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long userId) {
        long count = messageRepository.countByReceiverIdAndIsReadFalse(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        return messageRepository.findById(id)
                .map(message -> {
                    message.setRead(true);
                    return ResponseEntity.ok(messageRepository.save(message));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
