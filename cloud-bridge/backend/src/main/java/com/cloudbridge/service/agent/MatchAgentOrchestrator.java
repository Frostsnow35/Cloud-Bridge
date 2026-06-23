package com.cloudbridge.service.agent;

import com.cloudbridge.dto.MatchingProfile;
import com.cloudbridge.entity.Achievement;
import com.cloudbridge.repository.AchievementRepository;
import com.cloudbridge.service.ai.AIService;
import com.cloudbridge.service.rag.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @brief 手动编排器 - 替代 AiServices（DeepSeek 不支持 function calling）
 *
 * 执行固定的三层推导流程：
 * 1. 画像提取: aiService.extractMatchingProfile(userMessage)
 * 2. 搜索: searchService.search("achievements") + achievementRepository.findPublishedByKeyword()
 * 3. 配套资源: policies/funds/experts/equipments
 * 4. 生成回复: LLM 汇总结果生成可读方案
 */
@Component
public class MatchAgentOrchestrator {

    @Autowired
    private AIService aiService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private ToolChainLogger toolChainLogger;

    /**
     * @brief 执行完整供需对接编排流程
     * 先进行意图分类：闲聊直接回复，匹配意图才走流水线
     * @param sessionId 会话ID
     * @param userMessage 用户原始消息
     * @return 回复文本
     */
    public String orchestrate(String sessionId, String userMessage) {
        // ====== 意图分类：先判断用户意图 ======
        String intent = classifyIntent(userMessage);
        
        if ("CHAT".equals(intent)) {
            // 闲聊/问候/自我介绍 → 直接对话回复
            return aiService.chatReply(userMessage);
        }
        
        // 匹配意图 → 走完整流水线
        return runMatchingPipeline(sessionId, userMessage);
    }
    
    /**
     * @brief 用 AI 分类意图：CHAT（闲聊/问候/介绍） vs MATCH（供需匹配）
     */
    private String classifyIntent(String message) {
        // 快速规则：极短消息大概率是闲聊
        String trimmed = message != null ? message.trim() : "";
        if (trimmed.length() <= 4) {
            String lower = trimmed.toLowerCase();
            if (lower.startsWith("你好") || lower.startsWith("嗨") || lower.startsWith("hi") 
                || lower.equals("是谁") || lower.startsWith("谁") || lower.startsWith("你")
                || lower.startsWith("谢谢") || lower.contains("你好") || lower.startsWith("hello")
                || lower.equals("？") || lower.equals("?")) {
                return "CHAT";
            }
        }
        
        // 快速规则：明显长需求 → 直接匹配
        if (trimmed.length() > 30) {
            return "MATCH";
        }
        
        // AI 分类
        try {
            String prompt = String.format(
                "判断以下用户消息的意图类型，只回复 CHAT 或 MATCH。\n" +
                "CHAT = 闲聊、打招呼、询问你是谁、咨询平台功能、问天气等非匹配意图\n" +
                "MATCH = 描述技术需求、寻找科技成果、希望对接资源\n" +
                "用户消息: \"%s\"", trimmed.replace("%", "%%")
            );
            var request = new com.cloudbridge.dto.AIRequest();
            request.setModel("deepseek-chat");
            request.setTemperature(0);
            request.setMessages(java.util.Arrays.asList(
                new com.cloudbridge.dto.AIRequest.Message("system", "Output only CHAT or MATCH."),
                new com.cloudbridge.dto.AIRequest.Message("user", prompt)
            ));
            String result = aiService.callAIDirect(request).trim().toUpperCase();
            if (result.contains("CHAT")) return "CHAT";
        } catch (Exception e) {
            // fallback: 短消息当闲聊
        }
        return "MATCH";
    }
    
