package com.cloudbridge.controller;

import com.cloudbridge.service.agent.MatchAgentService;
import com.cloudbridge.service.ai.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private MatchAgentService matchAgentService;

    @PostMapping("/chat")
    public String chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return "{\"error\": \"Message cannot be empty\"}";
        }
        return aiService.chatWithIntent(message);
    }

    /**
     * @brief Agent 供需对接对话接口（非流式Agent + 手动分块SSE）
     * Agent 自主调用工具后返回完整结果，Controller 将结果分块为SSE事件流
     */
    @PostMapping(value = "/agent/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter agentChat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            SseEmitter errorEmitter = new SseEmitter(5000L);
            CompletableFuture.runAsync(() -> {
                try {
                    errorEmitter.send(SseEmitter.event().name("error").data("消息不能为空"));
                    errorEmitter.complete();
                } catch (IOException e) {
                    errorEmitter.completeWithError(e);
                }
            });
            return errorEmitter;
        }

        String sessionId = request.get("sessionId");
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        SseEmitter emitter = new SseEmitter(300000L);
        final String finalSessionId = sessionId;

        CompletableFuture.runAsync(() -> {
            try {
                // 发送 sessionId
                emitter.send(SseEmitter.event()
                        .name("session")
                        .data("{\"sessionId\":\"" + finalSessionId + "\"}"));

                // 调用 Agent 编排器（画像→搜索→推荐 Pipeline）
                String fullResponse = matchAgentService.chat(finalSessionId, message);

                // 手动分块发送，模拟流式效果
                int chunkSize = 3;
                for (int i = 0; i < fullResponse.length(); i += chunkSize) {
                    int end = Math.min(i + chunkSize, fullResponse.length());
                    String chunk = fullResponse.substring(i, end);
                    emitter.send(SseEmitter.event().name("token").data(chunk));
                    // 小延迟让前端逐字渲染
                    try { Thread.sleep(30); } catch (InterruptedException ignored) {}
                }

                // 发送完成事件
                emitter.send(SseEmitter.event()
                        .name("done")
                        .data("{\"sessionId\":\"" + finalSessionId + "\"}"));
                emitter.complete();

            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("{\"message\":\"" + escapeJson(e.getMessage()) + "\"}"));
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        emitter.onTimeout(() -> {
            System.err.println("SSE timeout for session: " + finalSessionId);
        });
        emitter.onError(throwable -> {
            System.err.println("SSE error for session " + finalSessionId + ": " + throwable.getMessage());
        });

        return emitter;
    }

    @PostMapping("/agent/reset")
    public Map<String, String> resetAgent(@RequestBody Map<String, String> request) {
        String sessionId = request.get("sessionId");
        if (sessionId != null && !sessionId.isEmpty()) {
            matchAgentService.resetMemory(sessionId);
        }
        return Map.of("status", "ok", "message", "会话已重置");
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
