package com.cloudbridge.service.agent;

import com.cloudbridge.dto.MatchingProfile;
import com.cloudbridge.entity.Achievement;
import com.cloudbridge.repository.AchievementRepository;
import com.cloudbridge.service.ai.AIService;
import com.cloudbridge.service.rag.SearchService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @brief Agent 工具集，封装平台现有服务能力为 LLM 可调用的 Tool
 * 每个 @Tool 方法的 value 描述字符串指导 LLM 何时调用该工具
 */
@Component
public class MatchAgentTools {

    @Autowired
    private SearchService searchService;

    @Autowired
    private AIService aiService;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * @brief 混合检索科技成果
     * @param query 搜索关键词（技术名称、领域、应用场景等）
     * @return 格式化后的成果列表文本
     */
    @Tool("在科技成果库中检索匹配的成果。当你需要为用户查找技术方案、匹配成果时调用。参数query为搜索关键词。")
    public String searchAchievements(String query) {
        try {
            List<String> results = searchService.search("achievements", query);
            return formatSearchResults("科技成果", results, 5);
        } catch (Exception e) {
            return "科技成果检索失败: " + e.getMessage() + "。请尝试其他工具或告知用户该服务暂不可用。";
        }
    }

    /**
     * @brief 检索产业政策
     * @param query 搜索关键词
     * @return 格式化后的政策列表文本
     */
    @Tool("检索与需求相关的产业扶持政策和补贴措施。当你需要帮用户了解政策支持时调用。参数query为搜索关键词。")
    public String searchPolicies(String query) {
        try {
            List<String> results = searchService.search("policies", query);
            return formatSearchResults("产业政策", results, 5);
        } catch (Exception e) {
            return "政策检索失败: " + e.getMessage() + "。该模块暂不可用，请先关注其他资源。";
        }
    }

    /**
     * @brief 检索科技金融产品
     * @param query 搜索关键词
     * @return 格式化后的资金列表文本
     */
    @Tool("检索科技金融产品、引导基金和贷款渠道。当你需要帮用户寻找资金来源时调用。参数query为搜索关键词。")
    public String searchFunds(String query) {
        try {
            List<String> results = searchService.search("funds", query);
            return formatSearchResults("科技金融", results, 5);
        } catch (Exception e) {
            return "资金检索失败: " + e.getMessage() + "。该模块暂不可用。";
        }
    }

    /**
     * @brief 检索领域专家
     * @param query 搜索关键词
     * @return 格式化后的专家列表文本
     */
    @Tool("检索相关领域的技术专家和研究团队。当你需要帮用户寻找技术合作方或咨询专家时调用。参数query为搜索关键词。")
    public String searchExperts(String query) {
        try {
            List<String> results = searchService.search("experts", query);
            return formatSearchResults("领域专家", results, 5);
        } catch (Exception e) {
            return "专家检索失败: " + e.getMessage() + "。该模块暂不可用。";
        }
    }

    /**
     * @brief 检索共享设备
     * @param query 搜索关键词
     * @return 格式化后的设备列表文本
     */
    @Tool("检索共享科研设备和仪器。当你需要帮用户寻找实验设备或检测仪器时调用。参数query为搜索关键词。")
    public String searchEquipments(String query) {
        try {
            List<String> results = searchService.search("equipments", query);
            return formatSearchResults("共享设备", results, 5);
        } catch (Exception e) {
            return "设备检索失败: " + e.getMessage() + "。该模块暂不可用。";
        }
    }

