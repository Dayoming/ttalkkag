package com.apitester.ttalkkag.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 디스크 공간 모니터링 클래스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description 시스템의 디스크 사용량을 정기적으로 점검하여 임계치를 초과하면 경고를 로그로 출력하고 알림을 전송
 */
@Component
public class DiskSpaceMonitor {

    private static final Logger logger = LoggerFactory.getLogger(DiskSpaceMonitor.class);
    private static final long THRESHOLD = 80; // 임계치: 80%

    /**
     * 디스크 사용량 모니터링
     * 5분마다 실행되며, 디스크 사용량이 설정된 임계치(80%)를 초과할 경우 경고를 출력합니다.
     */
    @Scheduled(fixedRate = 300000)
    public void monitorDiskSpace() {
        File root = new File("/");
        long totalSpace = root.getTotalSpace();
        long usableSpace = root.getUsableSpace();
        long usedSpace = totalSpace - usableSpace;

        // 사용량 퍼센트 계산
        long usagePercentage = (usedSpace * 100) / totalSpace;

        if (usagePercentage >= THRESHOLD) {
            logger.warn("경고: 디스크 사용량이 {}%를 초과했습니다! 총 용량: {} GB, 사용 가능: {} GB",
                    usagePercentage,
                    totalSpace / (1024 * 1024 * 1024),
                    usableSpace / (1024 * 1024 * 1024));
            sendAlert("디스크 사용량 경고", String.format("사용량: %d%%, 남은 공간: %d GB",
                    usagePercentage, usableSpace / (1024 * 1024 * 1024)));
        }
    }

    /**
     * 경고 알림 전송 (추후 이메일, SMS, Slack 등으로 확장 가능)
     *
     * @param subject 알림 제목
     * @param message 알림 내용
     */
    private void sendAlert(String subject, String message) {
        logger.info("알림 전송: {} - {}", subject, message);
    }
}
