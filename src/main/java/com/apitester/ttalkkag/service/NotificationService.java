package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.Apis;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // 해당 프로젝트 참가자 모두에게 발송
    public void notifyProjectParticipants(Long projectId, String message) {
        messagingTemplate.convertAndSend("/topic/project/" + projectId, message);
    }

    // 해당 프로젝트 참가자 중 특정 유저에게만 발송
    public void notifyProjectParticipantsUserId(Long projectId, Long userId, String message) {
        messagingTemplate.convertAndSend("/topic/project/" + projectId + "/" + userId, message);
    }

    // 해당 프로젝트 참가자 중 특정 API를 사용하고 있는 모두에게 발송
    public void notifyProjectApi(Long projectId, Long apiId, Apis api) {
        messagingTemplate.convertAndSend("/topic/project/" + projectId + "/api/" + apiId, api);
    }
}
