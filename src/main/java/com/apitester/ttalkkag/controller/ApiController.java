package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.dto.Apis;
import com.apitester.ttalkkag.dto.ProjectItems;
import com.apitester.ttalkkag.dto.User;
import com.apitester.ttalkkag.service.ApiService;
import com.apitester.ttalkkag.service.NotificationService;
import com.apitester.ttalkkag.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/apis")
@RequiredArgsConstructor
public class ApiController {

    private final ApiService apiService;

    @PostMapping
    public Apis saveApi(@RequestBody Apis apis) {
        return apiService.saveApi(apis);
    }

    @PatchMapping
    public void updateApi(@RequestBody Apis apis) {
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
    public List<User> findUsersByProjectId(@PathVariable Long projectId) {
        // 해당 API를 사용하는 사용자 목록 조회
        List<User> users = apiService.getUsersUsageByProjectId(projectId);
        return users;
    }

    @PostMapping("/usage/{projectId}/{itemId}")
    public void updateUserApiUsage(@AuthenticationPrincipal String email, @PathVariable Long projectId, @PathVariable Long itemId) {
        apiService.updateUserApiUsage(email, projectId, itemId);
    }

    @DeleteMapping("/usage/out/{projectId}/{userId}")
    public void deleteUserApiUsage(@PathVariable Long projectId, @PathVariable Long userId) {
        System.out.println("projectId: " + projectId + ", userId: " + userId);
        apiService.deleteUserApiUsage(projectId, userId);
    }
}
