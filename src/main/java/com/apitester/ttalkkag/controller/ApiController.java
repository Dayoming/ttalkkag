package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.dto.Apis;
import com.apitester.ttalkkag.service.ApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/apis")
@RequiredArgsConstructor
public class ApiController {

    private final ApiService apiService;

    @PostMapping
    public void saveApi(@RequestBody Apis apis) {
        apiService.saveApi(apis);
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
}
