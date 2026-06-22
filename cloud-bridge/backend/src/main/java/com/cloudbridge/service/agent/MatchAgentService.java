package com.cloudbridge.service.agent;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @brief Agent 编排服务
 * 管理 ChatLanguageModel、ChatMemory 和 MatchAgent 实例的生命周期
 */
@Service
public class MatchAgentService {

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.model}")
    private String modelName;

    @Value("${ai.agent.max-iterations:8}")
    private int maxIterations;

    @Value("${ai.agent.temperature:0.1}")
    private double temperature;

    @Value("${ai.agent.timeout-seconds:120}")
    private int timeoutSeconds;

    @Value("${ai.agent.max-tokens:2048}")
    private int maxTokens;

    @Autowired
    private MatchAgentTools agentTools;

    // 流式模型（用于 TokenStream 响应）
    private OpenAiStreamingChatModel streamingModel;

    // 非流式模型（用于记忆管理的标准模型，LangChain4j需要它来处理工具调用）
    private ChatLanguageModel chatModel;

    // 按 sessionId 隔离对话记忆，每个会话最多保留20条消息
    private final Map<String, ChatMemory> sessionMemories = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // 构建流式模型
        streamingModel = OpenAiStreamingChatModel.builder()
                .baseUrl(apiUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();

        // 构建标准模型
        chatModel = OpenAiChatModel.builder()
                .baseUrl(apiUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();

        System.out.println("MatchAgentService initialized: model=" + modelName
                + ", maxIterations=" + maxIterations + ", timeout=" + timeoutSeconds + "s");
    }

    /**
     * @brief 根据 sessionId 获取或创建对话记忆
     * 每个 session 独立隔离，默认保留最近20条消息
     */
    private ChatMemory getOrCreateMemory(String sessionId) {
        return sessionMemories.computeIfAbsent(sessionId,
                k -> MessageWindowChatMemory.withMaxMessages(20));
    }

    /**
     * @brief 重置指定会话的记忆
     */
    public void resetMemory(String sessionId) {
        sessionMemories.remove(sessionId);
    }

    /**
     * @brief 获取 Agent 的 TokenStream 用于流式 SSE 响应
     * @param sessionId 会话ID，用于记忆隔离
     * @param userMessage 用户消息
     * @return TokenStream 流式响应对象
     */
    public TokenStream chat(String sessionId, String userMessage) {
        ChatMemory memory = getOrCreateMemory(sessionId);

        MatchAgent agent = AiServices.builder(MatchAgent.class)
                .streamingChatLanguageModel(streamingModel)
                .chatMemory(memory)
                .tools(agentTools)
                .build();

        return agent.chat(sessionId, userMessage);
    }
}
