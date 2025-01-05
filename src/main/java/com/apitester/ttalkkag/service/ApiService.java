package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.Apis;
import com.apitester.ttalkkag.dto.ProjectItems;
import com.apitester.ttalkkag.dto.User;
import com.apitester.ttalkkag.mapper.ApiMapper;
import com.apitester.ttalkkag.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ApiService {

    private final ApiMapper apiMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final ProjectService projectService;

    public Apis saveApi(Apis apis) {
        apiMapper.saveApi(apis);
        Apis savedApi = apiMapper.findById(apis.getId());
        ProjectItems projectItem = projectService.getItemByItemId(apis.getItemId());
        Long projectId = projectItem.getProjectId();
        notificationService.notifyProjectParticipants(projectId, "api save");
        notificationService.notifyProjectApi(projectId, savedApi.getId(), apis);
        // 생성된 ID로 데이터 조회
        return savedApi;
    }

    public Apis loadApi(Long itemId) {
        return apiMapper.loadApi(itemId);
    }

    public void updateApi(Apis apis) {
        ProjectItems projectItem = projectService.getItemByItemId(apis.getItemId());
        Long projectId = projectItem.getProjectId();
        notificationService.notifyProjectParticipants(projectId, "api save");
        notificationService.notifyProjectApi(projectId, apis.getId(), apis);
        apiMapper.updateApi(apis);
    }

    // 사용자가 API를 사용하기 시작
    public void updateUserApiUsage(String email, Long projectId, Long itemId) {
        Long userId = userMapper.findByEmail(email).getId();
        // 이전 사용 기록 삭제
        apiMapper.deleteUsageByUserId(userId);

        // 새로운 사용 기록 추가
        apiMapper.insertUsage(projectId, userId, itemId);
        notificationService.notifyProjectParticipants(projectId, "api select");
    }

    // Project별 사용자 목록 조회
    public List<User> getUsersUsageByProjectId(Long projectId) {
        return apiMapper.findUsersByProjectId(projectId);
    }

    // 사용자 이용 기록 삭제
    public void deleteUserApiUsage(Long projectId, Long userId) {
        apiMapper.deleteUsageByUserId(userId);
        notificationService.notifyProjectParticipants(projectId, "api select");
    }
}
