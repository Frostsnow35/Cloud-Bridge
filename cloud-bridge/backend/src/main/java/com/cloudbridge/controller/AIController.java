package com.cloudbridge.controller;

import com.cloudbridge.service.ai.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @brief AI服务控制器，提供聊天和智能分析接口
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AIController {

    @Autowired
    private AIService aiService;

    /**
     * @brief 智能对话接口
     * @param request 请求体，包含 message 字段
     * @return AI响应结果 (JSON字符串)
     */
    @PostMapping("/chat")
    public String chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return "{\"error\": \"Message cannot be empty\"}";
        }
        return aiService.chatWithIntent(message);
    }
}
