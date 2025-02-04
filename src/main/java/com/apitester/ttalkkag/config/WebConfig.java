package com.apitester.ttalkkag.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 리소스 설정 클래스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description 정적 리소스(프로필 이미지) 핸들링을 위한 설정을 정의하는 클래스.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 정적 리소스 핸들러 추가
     * <p>
     * "/uploads/profiles/**" URL 패턴으로 요청이 들어오면
     * "C:/ttalkkag-dev/uploads/profiles/" 디렉터리에서 파일을 제공하도록 설정.
     *
     * @param registry 리소스 핸들러 레지스트리
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = "file:///C:/ttalkkag-dev/uploads/profiles/";

        registry.addResourceHandler("/uploads/profiles/**")
                .addResourceLocations(uploadDir);
    }
}
