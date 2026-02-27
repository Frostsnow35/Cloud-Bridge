package com.cloudbridge.config;

import com.cloudbridge.entity.Achievement;
import com.cloudbridge.entity.graph.Technology;
import com.cloudbridge.repository.AchievementRepository;
import com.cloudbridge.repository.graph.TechnologyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(AchievementRepository achievementRepository, TechnologyRepository technologyRepository) {
        return args -> {
            try {
                // Initialize Neo4j data only if connection is available
                long count = 0;
                try {
                    count = technologyRepository.count();
                } catch (Exception e) {
                    System.out.println("Neo4j is not available, skipping graph data initialization.");
                    return;
                }

                if (count == 0) {
                    System.out.println("Initializing Neo4j graph data...");
                    technologyRepository.save(new Technology("人工智能", "AI"));
                    technologyRepository.save(new Technology("区块链", "Blockchain"));
                    technologyRepository.save(new Technology("云计算", "Cloud Computing"));
                    technologyRepository.save(new Technology("大数据", "Big Data"));
                    technologyRepository.save(new Technology("物联网", "IoT"));
                }
            } catch (Exception e) {
                System.out.println("Failed to initialize Neo4j data: " + e.getMessage());
            }
        };
    }
}
