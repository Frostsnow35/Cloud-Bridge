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

/**
 * @brief Agent 精简工具集（3个核心工具，适配 llama-3.1 单工具调用限制）
 */
@Component("agentCoreTools")
public class MatchAgentCoreTools {

    @Autowired
    private SearchService searchService;

    @Autowired
    private AIService aiService;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Tool("从用户需求描述中提取核心画像：关键词、所属领域、应用场景和技术目标。当用户需求描述足够明确时调用此工具。")
    public String extractMatchingProfile(String description) {
        try {
            MatchingProfile profile = aiService.extractMatchingProfile(description);
            return String.format(
                "需求画像: 关键词=%s, 领域=%s, 子领域=%s, 应用场景=%s, 技术目标=%s",
                nvl(profile.getKeyword()), nvl(profile.getField()), nvl(profile.getSubField()),
                nvl(profile.getApplicationScenario()), nvl(profile.getTechnicalGoal()));
        } catch (Exception e) {
            return "画像提取失败: " + e.getMessage();
        }
    }

    @Tool("统一检索平台资源。type 取值为: achievements(科技成果), policies(产业政策), funds(科技金融), experts(领域专家), equipments(共享设备)。query 为搜索关键词。你可以多次调用此工具检索不同类型资源。")
    public String searchResources(String type, String query) {
        try {
            List<String> results = searchService.search(type, query);
            if (results == null || results.isEmpty()) {
                return "未找到相关" + getTypeName(type) + "。";
            }
            StringBuilder sb = new StringBuilder();
            sb.append(getTypeName(type)).append("(").append(results.size()).append("项):\n");
            int count = 0;
            for (String json : results) {
                if (count >= 5) { sb.append("...(更多结果)\n"); break; }
                try {
                    JsonNode node = objectMapper.readTree(json);
                    String title = node.has("title") ? node.get("title").asText()
                            : node.has("name") ? node.get("name").asText() : "未知";
                    String desc = "";
                    if (node.has("description")) {
                        desc = node.get("description").asText();
                        if (desc.length() > 60) desc = desc.substring(0, 60) + "...";
                    }
                    sb.append("- ").append(title);
                    if (!desc.isEmpty()) sb.append(": ").append(desc);
                    sb.append("\n");
                } catch (Exception e) {
                    String s = json.length() > 100 ? json.substring(0, 100) + "..." : json;
                    sb.append("- ").append(s).append("\n");
                }
                count++;
            }
            return sb.toString();
        } catch (Exception e) {
            return getTypeName(type) + "检索失败: " + e.getMessage();
        }
    }

    @Tool("根据关键词在数据库中检索候选科技成果。当需要获取详细成果数据（ID、成熟度、价格等）进行筛选推荐时调用。")
    public String searchCandidateAchievements(String keyword) {
        try {
            List<Achievement> candidates = achievementRepository.findPublishedByKeyword(keyword);
            if (candidates.isEmpty()) {
                candidates = achievementRepository.findByFieldContainingAndStatus(keyword, Achievement.Status.PUBLISHED);
            }
            if (candidates.isEmpty()) {
                return "未找到与'" + keyword + "'匹配的科技成果。建议扩大搜索范围。";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("找到").append(candidates.size()).append("项候选成果:\n");
            int count = 0;
            for (Achievement a : candidates) {
                if (count >= 10) { sb.append("...(还有更多)\n"); break; }
                sb.append(String.format("- ID:%d | %s | 领域:%s | 成熟度:%s | 价格:%s\n",
                    a.getId(), a.getTitle(),
                    nvl(a.getField()), nvl(a.getMaturity()),
                    a.getPrice() != null ? a.getPrice().toString() : "面议"));
                count++;
            }
            return sb.toString();
        } catch (Exception e) {
            return "候选成果检索失败: " + e.getMessage();
        }
    }

    private String getTypeName(String type) {
        switch (type) {
            case "achievements": return "科技成果";
            case "policies": return "产业政策";
            case "funds": return "科技金融";
            case "experts": return "领域专家";
            case "equipments": return "共享设备";
            default: return type;
        }
    }

    private String nvl(String s) { return s != null ? s : "未标注"; }
}
