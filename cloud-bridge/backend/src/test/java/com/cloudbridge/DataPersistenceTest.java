package com.cloudbridge;

import com.cloudbridge.entity.Achievement;
import com.cloudbridge.repository.AchievementRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataPersistenceTest {

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private DataInitializer dataInitializer;

    @Test
    @Order(1)
    public void testInitialLoad() throws Exception {
        // 第一次运行，应该加载数据
        // 注意：由于 SpringBootTest 会启动应用，DataInitializer.run() 已经被 Spring 调用过一次了
        long count = achievementRepository.count();
        assertTrue(count > 0, "Initial data should be loaded");
        
        // 添加一条测试数据
        Achievement achievement = new Achievement();
        achievement.setTitle("Test Persistence Achievement");
        achievement.setDescription("Description");
        achievement.setField("Test Field");
        achievement.setMaturity("TRL 5");
        achievement.setOwnerId(1L);
        achievement.setPrice(java.math.BigDecimal.valueOf(100.0));
        achievement.setStatus(Achievement.Status.PUBLISHED);
        achievementRepository.save(achievement);
    }

    @Test
    @Order(2)
    public void testDataPersistence() throws Exception {
        // 模拟重启：这里我们手动再次调用 dataInitializer.run()
        // 因为我们修改了逻辑，如果数据存在，它应该跳过初始化（也就是不会删除数据）
        
        long countBefore = achievementRepository.count();
        
        // 手动触发初始化逻辑
        dataInitializer.run();
        
        long countAfter = achievementRepository.count();
        
        assertEquals(countBefore, countAfter, "Data count should remain same after re-initialization");
        
        // 验证我们添加的数据是否还在
        boolean exists = achievementRepository.findAll().stream()
                .anyMatch(a -> "Test Persistence Achievement".equals(a.getTitle()));
        assertTrue(exists, "Added data should persist");
    }
}
