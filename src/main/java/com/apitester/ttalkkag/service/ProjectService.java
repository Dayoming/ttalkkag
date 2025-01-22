package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.*;
import com.apitester.ttalkkag.mapper.ProjectMapper;
import com.apitester.ttalkkag.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

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
        Integer nextItemOrder = projectMapper.getNextItemOrder(projectItems.getProjectId(), projectItems.getParentId());

        ProjectItems newFolder = new ProjectItems();
        newFolder.setProjectId(projectItems.getProjectId());
        newFolder.setParentId(projectItems.getParentId());
        newFolder.setType("folder");
        newFolder.setName(projectItems.getName());
        newFolder.setDepth(projectItems.getDepth());
        newFolder.setCreateAt(LocalDateTime.now().toString());
        newFolder.setItemOrder(nextItemOrder);

        Long projectId = newFolder.getProjectId();

        sendProjectMessage(projectId, "UPDATE_PROJECT_ITEM");

        projectMapper.insertProjectItem(newFolder);
    }

    public ProjectItems addProjectItemApi(ProjectItems projectItems) {
        Integer nextItemOrder = projectMapper.getNextItemOrder(projectItems.getProjectId(), projectItems.getParentId());
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

        Long projectId = projectItems.getProjectId();;

        sendProjectMessage(projectId, "UPDATE_PROJECT_ITEM");

        // 업데이트된 객체 조회
        return projectMapper.getProjectItemsById(projectItems.getId());
    }

    public void updateParentId(ProjectItems projectItems) {
        Long projectId = projectMapper.getItemByItemId(projectItems.getId()).getProjectId();
        Integer itemOrder = projectMapper.getNextItemOrder(projectId, projectItems.getParentId());
        projectItems.setItemOrder(itemOrder);

        projectMapper.updateParentId(projectItems);
        ProjectItems updatedProjectItem = projectMapper.getItemByItemId(projectItems.getId());

        sendProjectMessage(projectId, "UPDATE_PROJECT_ITEM");
    }

    public void deleteItemById(Long itemId) {
        ProjectItems item = projectMapper.getItemByItemId(itemId);
        projectMapper.deleteByItemId(itemId);

        Long projectId = item.getProjectId();

        sendProjectMessage(projectId, "UPDATE_PROJECT_ITEM");
    }

    @Transactional
    public void updateItemOrder(Long draggedItemId, Long targetParentId, Integer targetOrder) {
        // 1. 대상 parent_id와 item_order 업데이트
        ProjectItems item = projectMapper.getItemByItemId(draggedItemId);
        projectMapper.incrementItemOrder(targetParentId, targetOrder, item.getProjectId());

        // 2. 드래그된 요소 업데이트
        projectMapper.updateItemOrder(draggedItemId, targetOrder, targetParentId);
        ProjectItems updatedItem = projectMapper.getItemByItemId(draggedItemId);

        // 3. 알림 전송
        Long projectId = updatedItem.getProjectId();;
        sendProjectMessage(projectId, "UPDATE_PROJECT_ITEM");
    }


    public ProjectItems getItemByItemId(Long itemId) {
        return projectMapper.getItemByItemId(itemId);
    }

    public String generateInviteCode(Long projectId, String userEmail) {
        // 초대 코드 생성
        String inviteCode = UUID.randomUUID().toString().substring(0, 6); // 8자리 코드 생성

        // 초대 코드 저장 (1시간 유효 기간 설정)
        InviteCode code = new InviteCode();
        code.setProjectId(projectId);
        code.setUserEmail(userEmail);
        code.setCode(inviteCode);
        code.setExpiryTime(LocalDateTime.now().plusHours(1));

        projectMapper.insertInviteCode(code);
        return inviteCode;
    }

    public Map<String, Object> validateInviteCode(String inviteCode, String loginEmail) {
        Map<String, Object> response = new HashMap<>();

        // 초대 코드 유효성 확인 - 사용 가능 여부가 true인 것들 중에서 코드가 같은 record 찾기
        InviteCode code = projectMapper.findByCode(inviteCode);

        // 코드가 없거나, 유효 시간이 지났거나, 사용 불가능한 코드인 경우
        if (code == null || code.isExpired() || !code.isAvailability()) {
            response.put("errorMessage", "유효하지 않은 코드입니다. 다시 확인해 주세요.");
            return response;
        }

        // 받는 사람 userId
        Long userId = userMapper.findByEmail(code.getUserEmail()).getId();
        // 현재 로그인한 userId
        Long loginUserId = userMapper.findByEmail(loginEmail).getId();
        // 초대하는 projectId
        Long projectId = code.getProjectId();
        // 초대받는 사람 객체
        ProjectParticipants participant = projectMapper.getParticipantByProjectIdAndUserId(projectId, userId);

        // 만약 이미 참여한 프로젝트인 경우
        if (participant != null) {
            response.put("errorMessage", "이미 참여한 프로젝트입니다. 다시 확인해 주세요.");
            return response;
        }

        // 내 프로젝트인 경우
        if (projectMapper.getProjectByProjectId(projectId).getUserId() == loginUserId) {
            response.put("errorMessage", "자신의 프로젝트에는 참여할 수 없습니다.");
            return response;
        }

        code.setAvailability(false);
        projectMapper.updateInviteCode(code);

        response.put("projectId", code.getProjectId());

        return response;
    }

    public ProjectParticipants addParticipant(String userEmail, Long projectId) {
        // 현재 로그인한 사용자
        User user = userMapper.findByEmail(userEmail);

        // 프로젝트 이름
        String projectName = projectMapper.getProjectByProjectId(projectId).getName();

        // 프로젝트 참여자 추가
        ProjectParticipants participant = new ProjectParticipants();
        participant.setProjectId(projectId);
        participant.setName(projectName);
        participant.setUserId(user.getId());
        participant.setRole("participant"); // 소유자, 참여자 구분
        participant.setPermissionLevel("read"); // 기본 권한: 읽기 전용
        projectMapper.insertParticipant(participant);

        // 자동 저장을 사용하지 않고 있다면 사용으로 변경
        if (!user.isAutoSaveUse()) {
            user.setAutoSaveUse(true);
            userMapper.settingUser(user);
        }

        // 알림 전송
        sendProjectMessage(projectId, "NEW_PARTICIPANT");

        return projectMapper.getParticipantsById(participant.getId());
    }

    public List<ProjectParticipants> getParticipantsByProjectId(Long projectId) {
        return projectMapper.getParticipantsByProjectId(projectId);
    }

    public void updateParticipants(Long projectId, List<ProjectParticipants> participants) {
        for (ProjectParticipants participant : participants) {
            projectMapper.updateParticipant(projectId, participant);
            // 각 참가자에게 WebSocket 메시지 전송
            notificationService.notifyProjectParticipants(participant.getUserId(), projectId, "UPDATE_AUTH");
        }
    }

    public ProjectParticipants getParticipantByProjectIdAndUserId(Long projectId, Long userId) {
        return projectMapper.getParticipantByProjectIdAndUserId(projectId, userId);
    }

    public void removeParticipant(Long projectId, Long participantId) {
        ProjectParticipants participant = projectMapper.getParticipantsById(participantId);
        // 강퇴당한 유저
        User user = userMapper.findById(participant.getUserId());
        Project project = projectMapper.getProjectByProjectId(projectId);
        projectMapper.removeParticipant(projectId, participantId);
        notificationService.notifyProjectParticipants(user.getId(), projectId,
                project.getName() + " 프로젝트에서 강퇴당했습니다.");
    }

    public void exitParticipant(Long projectId, String userEmail) {
        // 나가는 유저
        User user = userMapper.findByEmail(userEmail);
        ProjectParticipants participants = projectMapper.getParticipantByProjectIdAndUserId(projectId, user.getId());
        projectMapper.removeParticipant(projectId, participants.getId());
        sendProjectMessage(projectId, "EXIT_PROJECT");
    }

    private void sendProjectMessage(Long projectId, String message) {
        List<ProjectParticipants> participants = projectMapper.getParticipantsByProjectId(projectId);

        for (ProjectParticipants participant : participants) {
            notificationService.notifyProjectParticipants(participant.getUserId(), projectId, message);
        }

        Project project = projectMapper.getProjectByProjectId(projectId);
        Long ownerId = project.getUserId();

        notificationService.notifyProjectParticipants(ownerId, projectId, message);
    }
}
