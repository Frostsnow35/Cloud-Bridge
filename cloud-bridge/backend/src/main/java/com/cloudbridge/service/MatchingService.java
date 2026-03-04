package com.cloudbridge.service;

import com.cloudbridge.entity.Achievement;
import com.cloudbridge.entity.graph.Technology;
import com.cloudbridge.repository.AchievementRepository;
import com.cloudbridge.repository.graph.TechnologyRepository;
import com.cloudbridge.service.ai.AIService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;

import com.cloudbridge.util.DomainHierarchyUtil;

@Service
public class MatchingService {

    @Autowired
    private TechnologyRepository technologyRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private AIService aiService;

    @Autowired
    private ObjectMapper objectMapper;

    // Scoring constants
    private static final int SCORE_FIELD_MATCH = 100;
    private static final int SCORE_KEYWORD_MATCH = 50;
    private static final int SCORE_GRAPH_MATCH = 20;
    private static final int SCORE_RELATED_MATCH = 10;
    private static final int SCORE_TEXT_TITLE_MATCH = 10;
    private static final int SCORE_TEXT_DESC_MATCH = 5;
    
    // Price scoring
    private static final int SCORE_PRICE_EXCELLENT = 20; // Within 20%
    private static final int SCORE_PRICE_GOOD = 10;      // Within 50%
    private static final int SCORE_PRICE_PENALTY = -20;  // > 150% budget

    public static class ScoredAchievement {
        private final Achievement achievement;
        private int score;

        public ScoredAchievement(Achievement achievement, int score) {
            this.achievement = achievement;
            this.score = score;
        }

        public void addScore(int points) {
            this.score += points;
            // Cap at 100 is removed during calculation to allow high differentiation, capped at display if needed
            // But let's keep it reasonable
            if (this.score > 100) this.score = 100;
            if (this.score < 0) this.score = 0;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public Achievement getAchievement() {
            return achievement;
        }

        public int getScore() {
            return score;
        }
    }

    // Overload for backward compatibility
    public Map<String, Object> match(String demandDescription) {
        return match(demandDescription, null, null);
    }

