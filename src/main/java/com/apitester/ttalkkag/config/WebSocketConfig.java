package com.apitester.ttalkkag.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정 클래스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description STOMP 기반 WebSocket 설정을 정의하는 클래스.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 메시지 브로커 구성
     * 클라이언트가 메시지를 수신할 때 "/topic" 경로를 사용하도록 설정.
     * 클라이언트가 서버로 메시지를 보낼 때 "/app" 접두어를 사용.
     *
     * @param config 메시지 브로커 레지스트리
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // 클라이언트로 보낼 경로
        config.setApplicationDestinationPrefixes("/app"); // 서버로 보낼 경로
    }

    /**
     * STOMP 엔드포인트 등록
     * 클라이언트가 "/ws" 엔드포인트를 통해 WebSocket 연결을 설정하도록 구성.
     * SockJS를 활성화하여 WebSocket 미지원 브라우저에서도 사용 가능하게 설정.
     *
     * @param registry STOMP 엔드포인트 레지스트리
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS(); // WebSocket 연결
    }
}