package com.cloudbridge.service.agent;

import com.cloudbridge.service.ai.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @brief 平台问答 Agent —— 只负责解答 Agent 身份、平台功能和职责问题
 *
 * 不做"需求澄清-智能匹配"链路。用户如需智能匹配，回复中引导其前往智能匹配页面。
 */
@Component
public class MatchAgentOrchestrator {

    @Autowired
    private AIService aiService;

    /**
     * @brief 纯平台问答：回答关于 Agent 身份、平台功能、使用指引的问题。
     *        绝不执行匹配流水线。如用户提到技术需求，引导前往智能匹配页面。
     */
    public String orchestrate(String sessionId, String userMessage) {
        return aiService.platformQAReply(userMessage);
    }
}
