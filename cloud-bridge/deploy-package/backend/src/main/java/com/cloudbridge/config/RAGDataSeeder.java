package com.cloudbridge.config;

import com.cloudbridge.entity.Achievement;
import com.cloudbridge.repository.AchievementRepository;
import com.cloudbridge.service.rag.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.cloudbridge.util.DomainHierarchyUtil;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Profile("!test")
@Order(2)
public class RAGDataSeeder implements CommandLineRunner {

    @Autowired
    private SearchService searchService;

    @Autowired
    private AchievementRepository achievementRepository;
    
    @Autowired
    private com.cloudbridge.service.ai.EmbeddingService embeddingService;

    @Autowired
    private com.cloudbridge.repository.DemandRepository demandRepository;

    @Autowired
    private com.cloudbridge.repository.EvaluationMetricsRepository evaluationMetricsRepository;

    @Autowired
    private com.cloudbridge.service.ai.AIService aiService;

    @Override
    public void run(String... args) throws Exception {
        System.err.println("=== RAGDataSeeder STARTED ===");
        try {
            // New CSV seeders (Prioritized)
            // Mock demands first: fast (no embedding), uses existing achievements
            seedMockDemandsFromAchievements();
            seedAchievementsFromProjectCSV();
            seedPublicPlatformsFromCSV();
            seedExpertsFromCSV();
            
        } catch (Throwable e) {
            System.err.println("RAG Data Seeding Failed (Non-critical): " + e.getMessage());
            e.printStackTrace();
        }
        System.err.println("=== RAGDataSeeder FINISHED ===");
    }

