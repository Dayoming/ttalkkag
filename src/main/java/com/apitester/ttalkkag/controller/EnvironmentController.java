package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.dto.Environment;
import com.apitester.ttalkkag.dto.EnvironmentVariable;
import com.apitester.ttalkkag.service.EnvironmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/environments")
@RequiredArgsConstructor
public class EnvironmentController {
    private final EnvironmentService environmentService;

    @GetMapping
    public List<Environment> getEnvironments(@AuthenticationPrincipal String userEmail) {
        System.out.println(environmentService.findEnvironmentsByUserEmail(userEmail));
        return environmentService.findEnvironmentsByUserEmail(userEmail);
    }

    @PostMapping
    public void createEnvironment(@AuthenticationPrincipal String userEmail, @RequestBody Environment environment) {
        environmentService.createEnvironment(userEmail, environment);
    }

    @DeleteMapping("/{id}")
    public void deleteEnvironment(@PathVariable Long id) {
        environmentService.deleteEnvironment(id);
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
        System.out.println(id);
        environmentService.deleteVariable(id);
    }

}
