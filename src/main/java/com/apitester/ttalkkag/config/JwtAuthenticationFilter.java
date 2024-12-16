package com.apitester.ttalkkag.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;
    private final SecretKey secretKey;

    public JwtAuthenticationFilter(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        System.out.println("Authorization header: " + header);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                if (validateToken(token)) {
                    UsernamePasswordAuthenticationToken auth = getAuthentication(token);
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    throw new RuntimeException("Invalid JWT token");
                }
            } catch (ExpiredJwtException e) { // JWT 토큰 만료 시 401 Unauthorized 반환
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("JWT token has expired");
                return;
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT token");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }


    // JWT 검증 메서드
    public boolean validateToken(String token) {
        Jwts.parserBuilder()
                .setSigningKey(secretKey) // 서명 검증
                .build()
                .parseClaimsJws(token); // 파싱 및 유효성 검사
        return true; // 토큰이 유효하면 true
    }

    // 인증 객체 생성
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

    @Bean
    public SecretKey secretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret); // JWT 비밀 키 (Base64로 인코딩된 값)
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(SecretKey secretKey) {
        return new JwtAuthenticationFilter(secretKey);
    }
}