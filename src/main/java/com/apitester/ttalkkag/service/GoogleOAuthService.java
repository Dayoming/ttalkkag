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
public class GoogleOAuthService {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getAccessToken(String code) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        String body = "code=" + code +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&redirect_uri=" + redirectUri +
                "&grant_type=authorization_code";

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
        Map<String, Object> responseBody = response.getBody();
        return (String) responseBody.get("access_token");
    }

    public Map<String, Object> getUserInfo(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);
        return response.getBody();
    }
}

