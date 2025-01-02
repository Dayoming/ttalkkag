package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.InviteCode;
import com.apitester.ttalkkag.dto.Project;
import com.apitester.ttalkkag.dto.ProjectItems;
import com.apitester.ttalkkag.dto.ProjectParticipants;
import com.apitester.ttalkkag.mapper.ProjectMapper;
import com.apitester.ttalkkag.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;

    // 사용자별 프로젝트 조회
    public List<Project> getProjectsByUserId(String userEmail) {
        Long userId = userMapper.findByEmail(userEmail).getId();
        return projectMapper.getProjectsByUserId(userId);
    }

    // 하위 폴더 조회
    public List<ProjectItems> getItemsByParentId(Long parentId) {
        return projectMapper.findByParentId(parentId);
    }

    // 프로젝트 생성
    public Project createProject(String name, String userEmail) {
        Long userId = userMapper.findByEmail(userEmail).getId();
        Project project = new Project();
        project.setName(name);
        project.setUserId(userId);
        projectMapper.insertProject(project);

        // 생성된 ID로 Project 객체 조회
        return projectMapper.getProjectByProjectId(project.getId());
    }

    // 프로젝트 삭제
    public void deleteProject(Long projectId) {
        projectMapper.deleteProject(projectId);
    }

    // 폴더 선택 삭제
    @Transactional
    public void deleteItems(List<Long> itemIds) {
        for (Long itemId : itemIds) {
            deleteItemRecursively(itemId);
        }
    }

    private void deleteItemRecursively(Long itemId) {
        // 하위 항목 검색
        List<ProjectItems> childItems = projectMapper.findByParentId(itemId);

        // 하위 항목 삭제
        for (ProjectItems child : childItems) {
            deleteItemRecursively(child.getId());
        }

        // 현재 항목 삭제
        projectMapper.deleteByItemId(itemId);
    }

    public void addFolder(ProjectItems projectItems) {
        Integer nextItemOrder = projectMapper.getNextItemOrder(projectItems.getProjectId());

        ProjectItems newFolder = new ProjectItems();
        newFolder.setProjectId(projectItems.getProjectId());
        newFolder.setParentId(projectItems.getParentId());
        newFolder.setType("folder");
        newFolder.setName(projectItems.getName());
        newFolder.setDepth(projectItems.getDepth());
        newFolder.setCreateAt(LocalDateTime.now().toString());
        newFolder.setItemOrder(nextItemOrder);

        projectMapper.insertProjectItem(newFolder);
    }

    public ProjectItems addProjectItemApi(ProjectItems projectItems) {
        Integer nextItemOrder = projectMapper.getNextItemOrder(projectItems.getProjectId());
        ProjectItems newApi = new ProjectItems();
        newApi.setProjectId(projectItems.getProjectId());
        newApi.setParentId(projectItems.getParentId());
        newApi.setType("api");
        newApi.setName(projectItems.getName());
        newApi.setDepth(projectItems.getDepth());
        newApi.setCreateAt(LocalDateTime.now().toString());
        newApi.setItemOrder(nextItemOrder);
        projectMapper.insertProjectItem(newApi);
        return projectMapper.getProjectItemsById(newApi.getId());
    }

    public List<ProjectItems> getProjectItemsByProjectId(Long projectId) {
        return projectMapper.findByProjectId(projectId);
    }

    public void saveApi(ProjectItems item) {
        projectMapper.insertApi(item);
    }

    public List<ProjectItems> searchProjectItems(Long projectId, String query, String type, String method) {
        return projectMapper.searchProjectItems(projectId, query, type, method);
    }

    public Project getProjectByProjectId(Long projectId) {
        return projectMapper.getProjectByProjectId(projectId);
    }

    public ProjectItems updateProjectItemName(ProjectItems projectItems) {
        projectMapper.updateProjectItemName(projectItems);

        // 업데이트된 객체 조회
        return projectMapper.getProjectItemsById(projectItems.getId());
    }

    public void updateParentId(ProjectItems projectItems) {
        projectMapper.updateParentId(projectItems);
    }

    public void deleteItemById(Long itemId) {
        projectMapper.deleteByItemId(itemId);
    }

    public void updateItemOrder(Long parentId, List<Map<String, Object>> items) {
        items.forEach(item -> {
            Long id = Long.valueOf(item.get("id").toString());
            Integer order = Integer.valueOf(item.get("order").toString());
            projectMapper.updateItemOrder(parentId, id, order);
        });
    }
    public ProjectItems getItemByItemId(Long itemId) {
        return projectMapper.getItemByItemId(itemId);
    }

    public String generateInviteCode(Long projectId) {
        // 초대 코드 생성
        String inviteCode = UUID.randomUUID().toString().substring(0, 6); // 8자리 코드 생성

        // 초대 코드 저장 (5분 유효 기간 설정)
        InviteCode code = new InviteCode();
        code.setProjectId(projectId);
        code.setCode(inviteCode);
        code.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        if (projectMapper.getInviteCodeByProjectId(projectId) != null) {
            projectMapper.updateInviteCode(code);
            return inviteCode;
        }

        projectMapper.insertInviteCode(code);
        return inviteCode;
    }

    public Long validateInviteCode(String inviteCode) {
        // 초대 코드 유효성 확인
        InviteCode code = projectMapper.findByCode(inviteCode);


        if (code == null || code.isExpired()) {
            throw new IllegalArgumentException("유효하지 않은 코드입니다. 다시 확인해 주세요.");
        }

        return code.getProjectId();
    }

    public ProjectParticipants addParticipant(String userEmail, Long projectId) {
        // 현재 로그인한 사용자 ID
        Long userId = userMapper.findByEmail(userEmail).getId();

        // 프로젝트 이름
        String projectName = projectMapper.getProjectByProjectId(projectId).getName();

        // 프로젝트 참여자 추가
        ProjectParticipants participant = new ProjectParticipants();
        participant.setProjectId(projectId);
        participant.setName(projectName);
        participant.setUserId(userId);
        participant.setRole("participant"); // 소유자, 참여자 구분
        participant.setPermissionLevel("read"); // 기본 권한: 읽기 전용
        projectMapper.insertParticipant(participant);
        return projectMapper.getParticipantsById(participant.getId());
    }
}
