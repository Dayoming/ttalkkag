package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.dto.Apis;
import com.apitester.ttalkkag.service.ApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/apis")
@RequiredArgsConstructor
public class ApiController {

    private final ApiService apiService;

    @PostMapping
    public void saveApi(@RequestBody Apis apis) {
        apiService.saveApi(apis);
    }
}
