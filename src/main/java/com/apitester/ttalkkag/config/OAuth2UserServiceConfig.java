package com.apitester.ttalkkag.config;

import com.apitester.ttalkkag.mapper.UserMapper;
import com.apitester.ttalkkag.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OAuth2UserServiceConfig {

    private final UserMapper userMapper;

    public OAuth2UserServiceConfig(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Bean
    public CustomOAuth2UserService customOAuth2UserService() {
        return new CustomOAuth2UserService(userMapper);
    }
}

