package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.dto.Project;
import com.apitester.ttalkkag.dto.ProjectItems;
import com.apitester.ttalkkag.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // 사용자별 프로젝트 조회
    @GetMapping
    public List<Project> getUserProjects(@AuthenticationPrincipal String userEmail) {
        return projectService.getProjectsByUserId(userEmail);
    }

    @GetMapping("/find/{projectId}")
    public Project getProjectByProjectId(@PathVariable Long projectId) {
        return projectService.getProjectByProjectId(projectId);
    }

    @GetMapping("/search/{projectId}")
    public List<ProjectItems> searchProjectItems(
            @PathVariable Long projectId,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "method", required = false) String method
    ) {
        return projectService.searchProjectItems(projectId, query, method);
    }

    // 프로젝트 생성
    @PostMapping
    public Project createProject(@RequestBody Project project, @AuthenticationPrincipal String userEmail) {
        return projectService.createProject(project.getName(), userEmail);
    }

    // 프로젝트 삭제
    @DeleteMapping("/{projectId}")
    public void deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
    }

    @PostMapping("/add-folder")
    public void addFolder(@RequestBody ProjectItems projectItems) {
        projectService.addFolder(projectItems);
    }

    @PostMapping("/add-api")
    public ProjectItems addProjectItemApi(@RequestBody ProjectItems projectItems) {
        return projectService.addProjectItemApi(projectItems);
    }

    @PatchMapping("/update/projectItemName")
    public Map<String, Object> updateProjectItemName(@RequestBody ProjectItems projectItems) {
        Map<String, Object> response = new HashMap<>();
        try {
            projectService.updateProjectItemName(projectItems);
        } catch (Exception e) {
            response.put("errorMessage", "이름 변경에 실패했습니다.");
        }
        return response;
    }

    @PatchMapping("/update/parentId")
    public Map<String, Object> updateParentId(@RequestBody ProjectItems projectItems) {
        Map<String, Object> response = new HashMap<>();
        try {
            projectService.updateParentId(projectItems);
            System.out.println(projectItems);
        } catch (Exception e) {
            response.put("errorMessage", "경로 변경에 실패했습니다.");
        }
        return response;
    }

    @GetMapping("/{projectId}")
    public List<ProjectItems> getProjectItems(@PathVariable Long projectId) {
        return projectService.getProjectItemsByProjectId(projectId);
    }

    @GetMapping("/items/{parentId}")
    public List<ProjectItems> getItemsByParentId(@PathVariable Long parentId) {
        return projectService.getItemsByParentId(parentId);
    }

    @DeleteMapping("/items")
    public void deleteItems(@RequestBody List<Long> itemIds) {
        projectService.deleteItems(itemIds);
    }

    @DeleteMapping("/items/{itemId}")
    public void deleteItemById(@PathVariable Long itemId) {
        projectService.deleteItemById(itemId);
    }

    // API 저장
    @PostMapping("/items")
    public Map<String, Object> saveApi(@RequestBody ProjectItems item) {
        Map<String, Object> response = new HashMap<>();
        projectService.saveApi(item);
        response.put("message", "API가 정상적으로 저장되었습니다.");
        return response;
    }

}
