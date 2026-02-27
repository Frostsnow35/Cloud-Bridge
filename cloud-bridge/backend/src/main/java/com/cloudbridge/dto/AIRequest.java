package com.cloudbridge.dto;

import lombok.Data;
import java.util.List;

@Data
public class AIRequest {
    private String model;
    private List<Message> messages;
    private double temperature;

    @Data
    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
