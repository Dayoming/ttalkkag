package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.config.JwtTokenUtil;
import com.apitester.ttalkkag.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    /* 회원가입 이메일 확인 코드 전송 */
    @PostMapping("/send-code")
    public Map<String, Object> sendVerificationCode(@RequestBody Map<String, String> request) {
        return userService.sendVerificationCode(request);
    }

    /* 회원가입 */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> request) {
        return userService.register(request);
    }

    /* 로그인 */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        return userService.login(request);
    }

    @GetMapping("/oauth/kakao")
    public Map<String, Object> kakaoCallback(@RequestParam String code) {
        return userService.loginWithKakao(code);
    }

    @GetMapping("/oauth/google")
    public Map<String, Object> googleCallback(@RequestParam String code) {
        return userService.loginWithGoogle(code);
    }

    @GetMapping("/user-info")
    public Map<String, Object> userInfo(HttpServletRequest request) {
        return userService.getUserInfo(request);
    }

    @PostMapping("/refresh-token")
    public Map<String, Object> refreshAccessToken(@RequestBody Map<String, String> request) {
        return userService.refreshAccessToken(request);
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            boolean response = jwtTokenUtil.validateToken(token.replace("Bearer ", ""));
            return ResponseEntity.ok(Map.of("valid", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false, "error", "Invalid Token"));
        }
    }
}

