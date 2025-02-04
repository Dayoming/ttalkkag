package com.apitester.ttalkkag.config;

import com.apitester.ttalkkag.service.CustomOAuth2UserService;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import java.util.List;

/**
 * Spring Security 설정 클래스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description JWT 기반 인증, OAuth2 로그인, CORS 설정, 비밀번호 인코딩 등의 보안 설정을 관리하는 클래스.
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${cors.allowed.origin}")
    private String allowedOrigin;

    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * JWT 인증 필터 Bean 등록
     *
     * @return JwtAuthenticationFilter 인스턴스
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        SecretKey secret = Keys.hmacShaKeyFor(secretKey.getBytes());
        return new JwtAuthenticationFilter(secret);
    }

    /**
     * Spring Security 필터 체인 설정
     *
     * @param http                   HttpSecurity 객체
     * @param jwtAuthenticationFilter JWT 인증 필터
     * @param customOAuth2UserService OAuth2 사용자 서비스
     * @return 설정된 SecurityFilterChain 인스턴스
     * @throws Exception 예외 발생 시
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter,
                                    CustomOAuth2UserService customOAuth2UserService) throws Exception {

        http
                // 인증/인가 설정
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        .requestMatchers("/api/auth/**", "/error", "/oauth/**", "/ws/**", "/topic/**", "/api/test/**", "/uploads/**").permitAll()  // 회원가입, 로그인은 인증 없이 접근 가능
                        .anyRequest().authenticated())  // 인증 필요
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // CORS 설정
                .formLogin(AbstractHttpConfigurer::disable)
                .logout((logout) -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // 필터 추가
                .oauth2Login(oauth -> oauth.loginPage("/login")
                        .defaultSuccessUrl("/test-api")
                        .failureUrl("/login")
                        .userInfoEndpoint(Customizer.withDefaults()) // Deprecated 사용 제거
                        .userInfoEndpoint(endpoint -> endpoint.userService(customOAuth2UserService))); // Bean으로 등록된 CustomOAuth2UserService 사용
        return http.build();
    }

    /**
     * CORS 설정 Bean 등록
     *
     * @return CorsConfigurationSource 인스턴스
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigin));  // Vue.js 개발 서버 도메인
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "PUT", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));  // 모든 헤더 허용
        configuration.setAllowCredentials(true);  // 자격 증명을 허용하여 쿠키가 전송될 수 있도록 설정

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    /**
     * 비밀번호 인코더 Bean 등록
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 인증 관리자 Bean 등록
     *
     * @param authenticationConfiguration 인증 구성 객체
     * @return AuthenticationManager 인스턴스
     * @throws Exception 예외 발생 시
     */
    @Bean
    AuthenticationManager authenticationManger(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}