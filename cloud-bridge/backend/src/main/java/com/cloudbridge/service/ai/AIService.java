package com.cloudbridge.service.ai;

import com.cloudbridge.dto.AIRequest;
import com.cloudbridge.dto.AIResponse;
import com.cloudbridge.dto.MatchingProfile;
import com.cloudbridge.service.rag.SearchService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

@Service
public class AIService {

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.model}")
    private String modelName;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SearchService searchService;

    @Autowired
    private ObjectMapper objectMapper;

    public String chatWithIntent(String message) {
        // Escape % to avoid String.format errors
        String safeMessage = message != null ? message.replace("%", "%%") : "";

        String prompt = String.format(
            "你是一个‘云桥’技术成果转化平台的智能助手。请分析用户的输入，判断意图并返回JSON格式的响应。\n" +
            "用户输入：\"%s\"\n" +
            "**任务**：\n" +
            "1. 判断用户意图（Intent）：\n" +
            "   - NAVIGATE: 用户想要跳转到某个功能页面（如搜索、匹配、发布）。\n" +
            "   - GUIDANCE: 用户询问如何使用平台功能（如“怎么发布需求”）。\n" +
            "   - FEEDBACK: 用户想要反馈问题或Bug。\n" +
            "   - CHAT: 普通闲聊或无关话题。\n" +
            "2. 生成回复（Reply）：亲切、专业的文本回复。\n" +
            "3. 生成操作（Action）：仅在NAVIGATE或FEEDBACK时需要。\n" +
            "\n" +
            "**路由规则（用于NAVIGATE）**：\n" +
            "- 找成果、找技术、搜索技术 -> /achievements (参数: keyword)\n" +
            "- 找需求、找资金、搜索需求 -> /needs (参数: keyword)\n" +
            "- 找专利 -> /libraries/patents (参数: keyword)\n" +
            "- 找企业、找公司 -> /libraries/enterprises (参数: keyword)\n" +
            "- 找专家、找人才 -> /libraries/experts (参数: keyword)\n" +
            "- 找设备、找仪器 -> /libraries/equipments (参数: keyword)\n" +
            "- 找政策 -> /libraries/policies (参数: keyword)\n" +
            "- 找资金 -> /libraries/funds (参数: keyword)\n" +
            "- 智能匹配、自动匹配 -> /match (参数: keyword)\n" +
            "- 发布需求 -> /needs/publish\n" +
            "- 个人中心、我的主页 -> /profile\n" +
            "- 首页 -> /\n" +
            "\n" +
            "**输出格式示例**：\n" +
            "1. 跳转示例：\n" +
            "   {\"intent\": \"NAVIGATE\", \"reply\": \"好的，正在为您查找碳纤维相关技术...\", \"action\": {\"type\": \"NAVIGATE\", \"payload\": {\"path\": \"/market\", \"query\": {\"keyword\": \"碳纤维\"}}}}\n" +
            "2. 反馈示例：\n" +
            "   {\"intent\": \"FEEDBACK\", \"reply\": \"收到，请填写以下表单反馈您的问题，我们会尽快处理。\", \"action\": {\"type\": \"OPEN_FEEDBACK_FORM\"}}\n" +
            "3. 指引示例：\n" +
            "   {\"intent\": \"GUIDANCE\", \"reply\": \"您可以点击顶部导航栏的‘需求大厅’，然后点击右上角的‘发布需求’按钮来提交您的需求。\"}\n" +
            "\n" +
            "请严格只返回JSON字符串，不要包含Markdown标记（如```json）。",
            safeMessage
        );

        AIRequest request = new AIRequest();
        request.setModel(modelName);
        request.setTemperature(0.3); // Lower temperature for stability
        request.setMessages(Arrays.asList(
            new AIRequest.Message("system", "You are a helpful and intelligent assistant for a Tech Transfer Platform. You respond in JSON."),
            new AIRequest.Message("user", prompt)
        ));

        try {
            return callAI(request);
        } catch (Exception e) {
            // Fallback for demo if AI fails
            System.err.println("AI Chat Intent Failed: " + e.getMessage());
            return fallbackChatIntent(message);
        }
    }

    private String fallbackChatIntent(String message) {
        // Simple rule-based fallback
        String intent = "CHAT";
        String reply = "抱歉，我现在还在学习中。";
        String actionJson = "null";

        if (message.contains("匹配") || message.contains("找")) {
            intent = "NAVIGATE";
            reply = "好的，正在为您跳转匹配页面...";
            actionJson = "{\"type\": \"NAVIGATE\", \"payload\": {\"path\": \"/smart-match\"}}";
        } else if (message.contains("发布")) {
            intent = "NAVIGATE";
            reply = "您可以前往需求大厅发布需求。";
            actionJson = "{\"type\": \"NAVIGATE\", \"payload\": {\"path\": \"/needs/add\"}}";
        } else if (message.contains("bug") || message.contains("反馈") || message.contains("问题")) {
            intent = "FEEDBACK";
            reply = "请填写反馈表单。";
            actionJson = "{\"type\": \"OPEN_FEEDBACK_FORM\"}";
        }

        return String.format(
            "{\"intent\": \"%s\", \"reply\": \"%s\", \"action\": %s}",
            intent, reply, actionJson
        );
    }

    public String extractGraphData(String text) {
        // Escape % to avoid String.format errors
        String safeText = text != null ? text.replace("%", "%%") : "";
        
        String prompt = String.format(
            "你是一个知识图谱专家。请分析以下需求描述，构建一个清晰、严格的**技术依赖图谱**。\n" +
            "需求描述：\"%s\"。\n" +
            "请按照以下步骤进行思维链（Chain of Thought）分析：\n" +
            "1. **实体识别**：仅提取**核心技术实体**和**直接应用场景**。忽略泛泛而谈的概念（如“高效”、“智能”）。\n" +
            "2. **关系构建**：仅允许建立以下三种强关系：\n" +
            "   - **BELONGS_TO** (属于): 如 'T800碳纤维' BELONGS_TO '新材料'\n" +
            "   - **APPLIED_IN** (应用于): 如 '脑机接口' APPLIED_IN '医疗康复'\n" +
            "   - **REQUIRES** (依赖): 如 '深度学习' REQUIRES '高性能计算'\n" +
            "   **禁止建立松散关联**（如 '脑机接口' -> '未来科技'，或 '脑机接口' -> '工业检测' 除非文中明确提及）。\n" +
            "3. **JSON生成**：基于上述分析，生成符合Schema的JSON。\n" +
            "\n" +
            "**Schema定义**：\n" +
            "- nodes: [{id, label, type}]\n" +
            "- relationships: [{source, target, type}]\n" +
            "**Few-Shot Example**:\n" +
            "Input: \"我们需要研发用于航空发动机叶片的耐高温陶瓷基复合材料。\"\n" +
            "CoT: \n" +
            "- 实体识别: 耐高温陶瓷基复合材料(Technology), 航空发动机叶片(Application)\n" +
            "- 分类推导: 陶瓷基复合材料 -> 复合材料(SubCategory) -> 新材料(Category)\n" +
            "- 关系构建: 陶瓷基复合材料 APPLIED_IN 航空发动机叶片\n" +
            "JSON: {\"nodes\": [{\"id\": \"root\", \"label\": \"耐高温陶瓷基复合材料\", \"type\": \"Demand\"}, {\"id\": \"c1\", \"label\": \"新材料\", \"type\": \"Category\"}, {\"id\": \"s1\", \"label\": \"复合材料\", \"type\": \"SubCategory\"}, {\"id\": \"t1\", \"label\": \"陶瓷基复合材料\", \"type\": \"Technology\"}, {\"id\": \"a1\", \"label\": \"航空发动机叶片\", \"type\": \"Application\"}], \"relationships\": [{\"source\": \"root\", \"target\": \"c1\", \"type\": \"BELONGS_TO\"}, {\"source\": \"c1\", \"target\": \"s1\", \"type\": \"INCLUDES\"}, {\"source\": \"s1\", \"target\": \"t1\", \"type\": \"IMPLEMENTED_BY\"}, {\"source\": \"t1\", \"target\": \"a1\", \"type\": \"APPLIED_IN\"}]}\n" +
            "\n" +
            "请输出最终的JSON，不要包含Markdown标记。",
            safeText
        );

        AIRequest request = new AIRequest();
        request.setModel(modelName);
        request.setTemperature(0.2); // Low temp for precision
        request.setMessages(Arrays.asList(
            new AIRequest.Message("system", "You are a knowledge graph extractor that uses strict Chain of Thought reasoning."),
            new AIRequest.Message("user", prompt)
        ));

        try {
            return callAI(request);
        } catch (Exception e) {
            System.err.println("AI Graph Extraction Failed, using fallback: " + e.getMessage());
            return fallbackGraphExtraction(text);
        }
    }

    public String analyzeDemandWithRAG(String demandText) {
        // 1. Retrieve context
        List<String> policies = searchService.search("policies", demandText);
        List<String> funds = searchService.search("funds", demandText);
        List<String> equipments = searchService.search("equipments", demandText);
        
        // 2. Construct Prompt with Context
        String context = "相关政策:\n" + String.join("\n", policies) + 
                         "\n相关资金:\n" + String.join("\n", funds) +
                         "\n相关设备:\n" + String.join("\n", equipments);
        
        // Escape for String.format
        String safeDemandText = demandText != null ? demandText.replace("%", "%%") : "";
        String safeContext = context.replace("%", "%%");
                         
        String prompt = String.format(
            "你是一个科技成果转化专家。请基于以下用户需求和参考资料（RAG Context），生成一份详细的分析报告。\n" +
            "用户需求：\"%s\"\n" +
            "参考资料：\n%s\n" +
            "请分析：\n" +
            "1. 技术可行性与领域定位。\n" +
            "2. 政策匹配：引用参考资料中的具体政策。\n" +
            "3. 资源推荐：引用参考资料中的资金或设备。\n",
            safeDemandText, safeContext
        );
        
        AIRequest request = new AIRequest();
        request.setModel(modelName);
        request.setTemperature(0.5);
        request.setMessages(Arrays.asList(
            new AIRequest.Message("system", "You are an expert consultant for technology transfer."),
            new AIRequest.Message("user", prompt)
        ));
        
        try {
            return callAI(request);
        } catch (Exception e) {
            return "Analysis failed: " + e.getMessage();
        }
    }

    public java.util.Map<Long, Double> evaluateMatches(String demandDesc, MatchingProfile profile, java.util.List<com.cloudbridge.entity.Achievement> candidates) {
        if (candidates.isEmpty()) return java.util.Collections.emptyMap();
        
        // Construct batch prompt
        StringBuilder candidateText = new StringBuilder();
        for (com.cloudbridge.entity.Achievement a : candidates) {
            candidateText.append(String.format("- ID:%d, Title:%s, Desc:%s\n", a.getId(), a.getTitle(), a.getDescription()));
        }

        String prompt = String.format(
            "你是一个严格的成果匹配评审专家。请根据以下需求，对候选成果进行匹配度打分。\n" +
            "**需求详情**：\n" +
            "描述：%s\n" +
            "核心画像：领域=%s, 子领域=%s, 应用场景=%s, 技术目标=%s\n" +
            "\n" +
            "**候选成果列表**：\n" +
            "%s\n" +
            "\n" +
            "**评分规则**（必须严格遵守）：\n" +
            "1. **完全不匹配 (0分)**：核心领域不同，或技术目标完全无关（如：'脑机接口' vs '工业检测'）。\n" +
            "2. **弱匹配 (1-50分)**：领域相同，但应用场景差异巨大（如：'医疗康复' vs '手术机器人'）。\n" +
            "3. **强匹配 (60-100分)**：子领域、应用场景、技术目标均高度吻合（语义模糊匹配即可）。\n" +
            "\n" +
            "**任务**：\n" +
            "请返回一个JSON对象，Key为成果ID，Value为0-100的评分。\n" +
            "格式示例：{\"101\": 85, \"102\": 0, \"103\": 40}\n" +
            "只返回JSON，不要Markdown。",
            demandDesc, profile.getField(), profile.getSubField(), profile.getApplicationScenario(), profile.getTechnicalGoal(), candidateText.toString()
        );

        AIRequest request = new AIRequest();
        request.setModel(modelName);
        request.setTemperature(0.1);
        request.setMessages(Arrays.asList(
            new AIRequest.Message("system", "You are a strict matching evaluator. Output JSON only."),
            new AIRequest.Message("user", prompt)
        ));

        try {
            String json = callAI(request);
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<Long, Double>>(){});
        } catch (Exception e) {
            System.err.println("AI Reranking Failed: " + e.getMessage());
            return java.util.Collections.emptyMap();
        }
    }

    private String callAI(AIRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.setBearerAuth(apiKey);
        }

        HttpEntity<AIRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<AIResponse> response = restTemplate.postForEntity(apiUrl, entity, AIResponse.class);
        
        if (response.getBody() != null && !response.getBody().getChoices().isEmpty()) {
            String content = response.getBody().getChoices().get(0).getMessage().getContent();
            return content.replaceAll("```json", "").replaceAll("```", "").trim();
        }
        throw new RuntimeException("Empty AI response");
    }

    private String fallbackGraphExtraction(String text) {
        // Simple rule-based extraction for demo purposes
        String label1 = "关键技术";
        String label2 = "应用场景";
        String catLabel = "相关领域";
        
        if (text.contains("碳纤维") || text.contains("材料")) { 
            label1 = "碳纤维复合材料"; label2 = "航空航天"; catLabel = "新材料";
        } else if (text.contains("AI") || text.contains("智能") || text.contains("数据")) { 
            label1 = "深度学习"; label2 = "智慧城市"; catLabel = "人工智能";
        } else if (text.contains("生物") || text.contains("医药") || text.contains("疫苗")) { 
            label1 = "基因编辑"; label2 = "新药研发"; catLabel = "生物医药";
        } else if (text.contains("能源") || text.contains("电池")) { 
            label1 = "固态电池"; label2 = "新能源汽车"; catLabel = "新能源";
        }

        String safeText = text.length() > 6 ? text.substring(0, 6) + "..." : text;
        
        return String.format(
            "{" +
            "  \"nodes\": [" +
            "    {\"id\": \"Input\", \"label\": \"%s\", \"type\": \"Demand\"}," +
            "    {\"id\": \"Cat1\", \"label\": \"%s\", \"type\": \"Category\"}," +
            "    {\"id\": \"Tech1\", \"label\": \"%s\", \"type\": \"Technology\"}," +
            "    {\"id\": \"App1\", \"label\": \"%s\", \"type\": \"Application\"}" +
            "  ]," +
            "  \"relationships\": [" +
            "    {\"source\": \"Input\", \"target\": \"Cat1\", \"type\": \"BELONGS_TO\"}," +
            "    {\"source\": \"Cat1\", \"target\": \"Tech1\", \"type\": \"INCLUDES\"}," +
            "    {\"source\": \"Tech1\", \"target\": \"App1\", \"type\": \"APPLIED_IN\"}" +
            "  ]" +
            "}",
            safeText, catLabel, label1, label2
        );
    }

    public String analyzeDemandWithFullRAG(String demandText) {
        // 1. Retrieve context from all indices
        java.util.Map<String, List<String>> allResults = searchService.searchAll(demandText);
        
        // 2. Construct Prompt with Categorized Context
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("相关政策:\n").append(String.join("\n", allResults.getOrDefault("policies", Collections.emptyList()))).append("\n\n");
        contextBuilder.append("相关资金:\n").append(String.join("\n", allResults.getOrDefault("funds", Collections.emptyList()))).append("\n\n");
        contextBuilder.append("相关专家:\n").append(String.join("\n", allResults.getOrDefault("experts", Collections.emptyList()))).append("\n\n");
        contextBuilder.append("相关设备:\n").append(String.join("\n", allResults.getOrDefault("equipments", Collections.emptyList()))).append("\n\n");
        contextBuilder.append("相关专利:\n").append(String.join("\n", allResults.getOrDefault("patents", Collections.emptyList()))).append("\n\n");
        contextBuilder.append("相关企业:\n").append(String.join("\n", allResults.getOrDefault("enterprises", Collections.emptyList()))).append("\n\n");
        
        String safeDemandText = demandText != null ? demandText.replace("%", "%%") : "";
        String safeContext = contextBuilder.toString().replace("%", "%%");
        
        String prompt = String.format(
            "你是一个科技成果转化与产业分析专家。请基于以下用户需求和全方位参考资料（RAG Context），生成一份深度的**可行性与落地分析报告**。\n" +
            "用户需求：\"%s\"\n" +
            "参考资料：\n%s\n" +
            "请按以下结构生成报告（Markdown格式）：\n" +
            "## 1. 技术可行性与领域定位\n" +
            "- 分析技术难点与当前成熟度。\n" +
            "- 结合【相关专利】和【相关专家】，评估技术壁垒。\n" +
            "\n" +
            "## 2. 产业资源匹配\n" +
            "- **政策支持**：引用【相关政策】中的具体条款，说明可申请的补贴或资质。\n" +
            "- **资金渠道**：推荐【相关资金】中的产品，并给出融资建议。\n" +
            "- **合作伙伴**：基于【相关企业】推荐潜在的上下游合作伙伴。\n" +
            "\n" +
            "## 3. 落地建议\n" +
            "- 综合以上信息，给出具体的行动路线图（如：先申请X专利，再对接Y基金）。\n",
            safeDemandText, safeContext
        );
        
        AIRequest request = new AIRequest();
        request.setModel(modelName);
        request.setTemperature(0.5);
        request.setMessages(Arrays.asList(
            new AIRequest.Message("system", "You are a senior expert consultant for technology transfer."),
            new AIRequest.Message("user", prompt)
        ));
        
        try {
            return callAI(request);
        } catch (Exception e) {
            return "Deep analysis failed: " + e.getMessage();
        }
    }

    public String generateResourceGraph(String type, String id) {
        // 1. Get the target entity
        String indexName = type.toLowerCase() + "s"; // simple pluralization
        if (indexName.endsWith("ss")) indexName = indexName.substring(0, indexName.length() - 1); // fix 'patentss' if type is 'patents'
        if (type.equals("policy")) indexName = "policies";
        if (type.equals("fund")) indexName = "funds";
        if (type.equals("expert")) indexName = "experts";
        if (type.equals("equipment")) indexName = "equipments";
        if (type.equals("patent")) indexName = "patents";
        if (type.equals("enterprise")) indexName = "enterprises";

        String entityJson = searchService.getById(indexName, id);
        if (entityJson == null) return "{\"error\": \"Entity not found\"}";
        
        // 2. Extract key info for searching related entities
        String keyword = "";
        try {
            JsonNode node = objectMapper.readTree(entityJson);
            if (node.has("title")) keyword = node.get("title").asText();
            else if (node.has("name")) keyword = node.get("name").asText();
            else if (node.has("field")) keyword = node.get("field").asText();
        } catch (Exception e) {
            keyword = "Technology";
        }
        
        // 3. Search related entities
        java.util.Map<String, List<String>> related = searchService.searchAll(keyword);
        
        // 4. Use AI to construct the graph JSON
        StringBuilder relatedContext = new StringBuilder();
        int count = 0;
        for (Map.Entry<String, List<String>> entry : related.entrySet()) {
            for (String item : entry.getValue()) {
                if (count++ > 15) break; // Limit context size
                relatedContext.append(entry.getKey()).append(": ").append(item).append("\n");
            }
        }
        
        String prompt = String.format(
            "请基于核心实体和相关搜索结果，构建一个资源关联图谱（JSON）。\n" +
            "核心实体 (%s): %s\n" +
            "相关资源:\n%s\n" +
            "任务：\n" +
            "1. 将核心实体作为根节点。\n" +
            "2. 从相关资源中挑选最相关的5-8个实体作为子节点。\n" +
            "3. 确定节点间的关系类型 (e.g., 'SUPPORTED_BY', 'RESEARCHED_BY', 'OWNED_BY', 'RELATED_TO').\n" +
            "4. 返回符合 ECharts Graph 数据格式的 JSON: { \"nodes\": [{ \"id\": \"...\", \"name\": \"...\", \"category\": \"...\" }], \"links\": [{ \"source\": \"...\", \"target\": \"...\", \"name\": \"...\" }] }\n" +
            "只返回JSON，不要Markdown。",
            type, entityJson.replace("%", "%%"), relatedContext.toString().replace("%", "%%")
        );
        
        AIRequest request = new AIRequest();
        request.setModel(modelName);
        request.setTemperature(0.2);
        request.setMessages(Arrays.asList(
            new AIRequest.Message("system", "You are a data visualization expert."),
            new AIRequest.Message("user", prompt)
        ));
        
        try {
            return callAI(request);
        } catch (Exception e) {
            return "{\"nodes\": [], \"links\": []}";
        }
    }

    public MatchingProfile extractMatchingProfile(String userDescription) {
        String safeDesc = userDescription != null ? userDescription.replace("%", "%%") : "";
        
        String prompt = String.format(
            "你是一个科技成果转化专家。请从以下用户需求描述中，提取出关键的匹配画像信息。\n" +
            "用户描述：\"%s\"。\n" +
            "请提取以下5个字段：\n" +
            "1. **keyword** (核心技术关键词): 如 '深度学习', '石墨烯'\n" +
            "2. **field** (所属一级领域): 必须从以下列表中选择一个: [\"生物医药\", \"新材料\", \"新能源\", \"人工智能\", \"大数据\", \"物联网\", \"环保科技\", \"智能制造\", \"金融科技\", \"数字孪生\", \"区块链\", \"量子通信\"]\n" +
            "3. **subField** (二级细分领域): 如 '脑机接口', '计算机视觉', '基因编辑'\n" +
            "4. **applicationScenario** (具体应用场景): 如 '医疗康复', '工业缺陷检测', '智慧交通'\n" +
            "5. **technicalGoal** (核心技术目标): 如 '信号采集与解码', '提高识别准确率', '降低能耗'\n" +
            "\n" +
            "**Few-Shot Examples**:\n" +
            "Input: \"希望能研发一种通过脑电波控制机械臂的技术，帮助中风患者进行康复训练。\"\n" +
            "Output: {\"keyword\": \"脑电波控制\", \"field\": \"人工智能\", \"subField\": \"脑机接口\", \"applicationScenario\": \"医疗康复\", \"technicalGoal\": \"运动控制与神经反馈\"}\n" +
            "\n" +
            "Input: \"工厂流水线上需要一种能自动识别产品表面划痕和污渍的视觉系统。\"\n" +
            "Output: {\"keyword\": \"表面缺陷检测\", \"field\": \"人工智能\", \"subField\": \"计算机视觉\", \"applicationScenario\": \"工业制造\", \"technicalGoal\": \"自动化缺陷识别\"}\n" +
            "\n" +
            "请严格以JSON格式返回，不要包含任何Markdown标记。",
            safeDesc
        );

        AIRequest request = new AIRequest();
        request.setModel(modelName);
        request.setTemperature(0.2); // Low temp for precision
        request.setMessages(Arrays.asList(
            new AIRequest.Message("system", "You are a helpful assistant that outputs JSON."),
            new AIRequest.Message("user", prompt)
        ));

        try {
            String json = callAI(request);
            return objectMapper.readValue(json, MatchingProfile.class);
        } catch (Exception e) {
            System.err.println("AI Profile Extraction Error: " + e.getMessage());
            // Fallback
            MatchingProfile p = new MatchingProfile();
            String k = fallbackExtractKeyword(userDescription);
            p.setKeyword(k);
            p.setField(inferFieldFromKeyword(k));
            return p;
        }
    }

    private String inferFieldFromKeyword(String keyword) {
        if (keyword == null) return "";
        if (keyword.equals("深度学习") || keyword.equals("神经网络") || keyword.equals("机器学习") || 
            keyword.equals("自然语言处理") || keyword.equals("计算机视觉") || keyword.equals("知识图谱")) {
            return "人工智能";
        }
        if (keyword.equals("区块链") || keyword.equals("分布式")) return "区块链";
        if (keyword.equals("量子通信")) return "量子通信";
        if (keyword.equals("辅助诊断") || keyword.equals("疾病筛查") || keyword.equals("智慧医疗")) return "生物医药";
        if (keyword.equals("金融科技")) return "金融科技";
        if (keyword.equals("环保科技")) return "环保科技";
        if (keyword.equals("智能制造")) return "智能制造";
        if (keyword.equals("智慧交通")) return "人工智能"; // Broad categorization
        return "";
    }

    public String extractKeywords(String userDescription) {
        try {
            MatchingProfile profile = extractMatchingProfile(userDescription);
            return objectMapper.writeValueAsString(profile);
        } catch (Exception e) {
            return "{}";
        }
    }


    private String fallbackExtractKeyword(String text) {
        if (text == null) return "";
        
        // 1. Specific Technology Keywords (High Priority)
        if (text.contains("深度学习")) return "深度学习";
        if (text.contains("神经网络")) return "神经网络";
        if (text.contains("机器学习")) return "机器学习";
        if (text.contains("自然语言")) return "自然语言处理";
        if (text.contains("计算机视觉")) return "计算机视觉";
        if (text.contains("知识图谱")) return "知识图谱";
        if (text.contains("区块链")) return "区块链";
        if (text.contains("分布式")) return "分布式";
        if (text.contains("量子")) return "量子通信";
        
        // 2. Application/Domain Keywords (Medium Priority)
        if (text.contains("诊断")) return "辅助诊断";
        if (text.contains("筛查")) return "疾病筛查";
        if (text.contains("医疗")) return "智慧医疗";
        if (text.contains("金融")) return "金融科技";
        if (text.contains("交通")) return "智慧交通";
        if (text.contains("环保")) return "环保科技";
        if (text.contains("制造")) return "智能制造";
        if (text.contains("教育")) return "智慧教育";
        if (text.contains("农业")) return "智慧农业";

        // 3. Fallback: First word or Short Phrase
        // Try to split by space first
        String[] parts = text.split("\\s+");
        if (parts.length > 0 && parts[0].length() > 0 && parts[0].length() < 10) {
            return parts[0];
        }
        // Fallback for Chinese or long text without spaces: take first 4 chars
        return text.substring(0, Math.min(text.length(), 4));
    }
}
