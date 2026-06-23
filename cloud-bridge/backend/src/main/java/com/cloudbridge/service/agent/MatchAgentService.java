package com.cloudbridge.service.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @brief Agent 编排服务
 * 委托给 MatchAgentOrchestrator 执行固定 Pipeline（画像→搜索→推荐）
 */
@Service
public class MatchAgentService {

    @Autowired
    private MatchAgentOrchestrator orchestrator;

    /**
     * @brief 执行 Agent 对话（使用手动编排器，不依赖 LLM function calling）
     * 因为 DeepSeek chat 模型不支持可靠的 function calling，
     * 改用固定 Pipeline: 画像提取 → 成果搜索 → 配套资源搜索
     * @param sessionId 会话ID
     * @param userMessage 用户消息
     * @return Agent 完整响应文本
     */
    public String chat(String sessionId, String userMessage) {
        return orchestrator.orchestrate(sessionId, userMessage);
    }

    public void resetMemory(String sessionId) {
        // No memory to reset with orchestrator approach
    }
}
