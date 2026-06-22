package com.cloudbridge.service.agent;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier("agentCoreTools")
    private Object agentTools;

    private ChatLanguageModel chatModel;

    private final Map<String, ChatMemory> sessionMemories = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // LangChain4j OpenAi builder appends /chat/completions, so strip from configured URL
        String baseUrl = apiUrl;
        if (baseUrl.endsWith("/chat/completions")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - "/chat/completions".length());
        }
        System.out.println("MatchAgentService init: baseUrl=" + baseUrl + ", model=" + modelName);

        chatModel = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .logRequests(true)
                .logResponses(true)
                .build();

        System.out.println("MatchAgentService ready: model=" + modelName
                + ", maxIterations=" + maxIterations + ", timeout=" + timeoutSeconds + "s");
    }

    private ChatMemory getOrCreateMemory(Object sessionId) {
        return sessionMemories.computeIfAbsent((String) sessionId,
                k -> MessageWindowChatMemory.withMaxMessages(20));
    }

    public void resetMemory(String sessionId) {
        sessionMemories.remove(sessionId);
    }

    /**
     * @brief 执行 Agent 对话（非流式，Agent 自主调用工具后返回完整结果）
     * @param sessionId 会话ID
     * @param userMessage 用户消息
     * @return Agent 完整响应文本
     */
    public String chat(String sessionId, String userMessage) {
        MatchAgent agent = AiServices.builder(MatchAgent.class)
                .chatLanguageModel(chatModel)
                .chatMemoryProvider(memoryId -> getOrCreateMemory(memoryId))
                .tools(agentTools)
                .build();

        return agent.chat(sessionId, userMessage);
    }
}
