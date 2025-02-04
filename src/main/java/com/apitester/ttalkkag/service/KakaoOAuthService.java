package com.apitester.ttalkkag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 카카오 OAuth2 인증 서비스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description 카카오 OAuth2 인증을 처리하고, 액세스 토큰을 받아 사용자 정보를 가져오는 서비스 클래스.
 */
@Slf4j
@Service
public class KakaoOAuthService {

    private final String CLIENT_ID; // 카카오 REST API 키
    private final String REDIRECT_URI;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 카카오 OAuth2 서비스 생성자
     *
     * @param clientId 카카오 OAuth2 클라이언트 ID
     * @param redirectUri 카카오 OAuth2 리디렉션 URI
     */
    public KakaoOAuthService(
            @Value("${spring.security.oauth2.client.registration.kakao.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}") String redirectUri) {
        this.CLIENT_ID = clientId;
        this.REDIRECT_URI = redirectUri;
    }

    /**
     * 카카오 OAuth2 인증 코드로 액세스 토큰을 요청
     *
     * @param code 카카오에서 반환한 OAuth2 인증 코드
     * @return 액세스 토큰 (Access Token)
     */
    public String getAccessToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        // 요청 바디 설정
        String requestBody = "grant_type=authorization_code"
                + "&client_id=" + CLIENT_ID
                + "&redirect_uri=" + REDIRECT_URI
                + "&code=" + code;

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 엔터티 생성
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        try {
            // 카카오 OAuth 서버로부터 액세스 토큰 요청
            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("access_token")) {
                log.info("카카오 OAuth2 Access Token 요청 성공");
                return (String) responseBody.get("access_token");
            } else {
                log.error("카카오 OAuth2 Access Token 요청 실패: 응답 없음");
                throw new RuntimeException("카카오 OAuth2 Access Token 요청 실패");
            }
        } catch (Exception e) {
            log.error("카카오 OAuth2 Access Token 요청 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("Failed to get access token from Kakao OAuth2", e);
        }
    }

    /**
     * 액세스 토큰을 이용하여 카카오 사용자 정보를 가져옴
     *
     * @param accessToken 카카오 OAuth2 액세스 토큰
     * @return 사용자 정보 (이메일, 닉네임 등)
     */
    public Map<String, Object> getUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        // HTTP 요청 엔터티 생성
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            // 사용자 정보 요청
            ResponseEntity<Map> response = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            Map<String, Object> userInfo = response.getBody();

            if (userInfo != null) {
                log.info("카카오 OAuth2 사용자 정보 요청 성공: {}", userInfo);
                return userInfo;
            } else {
                log.error("카카오 OAuth2 사용자 정보 요청 실패: 응답 없음");
                throw new RuntimeException("카카오 OAuth2 사용자 정보 요청 실패");
            }
        } catch (Exception e) {
            log.error("카카오 OAuth2 사용자 정보 요청 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("Failed to get user info from Kakao OAuth2", e);
        }
    }
}
