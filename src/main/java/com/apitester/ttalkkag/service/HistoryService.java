package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.ApiHistory;
import com.apitester.ttalkkag.mapper.HistoryMapper;
import com.apitester.ttalkkag.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class HistoryService {

    @Autowired
    private final HistoryMapper apiHistoryMapper;
    private final UserMapper userMapper;

    public void saveHistory(String userEmail, ApiHistory history) {
        Long userId = userMapper.findByEmail(userEmail).getId();
        history.setUserId(userId);
        apiHistoryMapper.insertHistory(history);
    }

    public List<ApiHistory> getAllHistories(String userEmail) {
        Long userId = userMapper.findByEmail(userEmail).getId();
        return apiHistoryMapper.getAllHistories(userId);
    }

    public List<ApiHistory> getErrorHistory(String userEmail, String start, String end) {
        Long userId = userMapper.findByEmail(userEmail).getId();
        return apiHistoryMapper.getErrorHistory(userId, start, end);
    }

    public List<ApiHistory> getHistoriesByProjectId(Long projectId) {
        return apiHistoryMapper.getHistoriesByProjectId(projectId);
    }

    public ApiHistory getHistoryById(Long id) {
        return apiHistoryMapper.getHistoryById(id);
    }
}
