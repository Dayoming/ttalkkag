package com.apitester.ttalkkag.mapper;

import com.apitester.ttalkkag.dto.ApiHistory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HistoryMapper {
    void insertHistory(ApiHistory apiHistory);
    List<ApiHistory> getAllHistories(Long userId);
    List<ApiHistory> getErrorHistory(Long userId, String start, String end);
    List<ApiHistory> getHistoriesByProjectId(Long projectId);
    ApiHistory getHistoryById(Long id);
}
