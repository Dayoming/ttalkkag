package com.apitester.history.service;

import com.apitester.history.dto.ApiChangeHistory;
import com.apitester.history.mapper.ApiChangeHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * ApiChangeHistory 서비스 클래스
 *
 * @author 정다영
 * @date 2025-02-24
 * @description APi 변경 이력을 등록, 조회하는 클래스
 */
@Service
public class ApiChangeHistoryService {

    @Value("${history.user-recent-count}")
    private int recentCount;

    @Autowired
    private final ApiChangeHistoryMapper historyMapper;

    public ApiChangeHistoryService(ApiChangeHistoryMapper historyMapper) {
        this.historyMapper = historyMapper;
    }

    /**
     * 변경된 API 이력을 저장
     *
     * @param history API 변경 이력
     */
    public void saveApiChangeHistory(ApiChangeHistory history) {
        System.out.println(history);
        historyMapper.insertApiChangeHistory(history);
    }

    /**
     * 모든 API 변경 이력 불러오기.
     *
     * @return 모든 API 변경 이력
     */
    public List<ApiChangeHistory> getAllApiChangeHistory() {
        return historyMapper.getAllApiChangeHistory();
    }

    /**
     * API ID로 API 변경 이력을 조회
     *
     * @param apiId
     * @return apiId에 해당하는 API 변경 이력
     */
    public List<ApiChangeHistory> getApiChangeHistoryByApiId(Long apiId) {
        List<Long> userIds = historyMapper.getUserList();
        List<ApiChangeHistory> apiChangeHistories = new ArrayList<>();
        for (Long userId : userIds) {
            apiChangeHistories.addAll(historyMapper.getApiChangeHistoryByUserId(apiId, userId, recentCount));
        }
        return apiChangeHistories;
    }

    /**
     * User ID로 API 변경 이력을 조회
     *
     * @param apiId
     * @param userId
     * @return apiId와 userId에 해당하는 API 변경 이력
     */
    public List<ApiChangeHistory> getApiChangeHistoryByUserId(Long apiId, Long userId) {
        return historyMapper.getApiChangeHistoryByUserId(apiId, userId, recentCount);
    }
}
