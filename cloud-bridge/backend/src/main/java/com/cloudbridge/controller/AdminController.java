package com.cloudbridge.controller;

import com.cloudbridge.repository.AchievementRepository;
import com.cloudbridge.repository.DemandRepository;
import com.cloudbridge.repository.UserRepository;
import com.cloudbridge.entity.Demand;
import com.cloudbridge.entity.Achievement;
import com.cloudbridge.service.rag.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private DemandRepository demandRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SearchService searchService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied.");
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDemands", demandRepository.count());
        stats.put("totalAchievements", achievementRepository.count());
        stats.put("totalUsers", userRepository.count());
        stats.put("pendingDemands", demandRepository.findByStatus(Demand.Status.PENDING_REVIEW).size());
        stats.put("pendingAchievements", achievementRepository.findByStatus(Achievement.Status.PENDING_REVIEW).size());

        return ResponseEntity.ok(stats);
    }

    private ResponseEntity<?> checkAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied.");
        }
        return null;
    }

    @GetMapping("/libraries/{category}")
    public ResponseEntity<?> listLibrary(
            HttpServletRequest request,
            @PathVariable String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ResponseEntity<?> denied = checkAdmin(request);
        if (denied != null) return denied;

        try {
            List<String> documents = searchService.listDocuments(category, page, size);
            long total = searchService.countDocuments(category);

            List<Object> results = new ArrayList<>();
            for (String doc : documents) {
                results.add(objectMapper.readValue(doc, Object.class));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("data", results);
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to list library: " + e.getMessage());
        }
    }

    @GetMapping("/libraries/{category}/{id}")
    public ResponseEntity<?> getLibraryItem(
            HttpServletRequest request,
            @PathVariable String category,
            @PathVariable String id
    ) {
        ResponseEntity<?> denied = checkAdmin(request);
        if (denied != null) return denied;

        String result = searchService.getById(category, id);
        if (result != null) {
            try {
                return ResponseEntity.ok(objectMapper.readValue(result, Object.class));
            } catch (Exception e) {
                return ResponseEntity.ok(result);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/libraries/{category}")
    public ResponseEntity<?> addLibraryItem(
            HttpServletRequest request,
            @PathVariable String category,
            @RequestBody Map<String, Object> body
    ) {
        ResponseEntity<?> denied = checkAdmin(request);
        if (denied != null) return denied;

        try {
            String id = body.get("id") != null ? body.get("id").toString() : UUID.randomUUID().toString();
            body.put("id", id);
            searchService.indexDocument(category, id, body);
            return ResponseEntity.ok(Map.of("id", id, "message", "添加成功"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to add item: " + e.getMessage());
        }
    }

    @PutMapping("/libraries/{category}/{id}")
    public ResponseEntity<?> updateLibraryItem(
            HttpServletRequest request,
            @PathVariable String category,
            @PathVariable String id,
            @RequestBody Map<String, Object> body
    ) {
        ResponseEntity<?> denied = checkAdmin(request);
        if (denied != null) return denied;

        try {
            body.put("id", id);
            searchService.indexDocument(category, id, body);
            return ResponseEntity.ok(Map.of("id", id, "message", "更新成功"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to update item: " + e.getMessage());
        }
    }

    @DeleteMapping("/libraries/{category}/{id}")
    public ResponseEntity<?> deleteLibraryItem(
            HttpServletRequest request,
            @PathVariable String category,
            @PathVariable String id
    ) {
        ResponseEntity<?> denied = checkAdmin(request);
        if (denied != null) return denied;

        try {
            searchService.deleteDocument(category, id);
            return ResponseEntity.ok(Map.of("message", "删除成功"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete item: " + e.getMessage());
        }
    }

    @PostMapping("/libraries/{category}/import")
    public ResponseEntity<?> importLibraryCSV(
            HttpServletRequest request,
            @PathVariable String category,
            @RequestParam("file") MultipartFile file
    ) {
        ResponseEntity<?> denied = checkAdmin(request);
        if (denied != null) return denied;

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件不能为空");
        }

        try {
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), Charset.forName("GBK")))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            } catch (Exception e) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }
                }
            }

            if (lines.size() < 2) {
                return ResponseEntity.badRequest().body("CSV文件至少包含表头和一行数据");
            }

            String header = lines.get(0);
            String[] headers = header.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            List<Map<String, Object>> dataList = new ArrayList<>();

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (values.length < headers.length) continue;

                Map<String, Object> item = new HashMap<>();
                for (int j = 0; j < headers.length; j++) {
                    String key = headers[j].trim().replace("\"", "");
                    String value = j < values.length ? values[j].trim().replace("\"", "") : "";
                    item.put(key, value);
                }

                if (!item.containsKey("id")) {
                    item.put("id", UUID.randomUUID().toString());
                }
                dataList.add(item);
            }

            int successCount = 0;
            for (Map<String, Object> item : dataList) {
                try {
                    String id = item.get("id").toString();
                    searchService.indexDocument(category, id, item);
                    successCount++;
                } catch (Exception e) {
                    System.err.println("Failed to import item: " + e.getMessage());
                }
            }

            return ResponseEntity.ok(Map.of(
                    "total", dataList.size(),
                    "success", successCount,
                    "message", String.format("成功导入 %d/%d 条数据", successCount, dataList.size())
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("导入失败: " + e.getMessage());
        }
    }

    @PostMapping("/libraries/{category}/clear")
    public ResponseEntity<?> clearLibrary(
            HttpServletRequest request,
            @PathVariable String category
    ) {
        ResponseEntity<?> denied = checkAdmin(request);
        if (denied != null) return denied;

        try {
            searchService.clearIndex(category);
            return ResponseEntity.ok(Map.of("message", "清空成功"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("清空失败: " + e.getMessage());
        }
    }

    @PostMapping("/libraries/{category}/rebuild")
    public ResponseEntity<?> rebuildLibraryIndex(
            HttpServletRequest request,
            @PathVariable String category
    ) {
        ResponseEntity<?> denied = checkAdmin(request);
        if (denied != null) return denied;

        try {
            searchService.createIndex(category);
            return ResponseEntity.ok(Map.of("message", "索引重建成功"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("索引重建失败: " + e.getMessage());
        }
    }

    @GetMapping("/libraries/categories")
    public ResponseEntity<?> getLibraryCategories(HttpServletRequest request) {
        ResponseEntity<?> denied = checkAdmin(request);
        if (denied != null) return denied;

        List<Map<String, Object>> categories = Arrays.asList(
                Map.of("key", "policies", "label", "政策库"),
                Map.of("key", "experts", "label", "专家库"),
                Map.of("key", "funds", "label", "资金库"),
                Map.of("key", "equipments", "label", "设备库"),
                Map.of("key", "patents", "label", "专利库"),
                Map.of("key", "enterprises", "label", "企业库"),
                Map.of("key", "public_platforms", "label", "公共平台")
        );

        return ResponseEntity.ok(categories);
    }
}
