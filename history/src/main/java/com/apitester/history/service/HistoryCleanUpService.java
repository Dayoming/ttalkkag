package com.apitester.history.service;

import com.apitester.history.mapper.ApiChangeHistoryMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 오래된 이력 삭제 스케줄러 클래스
 *
 * @author 정다영
 * @date 2025-02-24
 * @description 30일 이전 이력을 주기적으로 삭제하는 서비스 클래스.
 */
@Service
public class HistoryCleanUpService {
    private final ApiChangeHistoryMapper historyMapper;

    public HistoryCleanUpService(ApiChangeHistoryMapper historyMapper) {
        this.historyMapper = historyMapper;
    }


    /**
     * 오래된 이력 삭제
     */
    @Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시 실행
    public void cleanupOldHistory() {
        int deletedRows = historyMapper.deleteOldHistory();
        System.out.println("30일 이전 이력 " + deletedRows + "개 삭제 완료");
    }
}
