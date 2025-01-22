package com.apitester.ttalkkag.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    private static final String LOG_FILE_PATH = "monitoring/websocket-termination.log";

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
        if (headerAccessor != null) {
            String sessionId = headerAccessor.getSessionId();
            String disconnectTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 세션 종료 로그 작성
            String logMessage = String.format("Session ID: %s, Time: %s%n", sessionId, disconnectTime);
            try {
                Files.write(Paths.get(LOG_FILE_PATH), logMessage.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                logger.info("웹소켓 세션 종료: 세션 ID = {}, 종료 시간 = {}", sessionId, disconnectTime);
            } catch (Exception e) {
                logger.error("웹소켓 종료 로그 파일 쓰기 오류: {}", e.getMessage());
            }
        }
    }
}
