package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.dto.ApiHistory;
import com.apitester.ttalkkag.service.HistoryService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Validated
public class HistoryController {

    private final HistoryService historyService;

    @PostMapping
    public void saveHistory(@AuthenticationPrincipal @NotBlank String userEmail,
                            @RequestBody ApiHistory history) {
        historyService.saveHistory(userEmail, history);
    }

    @GetMapping
    public List<ApiHistory> getHistories(@AuthenticationPrincipal @NotBlank String userEmail) {
        return historyService.getAllHistories(userEmail);
    }

    @GetMapping("/{id}")
    public ApiHistory getHistoryById(@PathVariable Long id) {
        return historyService.getHistoryById(id);
    }

    @GetMapping("/projects/{projectId}")
    public List<ApiHistory> getHistoriesByProjectId(@PathVariable Long projectId) {
        return historyService.getHistoriesByProjectId(projectId);
    }

    @GetMapping("/errors")
    public List<ApiHistory> getErrorHistory(
            @AuthenticationPrincipal @NotBlank String userEmail,
            @RequestParam("start") String start,
            @RequestParam("end") String end) {
        return historyService.getErrorHistory(userEmail, start, end);
    }
}
