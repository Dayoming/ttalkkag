package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.ApiHistory;
import com.apitester.ttalkkag.mapper.HistoryMapper;
import com.apitester.ttalkkag.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * API 호출 기록(History) 관리 서비스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description API 호출 기록 저장 및 조회 기능을 제공하는 서비스 클래스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryMapper apiHistoryMapper;
    private final UserMapper userMapper;

    /**
     * API 호출 기록 저장
     *
     * @param userEmail 사용자 이메일
     * @param history 저장할 API 호출 기록 객체
     */
    @Transactional
    public void saveHistory(String userEmail, ApiHistory history) {
        try {
            Long userId = userMapper.findByEmail(userEmail).getId();
            history.setUserId(userId);
            apiHistoryMapper.insertHistory(history);
            log.info("API 호출 기록 저장 완료: {}", history);
        } catch (Exception e) {
            log.error("API 호출 기록 저장 실패: {}", e.getMessage());
            throw new RuntimeException("Failed to save API history", e);
        }
    }

    /**
     * 사용자의 전체 API 호출 기록 조회
     *
     * @param userEmail 사용자 이메일
     * @return API 호출 기록 리스트
     */
    public List<ApiHistory> getAllHistories(String userEmail) {
        try {
            Long userId = userMapper.findByEmail(userEmail).getId();
            return apiHistoryMapper.getAllHistories(userId);
        } catch (Exception e) {
            log.error("API 호출 기록 조회 실패: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve API history", e);
        }
    }

    /**
     * 특정 기간 동안의 오류 발생 API 호출 기록 조회
     *
     * @param userEmail 사용자 이메일
     * @param start 조회 시작 날짜 (YYYY-MM-DD)
     * @param end 조회 종료 날짜 (YYYY-MM-DD)
     * @return 오류 발생 API 호출 기록 리스트
     */
    public List<ApiHistory> getErrorHistory(String userEmail, String start, String end) {
        try {
            Long userId = userMapper.findByEmail(userEmail).getId();
            return apiHistoryMapper.getErrorHistory(userId, start, end);
        } catch (Exception e) {
            log.error("오류 기록 조회 실패: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve error history", e);
        }
    }

    /**
     * 특정 프로젝트의 API 호출 기록 조회
     *
     * @param projectId 프로젝트 ID
     * @return API 호출 기록 리스트
     */
    public List<ApiHistory> getHistoriesByProjectId(Long projectId) {
        try {
            return apiHistoryMapper.getHistoriesByProjectId(projectId);
        } catch (Exception e) {
            log.error("프로젝트 API 기록 조회 실패: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve API history for project", e);
        }
    }

    /**
     * 특정 ID의 API 호출 기록 조회
     *
     * @param id API 호출 기록 ID
     * @return API 호출 기록 객체
     */
    public ApiHistory getHistoryById(Long id) {
        try {
            return apiHistoryMapper.getHistoryById(id);
        } catch (Exception e) {
            log.error("API 기록 조회 실패 (ID={}): {}", id, e.getMessage());
            throw new RuntimeException("Failed to retrieve API history by ID", e);
        }
    }
}