package com.cloudbridge;

import com.cloudbridge.entity.Achievement;
import com.cloudbridge.entity.Demand;
import com.cloudbridge.entity.resource.*;
import com.cloudbridge.repository.AchievementRepository;
import com.cloudbridge.repository.DemandRepository;
import com.cloudbridge.repository.resource.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.cloudbridge.entity.User;
import com.cloudbridge.repository.UserRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private DemandRepository demandRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpertRepository expertRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private FundRepository fundRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private PatentRepository patentRepository;

    @Autowired
    private EnterpriseRepository enterpriseRepository;

    @Autowired(required = false)
    private Driver neo4jDriver;

    @Override
    public void run(String... args) throws Exception {
        logger.info("DataInitializer STARTED");
        System.out.println("DEBUG: DataInitializer STARTED. CWD: " + System.getProperty("user.dir"));
        try {
            initializeAchievements();
            initializeDemands();
            initializeAdminUser();
            initializeResources();
            if (neo4jDriver != null) {
                initializeGraph();
            } else {
                logger.warn("Neo4j driver not available. Skipping graph initialization.");
            }
        } catch (Exception e) {
            logger.error("Data initialization failed (non-critical): " + e.getMessage());
        }
        System.out.println("DEBUG: DataInitializer FINISHED");
        logger.info("DataInitializer FINISHED");
    }

    private void initializeResources() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            String baseDir = "E:/数据要素大赛作品/cloud-bridge/backend/data/";

            // Experts
            if (expertRepository.count() == 0) {
                List<Expert> experts = loadResource(baseDir + "res_experts.json", new TypeReference<List<Expert>>() {}, mapper);
                if (experts != null) expertRepository.saveAll(experts);
            }

            // Policies
            if (policyRepository.count() == 0) {
                List<Policy> policies = loadResource(baseDir + "res_policies.json", new TypeReference<List<Policy>>() {}, mapper);
                if (policies != null) policyRepository.saveAll(policies);
            }

            // Funds
            if (fundRepository.count() == 0) {
                List<Fund> funds = loadResource(baseDir + "res_funds.json", new TypeReference<List<Fund>>() {}, mapper);
                if (funds != null) fundRepository.saveAll(funds);
            }

            // Equipments
            if (equipmentRepository.count() == 0) {
                List<Equipment> equipments = loadResource(baseDir + "res_equipments.json", new TypeReference<List<Equipment>>() {}, mapper);
                if (equipments != null) equipmentRepository.saveAll(equipments);
            }

            // Patents
            if (patentRepository.count() == 0) {
                List<Patent> patents = loadResource(baseDir + "res_patents.json", new TypeReference<List<Patent>>() {}, mapper);
                if (patents != null) patentRepository.saveAll(patents);
            }

            // Enterprises
            if (enterpriseRepository.count() == 0) {
                List<Enterprise> enterprises = loadResource(baseDir + "res_enterprises.json", new TypeReference<List<Enterprise>>() {}, mapper);
                if (enterprises != null) enterpriseRepository.saveAll(enterprises);
            }

            logger.info("Resource initialization completed.");
        } catch (Exception e) {
            logger.error("Failed to initialize resources", e);
        }
    }

    private <T> List<T> loadResource(String path, TypeReference<List<T>> typeRef, ObjectMapper mapper) {
        File file = new File(path);
        if (!file.exists()) {
            logger.warn("Resource file not found: {}", path);
            return null;
        }
        try (java.io.InputStreamReader reader = new java.io.InputStreamReader(new java.io.FileInputStream(file), StandardCharsets.UTF_8)) {
            return mapper.readValue(reader, typeRef);
        } catch (IOException e) {
            logger.error("Error reading resource file: " + path, e);
            return null;
        }
    }

    private void initializeAdminUser() {
        try {
            System.out.println("DEBUG: Checking admin user...");
            if (userRepository.existsByUsername("admin")) {
                logger.info("Admin user exists. Skipping initialization.");
                return;
            }

            System.out.println("DEBUG: Creating default admin user...");
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(hashPassword("admin123")); // MD5 hash of "admin123"
            admin.setEmail("admin@cloudbridge.com");
            admin.setRole(User.Role.ADMIN);
            admin.setStatus(User.Status.ACTIVE);
            
            userRepository.save(admin);
            System.out.println("DEBUG: Admin user created successfully.");
            logger.info("Initialized admin user: admin/admin123");
        } catch (Exception e) {
            logger.error("Failed to initialize admin user", e);
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private void initializeAchievements() {
        try {
            System.out.println("DEBUG: Checking achievement count...");
            long count = achievementRepository.count();
            System.out.println("DEBUG: Achievement count: " + count);

            if (count > 0) {
                logger.info("Achievements exist. Skipping initialization.");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            // Use ClassPathResource to read from resources if file not found in root
            File file = new File("data/smart_achievements.json");
            System.out.println("DEBUG: Looking for file at: " + file.getAbsolutePath());
            
            if (!file.exists()) {
                 // Fallback to absolute path or ignore if just for dev
                 file = new File("E:/数据要素大赛作品/cloud-bridge/backend/data/smart_achievements.json");
                 System.out.println("DEBUG: Fallback to absolute path: " + file.getAbsolutePath());
            }
            
            if (!file.exists()) {
                logger.warn("Data file not found: {}", file.getAbsolutePath());
                System.out.println("DEBUG: File not found!");
                return;
            }

            logger.info("Loading achievements from: {}", file.getAbsolutePath());
            System.out.println("DEBUG: Reading file...");
            // Use InputStreamReader to ensure UTF-8
            List<Achievement> achievements;
            try (java.io.InputStreamReader reader = new java.io.InputStreamReader(new java.io.FileInputStream(file), StandardCharsets.UTF_8)) {
                achievements = mapper.readValue(reader, new TypeReference<List<Achievement>>() {});
            }
            System.out.println("DEBUG: Read " + achievements.size() + " achievements. Saving...");
            achievementRepository.saveAll(achievements);
            System.out.println("DEBUG: Saved achievements.");
            logger.info("Initialized {} achievements.", achievements.size());
        } catch (Exception e) {
            logger.error("Failed to initialize achievements", e);
            e.printStackTrace();
        }
    }

    private void initializeDemands() {
        try {
            System.out.println("DEBUG: Checking demand count...");
            long count = demandRepository.count();
            System.out.println("DEBUG: Demand count: " + count);

            if (count > 0) {
                logger.info("Demands exist. Skipping initialization.");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            File file = new File("data/smart_demands.json");
            System.out.println("DEBUG: Looking for demands file at: " + file.getAbsolutePath());
            
            if (!file.exists()) {
                 file = new File("E:/数据要素大赛作品/cloud-bridge/backend/data/smart_demands.json");
                 System.out.println("DEBUG: Fallback to absolute path: " + file.getAbsolutePath());
            }
            
            if (!file.exists()) {
                logger.warn("Data file not found: {}", file.getAbsolutePath());
                System.out.println("DEBUG: Demands file not found!");
                return;
            }

            System.out.println("DEBUG: Reading demands file...");
            // Use InputStreamReader to ensure UTF-8
            List<Demand> demands;
            try (java.io.InputStreamReader reader = new java.io.InputStreamReader(new java.io.FileInputStream(file), StandardCharsets.UTF_8)) {
                demands = mapper.readValue(reader, new TypeReference<List<Demand>>() {});
            }
            System.out.println("DEBUG: Read " + demands.size() + " demands.");
            
            // Check for potential validation issues
            for (Demand d : demands) {
                // System.out.println("DEBUG: Processing demand: " + d.getTitle() + ", status=" + d.getStatus());
                if (d.getStatus() == null) {
                    d.setStatus(Demand.Status.PUBLISHED);
                    // System.out.println("DEBUG: Fixed missing status to PUBLISHED");
                }
            }

            System.out.println("DEBUG: Saving demands...");
            demandRepository.saveAll(demands);
            System.out.println("DEBUG: Saved demands successfully.");
            logger.info("Initialized {} demands.", demands.size());
        } catch (Exception e) {
            logger.error("Failed to initialize demands", e);
            e.printStackTrace();
        }
    }

    private void initializeGraph() {
        if (neo4jDriver == null) return;
        try (Session session = neo4jDriver.session()) {
            logger.info("Initializing graph data...");
            ClassPathResource resource = new ClassPathResource("cypher/import.cypher");
            String cypherScript = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

            String[] statements = cypherScript.split(";");
            for (String stmt : statements) {
               if (stmt.trim().isEmpty()) continue;
               session.run(stmt);
            }
            logger.info("Graph data initialized successfully.");
        } catch (Exception e) {
            logger.error("Failed to initialize graph data (Skipping)", e);
        }
    }
}
