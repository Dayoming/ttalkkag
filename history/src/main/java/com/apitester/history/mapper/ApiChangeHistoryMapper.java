package com.apitester.history.mapper;

import com.apitester.history.dto.ApiChangeHistory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ApiChangeHistoryMapper {
    void insertApiChangeHistory(ApiChangeHistory history);
    List<ApiChangeHistory> getAllApiChangeHistory();
    List<ApiChangeHistory> getApiChangeHistoryByApiId(Long apiId);
    List<ApiChangeHistory> getApiChangeHistoryByUserId(Long apiId, Long userId, int recentCount);
    List<Long> getUserList();
    int deleteOldHistory();
}
