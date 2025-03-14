package com.apitester.history.controller;

import com.apitester.history.dto.ApiChangeHistory;
import com.apitester.history.service.ApiChangeHistoryService;
import com.apitester.history.service.JwtValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api-history")
public class ApiChangeHistoryController {
    private final ApiChangeHistoryService historyService;
    private final JwtValidationService jwtValidationService;

    public ApiChangeHistoryController(ApiChangeHistoryService historyService, JwtValidationService jwtValidationService) {
        this.historyService = historyService;
        this.jwtValidationService = jwtValidationService;
    }

    // 모든 이력 조회
    @GetMapping
    public List<ApiChangeHistory> getAllApiChangeHistory() {
        return historyService.getAllApiChangeHistory();
    }

    // 특정 API의 변경 이력 조회
    @GetMapping("/api/{apiId}")
    public ResponseEntity<?> getApiChangeHistoryByApiId(@RequestHeader("Authorization") String token,
                                                             @PathVariable Long apiId) {
        if (!jwtValidationService.validateJwtToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT Token");
        }
        return ResponseEntity.ok(historyService.getApiChangeHistoryByApiId(apiId));
    }

    // 특정 사용자의 변경 이력 조회
    @GetMapping("/user/{apiId}/{userId}")
    public ResponseEntity<?> getApiChangeHistoryByUserId(@RequestHeader("Authorization") String token,
                                                              @PathVariable Long apiId, @PathVariable Long userId) {
        if (!jwtValidationService.validateJwtToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT Token");
        }
        return ResponseEntity.ok(historyService.getApiChangeHistoryByUserId(apiId, userId));
    }

    // 변경 이력 저장
    @PostMapping
    public ResponseEntity<?> saveApiChangeHistory(@RequestHeader("Authorization") String token,
                                                       @RequestBody ApiChangeHistory history) {
        if (!jwtValidationService.validateJwtToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT Token");
        }
        historyService.saveApiChangeHistory(history);
        return ResponseEntity.ok("History saved!");
    }
}
