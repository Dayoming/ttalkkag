package com.apitester.ttalkkag.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class KakaoOAuthService {
    private final String CLIENT_ID; // 카카오 REST API 키
    private final String REDIRECT_URI;

    public KakaoOAuthService(
            @Value("${spring.security.oauth2.client.registration.kakao.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}") String redirectUri) {
        this.CLIENT_ID = clientId;
        this.REDIRECT_URI = redirectUri;
    }

    public String getAccessToken(String code) {
        // 1. 카카오 토큰 요청 URL
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        // 2. 요청 파라미터 설정
        String requestBody = "grant_type=authorization_code"
                + "&client_id=" + CLIENT_ID
                + "&redirect_uri=" + REDIRECT_URI
                + "&code=" + code;

        // 3. HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        // 4. 요청 엔터티 생성
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        // 5. RestTemplate로 POST 요청
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                request,
                Map.class
        );

        // 6. 응답에서 Access Token 추출
        Map<String, Object> responseBody = response.getBody();
        return (String) responseBody.get("access_token");
    }

    public Map<String, Object> getUserInfo(String accessToken) {
        String url = "https://kapi.kakao.com/v2/user/me";

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        // HTTP 요청 엔터티 생성
        HttpEntity<String> request = new HttpEntity<>(headers);

        // RestTemplate으로 요청
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                Map.class
        );

        return response.getBody(); // 사용자 정보 반환
    }
}
