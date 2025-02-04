package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * 실시간 알림(Notification) 서비스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description WebSocket을 이용한 실시간 알림 메시지를 사용자 및 프로젝트 참여자에게 전송하는 서비스 클래스.
 */
@Slf4j
@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * NotificationService 생성자
     *
     * @param messagingTemplate WebSocket 메시징을 위한 SimpMessagingTemplate 객체
     */
    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 특정 사용자에게 WebSocket 메시지를 전송
     *
     * @param userId 메시지를 받을 사용자 ID
     * @param message 전송할 알림 메시지 객체
     */
    public void notifyUser(Long userId, NotificationMessage message) {
        try {
            String destination = "/topic/user/" + userId;
            messagingTemplate.convertAndSend(destination, message);
            log.info("알림 전송 성공 (User ID={}): {}", userId, message);
        } catch (Exception e) {
            log.error("알림 전송 실패 (User ID={}): {}", userId, e.getMessage());
        }
    }

    /**
     * 특정 프로젝트의 참여자에게 메시지를 전송
     *
     * @param userId 메시지를 받을 사용자 ID
     * @param projectId 해당 프로젝트 ID
     * @param messageContent 전송할 메시지 내용
     */
    public void notifyProjectParticipants(Long userId, Long projectId, String messageContent) {
        NotificationMessage message = new NotificationMessage("PROJECT", messageContent, projectId);
        notifyUser(userId, message);
    }

    /**
     * 특정 사용자에게 API 관련 메시지를 전송
     *
     * @param userId 메시지를 받을 사용자 ID
     * @param apiData API 관련 데이터
     */
    public void notifyApiMessage(Long userId, Object apiData) {
        NotificationMessage message = new NotificationMessage("API", "API_UPDATE", apiData);
        notifyUser(userId, message);
    }
}