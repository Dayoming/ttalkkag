package com.apitester.ttalkkag.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 설정 클래스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description HTTP 요청을 처리하는 RestTemplate을 빈으로 등록하는 설정 클래스.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate 빈 등록
     *
     * @return RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

