package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.*;
import com.apitester.ttalkkag.log.LoggingUtil;
import com.apitester.ttalkkag.mapper.ApiMapper;
import com.apitester.ttalkkag.mapper.UserMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, Long, ApiUsage> hashOperations;

    /**
     * 생성자에서 Redis HashOperations 초기화
     */
    @PostConstruct
    public void init() {
        this.hashOperations = redisTemplate.opsForHash();
    }

    /**
     * API 저장 및 프로젝트 참가자에게 알림 전송
     *
     * @param apis 저장할 API 객체
     * @return 저장된 API 객체
     */
    @Transactional
    public Apis saveApi(Apis apis) {
        String logKey = MDC.get("LOG_KEY");
        String userEmail = MDC.get("USER_EMAIL");

        apiMapper.saveApi(apis);

        LoggingUtil.logTransactionStep(logKey, userEmail, "1. API 파일 저장 - apis: " + apis);

        Apis savedApi = apiMapper.findById(apis.getId());

        LoggingUtil.logTransactionStep(logKey, userEmail, "2. 저장한 API 파일 ID 반환 - apiId: " + savedApi.getId());

        // 프로젝트 정보 조회
        ProjectItems projectItem = projectService.getItemByItemId(apis.getItemId());
        Long projectId = projectItem.getProjectId();


        LoggingUtil.logTransactionStep(logKey, userEmail, "3. API 파일이 저장된 프로젝트 ID 조회 - projectId: " + projectId);

        // 프로젝트 참가자 목록 조회 및 알림 전송
        List<ProjectParticipants> projectParticipants = projectService.getParticipantsByProjectId(projectId);
        for (ProjectParticipants participant : projectParticipants) {
            notificationService.notifyProjectParticipants(participant.getUserId(), projectId, "API_SAVE");
        }

        LoggingUtil.logTransactionStep(logKey, userEmail, "4. 프로젝트 참가자 " + projectParticipants + "에게 API_SAVE 알림 전송 완료");

        // 같은 API를 사용 중인 사용자에게 알림 전송
        notifyApiUsers(projectId, savedApi);

        LoggingUtil.logTransactionStep(logKey, userEmail, "5. 같은 API를 사용 중인 사용자에게 알림 전송 완료");

        return savedApi;
    }

    /**
     * API 정보 조회
     *
     * @param itemId 조회할 API의 itemId
     * @return 조회된 API 객체
     */
    public Apis loadApi(Long itemId) {
        String logKey = MDC.get("LOG_KEY");
        String userEmail = MDC.get("USER_EMAIL");
        LoggingUtil.logTransactionStep(logKey, userEmail, "API " + itemId + "번 불러오기 완료");
        return apiMapper.loadApi(itemId);
    }

    /**
     * API 업데이트 및 관련 사용자 알림 전송
     *
     * @param apis 업데이트할 API 객체
     */
    @Transactional
    public void updateApi(Apis apis) {
        String logKey = MDC.get("LOG_KEY");
        String userEmail = MDC.get("USER_EMAIL");

        apiMapper.updateApi(apis);

        LoggingUtil.logTransactionStep(logKey, userEmail, "1. API 업데이트 완료 - apis: " + apis);
        Apis updatedApi = apiMapper.findById(apis.getId());
        LoggingUtil.logTransactionStep(logKey, userEmail, "2. 업데이트 된 API 파일 ID 조회 - apiId: " + updatedApi.getId());

        // 프로젝트 정보 조회
        ProjectItems projectItem = projectService.getItemByItemId(apis.getItemId());
        Long projectId = projectItem.getProjectId();
        LoggingUtil.logTransactionStep(logKey, userEmail, "3. 업데이트 된 API 파일이 있는 프로젝트 ID 조회 - projectId: " + projectId);

        // 프로젝트 참가자 목록 조회 및 알림 전송
        List<ProjectParticipants> projectParticipants = projectService.getParticipantsByProjectId(projectId);
        for (ProjectParticipants participant : projectParticipants) {
            notificationService.notifyProjectParticipants(participant.getUserId(), projectId, "API_UPDATE");
        }

        LoggingUtil.logTransactionStep(logKey, userEmail, "4. 프로젝트 참가자 " + projectParticipants + "에게 API_UPDATE 알림 전송 완료");

        // 같은 API를 사용 중인 사용자에게 알림 전송
        notifyApiUsers(projectId, updatedApi);
        LoggingUtil.logTransactionStep(logKey, userEmail, "5. 같은 API를 사용 중인 사용자에게 알림 전송 완료");
    }

    /**
     * 사용자가 API를 사용하기 시작할 때 호출
     * Key - 사용자 ID, Value - apiUsage (API 사용 정보)
     *
     * @param email 사용자 이메일
     * @param projectId 프로젝트 ID
     * @param itemId API itemId
     */
    public void updateUserApiUsage(String email, Long projectId, Long itemId) {
        String logKey = MDC.get("LOG_KEY");
        String userEmail = MDC.get("USER_EMAIL");

        User user = userMapper.findByEmail(email);
        String userKey = String.valueOf(user.getId());

        ApiUsage apiUsage = new ApiUsage(projectId, itemId, user.getEmail(), user.getProfileImage());
        LoggingUtil.logTransactionStep(logKey, userEmail, "1. API 사용 정보 객체 apiUsage 생성 - apiUsage: " + apiUsage);

        redisTemplate.opsForHash().put(userKey, "apiUsage", apiUsage);
        LoggingUtil.logTransactionStep(logKey, userEmail, "2. 유저 " + userKey + "번 " + apiUsage.getItemId() + " API 파일 사용 시작");

        sendProjectMessage(projectId, "API_SELECT");
        LoggingUtil.logTransactionStep(logKey, userEmail, "4. 프로젝트 " + projectId + "번 참가자들에게 API_SELECT 알림 전송 완료");
    }

    /**
     * 특정 프로젝트의 API 사용 중인 사용자 목록 조회
     *
     * @param projectId 프로젝트 ID
     * @return 프로젝트 내 API 사용자의 목록 (userId, ApiUsage 객체)
     */

    public Map<Long, ApiUsage> getUsersUsageByProjectId(Long projectId) {
        String logKey = MDC.get("LOG_KEY");
        String userEmail = MDC.get("USER_EMAIL");

        Map<Long, ApiUsage> result = new HashMap<>();

        // Redis에 저장된 모든 사용자 ID(Key) 가져오기
        Set<String> userKeys = redisTemplate.keys("*");
        LoggingUtil.logTransactionStep(logKey, userEmail, "1. Redis에 저장된 모든 사용자 ID(Key) 가져오기 - userKeys: " + userKeys);

        if (userKeys == null || userKeys.isEmpty()) {
            LoggingUtil.logTransactionStep(logKey, userEmail, "2. Redis에 사용자 ID가 존재하지 않아 빈 Map 반환");
            return result;
        }


        for (String userIdStr : userKeys) {
            Long userId = Long.valueOf(userIdStr);

            // 사용자별 Redis 데이터 가져오기
            ApiUsage apiUsage = (ApiUsage) hashOperations.get(userIdStr, "apiUsage");
            LoggingUtil.logTransactionStep(logKey, userEmail, "3. 사용자별 Redis 데이터 불러오기 -  userId: " + userId + ", apiUsage: " + apiUsage);

            // 사용자가 현재 조회하는 프로젝트의 API를 사용 중인지 확인
            if (apiUsage.getProjectId() != null && apiUsage.getProjectId().equals(projectId) && apiUsage != null) {
                result.put(userId, apiUsage);
                LoggingUtil.logTransactionStep(logKey, userEmail, "4. 사용자가 현재 조회하는 프로젝트의 API를 사용 중이면 result에 저장 - result: " + result);
            }
        }

        return result;
    }

    /**
     * 특정 사용자의 API 사용 정보 삭제
     *
     * @param projectId 프로젝트 ID
     * @param userId 사용자 ID
     */
    public void deleteUserApiUsage(Long projectId, Long userId) {
        String logKey = MDC.get("LOG_KEY");
        String userEmail = MDC.get("USER_EMAIL");

        String userKey = String.valueOf(userId);
        LoggingUtil.logTransactionStep(logKey, userEmail, "1. userId를 Redis에서 삭제 시 사용할 String 형식으로 변환");
        hashOperations.delete(userKey);
        LoggingUtil.logTransactionStep(logKey, userEmail, "2. Redis에서 현재 유저의 API 사용 정보 삭제 (projectId: " + projectId + ", userId: " + userId);
        sendProjectMessage(projectId, "API_SELECT");
        LoggingUtil.logTransactionStep(logKey, userEmail, "3. 현재 프로젝트를 사용 중인 사용자들에게 웹 소켓 알림 메시지 API_SELECT 전송");
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
        // Redis에서 해당 프로젝트의 API 사용 목록 조회
        Map<Long, ApiUsage> projectApiUsage = hashOperations.entries(String.valueOf(projectId));

        if (projectApiUsage != null && !projectApiUsage.isEmpty()) {
            // API를 사용 중인 모든 사용자에게 알림 전송
            projectApiUsage.forEach((userId, apiUsage) -> {
                if (apiUsage.getItemId().equals(api.getId())) {
                    notificationService.notifyApiMessage(userId, api);
                }
            });
        }
    }
}
