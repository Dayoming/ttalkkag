package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.config.JwtTokenUtil;
import com.apitester.ttalkkag.entity.User;
import com.apitester.ttalkkag.mapper.UserMapper;
import com.apitester.ttalkkag.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private final Map<String, String> verificationCodes = new HashMap<>();

    /* 회원가입 이메일 확인 코드 전송 */
    @PostMapping("/send-code")
    public Map<String, Object> sendVerificationCode(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String email = request.get("email");
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
            response.put("errorMessage", "해당 이메일로 가입된 계정이 존재합니다.");
            return response;
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        user.setVerified(true);
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
        System.out.println(user);
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

        return response;
    }

    /* 로그아웃 */
    @PostMapping("/logout")
    public Map<String, Object> logout() {
        Map<String, Object> response = new HashMap<>();
        return response;
    }
}

