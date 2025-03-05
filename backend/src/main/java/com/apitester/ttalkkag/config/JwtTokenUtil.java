package com.apitester.ttalkkag.config;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT 토큰 유틸리티 클래스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description JWT 생성, 검증 및 파싱 기능을 제공하는 유틸리티 클래스.
 */
@Slf4j
@Component
public class JwtTokenUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * 액세스 토큰 생성
     *
     * @param email 사용자 이메일 (토큰 subject)
     * @return 생성된 JWT 액세스 토큰
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();
    }

    /**
     * 리프레시 토큰 생성
     *
     * @param email 사용자 이메일 (토큰 subject)
     * @return 생성된 JWT 리프레시 토큰
     */
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();
    }

    /**
     * JWT 토큰에서 이메일(subject) 추출
     *
     * @param token JWT 토큰
     * @return 이메일 (subject) 또는 null
     */
    public String getEmailFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secret.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JWT 토큰 유효성 검증
     *
     * @param token JWT 토큰
     * @return 유효한 경우 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret.getBytes())
                    .build()
                    .parseClaimsJws(token);
            log.info("JWT 토큰 유효성 검증 성공");
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT 토큰 만료됨: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 JWT 토큰: {}", e.getMessage());
        } catch (SecurityException | IllegalArgumentException e) {
            log.warn("JWT 서명 검증 실패 또는 잘못된 토큰: {}", e.getMessage());
        }
        return false;
    }
}

