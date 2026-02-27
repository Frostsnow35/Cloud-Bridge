package com.cloudbridge.service.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SearchService {

    @Value("${es.url}")
    private String esUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // In-memory store for fallback when ES is unavailable
    // Map<IndexName, Map<DocID, JSONString>>
    private final Map<String, Map<String, String>> memoryStore = new ConcurrentHashMap<>();

    public void createIndex(String indexName) {
        // Initialize memory store for this index
        memoryStore.computeIfAbsent(indexName, k -> new ConcurrentHashMap<>());
        
        String url = esUrl + "/" + indexName;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Check if exists
        try {
            restTemplate.headForHeaders(url);
            return; // Exists
        } catch (Exception e) {
            // Not exists, create
        }
        
        String body = "{\"settings\": {\"number_of_shards\": 1, \"number_of_replicas\": 0}}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        try {
            restTemplate.put(url, entity);
        } catch (Exception e) {
            System.err.println("Failed to create index " + indexName + ": " + e.getMessage());
        }
    }

    public void indexDocument(String indexName, String id, Object document) {
        // Always store in memory first as fallback
        try {
            String json = objectMapper.writeValueAsString(document);
            memoryStore.computeIfAbsent(indexName, k -> new ConcurrentHashMap<>()).put(id, json);
            if ("public_platforms".equals(indexName)) {
                System.err.println("Indexed public_platform: " + id);
            }
        } catch (Exception e) {
            System.err.println("Failed to serialize document for memory store: " + e.getMessage());
        }

        String url = esUrl + "/" + indexName + "/_doc/" + id;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(document, headers);
        try {
            restTemplate.put(url, entity);
        } catch (Exception e) {
            // Suppress error for connection refused in dev mode without ES
            if (!e.getMessage().contains("Connection refused")) {
                 System.err.println("Failed to index document " + id + " in " + indexName + ": " + e.getMessage());
            }
        }
    }

    public List<String> search(String indexName, String queryText) {
        // Try ES first
        try {
            List<String> results = searchES(indexName, queryText);
            // If ES returns results, return them.
            // BUT: If ES returns empty list, it might be because the index is empty in ES
            // but we have data in memory (e.g. public_platforms loaded from CSV but ES failed).
            // So if empty, fallback to memory/mock.
            if (!results.isEmpty()) {
                return results;
            }
            System.err.println("ES returned 0 results for " + indexName + ", falling back to memory/mocks");
            return getMockData(indexName, queryText);
        } catch (Exception e) {
            System.err.println("ES Search failed (" + e.getMessage() + "), returning mock data for " + indexName);
            return getMockData(indexName, queryText);
        }
    }

    private List<String> searchES(String indexName, String queryText) {
        String url = esUrl + "/" + indexName + "/_search";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> bodyMap = new java.util.HashMap<>();
        
        if (queryText == null || queryText.trim().isEmpty()) {
            Map<String, Object> matchAll = new java.util.HashMap<>();
            bodyMap.put("query", Collections.singletonMap("match_all", matchAll));
        } else {
            // Construct query using Map to avoid JSON injection
            Map<String, Object> multiMatch = new java.util.HashMap<>();
            multiMatch.put("query", queryText);
            multiMatch.put("fields", new String[]{
                "title", "content", "name", "description", "specs", 
                "abstractText", "inventor", "assignee", "patentNumber", 
                "industry", "products", "field", "provider", "department"
            });

            Map<String, Object> query = new java.util.HashMap<>();
            query.put("multi_match", multiMatch);
            bodyMap.put("query", query);
        }

        bodyMap.put("size", 20); // Increase size for list view

        HttpEntity<Object> entity = new HttpEntity<>(bodyMap, headers);
        try {
            // Use postForEntity with Map/Object body, RestTemplate will serialize it
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, entity, JsonNode.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode hits = response.getBody().path("hits").path("hits");
                List<String> results = new ArrayList<>();
                if (hits.isArray()) {
                    for (JsonNode hit : hits) {
                        results.add(hit.path("_source").toString());
                    }
                }
                return results;
            }
        } catch (Exception e) {
            throw new RuntimeException("ES Connection Failed", e);
        }
        return Collections.emptyList();
    }

    private List<String> getMockData(String indexName, String queryText) {
        System.err.println("=== SearchService: getMockData called for index: " + indexName);
        List<String> mocks = new ArrayList<>();
        
        // 1. Add data from in-memory store (populated by DataInitializer/Seeder)
        Map<String, String> indexData = memoryStore.get(indexName);
        if (indexData != null) {
            System.err.println("=== SearchService: Found " + indexData.size() + " items in memoryStore for " + indexName);
            mocks.addAll(indexData.values());
        } else {
            System.err.println("=== SearchService: memoryStore is NULL for " + indexName + ". Available keys: " + memoryStore.keySet());
        }

        // 2. Add hardcoded mocks if memory is empty (or mixed, depending on need)
        // Only add hardcoded if memory is empty to avoid duplicates if seeder runs?
        // Or just add them. Let's add them for robustness but check for ID collision if possible.
        // For simplicity, just adding them.
        
        if ("policies".equals(indexName)) {
            mocks.add("{\"title\": \"关于促进生物医药产业高质量发展的若干措施\", \"department\": \"市发改委\", \"publishDate\": \"2024-01-15\", \"policyType\": \"产业扶持\", \"content\": \"对新获批的创新药给予最高500万元奖励，支持企业建设高水平研发中心。\", \"industry\": [\"生物医药\"]}");
            mocks.add("{\"title\": \"中小企业数字化转型补贴方案\", \"department\": \"市工信局\", \"publishDate\": \"2024-02-10\", \"policyType\": \"资金补贴\", \"content\": \"支持中小企业购买云服务、工业软件，补贴比例最高50%。\", \"industry\": [\"数字经济\", \"智能制造\"]}");
            mocks.add("{\"title\": \"科技创新券实施管理办法\", \"department\": \"市科技局\", \"publishDate\": \"2024-03-01\", \"policyType\": \"服务券\", \"content\": \"企业购买检验检测、技术研发服务可使用科技创新券抵扣。\", \"industry\": [\"全行业\"]}");
        } else if ("experts".equals(indexName)) {
            mocks.add("{\"name\": \"张伟\", \"title\": \"教授\", \"affiliation\": \"理工大学\", \"field\": [\"人工智能\", \"计算机视觉\"], \"achievements\": \"主持国家自然科学基金项目3项，发表顶级会议论文20余篇，拥有多项发明专利。\"}");
            mocks.add("{\"name\": \"李娜\", \"title\": \"研究员\", \"affiliation\": \"科学院\", \"field\": [\"生物医药\", \"基因工程\"], \"achievements\": \"研发的新型抗肿瘤药物已进入临床II期试验。\"}");
            mocks.add("{\"name\": \"王强\", \"title\": \"高级工程师\", \"affiliation\": \"某大型车企\", \"field\": [\"新能源汽车\", \"电池管理\"], \"achievements\": \"主导开发的BMS系统显著提升了电池续航里程和安全性。\"}");
        } else if ("funds".equals(indexName)) {
            mocks.add("{\"name\": \"科技成果转化引导基金\", \"fundType\": \"天使轮\", \"amountRange\": \"100-500万\", \"provider\": \"市科创投\", \"interestRate\": \"股权投资\", \"industryFocus\": [\"硬科技\", \"生物医药\"]}");
            mocks.add("{\"name\": \"中小微企业科创贷\", \"fundType\": \"银行贷款\", \"amountRange\": \"50-200万\", \"provider\": \"建设银行\", \"interestRate\": \"3.85%\", \"industryFocus\": [\"高新技术企业\"]}");
        } else if ("equipments".equals(indexName)) {
            mocks.add("{\"name\": \"冷冻电镜 Titan Krios\", \"category\": \"分析仪器\", \"availability\": \"Available\", \"facilityName\": \"结构生物学中心\", \"specs\": \"300kV加速电压，配备Gatan K3相机\", \"owner\": \"生命科学学院\"}");
            mocks.add("{\"name\": \"超导核磁共振波谱仪 600MHz\", \"category\": \"分析仪器\", \"availability\": \"Maintenance\", \"facilityName\": \"分析测试中心\", \"specs\": \"600MHz，配备超低温探头\", \"owner\": \"化学化工学院\"}");
        } else if ("patents".equals(indexName)) {
            mocks.add("{\"title\": \"一种基于深度学习的图像超分辨率重建方法\", \"status\": \"已授权\", \"publicationDate\": \"2023-11-20\", \"patentNumber\": \"CN112233445B\", \"abstractText\": \"本发明公开了一种图像处理方法，通过残差网络显著提升了低分辨率图像的重建质量。\", \"assignee\": \"科技大学\"}");
            mocks.add("{\"title\": \"一种高强度耐腐蚀铝合金材料及其制备工艺\", \"status\": \"公开\", \"publicationDate\": \"2024-01-05\", \"patentNumber\": \"CN115566778A\", \"abstractText\": \"本发明涉及金属材料领域，特别是海洋工程用铝合金，具有优异的抗海水腐蚀性能。\", \"assignee\": \"新材料研究院\"}");
        } else if ("enterprises".equals(indexName)) {
            mocks.add("{\"id\": \"1001\", \"name\": \"智云科技股份有限公司\", \"industry\": \"人工智能\", \"location\": \"高新区\", \"scale\": \"500-1000人\", \"description\": \"专注于自然语言处理和知识图谱技术的研发与应用，服务于金融、医疗等领域。\"}");
            mocks.add("{\"id\": \"1002\", \"name\": \"绿能动力科技有限公司\", \"industry\": \"新能源\", \"location\": \"经开区\", \"scale\": \"100-499人\", \"description\": \"致力于高性能锂离子电池及储能系统的研发、生产和销售。\"}");
        } 
        
        // Public Platforms: Only return memoryStore data (loaded from CSV). Do NOT use hardcoded mocks.
        
        // Filter by keyword if provided (simple contains check)
        if (queryText != null && !queryText.isEmpty()) {
            List<String> filtered = new ArrayList<>();
            for (String json : mocks) {
                if (json.contains(queryText)) {
                    filtered.add(json);
                }
            }
            return filtered;
        }
        
        return mocks;
    }

    public Map<String, List<String>> searchAll(String queryText) {
        Map<String, List<String>> results = new java.util.HashMap<>();
        String[] indices = {"policies", "funds", "equipments", "experts", "patents", "enterprises", "public_platforms"};
        
        for (String index : indices) {
            results.put(index, search(index, queryText));
        }
        return results;
    }

    public String getById(String indexName, String id) {
        String url = esUrl + "/" + indexName + "/_doc/" + id;
        try {
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode source = response.getBody().path("_source");
                if (!source.isMissingNode()) {
                    return source.toString();
                }
            }
        } catch (Exception e) {
            // Fallback to memory store first
            Map<String, String> indexData = memoryStore.get(indexName);
            if (indexData != null && indexData.containsKey(id)) {
                return indexData.get(id);
            }
            
            System.err.println("GetById failed for " + id + " in " + indexName + ": " + e.getMessage());
            // Fallback to mock data for demo (but avoid for public_platforms if empty)
            List<String> mocks = getMockData(indexName, null);
            if (!mocks.isEmpty()) {
                // BUG FIX: Do NOT blindly return the first item. Check if ID matches.
                // For hardcoded mocks, we might iterate.
                for (String mockJson : mocks) {
                    try {
                        JsonNode node = objectMapper.readTree(mockJson);
                        if (node.has("id") && node.get("id").asText().equals(id)) {
                            return mockJson;
                        }
                    } catch (Exception ex) {
                        // ignore
                    }
                }
            }
        }
        return null;
    }
}
