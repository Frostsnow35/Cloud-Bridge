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
        createIndex(indexName, 0); // Default no vector
    }

    public void createVectorIndex(String indexName, int dimension) {
        createIndex(indexName, dimension);
    }

    private void createIndex(String indexName, int dimension) {
        // Initialize memory store for this index
        memoryStore.computeIfAbsent(indexName, k -> new ConcurrentHashMap<>());
        
        String url = esUrl + "/" + indexName;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Check if exists
        try {
            restTemplate.headForHeaders(url);
            System.out.println("Index " + indexName + " already exists, skipping creation.");
            return; // Exists
        } catch (Exception e) {
            System.out.println("Index " + indexName + " does not exist, creating...");
        }
        
        String body;
        if (dimension > 0) {
            // Create with dense_vector mapping and IK Chinese analyzer
            // IK analyzer provides better Chinese text segmentation than 'standard'
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            // Index settings with analysis config
            sb.append("\"settings\": {");
            sb.append("\"number_of_shards\": 1,");
            sb.append("\"number_of_replicas\": 0,");
            // IK analyzer configuration (will gracefully fail if plugin not installed)
            sb.append("\"analysis\": {");
            sb.append("\"analyzer\": {");
            sb.append("\"ik_analyzer\": {");
            sb.append("\"type\": \"custom\",");
            sb.append("\"tokenizer\": \"ik_max_word\",");
            sb.append("\"filter\": [\"lowercase\"]");
            sb.append("},");
            sb.append("\"ik_search_analyzer\": {");
            sb.append("\"type\": \"custom\",");
            sb.append("\"tokenizer\": \"ik_smart\",");
            sb.append("\"filter\": [\"lowercase\"]");
            sb.append("}");
            sb.append("}");
            sb.append("},");
            sb.append("\"refresh_interval\": \"1s\"");
            sb.append("},");
            // Mappings with IK analyzer
            sb.append("\"mappings\": {");
            sb.append("\"properties\": {");
            // Full-text search fields with IK analyzer
            sb.append("\"title\": {\"type\": \"text\", \"analyzer\": \"ik_analyzer\", \"search_analyzer\": \"ik_search_analyzer\"},");
            sb.append("\"description\": {\"type\": \"text\", \"analyzer\": \"ik_analyzer\", \"search_analyzer\": \"ik_search_analyzer\"},");
            sb.append("\"field\": {\"type\": \"keyword\"},");
            sb.append("\"tags\": {\"type\": \"keyword\"},");
            sb.append("\"id\": {\"type\": \"keyword\"},");
            sb.append("\"institution\": {\"type\": \"text\", \"analyzer\": \"ik_analyzer\", \"search_analyzer\": \"ik_search_analyzer\"},");
            // Vector field
            sb.append("\"embedding\": {");
            sb.append("\"type\": \"dense_vector\",");
            sb.append("\"dims\": ").append(dimension).append(",");
            sb.append("\"index\": true,");
            sb.append("\"similarity\": \"cosine\"");
            sb.append("}");
            sb.append("}}");
            sb.append("}");
            body = sb.toString();
        } else {
            // Create basic index without vector, with IK analyzer
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"settings\": {");
            sb.append("\"number_of_shards\": 1,");
            sb.append("\"number_of_replicas\": 0,");
            // IK analyzer configuration
            sb.append("\"analysis\": {");
            sb.append("\"analyzer\": {");
            sb.append("\"ik_analyzer\": {");
            sb.append("\"type\": \"custom\",");
            sb.append("\"tokenizer\": \"ik_max_word\",");
            sb.append("\"filter\": [\"lowercase\"]");
            sb.append("},");
            sb.append("\"ik_search_analyzer\": {");
            sb.append("\"type\": \"custom\",");
            sb.append("\"tokenizer\": \"ik_smart\",");
            sb.append("\"filter\": [\"lowercase\"]");
            sb.append("}");
            sb.append("}");
            sb.append("},");
            sb.append("\"refresh_interval\": \"1s\"");
            sb.append("},");
            sb.append("\"mappings\": {");
            sb.append("\"properties\": {");
            sb.append("\"title\": {\"type\": \"text\", \"analyzer\": \"ik_analyzer\", \"search_analyzer\": \"ik_search_analyzer\"},");
            sb.append("\"description\": {\"type\": \"text\", \"analyzer\": \"ik_analyzer\", \"search_analyzer\": \"ik_search_analyzer\"},");
            sb.append("\"field\": {\"type\": \"keyword\"},");
            sb.append("\"tags\": {\"type\": \"keyword\"},");
            sb.append("\"name\": {\"type\": \"text\", \"analyzer\": \"ik_analyzer\", \"search_analyzer\": \"ik_search_analyzer\"},");
            sb.append("\"content\": {\"type\": \"text\", \"analyzer\": \"ik_analyzer\", \"search_analyzer\": \"ik_search_analyzer\"},");
            sb.append("\"provider\": {\"type\": \"text\", \"analyzer\": \"ik_analyzer\", \"search_analyzer\": \"ik_search_analyzer\"},");
            sb.append("\"department\": {\"type\": \"text\", \"analyzer\": \"ik_analyzer\", \"search_analyzer\": \"ik_search_analyzer\"},");
            sb.append("\"industry\": {\"type\": \"keyword\"},");
            sb.append("\"institution\": {\"type\": \"text\", \"analyzer\": \"ik_analyzer\", \"search_analyzer\": \"ik_search_analyzer\"},");
            sb.append("\"domain\": {\"type\": \"text\", \"analyzer\": \"ik_analyzer\", \"search_analyzer\": \"ik_search_analyzer\"}");
            sb.append("}}");
            sb.append("}");
            body = sb.toString();
        }

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        try {
            restTemplate.put(url, entity);
            System.out.println("Successfully created index " + indexName + " with IK analyzer (dim=" + dimension + ")");
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to create index " + indexName + ": " + e.getMessage());
            // Fallback: try without IK analyzer (using standard)
            System.out.println("Retrying with standard analyzer as fallback...");
            createIndexFallback(indexName, dimension);
        }
    }
    
    // Fallback method if IK plugin is not installed
    private void createIndexFallback(String indexName, int dimension) {
        String url = esUrl + "/" + indexName;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String body;
        if (dimension > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"settings\": {\"number_of_shards\": 1, \"number_of_replicas\": 0},");
            sb.append("\"mappings\": {");
            sb.append("\"properties\": {");
            sb.append("\"title\": {\"type\": \"text\", \"analyzer\": \"standard\"},");
            sb.append("\"description\": {\"type\": \"text\", \"analyzer\": \"standard\"},");
            sb.append("\"field\": {\"type\": \"keyword\"},");
            sb.append("\"tags\": {\"type\": \"keyword\"},");
            sb.append("\"id\": {\"type\": \"keyword\"},");
            sb.append("\"institution\": {\"type\": \"text\", \"analyzer\": \"standard\"},");
            sb.append("\"embedding\": {");
            sb.append("\"type\": \"dense_vector\",");
            sb.append("\"dims\": ").append(dimension).append(",");
            sb.append("\"index\": true,");
            sb.append("\"similarity\": \"cosine\"");
            sb.append("}");
            sb.append("}}");
            sb.append("}");
            body = sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"settings\": {\"number_of_shards\": 1, \"number_of_replicas\": 0},");
            sb.append("\"mappings\": {");
            sb.append("\"properties\": {");
            sb.append("\"title\": {\"type\": \"text\", \"analyzer\": \"standard\"},");
            sb.append("\"description\": {\"type\": \"text\", \"analyzer\": \"standard\"},");
            sb.append("\"field\": {\"type\": \"keyword\"},");
            sb.append("\"tags\": {\"type\": \"keyword\"},");
            sb.append("\"name\": {\"type\": \"text\", \"analyzer\": \"standard\"},");
            sb.append("\"content\": {\"type\": \"text\", \"analyzer\": \"standard\"},");
            sb.append("\"provider\": {\"type\": \"text\"},");
            sb.append("\"department\": {\"type\": \"text\"},");
            sb.append("\"industry\": {\"type\": \"keyword\"},");
            sb.append("\"institution\": {\"type\": \"text\"},");
            sb.append("\"domain\": {\"type\": \"text\", \"analyzer\": \"standard\"}");
            sb.append("}}");
            sb.append("}");
            body = sb.toString();
        }
        
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        try {
            restTemplate.put(url, entity);
            System.out.println("Successfully created index " + indexName + " with standard analyzer (IK not installed)");
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to create index even with fallback: " + e.getMessage());
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

    public List<String> searchVector(String indexName, List<Double> queryVector, int limit) {
        String url = esUrl + "/" + indexName + "/_search";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> bodyMap = new java.util.HashMap<>();
        
        // Script Score Query for Cosine Similarity
        Map<String, Object> script = new java.util.HashMap<>();
        script.put("source", "cosineSimilarity(params.query_vector, 'embedding') + 1.0");
        script.put("params", Collections.singletonMap("query_vector", queryVector));
        
        Map<String, Object> scriptScore = new java.util.HashMap<>();
        scriptScore.put("query", Collections.singletonMap("match_all", new java.util.HashMap<>()));
        scriptScore.put("script", script);
        
        Map<String, Object> query = new java.util.HashMap<>();
        query.put("script_score", scriptScore);
        
        bodyMap.put("query", query);
        bodyMap.put("size", limit);

        HttpEntity<Object> entity = new HttpEntity<>(bodyMap, headers);
        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, entity, JsonNode.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode hits = response.getBody().path("hits").path("hits");
                List<String> results = new ArrayList<>();
                if (hits.isArray()) {
                    for (JsonNode hit : hits) {
                        JsonNode source = hit.path("_source");
                        // Include the score for RRF fusion
                        double score = hit.path("_score").asDouble(0.0);
                        String resultJson = source.toString();
                        // Add score as metadata (prefix with score for parsing)
                        results.add("{\"_score\":" + score + ",\"_source\":" + resultJson + "}");
                    }
                }
                return results;
            }
        } catch (Exception e) {
            // Fallback: If vector search fails (e.g. index doesn't exist or no vector support), return empty
             System.err.println("Vector search failed: " + e.getMessage());
        }
        return Collections.emptyList();
    }
    
    /**
     * Hybrid search combining vector search and keyword search using RRF fusion.
     * RRF (Reciprocal Rank Fusion) formula: score = Σ 1/(k + rank)
     * where k is a constant (typically 60) and rank is the position in the result list.
     */
    public List<String> searchHybrid(String indexName, List<Double> queryVector, String queryText, int limit) {
        // Get results from both searches
        List<String> vectorResults = searchVectorWithScore(indexName, queryVector, limit);
        List<String> keywordResults = searchESWithScore(indexName, queryText, limit);
        
        // Apply RRF fusion
        return rrfFusion(vectorResults, keywordResults, limit);
    }
    
    /**
     * Vector search that returns results with scores for RRF fusion.
     */
    private List<String> searchVectorWithScore(String indexName, List<Double> queryVector, int limit) {
        String url = esUrl + "/" + indexName + "/_search";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> bodyMap = new java.util.HashMap<>();
        
        Map<String, Object> script = new java.util.HashMap<>();
        script.put("source", "cosineSimilarity(params.query_vector, 'embedding') + 1.0");
        script.put("params", Collections.singletonMap("query_vector", queryVector));
        
        Map<String, Object> scriptScore = new java.util.HashMap<>();
        scriptScore.put("query", Collections.singletonMap("match_all", new java.util.HashMap<>()));
        scriptScore.put("script", script);
        
        Map<String, Object> query = new java.util.HashMap<>();
        query.put("script_score", scriptScore);
        
        bodyMap.put("query", query);
        bodyMap.put("size", limit);

        HttpEntity<Object> entity = new HttpEntity<>(bodyMap, headers);
        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, entity, JsonNode.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode hits = response.getBody().path("hits").path("hits");
                List<String> results = new ArrayList<>();
                if (hits.isArray()) {
                    int rank = 1;
                    for (JsonNode hit : hits) {
                        JsonNode source = hit.path("_source");
                        String id = extractDocId(source);
                        double score = hit.path("_score").asDouble(1.0);
                        results.add(id + "::" + score + "::" + source.toString() + "::vector::" + rank);
                        rank++;
                    }
                }
                return results;
            }
        } catch (Exception e) {
            System.err.println("Vector search failed: " + e.getMessage());
        }
        return Collections.emptyList();
    }
    
    /**
     * Keyword search that returns results with scores for RRF fusion.
     */
    private List<String> searchESWithScore(String indexName, String queryText, int limit) {
        String url = esUrl + "/" + indexName + "/_search";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> bodyMap = new java.util.HashMap<>();
        
        if (queryText == null || queryText.trim().isEmpty()) {
            Map<String, Object> matchAll = new java.util.HashMap<>();
            bodyMap.put("query", Collections.singletonMap("match_all", matchAll));
        } else {
            Map<String, Object> multiMatch = new java.util.HashMap<>();
            multiMatch.put("query", queryText);
            multiMatch.put("fields", new String[]{
                "title^2", "name^2", "description", "field", "tags", 
                "provider", "domain", "institution"
            });
            multiMatch.put("type", "best_fields");
            multiMatch.put("fuzziness", "AUTO");
            
            Map<String, Object> query = new java.util.HashMap<>();
            query.put("multi_match", multiMatch);
            bodyMap.put("query", query);
        }

        bodyMap.put("size", limit);

        HttpEntity<Object> entity = new HttpEntity<>(bodyMap, headers);
        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, entity, JsonNode.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode hits = response.getBody().path("hits").path("hits");
                List<String> results = new ArrayList<>();
                if (hits.isArray()) {
                    int rank = 1;
                    for (JsonNode hit : hits) {
                        JsonNode source = hit.path("_source");
                        String id = extractDocId(source);
                        double score = hit.path("_score").asDouble(1.0);
                        results.add(id + "::" + score + "::" + source.toString() + "::keyword::" + rank);
                        rank++;
                    }
                }
                return results;
            }
        } catch (Exception e) {
            System.err.println("Keyword search failed: " + e.getMessage());
        }
        return Collections.emptyList();
    }
    
    /**
     * Extract document ID from source JSON.
     */
    private String extractDocId(JsonNode source) {
        if (source.has("id")) {
            return source.get("id").asText();
        }
        // If no id field, use hash of the source as identifier
        return String.valueOf(source.toString().hashCode());
    }
    
    /**
     * RRF (Reciprocal Rank Fusion) algorithm implementation.
     * Formula: RRF(score) = Σ 1/(k + rank)
     * @param vectorResults Results from vector search (format: "id::es_score::source::type::rank")
     * @param keywordResults Results from keyword search (format: "id::es_score::source::type::rank")
     * @param limit Maximum number of results to return
     * @return Fused and ranked results as JSON strings
     */
    private List<String> rrfFusion(List<String> vectorResults, List<String> keywordResults, int limit) {
        // RRF constant (typical value)
        int k = 60;
        
        // Map to store fused scores by document ID
        Map<String, Double> fusedScores = new java.util.HashMap<>();
        Map<String, String> docSources = new java.util.HashMap<>();
        Map<String, Double> esScores = new java.util.HashMap<>();
        Map<String, Boolean> hasVector = new java.util.HashMap<>();
        Map<String, Boolean> hasKeyword = new java.util.HashMap<>();
        
        // Process vector results
        for (String result : vectorResults) {
            String[] parts = result.split("::");
            if (parts.length >= 5) {
                String id = parts[0];
                double esScore = Double.parseDouble(parts[1]);
                String source = parts[2];
                int rank = Integer.parseInt(parts[4]);
                
                double rrfScore = 1.0 / (k + rank);
                fusedScores.merge(id, rrfScore, Double::sum);
                docSources.put(id, source);
                esScores.put(id, esScore);
                hasVector.put(id, true);
            }
        }
        
        // Process keyword results
        for (String result : keywordResults) {
            String[] parts = result.split("::");
            if (parts.length >= 5) {
                String id = parts[0];
                double esScore = Double.parseDouble(parts[1]);
                String source = parts[2];
                int rank = Integer.parseInt(parts[4]);
                
                double rrfScore = 1.0 / (k + rank);
                fusedScores.merge(id, rrfScore, Double::sum);
                docSources.put(id, source);
                // Use max of both scores
                if (!esScores.containsKey(id) || esScore > esScores.get(id)) {
                    esScores.put(id, esScore);
                }
                hasKeyword.put(id, true);
            }
        }
        
        // Sort by fused score
        List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(fusedScores.entrySet());
        sortedEntries.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));
        
        // Build final results
        List<String> fusedResults = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, Double> entry : sortedEntries) {
            if (count >= limit) break;
            
            String id = entry.getKey();
            String source = docSources.get(id);
            double fusedScore = entry.getValue();
            boolean isVector = hasVector.getOrDefault(id, false);
            boolean isKeyword = hasKeyword.getOrDefault(id, false);
            
            // Create result JSON with metadata
            String resultJson = String.format(
                "{\"_source\":%s,\"_score\":%.4f,\"_fused_score\":%.4f,\"_source_type\":\"%s\",\"_has_vector\":%s,\"_has_keyword\":%s}",
                source, esScores.getOrDefault(id, 0.0), fusedScore,
                isVector && isKeyword ? "hybrid" : (isVector ? "vector" : "keyword"),
                isVector, isKeyword
            );
            fusedResults.add(resultJson);
            count++;
        }
        
        return fusedResults;
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
            // Fields are based on actual data structures from RAGDataSeeder:
            // - achievements: title, description, field, tags, institution
            // - public_platforms: name, description, provider, domain
            Map<String, Object> multiMatch = new java.util.HashMap<>();
            multiMatch.put("query", queryText);
            multiMatch.put("fields", new String[]{
                "title^2", "name^2", "description", "field", "tags", 
                "provider", "domain", "institution"
            });
            // ^2 boosts title/name relevance

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
        
        // IMPORTANT: All mock data MUST have "id" field so that:
        // 1. Frontend list page can navigate to detail (item.id is truthy)
        // 2. Backend getById() can find the item by id
        
        if ("policies".equals(indexName)) {
            mocks.add("{\"id\":1,\"title\":\"关于促进生物医药产业高质量发展的若干措施\",\"department\":\"市发改委\",\"publishDate\":\"2024-01-15\",\"type\":\"产业扶持\",\"field\":\"生物医药\",\"content\":\"为贯彻落实国家关于生物医药产业高质量发展的战略部署，现制定以下措施：\\n一、对获批创新药给予最高500万元奖励。\\n二、支持企业建设高水平研发中心，给予最高300万元资助。\\n三、对引进的顶尖人才团队给予最高1000万元项目资助。\\n四、鼓励企业开展国际多中心临床试验，给予最高200万元补贴。\\n本措施自发布之日起实施，有效期三年。\"}");
            mocks.add("{\"id\":2,\"title\":\"中小企业数字化转型补贴方案\",\"department\":\"市工信局\",\"publishDate\":\"2024-02-10\",\"type\":\"资金补贴\",\"field\":\"数字经济\",\"content\":\"为加快推动中小企业数字化转型，特制定本补贴方案：\\n一、支持企业购买云服务，补贴比例最高50%。\\n二、支持企业部署工业软件，单项补贴最高50万元。\\n三、对建成数字化转型标杆的企业给予一次性奖励100万元。\\n四、支持数字化人才培训，每人最高补贴5000元。\\n申请条件：注册地在本地，年营收500万元以上。\"}");
            mocks.add("{\"id\":3,\"title\":\"科技创新券实施管理办法\",\"department\":\"市科技局\",\"publishDate\":\"2024-03-01\",\"type\":\"服务券\",\"field\":\"全行业\",\"content\":\"为进一步激发中小微企业科技创新活力，制定本办法：\\n一、创新券用于企业购买检验检测、技术研发、专利分析等服务。\\n二、每家企业每年最高申领额度20万元。\\n三、创新券可抵扣服务费用的50%。\\n四、优先支持高新技术企业和科技型中小企业。\\n本办法自2024年4月1日起施行，由市科技局负责解释。\"}");
            mocks.add("{\"id\":4,\"title\":\"高新技术企业培育扶持政策\",\"department\":\"市科技局\",\"publishDate\":\"2024-04-20\",\"type\":\"资质认定\",\"field\":\"全行业\",\"content\":\"为加快培育高新技术企业，推动产业转型升级，制定以下扶持措施：\\n一、对认定为国家高新技术企业的，给予一次性奖励50万元。\\n二、对重新认定的高新技术企业，给予奖励20万元。\\n三、高新技术企业研发费用加计扣除比例提高至100%。\\n四、给予高新技术企业人才引进住房补贴。\\n五、优先支持高新技术企业申报国家和省级科技项目。\"}");
            mocks.add("{\"id\":5,\"title\":\"产学研合作促进办法\",\"department\":\"市教育局\",\"publishDate\":\"2024-05-15\",\"type\":\"合作机制\",\"field\":\"全行业\",\"content\":\"为促进产学研深度融合，推动科技成果就地转化，制定本办法：\\n一、支持高校与企业共建联合实验室，最高资助200万元。\\n二、对技术转让合同金额超过100万元的，给予双方各5%的奖励。\\n三、支持高校科研人员到企业兼职开展技术攻关。\\n四、建立产学研合作项目库，优先推荐申报各类科技计划。\"}");
        } else if ("experts".equals(indexName)) {
            mocks.add("{\"id\":101,\"name\":\"张伟\",\"title\":\"教授\",\"institution\":\"理工大学\",\"field\":\"人工智能\",\"description\":\"长期从事人工智能和计算机视觉领域研究，主持国家自然科学基金项目3项，发表顶级会议论文20余篇，拥有多项发明专利。在图像识别和目标检测方面有深厚造诣。\",\"phone\":\"13800138001\",\"email\":\"zhangwei@university.edu.cn\",\"status\":\"VERIFIED\"}");
            mocks.add("{\"id\":102,\"name\":\"李娜\",\"title\":\"研究员\",\"institution\":\"科学院\",\"field\":\"生物医药\",\"description\":\"长期从事生物医药和基因工程领域研究，研发的新型抗肿瘤药物已进入临床II期试验。在基因编辑技术和靶向药物研发方面有丰富经验。\",\"phone\":\"13800138002\",\"email\":\"lina@academy.cn\",\"status\":\"VERIFIED\"}");
            mocks.add("{\"id\":103,\"name\":\"王强\",\"title\":\"高级工程师\",\"institution\":\"某大型车企\",\"field\":\"新能源\",\"description\":\"长期从事新能源汽车和电池管理领域研究，主导开发的BMS系统显著提升了电池续航里程和安全性。在动力电池热管理方面有十多年工程实践经验。\",\"phone\":\"13800138003\",\"email\":\"wangqiang@auto.com\",\"status\":\"VERIFIED\"}");
            mocks.add("{\"id\":104,\"name\":\"陈敏\",\"title\":\"教授\",\"institution\":\"科技大学\",\"field\":\"智能制造\",\"description\":\"长期从事智能制造和工业机器人领域研究，主持多项国家重点研发计划项目。在机器人运动控制和智能装配方面取得多项突破性成果。\",\"phone\":\"13800138004\",\"email\":\"chenmin@tech.edu.cn\",\"status\":\"VERIFIED\"}");
            mocks.add("{\"id\":105,\"name\":\"赵丽\",\"title\":\"教授\",\"institution\":\"材料科学与工程学院\",\"field\":\"新材料\",\"description\":\"长期从事新材料和复合材料领域研究，在碳纤维增强复合材料和纳米功能材料方面有深厚造诣。发表SCI论文50余篇，拥有发明专利15项。\",\"phone\":\"13800138005\",\"email\":\"zhaoli@materials.edu.cn\",\"status\":\"VERIFIED\"}");
        } else if ("funds".equals(indexName)) {
            mocks.add("{\"id\":201,\"name\":\"科技成果转化引导基金\",\"amount\":\"100-500万\",\"provider\":\"市科创投\",\"field\":\"硬科技\",\"description\":\"专注于早期科技成果转化的天使投资基金，重点支持拥有核心技术的初创团队。投资范围涵盖生物医药、人工智能、新材料等高技术领域。\",\"requirements\":\"拥有自主知识产权，团队核心成员有相关行业背景\"}");
            mocks.add("{\"id\":202,\"name\":\"中小微企业科创贷\",\"amount\":\"50-200万\",\"provider\":\"建设银行\",\"field\":\"高新技术企业\",\"description\":\"面向高新技术企业的低息信用贷款产品，无需抵押，审批快速。利率优惠，支持随借随还。\",\"requirements\":\"高新技术企业资质，经营满两年以上\"}");
            mocks.add("{\"id\":203,\"name\":\"产业创新股权投资资金\",\"amount\":\"500-2000万\",\"provider\":\"市产业基金\",\"field\":\"智能制造\",\"description\":\"对符合本地产业发展方向的高成长型科技企业进行股权投资。优先支持智能制造、新能源、新一代信息技术等领域。\",\"requirements\":\"年营收超过1000万元，有明确的商业模式\"}");
            mocks.add("{\"id\":204,\"name\":\"研发费用补贴资金\",\"amount\":\"10-100万\",\"provider\":\"市科技局\",\"field\":\"全行业\",\"description\":\"对企业实际发生的研发费用按比例给予补贴，降低企业创新成本。研发费用包括人员人工、直接投入、折旧费用等。\",\"requirements\":\"有持续研发投入的科技型企业\"}");
        } else if ("equipments".equals(indexName)) {
            mocks.add("{\"id\":301,\"name\":\"冷冻电镜 Titan Krios\",\"category\":\"分析仪器\",\"availability\":\"Available\",\"facilityName\":\"结构生物学中心\",\"specs\":\"300kV加速电压，配备Gatan K3相机\",\"owner\":\"生命科学学院\"}");
            mocks.add("{\"id\":302,\"name\":\"超导核磁共振波谱仪 600MHz\",\"category\":\"分析仪器\",\"availability\":\"Maintenance\",\"facilityName\":\"分析测试中心\",\"specs\":\"600MHz，配备超低温探头\",\"owner\":\"化学化工学院\"}");
            mocks.add("{\"id\":303,\"name\":\"场发射扫描电子显微镜\",\"category\":\"显微分析\",\"availability\":\"Available\",\"facilityName\":\"材料分析中心\",\"specs\":\"分辨率0.8nm，配备EDS能谱仪\",\"owner\":\"材料学院\"}");
        } else if ("patents".equals(indexName)) {
            mocks.add("{\"id\":401,\"title\":\"一种基于深度学习的图像超分辨率重建方法\",\"status\":\"已授权\",\"publicationDate\":\"2023-11-20\",\"patentNumber\":\"CN112233445B\",\"abstractText\":\"本发明公开了一种基于深度学习的图像超分辨率重建方法，属于计算机视觉领域。通过引入残差注意力机制和感知损失函数，显著提升了低分辨率图像的重建质量和细节保真度。\",\"assignee\":\"科技大学\"}");
            mocks.add("{\"id\":402,\"title\":\"一种高强度耐腐蚀铝合金材料及其制备工艺\",\"status\":\"公开\",\"publicationDate\":\"2024-01-05\",\"patentNumber\":\"CN115566778A\",\"abstractText\":\"本发明涉及金属材料领域，公开了一种海洋工程用高强度耐腐蚀铝合金材料。通过优化合金成分和热处理工艺，使材料兼具高强度、优异的抗海水腐蚀性能和良好的焊接性能。\",\"assignee\":\"新材料研究院\"}");
            mocks.add("{\"id\":403,\"title\":\"一种基于区块链的科技成果存证系统\",\"status\":\"已授权\",\"publicationDate\":\"2024-03-10\",\"patentNumber\":\"CN113445566B\",\"abstractText\":\"本发明涉及区块链技术领域，公开了一种基于区块链的科技成果存证与溯源系统。利用智能合约实现成果的自动确权、可信存证和全生命周期溯源。\",\"assignee\":\"区块链技术研究所\"}");
        } else if ("enterprises".equals(indexName)) {
            mocks.add("{\"id\":\"1001\",\"name\":\"智云科技股份有限公司\",\"industry\":\"人工智能\",\"location\":\"高新区\",\"scale\":\"500-1000人\",\"description\":\"专注于自然语言处理和知识图谱技术的研发与应用，服务于金融、医疗等领域。\"}");
            mocks.add("{\"id\":\"1002\",\"name\":\"绿能动力科技有限公司\",\"industry\":\"新能源\",\"location\":\"经开区\",\"scale\":\"100-499人\",\"description\":\"致力于高性能锂离子电池及储能系统的研发、生产和销售。\"}");
            mocks.add("{\"id\":\"1003\",\"name\":\"博创医疗科技有限公司\",\"industry\":\"生物医药\",\"location\":\"高新区\",\"scale\":\"200-500人\",\"description\":\"专注于高端医疗器械和体外诊断试剂的研发与产业化。\"}");
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
            Map<String, String> indexData = memoryStore.get(indexName);
            if (indexData != null && indexData.containsKey(id)) {
                return indexData.get(id);
            }
            
            System.err.println("GetById failed for " + id + " in " + indexName + ": " + e.getMessage());
            List<String> mocks = getMockData(indexName, null);
            if (!mocks.isEmpty()) {
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

    public void deleteDocument(String indexName, String id) {
        memoryStore.computeIfAbsent(indexName, k -> new ConcurrentHashMap<>()).remove(id);
        
        String url = esUrl + "/" + indexName + "/_doc/" + id;
        try {
            restTemplate.delete(url);
        } catch (Exception e) {
            System.err.println("Failed to delete document " + id + " from " + indexName + ": " + e.getMessage());
        }
    }

    public List<String> listDocuments(String indexName, int page, int size) {
        String url = esUrl + "/" + indexName + "/_search";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> bodyMap = new java.util.HashMap<>();
        bodyMap.put("query", Collections.singletonMap("match_all", new java.util.HashMap<>()));
        bodyMap.put("from", (page - 1) * size);
        bodyMap.put("size", size);

        HttpEntity<Object> entity = new HttpEntity<>(bodyMap, headers);
        try {
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
            System.err.println("List documents failed: " + e.getMessage());
        }
        
        Map<String, String> indexData = memoryStore.get(indexName);
        if (indexData != null) {
            return new ArrayList<>(indexData.values()).subList(0, Math.min(size, indexData.size()));
        }
        return Collections.emptyList();
    }

    public long countDocuments(String indexName) {
        String url = esUrl + "/" + indexName + "/_count";
        try {
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody().path("count").asLong();
            }
        } catch (Exception e) {
            System.err.println("Count documents failed: " + e.getMessage());
        }
        
        Map<String, String> indexData = memoryStore.get(indexName);
        return indexData != null ? indexData.size() : 0;
    }

    public void clearIndex(String indexName) {
        memoryStore.remove(indexName);
        
        String url = esUrl + "/" + indexName + "/_delete_by_query";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String body = "{\"query\": {\"match_all\": {}}}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        try {
            restTemplate.postForEntity(url, entity, JsonNode.class);
        } catch (Exception e) {
            System.err.println("Clear index failed: " + e.getMessage());
        }
    }
}