    /**
     * @brief AI 提取需求画像
     * @param description 用户的需求描述文本
     * @return 提取出的关键词、领域、应用场景、技术目标等结构化信息
     */
    @Tool("从用户的自然语言需求描述中提取核心画像：关键词、所属领域、子领域、应用场景和技术目标。当你需要精确理解用户技术需求时调用。参数description为用户的需求描述。")
    public String extractMatchingProfile(String description) {
        try {
            MatchingProfile profile = aiService.extractMatchingProfile(description);
            return String.format(
                "需求画像提取结果:\n- 核心关键词: %s\n- 所属领域: %s\n- 子领域: %s\n- 应用场景: %s\n- 技术目标: %s",
                profile.getKeyword() != null ? profile.getKeyword() : "未识别",
                profile.getField() != null ? profile.getField() : "未识别",
                profile.getSubField() != null ? profile.getSubField() : "未识别",
                profile.getApplicationScenario() != null ? profile.getApplicationScenario() : "未识别",
                profile.getTechnicalGoal() != null ? profile.getTechnicalGoal() : "未识别"
            );
        } catch (Exception e) {
            return "需求画像提取失败: " + e.getMessage() + "。请基于用户原始描述继续分析。";
        }
    }

    /**
     * @brief 根据DB中的成果数据生成候选集信息供Agent决策参考
     * @param keyword 搜索关键词
     * @return 候选成果简要信息
     */
    @Tool("根据关键词检索数据库中的候选科技成果并返回简要信息，用于Agent后续筛选推荐。当你已明确用户需求、需要具体成果数据时调用。参数keyword为从需求画像中提取的核心关键词。")
    public String searchCandidateAchievements(String keyword) {
        try {
            List<Achievement> candidates = achievementRepository.findPublishedByKeyword(keyword);
            if (candidates.isEmpty()) {
                // 尝试领域扩展搜索
                candidates = achievementRepository.findByFieldContainingAndStatus(keyword, Achievement.Status.PUBLISHED);
            }
            if (candidates.isEmpty()) {
                return "未找到与'" + keyword + "'直接匹配的科技成果。建议扩大搜索范围或尝试相关关键词。";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("找到 ").append(candidates.size()).append(" 项候选科技成果:\n");
            int count = 0;
            for (Achievement a : candidates) {
                if (count >= 10) {
                    sb.append("...（还有更多结果，已截取前10项）\n");
                    break;
                }
                sb.append(String.format("- ID:%d | %s | 领域:%s | 成熟度:%s | 价格:%s\n",
                    a.getId(),
                    a.getTitle(),
                    a.getField() != null ? a.getField() : "未标注",
                    a.getMaturity() != null ? a.getMaturity() : "未标注",
                    a.getPrice() != null ? a.getPrice().toString() : "面议"
                ));
                count++;
            }
            return sb.toString();
        } catch (Exception e) {
            return "候选成果检索失败: " + e.getMessage();
        }
    }

    /**
     * @brief 将JSON搜索结果格式化为可读文本
     */
    private String formatSearchResults(String category, List<String> results, int maxItems) {
        if (results == null || results.isEmpty()) {
            return "未找到相关" + category + "资源。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(category).append("匹配结果（").append(results.size()).append("项）:\n");
        int count = 0;
        for (String json : results) {
            if (count >= maxItems) {
                sb.append("...（还有更多结果）\n");
                break;
            }
            try {
                JsonNode node = objectMapper.readTree(json);
                String title = node.has("title") ? node.get("title").asText()
                        : node.has("name") ? node.get("name").asText()
                        : "未知";
                String desc = "";
                if (node.has("description")) {
                    desc = node.get("description").asText();
                    if (desc.length() > 80) desc = desc.substring(0, 80) + "...";
                } else if (node.has("content")) {
                    desc = node.get("content").asText();
                    if (desc.length() > 80) desc = desc.substring(0, 80) + "...";
                }
                sb.append("- ").append(title);
                if (!desc.isEmpty()) sb.append(": ").append(desc);
                sb.append("\n");
            } catch (Exception e) {
                // 直接展示原始片段
                String snippet = json.length() > 120 ? json.substring(0, 120) + "..." : json;
                sb.append("- ").append(snippet).append("\n");
            }
            count++;
        }
        return sb.toString();
    }
}
