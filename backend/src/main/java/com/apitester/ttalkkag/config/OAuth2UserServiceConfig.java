package com.apitester.ttalkkag.config;

import com.apitester.ttalkkag.mapper.UserMapper;
import com.apitester.ttalkkag.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OAuth2 사용자 서비스 설정 클래스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description OAuth2 사용자 인증을 처리하는 CustomOAuth2UserService를 빈으로 등록하는 설정 클래스.
 */
@Configuration
public class OAuth2UserServiceConfig {

    private final UserMapper userMapper;

    /**
     * OAuth2UserServiceConfig 생성자
     *
     * @param userMapper 사용자 정보를 조회하는 UserMapper
     */
    public OAuth2UserServiceConfig(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * CustomOAuth2UserService 빈 등록
     *
     * @return CustomOAuth2UserService 인스턴스
     */
    @Bean
    public CustomOAuth2UserService customOAuth2UserService() {
        return new CustomOAuth2UserService(userMapper);
    }
}