    /**
     * @brief 执行匹配流水线（画像提取 → 搜索 → 推荐）
     */
    private String runMatchingPipeline(String sessionId, String userMessage) {
        long totalStart = System.currentTimeMillis();
        StringBuilder report = new StringBuilder();

        // ====== Tier 1: 画像提取 ======
        long t1Start = System.currentTimeMillis();
        toolChainLogger.logToolStart(sessionId, "extractMatchingProfile",
                userMessage.length() > 80 ? userMessage.substring(0, 80) + "..." : userMessage);

        MatchingProfile profile;
        try {
            profile = aiService.extractMatchingProfile(userMessage);
        } catch (Exception e) {
            String errMsg = "画像提取失败: " + e.getMessage();
            toolChainLogger.logToolEnd(sessionId, "extractMatchingProfile",
                    System.currentTimeMillis() - t1Start, errMsg);
            return "抱歉，我暂时无法分析您的需求（" + e.getMessage() + "），请稍后重试或尝试更具体的描述。";
        }

        long t1Duration = System.currentTimeMillis() - t1Start;
        String profileSummary = String.format("关键词=%s, 领域=%s, 子领域=%s, 场景=%s, 目标=%s",
                nvl(profile.getKeyword()), nvl(profile.getField()), nvl(profile.getSubField()),
                nvl(profile.getApplicationScenario()), nvl(profile.getTechnicalGoal()));
        toolChainLogger.logToolEnd(sessionId, "extractMatchingProfile", t1Duration, profileSummary);
        report.append("【需求画像】\n").append(profileSummary).append("\n\n");

        // ====== Tier 2: 搜索科技成果 ======
        String keyword = profile.getKeyword();
        if (keyword == null || keyword.trim().isEmpty()) {
            keyword = extractKeywordFallback(userMessage);
        }

        // 2a: ES search
        long t2aStart = System.currentTimeMillis();
        toolChainLogger.logToolStart(sessionId, "searchResources", "type=achievements, query=" + keyword);
        List<String> esResults = searchService.search("achievements", keyword);
        long t2aDuration = System.currentTimeMillis() - t2aStart;
        String esSummary = esResults != null && !esResults.isEmpty()
                ? "找到" + esResults.size() + "项科技成果"
                : "未找到匹配成果";
        toolChainLogger.logToolEnd(sessionId, "searchResources", t2aDuration, esSummary);

        // 2b: DB search
        long t2bStart = System.currentTimeMillis();
        toolChainLogger.logToolStart(sessionId, "searchCandidateAchievements", keyword);
        List<Achievement> candidates = achievementRepository.findPublishedByKeyword(keyword);
        if (candidates.isEmpty()) {
            // Fallback 1: search by field
            String field = profile.getField();
            if (field != null && !field.isEmpty()) {
                candidates = achievementRepository.findByFieldContainingAndStatus(field, Achievement.Status.PUBLISHED);
            }
        }
        if (candidates.isEmpty() && !keyword.equals(extractKeywordFallback(userMessage))) {
            // Fallback 2: AI画像可能偏差，用原始文本提取短关键词重试
            String rawKeyword = extractKeywordFallback(userMessage);
            System.out.println("[DEBUG] Fallback2: userMessage='" + userMessage + "', rawKeyword='" + rawKeyword + "'");
            toolChainLogger.logToolStart(sessionId, "searchCandidateAchievements(回退)", rawKeyword);
            candidates = achievementRepository.findPublishedByKeyword(rawKeyword);
        }
        // Fallback 3: 仍无结果，直接用前2个中文字符做最宽搜索
        if (candidates.isEmpty()) {
            String ultraShort = extractUltraShortKeyword(userMessage);
            System.out.println("[DEBUG] Fallback3: ultraShort='" + ultraShort + "'");
            toolChainLogger.logToolStart(sessionId, "searchCandidateAchievements(极短回退)", ultraShort);
            candidates = achievementRepository.findPublishedByKeyword(ultraShort);
        }
        long t2bDuration = System.currentTimeMillis() - t2bStart;
        String dbSummary = candidates != null && !candidates.isEmpty()
                ? "找到" + candidates.size() + "项候选成果"
                : "未找到匹配成果";
        toolChainLogger.logToolEnd(sessionId, "searchCandidateAchievements", t2bDuration, dbSummary);

        // Format achievement results - 去重并按整洁纯文本展示
        report.append("【成果匹配结果】\n");
        if (candidates != null && !candidates.isEmpty()) {
            java.util.Set<String> seen = new java.util.HashSet<>();
            int count = 0;
            for (Achievement a : candidates) {
                if (count >= 5) break;
                if (seen.contains(a.getTitle())) continue;
                seen.add(a.getTitle());
                count++;
                report.append(count).append(". ").append(a.getTitle()).append("\n");
                report.append("   机构: ").append(nvl(a.getInstitution()))
                      .append(" | 领域: ").append(nvl(a.getField()))
                      .append(" | 成熟度: ").append(nvl(a.getMaturity())).append("\n\n");
            }
        } else {
            report.append("未找到完全匹配的科技成果，建议换个角度描述需求。\n\n");
        }

        // ====== Tier 3: 配套资源搜索 ======
        report.append("【配套资源】\n");
        String[] resourceTypes = {"policies", "funds", "experts", "equipments"};
        String[] resourceNames = {"产业政策", "科技金融", "领域专家", "共享设备"};

        for (int i = 0; i < resourceTypes.length; i++) {
            long tStart = System.currentTimeMillis();
            String type = resourceTypes[i];
            String query = keyword;
            if (profile.getField() != null && !profile.getField().isEmpty()) {
                query = keyword + " " + profile.getField();
            }
            toolChainLogger.logToolStart(sessionId, "searchResources", "type=" + type + ", query=" + query);
            List<String> results = searchService.search(type, query);
            long tDuration = System.currentTimeMillis() - tStart;
            String rSummary = results != null && !results.isEmpty()
                    ? "找到" + results.size() + "项" + resourceNames[i]
                    : "未找到" + resourceNames[i];
            toolChainLogger.logToolEnd(sessionId, "searchResources", tDuration, rSummary);

            if (results != null && !results.isEmpty()) {
                report.append(String.format("  %s: 找到%d项\n", resourceNames[i], results.size()));
            } else {
                report.append(String.format("  %s: 暂无直接匹配结果\n", resourceNames[i]));
            }
        }

        long totalDuration = System.currentTimeMillis() - totalStart;
        System.out.println("[AGENT_TOOLCHAIN] [" + sessionId + "] TOTAL Orchestrator response time: " + totalDuration + " ms");

        return report.toString();
    }

