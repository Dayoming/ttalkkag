package com.apitester.history.controller;

import com.apitester.history.dto.ApiChangeHistory;
import com.apitester.history.service.ApiChangeHistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class ApiChangeHistoryController {
    private final ApiChangeHistoryService historyService;

    public ApiChangeHistoryController(ApiChangeHistoryService historyService) {
        this.historyService = historyService;
    }

    // 모든 이력 조회
    @GetMapping
    public List<ApiChangeHistory> getAllApiChangeHistory() {
        return historyService.getAllApiChangeHistory();
    }

    // 특정 API의 변경 이력 조회
    @GetMapping("/api/{apiId}")
    public List<ApiChangeHistory> getApiChangeHistoryByApiId(@PathVariable Long apiId) {
        return historyService.getApiChangeHistoryByApiId(apiId);
    }

    // 특정 사용자의 변경 이력 조회
    @GetMapping("/user/{apiId}/{userId}")
    public List<ApiChangeHistory> getApiChangeHistoryByUserId(@PathVariable Long apiId, @PathVariable Long userId) {
        return historyService.getApiChangeHistoryByUserId(apiId, userId);
    }

    // 변경 이력 저장
    @PostMapping
    public String saveApiChangeHistory(@RequestBody ApiChangeHistory history) {
        historyService.saveApiChangeHistory(history);
        return "History saved!";
    }
}
