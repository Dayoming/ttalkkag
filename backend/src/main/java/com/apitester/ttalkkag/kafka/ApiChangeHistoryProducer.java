package com.apitester.ttalkkag.kafka;

import com.apitester.ttalkkag.dto.ApiChangeHistory;
import com.apitester.ttalkkag.dto.ApiChangeHistoryMessage;
import com.apitester.ttalkkag.dto.ApiHistoryExternalRequest;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ApiChangeHistoryProducer {
    private final KafkaTemplate<String, ApiChangeHistoryMessage> kafkaTemplate;
    private final KafkaTemplate<String, ApiHistoryExternalRequest> apiHistoryRequestTemplate;

    public void sendApiChangeHistory(ApiChangeHistory history, String jwtToken) {
        ApiChangeHistoryMessage message = new ApiChangeHistoryMessage(jwtToken, history);
        kafkaTemplate.send("api-change-events", message);
    }

    public void sendApiHistoryRequest(String jwtToken, Long apiId) {
        ApiHistoryExternalRequest request = new ApiHistoryExternalRequest(jwtToken, apiId);
        apiHistoryRequestTemplate.send("api-history-request", request);
    }
}