    /**
     * @brief 从JSON结果中提取标题
     */
    private String extractTitleFromJson(String json) {
        try {
            if (json.contains("\"title\"")) {
                int start = json.indexOf("\"title\"") + 9;
                int end = json.indexOf("\"", start);
                if (end > start) return json.substring(start, end);
            }
            if (json.contains("\"name\"")) {
                int start = json.indexOf("\"name\"") + 8;
                int end = json.indexOf("\"", start);
                if (end > start) return json.substring(start, end);
            }
        } catch (Exception ignored) {}
        return json.length() > 30 ? json.substring(0, 30) + "..." : json;
    }

    /**
     * @brief 从用户消息中简单提取关键词（画像提取失败时的回退）
     * 中文关键词控制在4-6字符内，确保LIKE匹配命中率
     */
    private String extractKeywordFallback(String text) {
        if (text == null || text.isEmpty()) return "科技成果";
        // 移除非中文字符和标点，取前4-6个汉字作为关键词
        String cleaned = text.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", "");
        if (cleaned.isEmpty()) return "科技成果";
        // 取前4个字符，短关键词确保LIKE模糊匹配命中率
        int len = Math.min(cleaned.length(), 4);
        return cleaned.substring(0, len);
    }

    /**
     * @brief 提取超短关键词（2个中文字符），最后兜底
     */
    private String extractUltraShortKeyword(String text) {
        if (text == null || text.isEmpty()) return "科技";
        String cleaned = text.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", "");
        if (cleaned.isEmpty()) return "科技";
        int len = Math.min(cleaned.length(), 2);
        return cleaned.substring(0, len);
    }

    private String nvl(String s) {
        return s != null ? s : "未标注";
    }
}
