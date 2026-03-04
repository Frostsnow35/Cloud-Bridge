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
    
    @Autowired
    private com.cloudbridge.repository.DemandRepository demandRepository;

    @Override
    public void run(String... args) throws Exception {
        System.err.println("=== RAGDataSeeder STARTED ===");
        try {
            // New CSV seeders (Prioritized)
            seedAchievementsFromProjectCSV();
            seedPublicPlatformsFromCSV();
            seedTestDemands();
            
        } catch (Throwable e) {
            System.err.println("RAG Data Seeding Failed (Non-critical): " + e.getMessage());
            e.printStackTrace();
        }
        System.err.println("=== RAGDataSeeder FINISHED ===");
    }

    // Updated to use the project list CSV as the SOLE source of Achievements
    private void seedAchievementsFromProjectCSV() {
        System.err.println("Attempting to seed Achievements from Project List CSV...");
        
        // Path Resolution Strategy
        Path path = null;
        Path dockerPath = Paths.get("/app/datasets/广州市白云区省、市级科技项目立项名单.csv");
        Path localPath = Paths.get("e:\\数据要素大赛作品\\数据集\\广州市白云区省、市级科技项目立项名单.csv");
        Path relativePath = Paths.get("数据集/广州市白云区省、市级科技项目立项名单.csv");

        if (Files.exists(dockerPath)) {
            path = dockerPath;
            System.err.println("Found CSV in Docker path: " + path.toAbsolutePath());
        } else if (Files.exists(localPath)) {
            path = localPath;
            System.err.println("Found CSV in Local path: " + path.toAbsolutePath());
        } else if (Files.exists(relativePath)) {
            path = relativePath;
            System.err.println("Found CSV in Relative path: " + path.toAbsolutePath());
        }

        try {
            // ALWAYS Clear existing achievements to ensure clean state
            System.err.println("Clearing existing achievements...");
            achievementRepository.deleteAll();
            
            if (path == null) {
                System.err.println("CRITICAL: Project List CSV NOT FOUND in any expected location.");
                System.err.println("Checked: " + dockerPath + ", " + localPath + ", " + relativePath);
                return;
            }

            // Try GBK encoding first (Excel CSV default)
            List<String> lines = null;
            try {
                lines = Files.readAllLines(path, java.nio.charset.Charset.forName("GBK"));
            } catch (Exception e) {
                System.err.println("GBK read failed for Project CSV, trying UTF-8...");
                lines = Files.readAllLines(path, java.nio.charset.StandardCharsets.UTF_8);
            }
            
            if (lines == null || lines.isEmpty()) {
                System.err.println("Project CSV is empty.");
                return;
            }

            // Header: 承担单位,级别,序号,项目名称,项目批次,支持方向
            List<String> dataLines = lines.stream().skip(1).collect(Collectors.toList());
            int count = 0;
            
            for (String line : dataLines) {
                String[] parts = line.split(",");
                // Expected at least 6 parts
                if (parts.length < 4) continue; // Relaxed check
                
                String unit = getPart(parts, 0); // 承担单位 -> Institution/Owner
                String level = getPart(parts, 1); // 级别 -> Tag
                // index 2 is serial number
                String title = getPart(parts, 3); // 项目名称 -> Title
                String batch = getPart(parts, 4); // 项目批次
                String direction = getPart(parts, 5); // 支持方向 -> Field
                
                if (title.isEmpty()) continue;

                Achievement achievement = new Achievement();
                achievement.setTitle(title);
                // Combine batch and direction for description
                String desc = "项目批次: " + batch + ". 支持方向: " + direction + ". 级别: " + level;
                achievement.setDescription(desc);
                
                // Use direction as Field if available, else generic
                achievement.setField(direction.isEmpty() ? "科技项目" : direction);
                
                achievement.setInstitution(unit);
                achievement.setMaturity("研发中"); // Default for "Projects"
                achievement.setPrice(new BigDecimal("0.00")); // Negotiable
                achievement.setOwnerId(1L); // Default admin owner
                achievement.setStatus(Achievement.Status.PUBLISHED);
                
                // Save to DB
                achievementRepository.save(achievement);
                count++;
            }
            System.err.println("SUCCESS: Seeded " + count + " Achievements from Project List CSV.");
            
        } catch (Exception e) {
            System.err.println("Failed to seed Achievements from Project CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seedPublicPlatformsFromCSV() {
        System.err.println("Attempting to seed Public Platforms...");
        
        // Path Resolution Strategy
        Path path = null;
        Path dockerPath = Paths.get("/app/datasets/广州市白云区公共数据开放计划.csv");
        Path localPath = Paths.get("e:\\数据要素大赛作品\\数据集\\广州市白云区公共数据开放计划.csv");
        Path relativePath = Paths.get("数据集/广州市白云区公共数据开放计划.csv");

        if (Files.exists(dockerPath)) {
            path = dockerPath;
            System.err.println("Found CSV in Docker path: " + path.toAbsolutePath());
        } else if (Files.exists(localPath)) {
            path = localPath;
            System.err.println("Found CSV in Local path: " + path.toAbsolutePath());
        } else if (Files.exists(relativePath)) {
            path = relativePath;
            System.err.println("Found CSV in Relative path: " + path.toAbsolutePath());
        }
        
        if (path == null) {
            System.err.println("CRITICAL: Public Platforms CSV NOT FOUND in any expected location.");
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

    private void seedTestDemands() {
        System.err.println("Attempting to seed Derived Test Demands...");
        
        // 1. Clear existing demands
        demandRepository.deleteAll();
        
        // 2. Fetch some achievements to base demands on
        List<Achievement> achievements = achievementRepository.findAll();
        if (achievements.isEmpty()) {
            System.err.println("No achievements found. Skipping demand seeding.");
            return;
        }
        
        int count = 0;
        // Pick up to 5 random achievements (or first 5)
        for (int i = 0; i < Math.min(5, achievements.size()); i++) {
            Achievement ach = achievements.get(i);
            
            // Create a matching demand
            com.cloudbridge.entity.Demand demand = new com.cloudbridge.entity.Demand();
            demand.setTitle("寻求" + ach.getTitle() + "相关技术合作");
            demand.setDescription("我司正在寻找" + ach.getField() + "领域的解决方案，特别是关于" + ach.getTitle() + "的技术。希望与高校或科研机构合作。");
            demand.setField(ach.getField());
            demand.setBudget(new BigDecimal("500000")); // 500k budget
            demand.setDeadline(java.time.LocalDate.now().plusMonths(3));
            demand.setContactName("测试经理");
            demand.setPhone("13800138000");
            demand.setInstitution("某知名科技企业");
            demand.setType("技术攻关");
            demand.setOwnerId(1L);
            demand.setStatus(com.cloudbridge.entity.Demand.Status.PUBLISHED);
            
            demandRepository.save(demand);
            count++;
        }
        
        System.err.println("SUCCESS: Seeded " + count + " Test Demands derived from Achievements.");
    }

    private String getPart(String[] parts, int index) {
        if (index < parts.length) {
            return parts[index].trim();
        }
        return "";
    }
}