    public Map<String, Object> match(String demandDescription, String filterField, Double budget) {
        Map<String, Object> result = new HashMap<>();
        Map<Long, ScoredAchievement> scoredMatches = new HashMap<>();
        
        if (demandDescription == null || demandDescription.trim().isEmpty()) {
            result.put("matches", new ArrayList<>());
            result.put("recommendations", new ArrayList<>());
            result.put("relatedKeywords", new ArrayList<>());
            return result;
        }

        // 1. Extract Profile using AI Service
        com.cloudbridge.dto.MatchingProfile profile = aiService.extractMatchingProfile(demandDescription);
        String keyword = profile.getKeyword();
        String aiField = profile.getField();
        
        // Use provided filterField, fallback to AI detected field from profile
        String effectiveField = (filterField != null && !filterField.isEmpty()) ? filterField : aiField;
        
        System.out.println("Matching Params -> Keyword: " + keyword + ", Field: " + effectiveField + ", SubField: " + profile.getSubField());

        // 1.1 Extract Knowledge Graph (Dynamic)
        String graphJson = aiService.extractGraphData(demandDescription);
        try {
            JsonNode graphNode = objectMapper.readTree(graphJson);
            // Use profile.getField() as filter for graph augmentation too
            augmentGraphWithAchievements(graphNode, scoredMatches, effectiveField);
            result.put("aiGraph", graphNode);
        } catch (Exception e) {
            System.err.println("Failed to parse graph JSON: " + e.getMessage());
        }

        if (keyword == null || keyword.isEmpty()) {
            keyword = fallbackExtractKeyword(demandDescription);
        }
        
        // 2. Find related technologies
        List<Technology> relatedTechs = new ArrayList<>();
        try {
            if (technologyRepository != null) {
                 relatedTechs = technologyRepository.findRelatedTechnologies(keyword);
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to query Knowledge Graph: " + e.getMessage());
        }

        List<String> relatedKeywords = relatedTechs.stream()
                .map(Technology::getName)
                .collect(Collectors.toList());
        if (keyword != null && keyword.length() > 5) {
            relatedKeywords.add(keyword.substring(0, 4));
        }
        relatedKeywords.add(keyword);

        // 3. Initial Retrieval (Broad Search)
        
        // 3.1 Field Match (Strict Filter Base)
        if (effectiveField != null && !effectiveField.isEmpty()) {
            List<Achievement> fieldMatches = achievementRepository.findByFieldContainingAndStatus(effectiveField, Achievement.Status.PUBLISHED);
            for (Achievement a : fieldMatches) {
                scoredMatches.computeIfAbsent(a.getId(), k -> new ScoredAchievement(a, 0)).addScore(SCORE_FIELD_MATCH);
            }
        }
        
        // 3.2 Main Keyword Match & HIERARCHY EXPANSION
        if (keyword != null && !keyword.isEmpty()) {
            // A. Search by keyword itself
            List<Achievement> keywordMatches = achievementRepository.findPublishedByKeyword(keyword);
            for (Achievement a : keywordMatches) {
                if (isFieldMismatch(a, effectiveField)) continue;
                scoredMatches.computeIfAbsent(a.getId(), k -> new ScoredAchievement(a, 0)).addScore(SCORE_KEYWORD_MATCH);
            }
            
            // B. Search by hierarchy children (Expansion)
            for (Map.Entry<String, List<String>> entry : DomainHierarchyUtil.DOMAIN_HIERARCHY.entrySet()) {
                String domain = entry.getKey();
                // If keyword matches a domain name (e.g. "生物医药")
                if (keyword.contains(domain) || domain.contains(keyword)) {
                    List<String> children = entry.getValue();
                    for (String child : children) {
                        List<Achievement> childMatches = achievementRepository.findPublishedByKeyword(child);
                        for (Achievement a : childMatches) {
                             if (isFieldMismatch(a, effectiveField)) continue;
                             // Score child matches slightly less but still significant
                             scoredMatches.computeIfAbsent(a.getId(), k -> new ScoredAchievement(a, 0)).addScore((int)(SCORE_KEYWORD_MATCH * 0.8));
                        }
                        // Also add to related keywords for frontend
                        if (!relatedKeywords.contains(child)) {
                            relatedKeywords.add(child);
                        }
                    }
                }
            }
        }

        // 3.3 Related Keyword Match
        for (Technology t : relatedTechs) {
            String relatedKey = t.getName();
            if (relatedKey != null && !relatedKey.trim().isEmpty() && !relatedKey.equals(keyword)) {
                List<Achievement> found = achievementRepository.findPublishedByKeyword(relatedKey);
                for (Achievement a : found) {
                    if (isFieldMismatch(a, effectiveField)) continue;
                    scoredMatches.computeIfAbsent(a.getId(), k -> new ScoredAchievement(a, 0)).addScore(SCORE_RELATED_MATCH);
                }
            }
        }
        
        // 3.4 Text Similarity & Price Logic (Base Scoring)
        for (ScoredAchievement sa : scoredMatches.values()) {
            Achievement a = sa.getAchievement();
            String title = a.getTitle();
            String desc = a.getDescription();
            
            // Double Check Field Mismatch (Penalize heavily if somehow slipped through)
            if (isFieldMismatch(a, effectiveField)) {
                sa.setScore(0); 
                continue;
            }
            
            if (keyword != null && !keyword.isEmpty()) {
                 if (title != null && title.contains(keyword)) sa.addScore(SCORE_TEXT_TITLE_MATCH);
                 if (desc != null && desc.contains(keyword)) sa.addScore(SCORE_TEXT_DESC_MATCH);
            }
            
            // Price Logic
            if (budget != null && a.getPrice() != null && a.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                double priceVal = a.getPrice().doubleValue();
                double budgetVal = budget.doubleValue();
                double diff = Math.abs(priceVal - budgetVal);
                double ratio = diff / budgetVal;
                
                if (ratio <= 0.2) sa.addScore(SCORE_PRICE_EXCELLENT);
                else if (ratio <= 0.5) sa.addScore(SCORE_PRICE_GOOD);
                else if (priceVal > budgetVal * 1.5) sa.addScore(SCORE_PRICE_PENALTY);
            }
        }

        // 4. AI Reranking (The "Strict Filter")
        // Select top candidates for AI evaluation
        List<ScoredAchievement> topCandidates = scoredMatches.values().stream()
                .filter(s -> s.getScore() > 0)
                .sorted((a, b) -> b.getScore() - a.getScore())
                .limit(50) // Limit to top 50 for AI processing
                .collect(Collectors.toList());

        List<Achievement> candidateEntities = topCandidates.stream()
                .map(ScoredAchievement::getAchievement)
                .collect(Collectors.toList());

        if (!candidateEntities.isEmpty()) {
            Map<Long, Double> aiScores = aiService.evaluateMatches(demandDescription, profile, candidateEntities);
            
            // Update scores based on AI evaluation
            for (ScoredAchievement sa : topCandidates) {
                Double aiScore = aiScores.get(sa.getAchievement().getId());
                if (aiScore != null) {
                    if (aiScore < 10) {
                        sa.setScore(0); // Eliminate
                    } else {
                        // Blend AI score (weight 2.0)
                        sa.addScore((int)(aiScore * 2.0)); 
                    }
                }
            }
        }

        // 5. Final Sort and Select
        List<ScoredAchievement> allScored = new ArrayList<>(scoredMatches.values());
        allScored = allScored.stream().filter(s -> s.getScore() > 0).collect(Collectors.toList());
        allScored.sort((a, b) -> b.getScore() - a.getScore());

        // Return top 10 relevant ones
        List<Map<String, Object>> matches = new ArrayList<>();
        for (int i=0; i<Math.min(allScored.size(), 10); i++) {
            ScoredAchievement sa = allScored.get(i);
            Map<String, Object> map = new HashMap<>();
            Achievement a = sa.getAchievement();
            map.put("id", a.getId());
            map.put("title", a.getTitle());
            map.put("description", a.getDescription());
            map.put("field", a.getField());
            map.put("maturity", a.getMaturity());
            map.put("price", a.getPrice());
            map.put("ownerId", a.getOwnerId());
            map.put("status", a.getStatus() != null ? a.getStatus().toString() : "PUBLISHED");
            map.put("createdAt", a.getCreatedAt() != null ? a.getCreatedAt().toString() : new Date().toString());
            map.put("score", sa.getScore());
            matches.add(map);
        }
        
        result.put("matches", matches);

        // 6. Recommendations (Guess You Like)
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        // Find achievements in the same field but NOT in the top strict matches
        // Base recommendation on Field + Popularity (Mock popularity by ID for now or random)
        if (effectiveField != null && !effectiveField.isEmpty()) {
            List<Achievement> recCandidates = achievementRepository.findByFieldContainingAndStatus(effectiveField, Achievement.Status.PUBLISHED);
            
            // Exclude already matched
            Set<Long> matchedIds = matches.stream().map(m -> (Long)m.get("id")).collect(Collectors.toSet());
            
            recCandidates = recCandidates.stream()
                .filter(a -> !matchedIds.contains(a.getId()))
                .limit(6) // Limit recommendations
                .collect(Collectors.toList());
                
            for (Achievement a : recCandidates) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", a.getId());
                map.put("title", a.getTitle());
                map.put("description", a.getDescription());
                map.put("field", a.getField());
                map.put("maturity", a.getMaturity());
                map.put("price", a.getPrice());
                map.put("status", a.getStatus() != null ? a.getStatus().toString() : "PUBLISHED");
                map.put("score", 50); // Base score for domain match
                recommendations.add(map);
            }
        }
        
        result.put("recommendations", recommendations);
        result.put("relatedKeywords", relatedKeywords);
        
        return result;
    }

