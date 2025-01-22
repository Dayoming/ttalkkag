package com.apitester.ttalkkag.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DiskSpaceMonitor {

    private static final Logger logger = LoggerFactory.getLogger(DiskSpaceMonitor.class);
    private static final long THRESHOLD = 80; // 임계치: 80%

    @Scheduled(fixedRate = 300000) // 5분마다 실행
    public void monitorDiskSpace() {
        File root = new File("/");
        long totalSpace = root.getTotalSpace();
        long usableSpace = root.getUsableSpace();
        long usedSpace = totalSpace - usableSpace;

        // 사용량 퍼센트 계산
        long usagePercentage = (usedSpace * 100) / totalSpace;

        // 현재 디스크 상태 로그 기록
        logger.info("디스크 상태: 총 용량 = {} GB, 사용 가능 = {} GB, 사용량 = {}%",
                totalSpace / (1024 * 1024 * 1024),
                usableSpace / (1024 * 1024 * 1024),
                usagePercentage);

        if (usagePercentage >= THRESHOLD) {
            logger.warn("경고: 디스크 사용량이 {}%를 초과했습니다! 총 용량: {} GB, 사용 가능: {} GB",
                    usagePercentage,
                    totalSpace / (1024 * 1024 * 1024),
                    usableSpace / (1024 * 1024 * 1024));
            sendAlert("디스크 사용량 경고", String.format("사용량: %d%%, 남은 공간: %d GB",
                    usagePercentage, usableSpace / (1024 * 1024 * 1024)));
        }
    }

    private void sendAlert(String subject, String message) {
        logger.info("알림 전송: {} - {}", subject, message);
    }
}
