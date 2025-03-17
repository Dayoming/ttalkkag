package com.apitester.history.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class JwtValidationService {
    private final RestTemplate restTemplate;

    @Value("${history.auth-url}")
    private String TTALKKAG_AUTH_URL;


    public JwtValidationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean validateJwtToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<String> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    TTALKKAG_AUTH_URL, HttpMethod.POST, request, Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();
                if (body != null && body.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) body.get("data");
                    return Boolean.TRUE.equals(data.get("valid"));
                }
            }
            return false;
        } catch (HttpClientErrorException.Unauthorized e) {
            return false;
        }
    }
}
