package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.dto.ItemOrderUpdateRequest;
import com.apitester.ttalkkag.dto.Project;
import com.apitester.ttalkkag.dto.ProjectItems;
import com.apitester.ttalkkag.dto.ProjectParticipants;
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
            @RequestParam String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String method
    ) {
        return projectService.searchProjectItems(projectId, query, type, method);
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

    @PostMapping("/invite")
    public Map<String, Object> generateInviteCode(@RequestBody Map<String, Long> requestBody) {
        Map<String, Object> response = new HashMap<>();
        Long projectId = requestBody.get("projectId");
        if (projectId == null) {
            response.put("errorMessage", "Invalid project ID");
            return response;
        }

        String inviteCode = projectService.generateInviteCode(projectId);
        response.put("code", inviteCode);
        return response;
    }

    @PostMapping("/validate-invite")
    public Map<String, Object> validateInviteCode(@AuthenticationPrincipal String userEmail,
                                                  @RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        String inviteCode = requestBody.get("inviteCode");
        try {
            Long projectId = projectService.validateInviteCode(inviteCode);
            projectService.addParticipant(userEmail, projectId);
            response.put("projectId", projectId);
            return response;
        } catch (IllegalArgumentException e) {
            response.put("errorMessage", "초대 코드를 확인할 수 없습니다. 다시 시도해주세요.");
            e.printStackTrace();
            return response;
        }
    }

    @PatchMapping("/update/projectItemName")
    public Map<String, Object> updateProjectItemName(@RequestBody ProjectItems projectItems) {
        Map<String, Object> response = new HashMap<>();
        try {
            ProjectItems updatedItem = projectService.updateProjectItemName(projectItems);
            response.put("updatedItem", updatedItem);
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
        } catch (Exception e) {
            response.put("errorMessage", "경로 변경에 실패했습니다.");
        }
        return response;
    }

    @PatchMapping("/update/itemOrder")
    public void updateItemOrder(@RequestBody ItemOrderUpdateRequest request) {
        projectService.updateItemOrder(
                request.getDraggedItemId(),
                request.getTargetParentId(),
                request.getTargetOrder()
        );
    }


    @GetMapping("/{projectId}")
    public List<ProjectItems> getProjectItems(@PathVariable Long projectId) {
        return projectService.getProjectItemsByProjectId(projectId);
    }

    @GetMapping("/items/{parentId}")
    public List<ProjectItems> getItemsByParentId(@PathVariable Long parentId) {
        return projectService.getItemsByParentId(parentId);
    }

    @GetMapping("/item/{itemId}")
    public ProjectItems getItemByItemId(@PathVariable Long itemId) {
        return projectService.getItemByItemId(itemId);
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

    // 특정 프로젝트 참여자 조회
    @GetMapping("/{projectId}/participants")
    public List<ProjectParticipants> getParticipants(@PathVariable Long projectId) {
        List<ProjectParticipants> participants = projectService.getParticipantsByProjectId(projectId);
        return participants;
    }

    // 특정 프로젝트의 특정 참여자 정보 조회
    @GetMapping("/{projectId}/{userId}/participants")
    public ProjectParticipants getParticipantByProjectIdAndUserId(@PathVariable Long projectId, @PathVariable Long userId) {
        ProjectParticipants participant = projectService.getParticipantByProjectIdAndUserId(projectId, userId);
        return participant;
    }

    // 특정 프로젝트 참여자 권한 수정
    @PostMapping("/{projectId}/participants")
    public Map<String, Object> updateParticipants(@PathVariable Long projectId, @RequestBody List<ProjectParticipants> participants) {
        projectService.updateParticipants(projectId, participants);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "권한이 업데이트 되었습니다.");
        return response;
    }

}
