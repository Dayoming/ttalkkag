package com.apitester.ttalkkag.config;

import com.apitester.ttalkkag.exception.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT 인증 필터
 *
 * @author 정다영
 * @date 2025-02-01
 * @description JWT 토큰을 검증하고 인증 정보를 설정하는 필터 클래스.
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;
    private final SecretKey secretKey;

    /**
     * JwtAuthenticationFilter 생성자
     *
     * @param secretKey JWT 검증을 위한 SecretKey
     */
    public JwtAuthenticationFilter(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * 요청 필터링을 통해 JWT 검증 및 인증 정보 설정
     *
     * @param request     HTTP 요청
     * @param response    HTTP 응답
     * @param filterChain 필터 체인
     * @throws ServletException 필터 예외
     * @throws IOException      입출력 예외
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7); // "Bearer " 이후의 JWT 추출

            try {
                if (validateToken(token)) {
                    UsernamePasswordAuthenticationToken auth = getAuthentication(token);
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    log.warn("유효하지 않은 JWT 토큰");
                    throw new JwtAuthenticationException("Invalid JWT token");
                }
            } catch (ExpiredJwtException e) { // JWT 토큰 만료 시 401 Unauthorized 반환
                log.error("JWT 토큰 만료: {}", e.getMessage());
                throw new JwtAuthenticationException("JWT token has expired");
            } catch (JwtException e) {
                log.error("JWT 인증 실패: {}", e.getMessage());
                throw new JwtAuthenticationException("Invalid JWT token");
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * JWT 검증 메서드
     *
     * @param token JWT 토큰
     * @return 유효하면 true 반환
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey) // 서명 검증
                    .build()
                    .parseClaimsJws(token); // 파싱 및 유효성 검사
            return true;
        } catch (JwtException e) {
            log.warn("JWT 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * JWT를 기반으로 사용자 인증 객체 생성
     *
     * @param token JWT 토큰
     * @return 인증 객체 (UsernamePasswordAuthenticationToken)
     */
    private UsernamePasswordAuthenticationToken getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        String username = claims.getSubject();

        // 기본 권한 추가
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    /**
     * JWT 비밀 키 생성 (Base64 인코딩된 값으로부터 SecretKey 생성)
     *
     * @return SecretKey 객체
     */
    @Bean
    public SecretKey secretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * JwtAuthenticationFilter 빈 등록
     *
     * @param secretKey SecretKey 객체
     * @return JwtAuthenticationFilter 인스턴스
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(SecretKey secretKey) {
        return new JwtAuthenticationFilter(secretKey);
    }
}