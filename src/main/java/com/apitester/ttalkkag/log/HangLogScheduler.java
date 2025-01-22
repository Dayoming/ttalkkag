package com.apitester.ttalkkag.log;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@EnableScheduling
public class HangLogScheduler {

    // 1분마다 실행되는 작업
    @Scheduled(cron = "0 * * * * *") // 매분 0초에 실행
    public void logHangMessage() {
        // 현재 시간 포맷
        String currentTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 로그 메시지 출력
        System.out.println(currentTime + " Hang log");
    }
}