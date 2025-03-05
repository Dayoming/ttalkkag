package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.ApiChangeHistory;
import com.apitester.ttalkkag.log.LoggingUtil;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class ExternalApiHistoryService {
    private final RestTemplate restTemplate;
    @Value("${external.api-base-url}")
    private String EXTERNAL_API_BASE_URL;

    public ExternalApiHistoryService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private String getJwtToken() {
        return MDC.get("JWT_TOKEN"); // MDC에서 JWT 토큰 가져오기
    }

    // 외부 REST API에서 전체 변경 이력 가져오기
    public List<ApiChangeHistory> getExternalApiHistory(Long apiId) {
        String logKey = MDC.get("LOG_KEY");
        String userEmail = MDC.get("USER_EMAIL");
        String apiReqUrl = EXTERNAL_API_BASE_URL + "/api/" + apiId.toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + getJwtToken());

        HttpEntity<String> request = new HttpEntity<>(headers);
        LoggingUtil.logTransactionStep(logKey, userEmail, "1. " + apiReqUrl + "에 전체 변경 이력 조회 요청");
        ResponseEntity<ApiChangeHistory[]> response = restTemplate.exchange(
                apiReqUrl, HttpMethod.GET, request, ApiChangeHistory[].class);
        LoggingUtil.logTransactionStep(logKey, userEmail, "2. 전체 변경 이력 조회 완료: " + response);
        return Arrays.asList(response.getBody());
    }

    // 외부 REST API에서 API 변경 이력 저장 요청 보내기
    public String saveExternalApiHistory(ApiChangeHistory history) {
        String logKey = MDC.get("LOG_KEY");
        String userEmail = MDC.get("USER_EMAIL");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + getJwtToken());

        HttpEntity<ApiChangeHistory> request = new HttpEntity<>(history, headers);
        LoggingUtil.logTransactionStep(logKey, userEmail, "1. " + EXTERNAL_API_BASE_URL + "에 변경 이력 저장 요청");
        LoggingUtil.logTransactionStep(logKey, userEmail, "1. headers: " + headers + ", request: " + request);
        ResponseEntity<String> response = restTemplate.postForEntity(EXTERNAL_API_BASE_URL, request, String.class);
        LoggingUtil.logTransactionStep(logKey, userEmail, "2. 변경 이력 저장 요청 완료: " + response);
        return response.getBody();
    }
}