    // Updated to use the project list CSV as the SOLE source of Achievements
    private void seedAchievementsFromProjectCSV() {
        System.err.println("Attempting to seed Achievements from Project List CSV...");
        
        // Dynamic Dimension Detection
        // Default: 1024 (matches nvidia/nv-embedqa-e5-v5 and BAAI/bge-large-zh-v1.5)
        int dimension = 1024;
        try {
            List<Double> dummy = embeddingService.getEmbedding("test");
            if (!dummy.isEmpty()) {
                dimension = dummy.size();
                System.out.println("Detected Embedding Dimension: " + dimension);
            }
        } catch (Exception e) {
            System.err.println("Failed to detect embedding dimension, using default 768");
            dimension = 768;
        }
        
        // Initialize Vector Index for Achievements
        try {
            searchService.createVectorIndex("achievements", dimension); 
        } catch (Exception e) {
            System.err.println("Failed to create ES index for achievements, will use memory store only");
        }

        Path path = findCsvFile("科技项目", "立项名单");

        try {
            if (path == null) {
                System.err.println("CRITICAL: Project List CSV NOT FOUND in any expected location.");
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

            // Skip if data already seeded (prevents slow reload on every startup)
            if (achievementRepository.count() > 0) {
                System.err.println("Achievements already seeded (" + achievementRepository.count()
                        + " existing). Running data cleanup...");
                // Fix field values that aren't valid domain categories (whitelist approach)
                java.util.Set<String> validDomains = new java.util.HashSet<>(java.util.Arrays.asList(
                    "生物医药","新材料","新能源","人工智能","大数据","物联网","环保科技","智能制造",
                    "金融科技","数字孪生","区块链","量子通信","航空航天","农业科技","电子信息","化学化工"
                ));
                // Meaningless tag words to strip
                java.util.Set<String> badTags = new java.util.HashSet<>(java.util.Arrays.asList(
                    "面上","重点","青年","面上项目","科技立项","重大","一般","省级","市级","国家级","支持方向"
                ));
                List<Achievement> all = achievementRepository.findAll();
                int fixedField = 0, fixedDesc = 0, fixedTags = 0;
                for (Achievement a : all) {
                    boolean changed = false;
                    // Fix field
                    if (a.getField() == null || !validDomains.contains(a.getField())) {
                        a.setField(inferDomain(a.getTitle(), ""));
                        fixedField++;
                        changed = true;
                    }
                    // Fix description: remove "支持方向: 面上" pattern
                    if (a.getDescription() != null && a.getDescription().contains("支持方向:")) {
                        a.setDescription(a.getDescription().replaceAll("\\s*\\|\\s*支持方向:\\s*\\S+", ""));
                        fixedDesc++;
                        changed = true;
                    }
                    // Fix tags: remove meaningless tag words
                    if (a.getTags() != null && !a.getTags().isEmpty()) {
                        String[] tags = a.getTags().split(",");
                        java.util.List<String> clean = new java.util.ArrayList<>();
                        for (String t : tags) {
                            t = t.trim();
                            if (!t.isEmpty() && !badTags.contains(t)) {
                                clean.add(t);
                            }
                        }
                        String newTags = String.join(",", clean);
                        if (!newTags.equals(a.getTags())) {
                            a.setTags(newTags);
                            fixedTags++;
                            changed = true;
                        }
                    }
                    if (changed) achievementRepository.save(a);
                }
                System.err.println("Cleanup: fixed " + fixedField + " fields, " + fixedDesc + " descriptions, " + fixedTags + " tags.");
                System.err.println("Indexing existing achievements to memory store...");
                indexExistingAchievements(all);
                return;
            }

            // ALWAYS Clear existing achievements ONLY if we successfully read the file
            // MUST delete child records first to avoid FK constraint violation
            System.err.println("Clearing existing data (metrics -> achievements)...");
            evaluationMetricsRepository.deleteAll();
            achievementRepository.deleteAll();

            // Header: 承担单位,级别,序号,项目名称,项目批次,支持方向
            List<String> dataLines = lines.stream().skip(1).collect(Collectors.toList());
            int count = 0;
            
            for (String line : dataLines) {
                // Use a smarter split to handle potential commas within quotes (though not common in simple CSVs)
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length < 4) continue; 
                
                String unit = getPart(parts, 0).replace("\"", ""); 
                String level = getPart(parts, 1).replace("\"", ""); 
                String title = getPart(parts, 3).replace("\"", ""); 
                String batch = getPart(parts, 4).replace("\"", ""); 
                String direction = getPart(parts, 5).replace("\"", ""); 
                
                if (title.isEmpty()) continue;

                Achievement achievement = new Achievement();
                achievement.setTitle(title);
                String desc = "立项批次: " + batch + " | 级别: " + level;
                achievement.setDescription(desc);
                achievement.setField(inferDomain(title, direction));
                achievement.setInstitution(unit);
                achievement.setMaturity("成熟应用"); 
                achievement.setPrice(new BigDecimal("0.00")); 
                achievement.setOwnerId(1L); 
                achievement.setStatus(Achievement.Status.PUBLISHED);
                
                // Generate Tags from title keywords only (don't use direction/level as they're not meaningful tags)
                Set<String> tagSet = new HashSet<>();
                
                // Simple Keyword Extraction from Title
                String[] keywords = {
                    "关键技术", "研发", "应用", "示范", "系统", "平台", "机器人", "智能", "大数据", "云平台", 
                    "物联网", "区块链", "新材料", "生物", "医疗", "健康", "农业", "生态", "环保", "治理", 
                    "监测", "检测", "装备", "制造", "工艺", "设计", "服务", "模式", "创新", "集成", 
                    "协同", "优化", "提升", "评估", "预警", "防控", "治疗", "诊断", "药物", "疫苗", 
                    "试剂", "基因", "细胞", "干细胞", "免疫", "神经", "脑科学", "心理", "认知", "教育",
                    "石墨烯", "纳米", "高分子", "复合材料", "金属", "陶瓷", "纤维", "涂层",
                    "诊断", "治疗", "预警", "防控", "康复", "筛查", 
                    "靶向", "抗体", "疫苗", "蛋白", "酶", "微生物组",
                    "传感器", "芯片", "电路", "通信", "5G", "边缘计算",
                    "数字孪生", "元宇宙", "AR", "VR", "机器人", "无人机",
                    "自动驾驶", "计算机视觉", "自然语言处理", "知识图谱",
                    "加密", "隐私计算", "联邦学习", "强化学习", "迁移学习",
                    "云计算", "容器", "微服务", "中间件", "数据库", "安全",
                    "中药", "活性成分", "药效", "药理", "毒理", "制剂",
                    "生物信息", "蛋白质组", "代谢组", "基因组", "转录组"
                };
                
                for (String kw : keywords) {
                    if (title.contains(kw)) {
                        tagSet.add(kw);
                        
                        // HIERARCHY EXPANSION: If keyword is a child, add parent!
                        for (Map.Entry<String, List<String>> entry : DomainHierarchyUtil.DOMAIN_HIERARCHY.entrySet()) {
                            if (entry.getValue().contains(kw)) {
                                tagSet.add(entry.getKey()); // Add parent domain (e.g. "新材料")
                                
                                // Optional: Add "High Performance" etc if "Graphene"
                                // Hardcoded rules for demonstration
                                if (kw.equals("石墨烯")) {
                                    tagSet.add("高性能材料");
                                    tagSet.add("碳材料");
                                }
                            }
                        }
                    }
                }
                
                // Ensure at least 2 tags
                if (tagSet.size() < 2) {
                     tagSet.add("科技成果");
                     if (tagSet.size() < 2) tagSet.add("前沿技术");
                }

                // Limit to 6 tags
                String tags = tagSet.stream().limit(6).collect(Collectors.joining(","));
                achievement.setTags(tags);
                
                achievementRepository.save(achievement);
                
                // Trigger AI Analysis for Initial Metrics (Async or Sync)
                // Remove mock data generation - Only process real data or leave for async processing
                try {
                    // Check if metrics already exist to avoid re-processing on restart
                    if (!evaluationMetricsRepository.findByAchievementId(achievement.getId()).isPresent()) {
                        System.out.println("Queuing analysis for achievement: " + achievement.getTitle());
                        // In a real production system, this should be pushed to a Message Queue (RabbitMQ/Kafka)
                        // For this implementation, we process a limited number synchronously to ensure the system has initial data,
                        // and log a message for the rest.
                        if (count < 10) { // Increased limit slightly
                             com.cloudbridge.entity.EvaluationMetrics metrics = aiService.analyzeAchievement(achievement);
                             evaluationMetricsRepository.save(metrics);
                        } else {
                             System.out.println("Skipping sync analysis for " + achievement.getId() + " to optimize startup time. Please trigger batch analysis API.");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Metrics analysis failed: " + e.getMessage());
                }

                // Index to ES with Vector
                try {
                    Map<String, Object> doc = new HashMap<>();
                    doc.put("id", achievement.getId());
                    doc.put("title", achievement.getTitle());
                    doc.put("description", achievement.getDescription());
                    doc.put("field", achievement.getField());
                    doc.put("tags", achievement.getTags());
                    
                    // Generate Embedding
                    String textToEmbed = achievement.getTitle() + " " + achievement.getDescription();
                    try {
                        List<Double> vector = embeddingService.getEmbedding(textToEmbed);
                        if (!vector.isEmpty()) {
                            doc.put("embedding", vector);
                        }
                    } catch (Exception embEx) {
                        System.err.println("Embedding generation failed for achievement " + achievement.getId() + ": " + embEx.getMessage());
                    }
                    
                    searchService.indexDocument("achievements", String.valueOf(achievement.getId()), doc);
                } catch (Exception e) {
                    System.err.println("Failed to index achievement " + achievement.getId() + ": " + e.getMessage());
                }

                count++;
            }
            System.err.println("SUCCESS: Seeded " + count + " Achievements from Project List CSV.");
            
        } catch (Exception e) {
            System.err.println("Failed to seed Achievements from Project CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void indexExistingAchievements(List<Achievement> achievements) {
        int count = 0;
        for (Achievement achievement : achievements) {
            try {
                Map<String, Object> doc = new HashMap<>();
                doc.put("id", achievement.getId());
                doc.put("title", achievement.getTitle());
                doc.put("description", achievement.getDescription());
                doc.put("field", achievement.getField());
                doc.put("tags", achievement.getTags());
                doc.put("institution", achievement.getInstitution());
                
                String textToEmbed = achievement.getTitle() + " " + achievement.getDescription();
                try {
                    List<Double> vector = embeddingService.getEmbedding(textToEmbed);
                    if (!vector.isEmpty()) {
                        doc.put("embedding", vector);
                    }
                } catch (Exception embEx) {
                    // Embedding failed, continue without it
                }
                
                searchService.indexDocument("achievements", String.valueOf(achievement.getId()), doc);
                count++;
            } catch (Exception e) {
                System.err.println("Failed to index existing achievement " + achievement.getId() + ": " + e.getMessage());
            }
        }
        System.err.println("SUCCESS: Indexed " + count + " existing achievements to memory store.");
    }

    private void seedPublicPlatformsFromCSV() {
        System.err.println("Attempting to seed Public Platforms...");
        
        Path path = findCsvFile("公共数据", "开放计划");
        
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
            } catch (Exception e) {
                System.err.println("GBK read failed for Public Platform CSV, trying UTF-8...");
                lines = Files.readAllLines(path, java.nio.charset.StandardCharsets.UTF_8);
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
                
                Map<String, Object> policyDoc = new HashMap<>();
                policyDoc.put("id", id);
                policyDoc.put("title", getPart(parts, 5));
                policyDoc.put("department", getPart(parts, 0));
                policyDoc.put("publishDate", "2024-01-01");
                policyDoc.put("policyType", "数据开放");
                policyDoc.put("content", getPart(parts, 7));
                policyDoc.put("industry", getPart(parts, 4));
                policyDoc.put("updateFrequency", getPart(parts, 1));
                policyDoc.put("openType", getPart(parts, 2));
                policyDoc.put("dataFormat", getPart(parts, 3));
                policyDoc.put("dataItems", getPart(parts, 6));
                policyDoc.put("region", "白云区");
                
                searchService.indexDocument("policies", id, policyDoc);
                count++;
            }
            System.err.println("SUCCESS: Seeded " + count + " public platforms from CSV.");

        } catch (Exception e) {
            System.err.println("Failed to seed public platforms: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seedExpertsFromCSV() {
        System.err.println("Attempting to seed Experts from CSV...");
        
        Path path = findCsvFile("评审专家", "专家名单");
        
        if (path == null) {
            System.err.println("CRITICAL: Experts CSV NOT FOUND in any expected location.");
            return;
        }

        System.err.println("Found Experts CSV at: " + path.toAbsolutePath());
        String indexName = "experts";
        searchService.createIndex(indexName);

        String[] expertNames = {"张伟", "李娜", "王强", "刘洋", "陈静", "杨帆", "周杰", "吴敏", "郑浩", "孙丽", "赵鹏", "黄磊"};
        String[] titles = {"教授", "研究员", "高级工程师", "副教授", "博士"};
        String[] affiliations = {"理工大学", "科学院", "工业研究院", "技术学院", "医科大学"};

        try {
            List<String> lines = null;
            try {
                lines = Files.readAllLines(path, java.nio.charset.Charset.forName("GBK"));
            } catch (Exception e) {
                System.err.println("GBK read failed for Experts CSV, trying UTF-8...");
                lines = Files.readAllLines(path, java.nio.charset.StandardCharsets.UTF_8);
            }
            
            if (lines == null || lines.isEmpty()) {
                System.err.println("Experts CSV is empty or could not be read.");
                return;
            }

            System.err.println("Experts CSV Header: " + lines.get(0));

            List<String> dataLines = lines.stream().skip(1).collect(Collectors.toList());

            int count = 0;
            java.util.Set<String> usedFields = new java.util.HashSet<>();
            
            for (String line : dataLines) {
                String[] parts = line.split(",");
                if (parts.length < 2) {
                    continue;
                }

                String field = getPart(parts, 1);
                if (field.isEmpty() || usedFields.size() >= expertNames.length) {
                    continue;
                }
                
                usedFields.add(field);
                
                Map<String, Object> doc = new HashMap<>();
                doc.put("id", String.valueOf(count + 1));
                doc.put("name", expertNames[count % expertNames.length]);
                doc.put("title", titles[count % titles.length]);
                doc.put("affiliation", affiliations[count % affiliations.length]);
                doc.put("field", field);
                doc.put("achievements", "在" + field + "领域具有丰富的研究经验，主持多项科研项目。");
                
                searchService.indexDocument(indexName, String.valueOf(count + 1), doc);
                count++;
            }
            System.err.println("SUCCESS: Seeded " + count + " Experts from CSV.");

        } catch (Exception e) {
            System.err.println("Failed to seed experts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seedMockDemandsFromAchievements() {
        System.err.println("Attempting to seed Mock Demands from Achievements...");
        
        try {
            List<Achievement> achievements = achievementRepository.findAll();
            if (achievements.isEmpty()) {
                System.err.println("No achievements found. Skipping mock demand seeding.");
                return;
            }

            demandRepository.deleteAll();
            System.err.println("Cleared existing demands. Generating mock demands for demo...");

            // Mock demand templates: diversified types/budgets/deadlines for showcase
            String[] demandPrefixes = {
                "寻求", "希望合作开发", "急需", "诚邀合作", "拟委托研发"
            };
            String[] demandSuffixes = {
                "相关技术支持", "的产业化方案", "的工艺优化", "的联合攻关", "的成果转化"
            };
            String[] demandInstitutions = {
                "广州某科技企业", "白云区制造业企业", "粤港澳大湾区创新中心", "广州某上市公司", "白云区高新技术企业"
            };
            String[] demandContactNames = {
                "李经理", "王总", "张工", "陈主任", "刘总监"
            };
            String[] demandDescTemplates = {
                "我司在{field}领域有产业化需求，希望与高校或科研机构合作，围绕《{title}》开展技术攻关。",
                "基于市场需要，现寻找{field}方向的解决方案，特别关注{title}相关成果的落地应用。",
                "为提升产品竞争力，拟委托开展{title}相关研究，预期形成自主知识产权。",
                "我司希望引进{title}相关技术，合作模式可灵活协商，包括联合研发或技术转让。",
                "围绕产业链升级，急需{field}领域的技术支持，期望对接《{title}》成果团队。"
            };
            BigDecimal[] budgets = {
                new BigDecimal("300000"), new BigDecimal("500000"), new BigDecimal("800000"),
                new BigDecimal("1200000"), new BigDecimal("2000000")
            };
            int[] deadlineMonths = {3, 6, 9, 12, 18};
            com.cloudbridge.entity.Demand.Type[] types = {
                com.cloudbridge.entity.Demand.Type.TECHNOLOGY_ATTACK,
                com.cloudbridge.entity.Demand.Type.NORMAL,
                com.cloudbridge.entity.Demand.Type.REWARD
            };

            java.util.Random rnd = new java.util.Random(20260720L); // 固定种子保证可复现
            int targetCount = Math.min(30, achievements.size());
            int count = 0;
            java.util.Set<Integer> usedIndexes = new java.util.HashSet<>();

            while (count < targetCount && usedIndexes.size() < achievements.size()) {
                int idx = rnd.nextInt(achievements.size());
                if (!usedIndexes.add(idx)) continue;
                Achievement ach = achievements.get(idx);

                String title = ach.getTitle();
                String field = ach.getField() != null ? ach.getField() : "综合科技";

                com.cloudbridge.entity.Demand demand = new com.cloudbridge.entity.Demand();
                demand.setTitle(demandPrefixes[rnd.nextInt(demandPrefixes.length)]
                        + (title.length() > 20 ? title.substring(0, 20) + "..." : title)
                        + demandSuffixes[rnd.nextInt(demandSuffixes.length)]);
                String desc = demandDescTemplates[rnd.nextInt(demandDescTemplates.length)]
                        .replace("{field}", field)
                        .replace("{title}", title);
                demand.setDescription(desc);
                demand.setField(field);
                demand.setBudget(budgets[rnd.nextInt(budgets.length)]);
                demand.setDeadline(java.time.LocalDate.now().plusMonths(deadlineMonths[rnd.nextInt(deadlineMonths.length)]));
                demand.setContactName(demandContactNames[rnd.nextInt(demandContactNames.length)]);
                demand.setPhone("13800138000");
                demand.setInstitution(demandInstitutions[rnd.nextInt(demandInstitutions.length)]);
                demand.setType(types[rnd.nextInt(types.length)]);
                demand.setOwnerId(1L);
                demand.setStatus(com.cloudbridge.entity.Demand.Status.PUBLISHED);

                demandRepository.save(demand);
                count++;
            }
            System.err.println("SUCCESS: Seeded " + count + " Mock Demands derived from Achievements.");
        } catch (Exception e) {
            System.err.println("Failed to seed mock demands: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getPart(String[] parts, int index) {
        if (index < parts.length) {
            return parts[index].trim();
        }
        return "";
    }

    private Path findCsvFile(String keyword1, String keyword2) {
        // List of base directories to check
        List<Path> baseDirs = java.util.Arrays.asList(
            Paths.get("/app/datasets"), // Docker
            Paths.get("e:\\数据要素大赛作品\\数据集"), // Windows Abs
            Paths.get("数据集") // Relative
        );

        for (Path baseDir : baseDirs) {
            if (Files.exists(baseDir) && Files.isDirectory(baseDir)) {
                System.err.println("Scanning directory: " + baseDir.toAbsolutePath());
                try {
                    // Forcefully list all files to see what's actually there
                    List<Path> allFiles = Files.list(baseDir).collect(Collectors.toList());
                    for (Path p : allFiles) {
                        System.err.println("  - Checking file: " + p.getFileName().toString());
                    }

                    // Content-based detection strategy
                    // Iterate through all CSV files and check the first line
                    for (Path p : allFiles) {
                        String name = p.getFileName().toString();
                        if (name.toLowerCase().endsWith(".csv")) {
                            try {
                                // Read header
                                String header = "";
                                // Try GBK first
                                try {
                                    List<String> lines = Files.readAllLines(p, java.nio.charset.Charset.forName("GBK"));
                                    if (!lines.isEmpty()) header = lines.get(0);
                                } catch (Exception e) {
                                    // Try UTF-8
                                    List<String> lines = Files.readAllLines(p, java.nio.charset.StandardCharsets.UTF_8);
                                    if (!lines.isEmpty()) header = lines.get(0);
                                }
                                
                                System.err.println("    Header for " + name + ": " + header);
                                
                                // Check for project list keywords in header
                                if (keyword1.equals("科技项目")) {
                                    if (header.contains("承担单位") || header.contains("项目名称")) {
                                        System.err.println("  -> MATCH FOUND (Content-based): " + p.toAbsolutePath());
                                        return p;
                                    }
                                }
                                // Check for public platform keywords in header
                                else if (keyword1.equals("公共数据")) {
                                    if (header.contains("公共数据开放主体名称") || header.contains("数据集名称")) {
                                        System.err.println("  -> MATCH FOUND (Content-based): " + p.toAbsolutePath());
                                        return p;
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("    Failed to read header for " + name + ": " + e.getMessage());
                            }
                        }
                    }
                    
                    // Fallback to name matching if content matching fails
                    System.err.println("  No content match found, trying name match...");
                     java.util.Optional<Path> match = allFiles.stream()
                        .filter(p -> {
                            String name = p.getFileName().toString();
                            return name.toLowerCase().endsWith(".csv") && (name.contains(keyword1) || name.contains(keyword2));
                        })
                        .findFirst();
                    
                    if (match.isPresent()) {
                        System.err.println("  -> MATCH FOUND (Name-based): " + match.get().toAbsolutePath());
                        return match.get();
                    }
                } catch (Exception e) {
                    System.err.println("Error listing files in " + baseDir + ": " + e.getMessage());
                }
            } else {
                 System.err.println("Directory not found: " + baseDir.toAbsolutePath());
            }
        }
        return null;
    }

    /**
     * @brief 根据标题关键词推断领域大类，替代CSV中的"支持方向"值
     */
    private String inferDomain(String title, String direction) {
        String t = title;
        // Drug/medical/biomedical titles
        if (t.contains("药") || t.contains("医") || t.contains("临床") || t.contains("治疗") || 
            t.contains("诊断") || t.contains("基因") || t.contains("细胞") || t.contains("蛋白") ||
            t.contains("免疫") || t.contains("病") || t.contains("靶向") || t.contains("传染") ||
            t.contains("菌") || t.contains("感染") || t.contains("癌") || t.contains("肿瘤") ||
            t.contains("干细胞") || t.contains("中药") || t.contains("药理") || t.contains("代谢") ||
            t.contains("血清") || t.contains("生殖") || t.contains("神经系统")) {
            return "生物医药";
        }
        // AI/computing
        if (t.contains("智能") || t.contains("深度学习") || t.contains("机器学习") || 
            t.contains("算法") || t.contains("图像识别") || t.contains("自然语言") ||
            t.contains("大模型") || t.contains("计算机视觉")) {
            return "人工智能";
        }
        // Materials
        if (t.contains("材料") || t.contains("纳米") || t.contains("石墨烯") || t.contains("涂层") ||
            t.contains("高分子") || t.contains("纤维") || t.contains("陶瓷") || t.contains("金属") ||
            t.contains("复合材料")) {
            return "新材料";
        }
        // Energy
        if (t.contains("能源") || t.contains("电池") || t.contains("光伏") || t.contains("太阳能") ||
            t.contains("风电") || t.contains("储能")) {
            return "新能源";
        }
        // Manufacturing/robotics
        if (t.contains("制造") || t.contains("机器人") || t.contains("机械") || t.contains("加工") ||
            t.contains("装备") || t.contains("生产")) {
            return "智能制造";
        }
        // Big data
        if (t.contains("大数据") || t.contains("数据") || t.contains("数据挖掘") || t.contains("预测")) {
            return "大数据";
        }
        // IoT
        if (t.contains("物联网") || t.contains("传感器") || t.contains("传感")) {
            return "物联网";
        }
        // Environment
        if (t.contains("生态") || t.contains("环保") || t.contains("污染") || t.contains("气候") ||
            t.contains("碳") || t.contains("环境") || t.contains("水处理")) {
            return "环保科技";
        }
        // Agriculture
        if (t.contains("农业") || t.contains("养殖") || t.contains("种植") || t.contains("土壤") ||
            t.contains("作物") || t.contains("畜牧") || t.contains("水产")) {
            return "农业科技";
        }
        // Aerospace
        if (t.contains("航天") || t.contains("航空") || t.contains("卫星") || t.contains("飞行器") ||
            t.contains("宇宙")) {
            return "航空航天";
        }
        // Electronics/communication
        if (t.contains("通信") || t.contains("电子") || t.contains("电路") || t.contains("芯片") ||
            t.contains("半导体") || t.contains("5G") || t.contains("光") || t.contains("信号")) {
            return "电子信息";
        }
        // Chemistry
        if (t.contains("化学") || t.contains("催化") || t.contains("分子") || t.contains("合成") ||
            t.contains("制剂")) {
            return "化学化工";
        }
        // Finance tech
        if (t.contains("金融") || t.contains("经济") || t.contains("支付")) {
            return "金融科技";
        }
        // Blockchain
        if (t.contains("区块链") || t.contains("分布式")) {
            return "区块链";
        }
        // Default fallback for medical/biology sounding
        if (t.contains("生物") || t.contains("酶") || t.contains("菌") || t.contains("药")) {
            return "生物医药";
        }
        return "电子信息"; // default for科研立项
    }
}
