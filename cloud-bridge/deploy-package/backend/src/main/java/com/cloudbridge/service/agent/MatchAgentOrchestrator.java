package com.cloudbridge.service.agent;

import com.cloudbridge.service.MatchingService;
import com.cloudbridge.service.ai.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @brief 智能匹配 Agent 编排器 —— 智能匹配功能背后的 Agent 大脑
 *
 * 职责：
 * 1. 接收需求描述 → 委托给 MatchingService 执行匹配流水线
 * 2. 作为 Agent 编排层，在匹配流程中加入日志、监控、降级策略
 * 3. 聊天 Agent（MatchAgentService）调用 platformQA 回答平台问题
 * 4. 匹配 Agent（本类 match()）调用 MatchingService 执行供需匹配
 */
@Component
public class MatchAgentOrchestrator {

    @Autowired
    private AIService aiService;

    @Autowired
    private MatchingService matchingService;

    /**
     * @brief Agent 匹配入口：智能匹配页面的核心引擎
     *
     * Agent 接收需求描述，委托 MatchingService 执行完整匹配流水线：
     * 画像提取 → 知识图谱 → 混合检索 → 多维度打分 → AI重排序 → 推荐
     *
     * @param description 需求描述
     * @param field       领域筛选（可选）
     * @param budget      预算筛选（可选）
     * @return 匹配结果（matches + recommendations + relatedKeywords + aiGraph）
     */
    public Map<String, Object> match(String description, String field, Double budget) {
        System.out.println("[MATCH_AGENT] 收到匹配请求 | 描述长度=" + (description != null ? description.length() : 0)
                + " | 领域=" + field + " | 预算=" + budget);

        long start = System.currentTimeMillis();

        // Agent 委托给 MatchingService 执行完整流水线
        Map<String, Object> result = matchingService.match(description, field, budget);

        long elapsed = System.currentTimeMillis() - start;
        if (result != null && result.containsKey("matches")) {
            int matchCount = ((java.util.List<?>) result.get("matches")).size();
            int recCount = result.containsKey("recommendations")
                    ? ((java.util.List<?>) result.get("recommendations")).size() : 0;
            System.out.println("[MATCH_AGENT] 匹配完成 | 耗时=" + elapsed + "ms | 匹配=" + matchCount + " | 推荐=" + recCount);
        } else {
            System.out.println("[MATCH_AGENT] 匹配完成 | 耗时=" + elapsed + "ms | 无结果");
        }

        return result;
    }

    /**
     * @brief 聊天 Agent 入口（右下角气泡）—— 纯平台问答，不执行匹配
     */
    public String orchestrate(String sessionId, String userMessage) {
        return aiService.platformQAReply(userMessage);
    }
}
