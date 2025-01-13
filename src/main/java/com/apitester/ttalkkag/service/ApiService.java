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

@Service
@Transactional
@RequiredArgsConstructor
public class ApiService {

    private final ApiMapper apiMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final ProjectService projectService;

    private final Map<Long, Map<Long, ApiUsage>> apiUsageMap = new ConcurrentHashMap<>();

    public Apis saveApi(Apis apis) {
        apiMapper.saveApi(apis);
        Apis savedApi = apiMapper.findById(apis.getId());

        // 프로젝트 정보 조회
        ProjectItems projectItem = projectService.getItemByItemId(apis.getItemId());
        Long projectId = projectItem.getProjectId();

        // 프로젝트 참가자 목록 조회
        List<ProjectParticipants> projectParticipants = projectService.getParticipantsByProjectId(projectId);

        for (ProjectParticipants participant : projectParticipants) {
            Long userId = participant.getUserId();

            // 프로젝트 내 모든 사용자에게 메시지 전송
            notificationService.notifyProjectParticipants(userId, projectId, "API_SAVE");
        }

        // apiUsageMap에서 같은 API를 사용 중인 사용자에게 알림 전송
        notifyApiUsers(projectId, savedApi);

        // 생성된 ID로 데이터 조회
        return savedApi;
    }

    public Apis loadApi(Long itemId) {
        return apiMapper.loadApi(itemId);
    }

    public void updateApi(Apis apis) {
        apiMapper.updateApi(apis);
        Apis updatedApi = apiMapper.findById(apis.getId());
        ProjectItems projectItem = projectService.getItemByItemId(apis.getItemId());
        Long projectId = projectItem.getProjectId();

        // 프로젝트 참가자 목록 조회
        List<ProjectParticipants> projectParticipants = projectService.getParticipantsByProjectId(projectId);

        for (ProjectParticipants participant : projectParticipants) {
            Long userId = participant.getUserId();
            // 프로젝트 내 모든 사용자에게 메시지 전송
            notificationService.notifyProjectParticipants(userId, projectId, "API_SAVE");
        }

        // apiUsageMap에서 같은 API를 사용 중인 사용자에게 알림 전송
        notifyApiUsers(projectId, updatedApi);
    }

    // 사용자가 API를 사용하기 시작
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

        System.out.println(apiUsageMap);
        sendProjectMessage(projectId, "API_SELECT");
    }

    // Project별 사용자 목록 조회 수정
    public Map<Long, ApiUsage> getUsersUsageByProjectId(Long projectId) {
        return apiUsageMap.getOrDefault(projectId, Map.of());
    }

    // 사용자 이용 기록 삭제
    public void deleteUserApiUsage(Long projectId, Long userId) {
        Map<Long, ApiUsage> projectUsage = apiUsageMap.get(projectId);
        if (projectUsage != null) {
            projectUsage.remove(userId);
        }
        sendProjectMessage(projectId, "API_SELECT");
    }

    // API 사용 정보 초기화 (테스트나 서버 재시작 시 호출 가능)
    public void resetApiUsage(Long projectId) {
        apiUsageMap.remove(projectId);
    }

    private void sendProjectMessage(Long projectId, String message) {
        List<ProjectParticipants> participants = projectService.getParticipantsByProjectId(projectId);

        for (ProjectParticipants participant : participants) {
            notificationService.notifyProjectParticipants(participant.getUserId(), projectId, message);
        }

        Project project = projectService.getProjectByProjectId(projectId);
        Long ownerId = project.getUserId();

        notificationService.notifyProjectParticipants(ownerId, projectId, message);
    }

    private void notifyApiUsers(Long projectId, Apis api) {
        // 프로젝트 내 API 사용 정보를 조회
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
