package com.apitester.ttalkkag.kafka;

import com.apitester.ttalkkag.dto.ApiChangeHistory;
import com.apitester.ttalkkag.dto.ApiHistoryExternalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ApiHistoryConsumer {
    private final ConcurrentHashMap<String, Map<Long, ApiHistoryExternalResponse>> responseCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CountDownLatch> latchMap = new ConcurrentHashMap<>();


    @KafkaListener(topics = "api-history-response", groupId = "history-group")
    public void consumeApiHistoryResponse(ApiHistoryExternalResponse response) {
        System.out.println("Received API History Response: " + response);

        // JWT 토큰을 키로 하는 API ID 별 응답 저장
        responseCache.computeIfAbsent(response.getJwtToken(), k -> new ConcurrentHashMap<>())
                .put(response.getApiId(), response);

        // 요청을 대기 중인 스레드 해제
        String key = response.getJwtToken() + "_" + response.getApiId();
        if (latchMap.containsKey(key)) {
            latchMap.get(key).countDown();
        }
    }

    public List<ApiChangeHistory> getApiHistories(String token, Long apiId) throws InterruptedException {
        String key = token + "_" + apiId;
        CountDownLatch latch = new CountDownLatch(1);
        latchMap.put(key, latch);

        // 최대 5초 대기 (응답이 도착할 때까지)
        latch.await(5, TimeUnit.SECONDS);

        // 응답 가져오기
        ApiHistoryExternalResponse apiHistoryExternalResponse =
                responseCache.getOrDefault(token, new ConcurrentHashMap<>()).get(apiId);

        return apiHistoryExternalResponse != null ? apiHistoryExternalResponse.getHistories() : null;
    }
}
