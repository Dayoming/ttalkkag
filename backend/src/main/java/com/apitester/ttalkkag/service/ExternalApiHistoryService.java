package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.ApiChangeHistory;
import com.apitester.ttalkkag.kafka.ApiChangeHistoryProducer;
import com.apitester.ttalkkag.kafka.ApiHistoryConsumer;
import com.apitester.ttalkkag.log.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ExternalApiHistoryService {

    private final ApiChangeHistoryProducer apiChangeHistoryProducer;
    private final ApiHistoryConsumer apiHistoryConsumer;

    @Value("${external.api-base-url}")
    private String EXTERNAL_API_BASE_URL;

    @Autowired
    public ExternalApiHistoryService(ApiChangeHistoryProducer apiChangeHistoryProducer, ApiHistoryConsumer apiHistoryConsumer) {
        this.apiChangeHistoryProducer = apiChangeHistoryProducer;
        this.apiHistoryConsumer = apiHistoryConsumer;
    }

    private String getJwtToken() {
        return MDC.get("JWT_TOKEN"); // MDC에서 JWT 토큰 가져오기
    }

    // 외부 REST API에서 전체 변경 이력 가져오기
    public List<ApiChangeHistory> getExternalApiHistory(Long apiId) throws InterruptedException {
        String logKey = MDC.get("LOG_KEY");
        String userEmail = MDC.get("USER_EMAIL");

        // Kafka로 요청 전송
        apiChangeHistoryProducer.sendApiHistoryRequest(getJwtToken(), apiId);
        LoggingUtil.logTransactionStep(logKey, userEmail, "1. Kafka로 변경 이력 조회 요청 완료");

        // Kafka 응답 대기 및 가져오기
        List<ApiChangeHistory> apiChangeHistories = apiHistoryConsumer.getApiHistories(getJwtToken(), apiId);
        LoggingUtil.logTransactionStep(logKey, userEmail, "2. apiId: " + apiId + "번 변경 이력 조회 데이터 읽어오기 완료");

        return apiChangeHistories;
    }

    // Kafka로 API 변경 이력 저장 요청 보내기
    @Async("asyncExecutor")
    @TransactionalEventListener
    public void saveExternalApiHistory(ApiChangeHistory history) {
        String logKey = MDC.get("LOG_KEY");
        String userEmail = MDC.get("USER_EMAIL");
        String jwtToken = getJwtToken();

        LoggingUtil.logTransactionStep(logKey, userEmail, "[Async] 현재 스레드: " + Thread.currentThread().getName());

        LoggingUtil.logTransactionStep(logKey, userEmail, "1. Kafka로 변경 이력 저장 요청");
        LoggingUtil.logTransactionStep(logKey, userEmail, "요청 토큰: " + jwtToken);
        LoggingUtil.logTransactionStep(logKey, userEmail, "변경 이력: " + history);
        apiChangeHistoryProducer.sendApiChangeHistory(history, jwtToken);
        LoggingUtil.logTransactionStep(logKey, userEmail, "2. 변경 이력 저장 요청 완료");

    }
}
