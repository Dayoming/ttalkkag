package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.Apis;
import com.apitester.ttalkkag.dto.NotificationMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // 사용자에게 메시지 발송
    public void notifyUser(Long userId, NotificationMessage message) {
        messagingTemplate.convertAndSend("/topic/user/" + userId, message);
    }

    // 프로젝트 메시지 발송
    public void notifyProjectParticipants(Long userId, Long projectId, String messageContent) {
        NotificationMessage message = new NotificationMessage("PROJECT", messageContent, projectId);
        notifyUser(userId, message);
    }

    // API 메시지 발송
    public void notifyApiMessage(Long userId, Object apiData) {
        NotificationMessage message = new NotificationMessage("API", "API_UPDATE", apiData);
        notifyUser(userId, message);
    }
}