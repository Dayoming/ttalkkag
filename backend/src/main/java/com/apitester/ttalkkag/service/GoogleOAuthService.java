package com.apitester.ttalkkag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Google OAuth2 인증 서비스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description Google OAuth2 인증을 처리하고, 액세스 토큰을 받아 사용자 정보를 가져오는 서비스 클래스.
 */
@Slf4j
@Service
public class GoogleOAuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUrl;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String userInfoUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Google OAuth2 인증 코드로 액세스 토큰을 요청
     *
     * @param code OAuth2 인증 코드
     * @return 액세스 토큰 (Access Token)
     */
    public String getAccessToken(String code) {
        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 바디 설정
        String body = "code=" + code +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&redirect_uri=" + redirectUri +
                "&grant_type=authorization_code";

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            // Google OAuth 서버로부터 액세스 토큰 요청
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("access_token")) {
                log.info("Google OAuth2 Access Token 요청 성공");
                return (String) responseBody.get("access_token");
            } else {
                log.error("Google OAuth2 Access Token 요청 실패: 응답 없음");
                throw new RuntimeException("Google OAuth2 Access Token 요청 실패");
            }
        } catch (Exception e) {
            log.error("Google OAuth2 Access Token 요청 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("Failed to get access token from Google OAuth2", e);
        }
    }

    /**
     * 액세스 토큰을 이용하여 Google 사용자 정보를 가져옴
     *
     * @param accessToken Google OAuth2 액세스 토큰
     * @return 사용자 정보 (이메일, 이름 등)
     */
    public Map<String, Object> getUserInfo(String accessToken) {
        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            // 사용자 정보 요청
            ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);
            Map<String, Object> userInfo = response.getBody();

            if (userInfo != null) {
                log.info("Google OAuth2 사용자 정보 요청 성공: {}", userInfo);
                return userInfo;
            } else {
                log.error("Google OAuth2 사용자 정보 요청 실패: 응답 없음");
                throw new RuntimeException("Google OAuth2 사용자 정보 요청 실패");
            }
        } catch (Exception e) {
            log.error("Google OAuth2 사용자 정보 요청 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("Failed to get user info from Google OAuth2", e);
        }
    }
}

