package com.cloudbridge.config;

import com.cloudbridge.entity.Achievement;
import com.cloudbridge.repository.AchievementRepository;
import com.cloudbridge.service.rag.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RAGDataSeeder implements CommandLineRunner {

    @Autowired
    private SearchService searchService;

    @Autowired
    private AchievementRepository achievementRepository;

    @Override
    public void run(String... args) throws Exception {
        System.err.println("=== RAGDataSeeder STARTED ===");
        try {
            // New CSV seeders (Prioritized)
            // Note: seedAchievementsFromCSV is now REPLACED by seedAchievementsFromExpertCSV as requested
            seedAchievementsFromExpertCSV();
            seedPublicPlatformsFromCSV();
            
            // Legacy seeders
            seedPolicies();
            seedFunds();
            seedEquipments();
            seedExperts();
            seedPatents();
            seedEnterprises();
        } catch (Throwable e) {
            System.err.println("RAG Data Seeding Failed (Non-critical): " + e.getMessage());
            e.printStackTrace();
        }
        System.err.println("=== RAGDataSeeder FINISHED ===");
    }

    // Renamed and updated to use the expert CSV as the SOLE source of Achievements
    private void seedAchievementsFromExpertCSV() {
        System.err.println("Attempting to seed Achievements from Expert CSV...");
        Path path = Paths.get("e:\\数据要素大赛作品\\数据集\\广州市白云区科技创新发展专项资金项目评审专家名单.csv.csv");
        
        if (!Files.exists(path)) {
            System.err.println("CRITICAL: Achievement CSV NOT FOUND at: " + path.toAbsolutePath());
            return;
        }

        try {
            // Clear existing achievements to ensure clean state
            System.err.println("Clearing existing achievements...");
            achievementRepository.deleteAll();
            
            // Try GBK encoding first
            List<String> lines = null;
            try {
                lines = Files.readAllLines(path, java.nio.charset.Charset.forName("GBK"));
            } catch (Exception e) {
                System.err.println("GBK read failed for Expert CSV, trying UTF-8...");
                lines = Files.readAllLines(path, java.nio.charset.StandardCharsets.UTF_8);
            }
            
            if (lines == null || lines.isEmpty()) {
                System.err.println("Achievement CSV is empty.");
                return;
            }

            // Header: 创建时间,领域,更新时间
            // We will map "领域" to Achievement Title/Field
            List<String> dataLines = lines.stream().skip(1).collect(Collectors.toList());
            int count = 0;
            
            for (String line : dataLines) {
                String[] parts = line.split(",");
                // Expected at least 2 parts (Time, Field)
                if (parts.length < 2) continue;
                
                String createTime = getPart(parts, 0);
                String field = getPart(parts, 1);
                String updateTime = getPart(parts, 2);
                
                if (field.isEmpty()) continue;

                Achievement achievement = new Achievement();
                // Synthesize a title since the CSV only has Field
                achievement.setTitle(field + "相关研究成果");
                achievement.setDescription("本成果由" + field + "领域的资深专家团队研发，具有较高的技术创新性和应用价值。");
                achievement.setField(field);
                achievement.setMaturity("成熟应用"); // Default
                achievement.setPrice(new BigDecimal("0.00")); // Default "Negotiable"
                achievement.setOwnerId(1L); // Default admin owner
                achievement.setStatus(Achievement.Status.PUBLISHED);
                
                // Save to DB
                achievementRepository.save(achievement);
                count++;
            }
            System.err.println("SUCCESS: Seeded " + count + " Achievements from Expert CSV.");
            
        } catch (Exception e) {
            System.err.println("Failed to seed Achievements from Expert CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seedPublicPlatformsFromCSV() {
        System.err.println("Attempting to seed Public Platforms...");
        // Use the exact absolute path verified by LS
        Path path = Paths.get("e:\\数据要素大赛作品\\数据集\\广州市白云区公共数据开放计划.csv");
        
        if (!Files.exists(path)) {
            System.err.println("CRITICAL: CSV NOT FOUND at: " + path.toAbsolutePath());
            // List parent directory
            try {
                Path parent = path.getParent();
                if (Files.exists(parent)) {
                    System.err.println("Listing files in " + parent + ":");
                    Files.list(parent).forEach(f -> System.err.println(" - " + f.getFileName()));
                } else {
                    System.err.println("Parent directory does not exist: " + parent);
                }
            } catch (Exception e) { e.printStackTrace(); }
            return;
        }

        System.err.println("Found CSV at: " + path.toAbsolutePath());
        String indexName = "public_platforms";
        searchService.createIndex(indexName);

        try {
            // Try GBK encoding first
            List<String> lines = null;
            try {
                lines = Files.readAllLines(path, java.nio.charset.Charset.forName("GBK"));
                System.err.println("Read " + lines.size() + " lines with GBK encoding.");
            } catch (Exception e) {
                System.err.println("GBK read failed: " + e.getMessage());
                try {
                    lines = Files.readAllLines(path, java.nio.charset.StandardCharsets.UTF_8);
                    System.err.println("Read " + lines.size() + " lines with UTF-8 encoding.");
                } catch (Exception ex) {
                    System.err.println("UTF-8 read failed: " + ex.getMessage());
                }
            }
            
            if (lines == null || lines.isEmpty()) {
                System.err.println("CSV is empty or could not be read.");
                return;
            }

            // Print header for debugging
            System.err.println("CSV Header: " + lines.get(0));

            // Skip header
            List<String> dataLines = lines.stream().skip(1).collect(Collectors.toList());

            int count = 0;
            for (String line : dataLines) {
                // Debug first few lines
                if (count < 3) System.err.println("Processing line: " + line);

                // Headers: 公共数据开放主体名称,更新频率,开放属性,数据格式,数据集领域,数据集名称,数据项,数据摘要,序号
                String[] parts = line.split(",");
                if (parts.length < 5) { 
                    System.err.println("Skipping invalid line (parts=" + parts.length + "): " + line);
                    continue; 
                }

                Map<String, Object> doc = new HashMap<>();
                doc.put("provider", getPart(parts, 0));
                doc.put("updateFrequency", getPart(parts, 1));
                doc.put("openType", getPart(parts, 2));
                doc.put("format", getPart(parts, 3));
                doc.put("domain", getPart(parts, 4));
                doc.put("name", getPart(parts, 5));
                doc.put("dataItems", getPart(parts, 6));
                doc.put("description", getPart(parts, 7));
                
                String id = getPart(parts, 8);
                if (id.isEmpty()) id = UUID.randomUUID().toString();
                doc.put("id", id);

                searchService.indexDocument(indexName, id, doc);
                count++;
            }
            System.err.println("SUCCESS: Seeded " + count + " public platforms from CSV.");

        } catch (Exception e) {
            System.err.println("Failed to seed public platforms: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getPart(String[] parts, int index) {
        if (index < parts.length) {
            return parts[index].trim();
        }
        return "";
    }

    private void seedPolicies() {
        searchService.createIndex("policies");
        loadMockData("policies");
        
        // Default seed if file empty or not found (for basic testing)
        if (!hasData("policies")) {
            Map<String, Object> policy1 = new HashMap<>();
            policy1.put("id", "P001");
            policy1.put("title", "关于支持新材料产业发展的若干措施");
            policy1.put("department", "工信部");
            policy1.put("content", "为加快新材料产业发展，对首批次应用保险给予保费补贴，最高不超过500万元。支持高性能碳纤维、超导材料等关键技术突破。");
            policy1.put("policyType", "Subsidy");
            policy1.put("industry", new String[]{"New Materials"});
            
            searchService.indexDocument("policies", "P001", policy1);
    
            Map<String, Object> policy2 = new HashMap<>();
            policy2.put("id", "P002");
            policy2.put("title", "生物医药产业创新发展行动计划");
            policy2.put("department", "广东省科技厅");
            policy2.put("content", "支持创新药研发，对进入临床试验阶段的项目给予最高1000万元资助。鼓励企业建设高水平研发中心。");
            policy2.put("policyType", "Grant");
            policy2.put("industry", new String[]{"Bio-medicine"});
    
            searchService.indexDocument("policies", "P002", policy2);
        }
    }

    private void seedFunds() {
        searchService.createIndex("funds");
        loadMockData("funds");

        if (!hasData("funds")) {
            Map<String, Object> fund1 = new HashMap<>();
            fund1.put("id", "F001");
            fund1.put("name", "科创贷");
            fund1.put("provider", "建设银行");
            fund1.put("fundType", "Loan");
            fund1.put("amountRange", "100-500万");
            fund1.put("industryFocus", new String[]{"Tech", "New Materials"});
            fund1.put("interestRate", "3.5%");
    
            searchService.indexDocument("funds", "F001", fund1);
        }
    }

    private void seedEquipments() {
        searchService.createIndex("equipments");
        loadMockData("equipments");

        if (!hasData("equipments")) {
            Map<String, Object> equip1 = new HashMap<>();
            equip1.put("id", "E001");
            equip1.put("name", "冷冻电镜 (Cryo-EM)");
            equip1.put("facilityName", "江门双碳实验室");
            equip1.put("owner", "香港科技大学(广州)");
            equip1.put("category", "Analysis");
            equip1.put("specs", "300kV, K3相机");
            equip1.put("availability", "Available");
    
            searchService.indexDocument("equipments", "E001", equip1);
        }
    }
    
    private void seedExperts() {
        searchService.createIndex("experts");
        loadMockData("experts");
        
        if (!hasData("experts")) {
             Map<String, Object> expert1 = new HashMap<>();
             expert1.put("id", "EXP001");
             expert1.put("name", "张教授");
             expert1.put("title", "首席科学家");
             expert1.put("affiliation", "清华大学");
             expert1.put("field", new String[]{"人工智能", "深度学习"});
             expert1.put("achievements", "发表顶级会议论文50余篇，引用过万。");
             
             searchService.indexDocument("experts", "EXP001", expert1);
        }
    }

    private void seedPatents() {
        searchService.createIndex("patents");
        loadMockData("patents");
        
        if (!hasData("patents")) {
             Map<String, Object> patent1 = new HashMap<>();
             patent1.put("id", "PAT001");
             patent1.put("title", "一种基于深度学习的图像识别方法");
             patent1.put("patentNumber", "CN108XXXXXXA");
             patent1.put("assignee", "云桥科技");
             patent1.put("inventor", "李工");
             patent1.put("publicationDate", "2023-05-20");
             patent1.put("abstractText", "本发明公开了一种基于卷积神经网络的实时图像识别系统，具有极高的准确率和低延迟...");
             patent1.put("status", "Authorized");
             
             searchService.indexDocument("patents", "PAT001", patent1);
        }
    }

    private void seedEnterprises() {
        searchService.createIndex("enterprises");
        loadMockData("enterprises");
        
        if (!hasData("enterprises")) {
             Map<String, Object> ent1 = new HashMap<>();
             ent1.put("id", "ENT001");
             ent1.put("name", "北京创新微电子有限公司");
             ent1.put("industry", "Semiconductor");
             ent1.put("scale", "100-500人");
             ent1.put("location", "中关村科学城");
             ent1.put("description", "专注于高性能模拟集成电路设计，国家高新技术企业。");
             
             searchService.indexDocument("enterprises", "ENT001", ent1);
        }
    }

    private java.nio.file.Path getDataPath(String category) {
        java.nio.file.Path p1 = java.nio.file.Paths.get("data", category + ".jsonl");
        if (java.nio.file.Files.exists(p1)) return p1;
        
        java.nio.file.Path p2 = java.nio.file.Paths.get("backend", "data", category + ".jsonl");
        if (java.nio.file.Files.exists(p2)) return p2;
        
        return p1; // Default to first path for error message
    }

    private void loadMockData(String category) {
        try {
            java.nio.file.Path path = getDataPath(category);
            
            if (java.nio.file.Files.exists(path)) {
                System.out.println("Loading mock data for " + category + " from " + path.toAbsolutePath());
                java.nio.file.Files.lines(path).forEach(line -> {
                    try {
                        Map<String, Object> doc = new com.fasterxml.jackson.databind.ObjectMapper().readValue(line, Map.class);
                        String id = (String) doc.get("id");
                        if (id == null) id = java.util.UUID.randomUUID().toString();
                        searchService.indexDocument(category, id, doc);
                    } catch (Exception e) {
                        System.err.println("Error parsing line for " + category + ": " + e.getMessage());
                    }
                });
            } else {
                System.out.println("No mock data file found for " + category + " at " + path.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Failed to load mock data for " + category + ": " + e.getMessage());
        }
    }
    
    private boolean hasData(String category) {
         return java.nio.file.Files.exists(getDataPath(category));
    }
}
