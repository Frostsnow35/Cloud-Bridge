package com.cloudbridge.controller;

import com.cloudbridge.service.rag.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @brief 资源库控制器，提供政策、专家、资金、设备等维度的查询接口
 */
@RestController
@RequestMapping("/api/libraries")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LibraryController {

    @Autowired
    private SearchService searchService;

    /**
     * @brief 搜索资源库
     * @param category 类别 (policies, funds, equipments, experts)
     * @param keyword 搜索关键字
     * @return 匹配的资源列表 (JSON字符串列表)
     */
    @GetMapping("/{category}")
    public List<String> searchLibrary(
            @PathVariable String category,
            @RequestParam(required = false) String keyword
    ) {
        System.err.println("=== LibraryController: Search request for category: " + category + ", keyword: " + keyword);
        List<String> results = searchService.search(category, keyword);
        System.err.println("=== LibraryController: Search results count: " + results.size());
        return results;
    }

    /**
     * @brief 获取单个资源详情
     * @param category 类别 (policies, funds, equipments, experts, patents, enterprises)
     * @param id 资源ID
     * @return 资源详情 (JSON字符串)
     */
    @GetMapping("/{category}/{id}")
    public String getLibraryItem(
            @PathVariable String category,
            @PathVariable String id
    ) {
        return searchService.getById(category, id);
    }
}
