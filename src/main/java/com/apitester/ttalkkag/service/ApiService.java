package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.*;
import com.apitester.ttalkkag.mapper.ApiMapper;
import com.apitester.ttalkkag.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API 관리 서비스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description API 생성, 조회, 업데이트 및 사용자 API 관리 기능을 제공하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
public class ApiService {

    private final ApiMapper apiMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final ProjectService projectService;

    private final Map<Long, Map<Long, ApiUsage>> apiUsageMap = new ConcurrentHashMap<>();

    /**
     * API 저장 및 프로젝트 참가자에게 알림 전송
     *
     * @param apis 저장할 API 객체
     * @return 저장된 API 객체
     */
    @Transactional
    public Apis saveApi(Apis apis) {
        apiMapper.saveApi(apis);
        Apis savedApi = apiMapper.findById(apis.getId());

        // 프로젝트 정보 조회
        ProjectItems projectItem = projectService.getItemByItemId(apis.getItemId());
        Long projectId = projectItem.getProjectId();

        // 프로젝트 참가자 목록 조회 및 알림 전송
        List<ProjectParticipants> projectParticipants = projectService.getParticipantsByProjectId(projectId);
        for (ProjectParticipants participant : projectParticipants) {
            notificationService.notifyProjectParticipants(participant.getUserId(), projectId, "API_SAVE");
        }

        // 같은 API를 사용 중인 사용자에게 알림 전송
        notifyApiUsers(projectId, savedApi);

        return savedApi;
    }

    /**
     * API 정보 조회
     *
     * @param itemId 조회할 API의 itemId
     * @return 조회된 API 객체
     */
    public Apis loadApi(Long itemId) {
        return apiMapper.loadApi(itemId);
    }

    /**
     * API 업데이트 및 관련 사용자 알림 전송
     *
     * @param apis 업데이트할 API 객체
     */
    @Transactional
    public void updateApi(Apis apis) {
        apiMapper.updateApi(apis);
        Apis updatedApi = apiMapper.findById(apis.getId());

        // 프로젝트 정보 조회
        ProjectItems projectItem = projectService.getItemByItemId(apis.getItemId());
        Long projectId = projectItem.getProjectId();

        // 프로젝트 참가자 목록 조회 및 알림 전송
        List<ProjectParticipants> projectParticipants = projectService.getParticipantsByProjectId(projectId);
        for (ProjectParticipants participant : projectParticipants) {
            notificationService.notifyProjectParticipants(participant.getUserId(), projectId, "API_UPDATE");
        }

        // 같은 API를 사용 중인 사용자에게 알림 전송
        notifyApiUsers(projectId, updatedApi);
    }

    /**
     * 사용자가 API를 사용하기 시작할 때 호출
     *
     * @param email 사용자 이메일
     * @param projectId 프로젝트 ID
     * @param itemId API itemId
     */
    public void updateUserApiUsage(String email, Long projectId, Long itemId) {
        User user = userMapper.findByEmail(email);

        // 프로젝트별 사용자 API 사용 정보 초기화
        apiUsageMap.putIfAbsent(projectId, new ConcurrentHashMap<>());

        // 사용자 사용 기록 업데이트
        apiUsageMap.get(projectId).put(user.getId(), new ApiUsage(
                itemId,
                user.getEmail(),
                user.getProfileImage() // 사용자 프로필 사진 URL
        ));

        sendProjectMessage(projectId, "API_SELECT");
    }

    /**
     * 특정 프로젝트의 API 사용 중인 사용자 목록 조회
     *
     * @param projectId 프로젝트 ID
     * @return 프로젝트 내 API 사용자의 목록 (userId, ApiUsage 객체)
     */
    public Map<Long, ApiUsage> getUsersUsageByProjectId(Long projectId) {
        return apiUsageMap.getOrDefault(projectId, Map.of());
    }

    /**
     * 특정 사용자의 API 사용 정보 삭제
     *
     * @param projectId 프로젝트 ID
     * @param userId 사용자 ID
     */
    public void deleteUserApiUsage(Long projectId, Long userId) {
        Map<Long, ApiUsage> projectUsage = apiUsageMap.get(projectId);
        if (projectUsage != null) {
            projectUsage.remove(userId);
        }
        sendProjectMessage(projectId, "API_SELECT");
    }

    /**
     * 특정 프로젝트의 모든 API 사용 정보 초기화
     *
     * @param projectId 프로젝트 ID
     */
    public void resetApiUsage(Long projectId) {
        apiUsageMap.remove(projectId);
    }

    /**
     * 프로젝트 내 모든 사용자에게 알림을 전송
     *
     * @param projectId 프로젝트 ID
     * @param message 전송할 메시지
     */
    private void sendProjectMessage(Long projectId, String message) {
        List<ProjectParticipants> participants = projectService.getParticipantsByProjectId(projectId);
        for (ProjectParticipants participant : participants) {
            notificationService.notifyProjectParticipants(participant.getUserId(), projectId, message);
        }

        // 프로젝트 소유자에게도 알림 전송
        Project project = projectService.getProjectByProjectId(projectId);
        notificationService.notifyProjectParticipants(project.getUserId(), projectId, message);
    }

    /**
     * 특정 API를 사용 중인 사용자에게 알림 전송
     *
     * @param projectId 프로젝트 ID
     * @param api API 객체
     */
    private void notifyApiUsers(Long projectId, Apis api) {
        if (apiUsageMap.containsKey(projectId)) {
            Map<Long, ApiUsage> projectApiUsage = apiUsageMap.get(projectId);

            // API를 사용 중인 모든 사용자에게 알림 전송
            projectApiUsage.forEach((userId, apiUsage) -> {
                if (apiUsage.getItemId().equals(api.getId())) {
                    User user = userMapper.findByEmail(apiUsage.getEmail());
                    if (user != null) {
                        notificationService.notifyApiMessage(user.getId(), api);
                    } else {
                        System.err.println("User not found for email: " + apiUsage.getEmail());
                    }
                }
            });
        } else {
            System.out.println("No users using the API in projectId: " + projectId);
        }
    }
}
