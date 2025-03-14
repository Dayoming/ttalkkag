package com.apitester.history.kafka;

import com.apitester.history.dto.*;
import com.apitester.history.mapper.ApiChangeHistoryMapper;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class ApiChangeHistoryConsumer {
    private final KafkaTemplate<String, ApiHistoryExternalResponse> kafkaTemplate;
    private final ApiChangeHistoryMapper apiChangeHistoryMapper;
    private final JwtValidationProducer jwtValidationProducer;
    private final JwtValidationCache jwtValidationCache;

    @KafkaListener(topics = "api-change-events", groupId = "history-group")
    public void consumeApiChangeHistory(ApiChangeHistoryMessage message) {
        System.out.println("Received API Change History: " + message);

        // JWT 토큰 검증 요청 전송
        jwtValidationProducer.sendJwtValidationRequest(message.getJwtToken(), message);
    }

    @KafkaListener(topics = "jwt-validation-response", groupId = "history-group")
    public void consumeJwtValidationResponse(JwtValidationResponse response) {
        System.out.println("Received JWT Validation Response: " + response);

        if (response.isValid()) {
            System.out.println("JWT is valid. Saving API Change History to DB.");
            // JWT가 유효하면 해당 API 변경 이력을 저장
            Object originalRequest = jwtValidationCache.getRequest(response.getJwtToken(), Object.class);

            // API 변경 이력 저장 요청인 경우
            if (originalRequest instanceof ApiChangeHistoryMessage) {
                // API 변경 이력 저장
                ApiChangeHistoryMessage message = (ApiChangeHistoryMessage) originalRequest;
                apiChangeHistoryMapper.insertApiChangeHistory(message.getHistory());
            } else if (originalRequest instanceof ApiHistoryExternalRequest) { // API 변경 이력 조회 요청인 경우
                ApiHistoryExternalRequest request = (ApiHistoryExternalRequest) originalRequest;
                List<ApiChangeHistory> apiChangeHistories = apiChangeHistoryMapper.getApiChangeHistoryByApiId(request.getApiId());
                ApiHistoryExternalResponse apiHistoryExternalResponse = new ApiHistoryExternalResponse(
                        response.getJwtToken(), request.getApiId(), response.isValid(), apiChangeHistories);
                // 응답 메시지 생성 후 kafka에 발행
                kafkaTemplate.send("api-history-response", apiHistoryExternalResponse);
            }
        } else {
            System.out.println("JWT is invalid.");
        }
    }

    @KafkaListener(topics = "api-history-request", groupId = "history-group")
    public void consumeApiHistoryRequest(ApiHistoryExternalRequest request) {
        System.out.println("Received API History Request: " + request);

        // JWT 검증 수행
        jwtValidationProducer.sendJwtValidationRequest(request.getJwtToken(), request);
    }
}
