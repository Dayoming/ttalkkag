package com.apitester.history.kafka;

import com.apitester.history.dto.ApiChangeHistoryMessage;
import com.apitester.history.dto.JwtValidationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtValidationProducer {
    private final KafkaTemplate<String, JwtValidationRequest> kafkaTemplate;
    private final JwtValidationCache jwtValidationCache;

    public void sendJwtValidationRequest(String jwtToken, Object requestObject) {
        jwtValidationCache.storeRequest(jwtToken, requestObject); // 요청을 보낼 때 메시지 저장
        kafkaTemplate.send("jwt-validation-request", new JwtValidationRequest(jwtToken));
    }
}
