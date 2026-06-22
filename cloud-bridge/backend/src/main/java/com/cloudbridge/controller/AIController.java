package com.cloudbridge.controller;

import com.cloudbridge.service.agent.MatchAgentService;
import com.cloudbridge.service.ai.AIService;
import dev.langchain4j.service.TokenStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @brief AI服务控制器，提供聊天、智能分析和Agent对话接口
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private MatchAgentService matchAgentService;

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

    /**
     * @brief Agent 供需对接对话接口（流式 SSE）
     * 支持多轮对话、工具调用和流式输出
     * @param request 请求体，包含 sessionId(可选) 和 message 字段
     * @return SseEmitter 流式响应
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

        // 超时时间 120 秒
        SseEmitter emitter = new SseEmitter(120000L);
        final String finalSessionId = sessionId;

        CompletableFuture.runAsync(() -> {
            try {
                // 先发送 sessionId
                emitter.send(SseEmitter.event()
                        .name("session")
                        .data("{\"sessionId\":\"" + finalSessionId + "\"}"));

                // 获取 Agent 的流式响应
                TokenStream tokenStream = matchAgentService.chat(finalSessionId, message);

                tokenStream
                    .onNext(partial -> {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("token")
                                    .data(partial));
                        } catch (IOException e) {
                            // 客户端断开连接
                        }
                    })
                    .onComplete(response -> {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("done")
                                    .data("{\"sessionId\":\"" + finalSessionId + "\"}"));
                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    })
                    .onError(error -> {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data("{\"message\":\"" + escapeJson(error.getMessage()) + "\"}"));
                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    })
                    .start();

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

        // 注册超时和错误回调
        emitter.onTimeout(() -> {
            System.err.println("SSE timeout for session: " + finalSessionId);
        });
        emitter.onError(throwable -> {
            System.err.println("SSE error for session " + finalSessionId + ": " + throwable.getMessage());
        });

        return emitter;
    }

    /**
     * @brief 重置 Agent 对话会话
     * @param request 请求体，包含 sessionId
     */
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
