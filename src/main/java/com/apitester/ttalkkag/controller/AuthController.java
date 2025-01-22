package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.config.JwtTokenUtil;
import com.apitester.ttalkkag.dto.User;
import com.apitester.ttalkkag.mapper.UserMapper;
import com.apitester.ttalkkag.service.EmailService;
import com.apitester.ttalkkag.service.GoogleOAuthService;
import com.apitester.ttalkkag.service.KakaoOAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@Transactional
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final EmailService emailService;
    private final KakaoOAuthService kakaoOAuthService;
    private final GoogleOAuthService googleOAuthService;
    private final JwtTokenUtil jwtTokenUtil;

    private final Map<String, String> verificationCodes = new HashMap<>();

    /* 회원가입 이메일 확인 코드 전송 */
    @PostMapping("/send-code")
    public Map<String, Object> sendVerificationCode(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        String email = request.get("email");

        if (userMapper.findByEmail(email) != null) {
            response.put("errorMessage", "이미 존재하는 회원입니다.");
            return response;
        }

        String code = String.valueOf(new Random().nextInt(999999));
        response.put("message", "코드가 " + email + "로 전송되었습니다.");
        verificationCodes.put(email, code);
        emailService.sendVerificationCode(email, code);
        return response;
    }

    /* 회원가입 */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String email = request.get("email");
        String password = request.get("password");
        String code = request.get("code");

        if (!verificationCodes.containsKey(email) || !verificationCodes.get(email).equals(code)) {
            response.put("errorMessage", "입력하신 이메일로 전송된 코드와 일치하지 않습니다.");
            return response;
        }

        if (userMapper.findByEmail(email) != null) {
            response.put("errorMessage", "이미 가입되어 있는 계정입니다.");
            return response;
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        user.setVerified(false);
        user.setAutoSaveUse(false);
        user.setAutoSaveTime(60);
        user.setAutoSaveTerm(5);
        user.setShowResponse(false);
        user.setProfileImage("/uploads/profiles/profile-default-icon.png");
        userMapper.insertUser(user);
        verificationCodes.remove(email);
        response.put("message", "회원가입이 완료되었습니다. 로그인 페이지로 이동합니다.");
        return response;
    }

    /* 로그인 */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String email = request.get("email");
        String password = request.get("password");

        User user = userMapper.findByEmail(email);

        if (user != null && user.getSocialProvider() != null) {
            response.put("errorMessage", "해당 계정은 소셜 로그인 계정입니다. 다시 확인해 주세요.");
            return response;
        }

        if (user == null || !new BCryptPasswordEncoder().matches(password, user.getPassword())) {
            response.put("errorMessage", "이메일이나 비밀번호가 일치하지 않습니다. 다시 확인해 주세요.");
            return response;
        }

        // 액세스 토큰 발급
        String accessToken = jwtTokenUtil.generateToken(email);

        // 리프레시 토큰 발급
        String refreshToken = jwtTokenUtil.generateRefreshToken(email);

        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);

        // 최초 로그인 시
        if (user.isVerified()) {
            response.put("verified", true);
        }

        return response;
    }

    @GetMapping("/oauth/kakao")
    public Map<String, Object> kakaoCallback(@RequestParam String code) {
        // 1. Access Token 요청
        String accessToken = kakaoOAuthService.getAccessToken(code);
        Map<String, Object> response = new HashMap<>();

        // 2. 사용자 정보 요청
        Map<String, Object> userInfo = kakaoOAuthService.getUserInfo(accessToken);

        String email = (String) ((Map<String, Object>) userInfo.get("kakao_account")).get("email");
        String nickname = (String) ((Map<String, Object>) ((Map<String, Object>) userInfo.get("kakao_account")).get("profile")).get("nickname");
        String profileImageUrl = (String) ((Map<String, Object>) ((Map<String, Object>) userInfo.get("kakao_account")).get("profile")).get("profile_image_url");

        // 3. 사용자 정보 저장 또는 조회
        User user = userMapper.findByEmail(email);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setVerified(false);
            user.setAutoSaveUse(false);
            user.setAutoSaveTime(60);
            user.setAutoSaveTerm(5);
            user.setShowResponse(false);
            user.setSocialProvider("kakao");
            user.setProfileImage(profileImageUrl);
            userMapper.insertSocialUser(user); // 새 사용자 저장
        }

        // 4. JWT 발급
        String jwtAccessToken = jwtTokenUtil.generateToken(email);
        String jwtRefreshToken = jwtTokenUtil.generateRefreshToken(email);

        // 5. 응답 반환
        response.put("accessToken", jwtAccessToken);
        response.put("refreshToken", jwtRefreshToken);
        response.put("nickname", nickname);
        response.put("email", email);
        response.put("verified", user.isVerified());
        response.put("profileImageUrl", profileImageUrl);

        return response;
    }

    @GetMapping("/oauth/google")
    public Map<String, Object> googleCallback(@RequestParam String code) {
        try {
            // 1. Access Token 요청
            String accessToken = googleOAuthService.getAccessToken(code);

            // 2. 사용자 정보 요청
            Map<String, Object> userInfo = googleOAuthService.getUserInfo(accessToken);

            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");
            String profileImageUrl = (String) userInfo.get("picture");

            // 3. 사용자 정보 저장 또는 조회
            User user = userMapper.findByEmail(email);

            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setVerified(false);
                user.setAutoSaveUse(false);
                user.setAutoSaveTime(60);
                user.setAutoSaveTerm(5);
                user.setShowResponse(false);
                user.setSocialProvider("google");
                user.setProfileImage(profileImageUrl);
                userMapper.insertSocialUser(user); // 새 사용자 저장
            }

            // 4. JWT 발급
            String jwtAccessToken = jwtTokenUtil.generateToken(email);
            String jwtRefreshToken = jwtTokenUtil.generateRefreshToken(email);

            // 5. 응답 반환
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", jwtAccessToken);
            response.put("refreshToken", jwtRefreshToken);
            response.put("name", name);
            response.put("email", email);
            response.put("verified", user.isVerified());
            response.put("profileImageUrl", profileImageUrl);

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("errorMessage", "Google 로그인 처리 중 문제가 발생했습니다.");
            return response;
        }
    }

    /* 로그아웃 */
    @PostMapping("/logout")
    public Map<String, Object> logout() {
        Map<String, Object> response = new HashMap<>();
        return response;
    }

    @GetMapping("/user-info")
    public Map<String, Object> userInfo(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String email = jwtTokenUtil.getEmailFromToken(token); // JWT에서 이메일 추출
                if (email == null) {
                    response.put("errorMessage", "이메일을 불러오지 못했습니다.");
                    return response;
                }
                response.put("email", email);
                return response;
            } catch (Exception e) {
                response.put("errorMessage", "토큰 처리 중 오류가 발생했습니다.");
                return response;
            }
        }
        response.put("errorMessage", "Authorization 헤더가 존재하지 않습니다.");
        return response;
    }

    @PostMapping("/refresh-token")
    public Map<String, Object> refreshAccessToken(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String refreshToken = request.get("refreshToken");

        try {
            // 리프레시 토큰 검증
            String email = jwtTokenUtil.getEmailFromToken(refreshToken);

            // 토큰이 유효한지 확인
            if (email == null || !jwtTokenUtil.validateToken(refreshToken)) {
                response.put("errorMessage", "유효하지 않은 리프레시 토큰입니다.");
                return response;
            }

            // 사용자 이메일로 사용자 정보 확인
            User user = userMapper.findByEmail(email);
            if (user == null) {
                response.put("errorMessage", "해당 사용자가 존재하지 않습니다.");
                return response;
            }

            // 새로운 액세스 토큰 생성
            String newAccessToken = jwtTokenUtil.generateToken(email);

            response.put("accessToken", newAccessToken);
            return response;

        } catch (Exception e) {
            response.put("errorMessage", "리프레시 토큰 처리 중 오류가 발생했습니다.");
            return response;
        }
    }
}

