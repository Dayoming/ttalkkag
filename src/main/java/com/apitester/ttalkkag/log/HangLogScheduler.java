package com.apitester.ttalkkag.log;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 정기 로그 출력 스케줄러
 *
 * @author 정다영
 * @date 2025-02-01
 * @description 매분 0초마다 실행되어 "Hang log" 메시지를 출력하는 스케줄러 클래스.
 */
@Component
@EnableScheduling
public class HangLogScheduler {

    /**
     * 매분 0초마다 실행되는 작업
     * 현재 시간을 포맷하여 "Hang log" 메시지를 출력
     * 로그를 통해 주기적인 실행 확인 가능.
     */
    @Scheduled(cron = "0 * * * * *") // 매분 0초에 실행
    public void logHangMessage() {
        // 현재 시간 포맷
        String currentTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 로그 메시지 출력
        System.out.println(currentTime + " Hang log");
    }
}