    private boolean isFieldMismatch(Achievement a, String effectiveField) {
        if (effectiveField == null || effectiveField.isEmpty()) return false;
        if (a.getField() == null) return true;
        // Simple containment check (can be improved with semantic similarity)
        return !a.getField().contains(effectiveField) && !effectiveField.contains(a.getField());
    }

    private void addScore(Map<Long, ScoredAchievement> map, Achievement a, int points, String filterField) {
        // Hard Filter Check
        if (isFieldMismatch(a, filterField)) {
            return;
        }
        map.computeIfAbsent(a.getId(), k -> new ScoredAchievement(a, 0)).addScore(points);
    }

    private void augmentGraphWithAchievements(JsonNode graphNode, Map<Long, ScoredAchievement> scoredMatches, String filterField) {
        if (graphNode == null || !graphNode.has("nodes")) return;

        ArrayNode nodes = (ArrayNode) graphNode.get("nodes");
        JsonNode relationshipsNode = graphNode.get("relationships");
        ArrayNode relationships;

        if (relationshipsNode == null || relationshipsNode.isNull()) {
             if (graphNode instanceof ObjectNode) {
                relationships = ((ObjectNode) graphNode).putArray("relationships");
             } else {
                return;
             }
        } else {
            relationships = (ArrayNode) relationshipsNode;
        }

        Set<String> existingIds = new HashSet<>();
        // Pre-populate existing IDs to avoid duplicates
        for (JsonNode n : nodes) {
            if (n.has("id")) existingIds.add(n.get("id").asText());
        }

        // Strategy: Only connect Achievements to "Leaf" nodes (SubCategories or Specific Technologies)
        // Avoid connecting to "Root" or high-level categories if possible.
        
        // --- NEW HIERARCHY LOGIC START ---
        // If the graph contains a broad category node (e.g., "生物医药"), check if we have achievements 
        // that match its children (e.g., "细胞") but "细胞" isn't in the graph yet.
        // If so, inject "细胞" node and connect it.
        
        List<JsonNode> nodesToAdd = new ArrayList<>();
        List<ObjectNode> relsToAdd = new ArrayList<>();
        
        for (JsonNode node : nodes) {
            String label = node.has("label") ? node.get("label").asText() : "";
            String nodeId = node.has("id") ? node.get("id").asText() : "";
            
            // Check if this node is a known parent domain
            for (Map.Entry<String, List<String>> entry : DomainHierarchyUtil.DOMAIN_HIERARCHY.entrySet()) {
                String domain = entry.getKey();
                if (label.contains(domain) || domain.contains(label)) {
                    List<String> children = entry.getValue();
                    for (String child : children) {
                        // Check if we have achievements for this child
                        List<Achievement> childMatches = achievementRepository.findPublishedByKeyword(child);
                        if (childMatches.isEmpty()) continue;
                        
                        // Check if child node already exists in graph (simple check by label)
                        boolean childExists = false;
                        String childNodeId = null;
                        for (JsonNode n : nodes) {
                            if (n.has("label") && n.get("label").asText().contains(child)) {
                                childExists = true;
                                childNodeId = n.get("id").asText();
                                break;
                            }
                        }
                        
                        // If child node doesn't exist, create it
                        if (!childExists) {
                            childNodeId = "node_" + UUID.randomUUID().toString().substring(0, 8);
                            ObjectNode childNode = objectMapper.createObjectNode();
                            childNode.put("id", childNodeId);
                            childNode.put("label", child);
                            childNode.put("type", "SubCategory"); // Mark as subcategory
                            nodesToAdd.add(childNode);
                            
                            // Connect Parent -> Child
                            ObjectNode rel = objectMapper.createObjectNode();
                            rel.put("source", nodeId);
                            rel.put("target", childNodeId);
                            rel.put("type", "INCLUDES");
                            relsToAdd.add(rel);
                            
                            existingIds.add(childNodeId); // Track ID
                        }
                        
                        // Now connect matching achievements to this CHILD node (whether new or existing)
                        // We will let the main loop below handle connections if the node exists, 
                        // BUT since we just added it or it might be skipped by main loop logic, let's force connect here for new nodes.
                        // Actually, better to just add the node to 'nodes' list and let the main loop process it?
                        // No, main loop iterates over original 'nodes'. We need to process these new nodes too.
                        
                        // Let's connect achievements to this child node directly here
                        int count = 0;
                        for (Achievement a : childMatches) {
                             if (filterField != null && !filterField.isEmpty()) {
                                boolean match = (a.getField() != null && a.getField().contains(filterField)) || (a.getField() != null && filterField.contains(a.getField()));
                                if (!match) continue;
                             }
                             if (count >= 3) break;
                             
                             String achId = "ach_" + a.getId();
                             if (!existingIds.contains(achId)) {
                                 ObjectNode achNode = objectMapper.createObjectNode();
                                 achNode.put("id", achId);
                                 String displayTitle = a.getTitle().length() > 6 ? a.getTitle().substring(0, 6) + "..." : a.getTitle();
                                 achNode.put("label", displayTitle);
                                 achNode.put("fullTitle", a.getTitle());
                                 achNode.put("price", a.getPrice() != null ? a.getPrice().toString() : "面议");
                                 achNode.put("type", "Achievement");
                                 achNode.put("isLeaf", true);
                                 
                                 nodesToAdd.add(achNode);
                                 existingIds.add(achId);
                                 
                                 ObjectNode rel = objectMapper.createObjectNode();
                                 rel.put("source", childNodeId);
                                 rel.put("target", achId);
                                 rel.put("type", "MATCHES");
                                 relsToAdd.add(rel);
                                 
                                 count++;
                             }
                        }
                    }
                }
            }
        }
        
        // Add all new nodes and rels
        nodes.addAll(nodesToAdd);
        relationships.addAll(relsToAdd);
        // --- NEW HIERARCHY LOGIC END ---

        for (JsonNode node : nodes) {
            String label = node.has("label") ? node.get("label").asText() : "";
            String type = node.has("type") ? node.get("type").asText() : "";
            String nodeId = node.has("id") ? node.get("id").asText() : "";
            
            // HIERARCHICAL LOGIC: 
            // Only attach achievements to specific/leaf nodes (e.g., "Technology", "InferredTech", "SubCategory").
            // Skip broad "Category" nodes unless no specific nodes exist.
            boolean isLeafNode = type.equalsIgnoreCase("Technology") || type.equalsIgnoreCase("InferredTech") || type.equalsIgnoreCase("SubCategory");
            
            if (label.length() > 1 && isLeafNode) {
                // Find achievements matching this specific node label
                List<Achievement> matches = achievementRepository.findPublishedByKeyword(label);
                
                int count = 0;
                for (Achievement a : matches) {
                    // Check Filter
                    if (filterField != null && !filterField.isEmpty()) {
                        // Loose check: field contains filter OR filter contains field
                        boolean match = (a.getField() != null && a.getField().contains(filterField)) || (a.getField() != null && filterField.contains(a.getField()));
                        if (!match) continue;
                    }

                    if (count >= 3) break; // Max 3 achievements per node to reduce clutter
                    
                    String achId = "ach_" + a.getId();
                    
                    // Add score to the main matching logic
                    ScoredAchievement scored = scoredMatches.computeIfAbsent(a.getId(), k -> new ScoredAchievement(a, 0));
                    scored.addScore(SCORE_GRAPH_MATCH);

                    // GRAPH VISUALIZATION THRESHOLD: 
                    // Only visualize if it's a decent match to keep graph clean
                    boolean isStrongMatch = scored.getScore() >= 30; 
                    
                    if (isStrongMatch) {
                        if (!existingIds.contains(achId)) {
                            ObjectNode achNode = objectMapper.createObjectNode();
                            achNode.put("id", achId);
                            // Truncate title for visual clarity
                            String displayTitle = a.getTitle().length() > 6 ? a.getTitle().substring(0, 6) + "..." : a.getTitle();
                            achNode.put("label", displayTitle);
                            achNode.put("fullTitle", a.getTitle());
                            achNode.put("price", a.getPrice() != null ? a.getPrice().toString() : "面议");
                            achNode.put("type", "Achievement");
                            // Add specific styling property for frontend if needed
                            achNode.put("isLeaf", true); 
                            
                            nodes.add(achNode);
                            existingIds.add(achId);
                            
                            // Create edge: Node -> Achievement
                            ObjectNode rel = objectMapper.createObjectNode();
                            rel.put("source", nodeId);
                            rel.put("target", achId);
                            rel.put("type", "MATCHES");
                            relationships.add(rel);
                        }
                        
                        count++;
                    }
                }
            }
        }
    }

    private String fallbackExtractKeyword(String text) {
        if (text == null) return "";
        String[] parts = text.split("\\s+");
        if (parts.length > 0 && parts[0].length() > 0 && parts[0].length() < 10) {
            return parts[0];
        }
        return text.substring(0, Math.min(text.length(), 4));
    }
}
