package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.dto.ApiUsage;
import com.apitester.ttalkkag.dto.Apis;
import com.apitester.ttalkkag.service.ApiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/apis")
@RequiredArgsConstructor
@Validated
public class ApiController {

    private final ApiService apiService;

    @PostMapping
    public Apis saveApi(@Valid @RequestBody Apis apis) {
        return apiService.saveApi(apis);
    }

    @PatchMapping
    public void updateApi(@Valid @RequestBody Apis apis) {
        apiService.updateApi(apis);
    }

    @GetMapping("/{itemId}")
    public Map<String, Object> loadApi(@PathVariable Long itemId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Apis api = apiService.loadApi(itemId);
            response.put("api", api);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("errorMessage", "해당 API 파일을 불러오지 못했습니다.");
        }
        return response;
    }

    @GetMapping("/usage/list/{projectId}")
    public ResponseEntity<Map<Long, ApiUsage>> getApiUsageList(@PathVariable Long projectId) {
        return ResponseEntity.ok(apiService.getUsersUsageByProjectId(projectId));
    }


    @PostMapping("/usage/{projectId}/{itemId}")
    public void updateUserApiUsage(@AuthenticationPrincipal String email, @PathVariable Long projectId, @PathVariable Long itemId) {
        apiService.updateUserApiUsage(email, projectId, itemId);
    }

    @DeleteMapping("/usage/out/{projectId}/{userId}")
    public void deleteUserApiUsage(@PathVariable Long projectId, @PathVariable Long userId) {
        apiService.deleteUserApiUsage(projectId, userId);
    }

    // 추가: API 사용 데이터 초기화 (테스트 또는 디버깅용)
    @DeleteMapping("/usage/reset/{projectId}")
    public void resetApiUsage(@PathVariable Long projectId) {
        apiService.resetApiUsage(projectId);
    }
}
