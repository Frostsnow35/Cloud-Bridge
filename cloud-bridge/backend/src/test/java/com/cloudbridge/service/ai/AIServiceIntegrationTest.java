package com.cloudbridge.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class AIServiceIntegrationTest {

    @MockBean
    private org.springframework.data.neo4j.core.Neo4jClient neo4jClient; // Mock Neo4j client to avoid connection

    @Autowired
    private AIService aiService;

    @Test
    public void testExtractKeywords() {
        String demand = "我们正在寻找一种能够用于制造下一代电动汽车电池的高容量锂离子电池材料，要求能量密度超过300Wh/kg。";
        System.out.println("Testing with demand: " + demand);
        
        String result = aiService.extractKeywords(demand);
        
        System.out.println("AI Result: " + result);
        
        assertNotNull(result);
        assertTrue(result.contains("keyword"), "Result should contain 'keyword' field");
        assertTrue(result.contains("field"), "Result should contain 'field' field");
    }
}
