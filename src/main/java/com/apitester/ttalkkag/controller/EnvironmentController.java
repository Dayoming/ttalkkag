package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.dto.Environment;
import com.apitester.ttalkkag.dto.EnvironmentVariable;
import com.apitester.ttalkkag.dto.Site;
import com.apitester.ttalkkag.service.EnvironmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/environments")
@RequiredArgsConstructor
public class EnvironmentController {

    private final EnvironmentService environmentService;

    @GetMapping("/sites/{projectId}")
    public List<Site> getSites(@PathVariable Long projectId) {
        return environmentService.getSites(projectId);
    }

    @GetMapping("/{siteId}")
    public List<Environment> getEnvironments(@PathVariable Long siteId) {
        return environmentService.getEnvironments(siteId);
    }

    @PostMapping("/site")
    public Site createSite(@RequestBody Site site) {
        return environmentService.createSite(site);
    }

    @PostMapping
    public Environment createEnvironment(@RequestBody Environment environment) {
        return environmentService.createEnvironment(environment);
    }

    @GetMapping("/env/{id}")
    public Environment getEnvironmentById(@PathVariable Long id) {
        return environmentService.getEnvironmentById(id);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteEnvironment(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            environmentService.deleteEnvironment(id);
            response.put("message", "환경 삭제 성공");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("errorMessage", "환경 삭제 실패");
        }
        return response;
    }

    @DeleteMapping("/sites/{id}")
    public Map<String, Object> deleteSite(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 사이트 및 환경 삭제
            environmentService.deleteSiteAndEnvironments(id);
            response.put("message", "사이트 삭제 성공");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("errorMessage", "사이트 삭제 실패");
        }
        return response;
    }

    @GetMapping("/variables/{environmentId}")
    public List<EnvironmentVariable> getVariables(@PathVariable Long environmentId) {
        return environmentService.findVariablesByEnvironmentId(environmentId);
    }

    @PostMapping("/variables")
    public void createVariable(@RequestBody EnvironmentVariable variable) {
        environmentService.createVariable(variable);
    }

    @PutMapping("/variables/{id}")
    public void updateVariable(@PathVariable Long id, @RequestBody EnvironmentVariable variable) {
        variable.setId(id);
        environmentService.updateVariable(variable);
    }

    @DeleteMapping("/variables/{id}")
    public void deleteVariable(@PathVariable Long id) {
        environmentService.deleteVariable(id);
    }

    @DeleteMapping("/variables/key/{key}")
    public void deleteVariableByKey(@PathVariable String key) {
        environmentService.deleteVariableByKey(key);
    }
}
