package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.config.JwtTokenUtil;
import com.apitester.ttalkkag.dto.User;
import com.apitester.ttalkkag.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 사용자 관리 서비스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description 사용자 정보 설정, 자동 저장 기능 관리, 사용자 인증 갱신 등의 기능을 제공하는 서비스 클래스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final FileService fileService;
    private final EmailService emailService;
    private final KakaoOAuthService kakaoOAuthService;
    private final GoogleOAuthService googleOAuthService;
    private final JwtTokenUtil jwtTokenUtil;

    private final Map<String, String> verificationCodes = new HashMap<>();

    /**
     * 이메일 인증 코드 전송
     *
     * @param request 이메일 정보를 포함한 요청 데이터
     * @return 이메일 전송 결과 응답 (성공 시 코드 전송 메시지 포함)
     */
    public Map<String, Object> sendVerificationCode(Map<String, String> request) {
        try {
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
        } catch (Exception e) {
            log.error("이메일 인증코드 발송 실패 (Email={}): {}", request.get("email"), e.getMessage());
            throw new RuntimeException("이메일 인증코드 발송 중 오류 발생", e);
        }
    }

    /**
     * 회원가입 처리
     *
     * @param request 회원가입 요청 데이터 (이메일, 비밀번호, 인증 코드 포함)
     * @return 회원가입 결과 응답 (성공 시 회원가입 완료 메시지 포함)
     */
    public Map<String, Object> register(Map<String, String> request) {
        try {
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
        } catch (Exception e) {
            log.error("회원가입 실패 (Email={}): {}", request.get("email"), e.getMessage());
            throw new RuntimeException("회원가입 중 오류 발생", e);
        }
    }

    /**
     * 로그인 처리
     *
     * @param request 로그인 요청 데이터 (이메일, 비밀번호 포함)
     * @return 로그인 결과 응답 (성공 시 액세스/리프레시 토큰 포함)
     */
    public Map<String, Object> login(Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String email = request.get("email");
        String password = request.get("password");

        try {
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
        } catch (Exception e) {
            log.error("로그인 중 예외 발생 (Email={}): {}", email, e.getMessage());
            response.put("errorMessage", "서버 오류로 인해 로그인에 실패했습니다. 다시 시도해 주세요.");
            return response;
        }
    }

    /**
     * Kakao OAuth 로그인 처리
     *
     * @param code Kakao에서 반환된 인증 코드
     * @return 로그인 처리 결과 (JWT 토큰 및 사용자 정보)
     */
    public Map<String, Object> loginWithKakao(String code) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Access Token 요청
            String accessToken = kakaoOAuthService.getAccessToken(code);

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
                userMapper.insertSocialUser(user);
            }

            // 4. JWT 발급
            response.put("accessToken", jwtTokenUtil.generateToken(email));
            response.put("refreshToken", jwtTokenUtil.generateRefreshToken(email));
            response.put("nickname", nickname);
            response.put("email", email);
            response.put("verified", user.isVerified());
            response.put("profileImageUrl", profileImageUrl);

            log.info("Kakao 로그인 성공 - Email: {}", email);
        } catch (Exception e) {
            log.error("Kakao 로그인 중 오류 발생: {}", e.getMessage());
            response.put("errorMessage", "Kakao 로그인 처리 중 문제가 발생했습니다.");
        }

        return response;
    }

    /**
     * Google OAuth 로그인 처리
     *
     * @param code Google에서 반환된 인증 코드
     * @return 로그인 처리 결과 (JWT 토큰 및 사용자 정보)
     */
    public Map<String, Object> loginWithGoogle(String code) {
        Map<String, Object> response = new HashMap<>();

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
                userMapper.insertSocialUser(user);
            }

            // 4. JWT 발급
            response.put("accessToken", jwtTokenUtil.generateToken(email));
            response.put("refreshToken", jwtTokenUtil.generateRefreshToken(email));
            response.put("name", name);
            response.put("email", email);
            response.put("verified", user.isVerified());
            response.put("profileImageUrl", profileImageUrl);

            log.info("Google 로그인 성공 - Email: {}", email);
        } catch (Exception e) {
            log.error("Google 로그인 중 오류 발생: {}", e.getMessage());
            response.put("errorMessage", "Google 로그인 처리 중 문제가 발생했습니다.");
        }

        return response;
    }

    /**
     * 사용자 정보 조회
     *
     * @param request HTTP 요청 객체
     * @return 사용자 정보 (이메일) 또는 오류 메시지
     */
    public Map<String, Object> getUserInfo(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String email = jwtTokenUtil.getEmailFromToken(token);
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

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
     *
     * @param request HTTP 요청 객체
     * @return 새 액세스 토큰 또는 오류 메시지
     */
    public Map<String, Object> refreshAccessToken(Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String refreshToken = request.get("refreshToken");

        try {
            String email = jwtTokenUtil.getEmailFromToken(refreshToken);

            if (email == null || !jwtTokenUtil.validateToken(refreshToken)) {
                response.put("errorMessage", "유효하지 않은 리프레시 토큰입니다.");
                return response;
            }

            User user = userMapper.findByEmail(email);
            if (user == null) {
                response.put("errorMessage", "해당 사용자가 존재하지 않습니다.");
                return response;
            }

            response.put("accessToken", jwtTokenUtil.generateToken(email));
        } catch (Exception e) {
            response.put("errorMessage", "리프레시 토큰 처리 중 오류가 발생했습니다.");
        }

        return response;
    }

    /**
     * 사용자 설정 업데이트 (프로필 이미지 포함)
     *
     * @param userEmail    사용자 이메일
     * @param user         업데이트할 사용자 정보
     * @param profileImage 프로필 이미지 (선택 사항)
     */
    public void settingUser(String userEmail, User user, MultipartFile profileImage) {
        try {
            Long userId = userMapper.findByEmail(userEmail).getId();
            user.setId(userId);

            // 프로필 이미지 저장 및 업데이트
            if (profileImage != null && !profileImage.isEmpty()) {
                String filePath = fileService.saveProfileImage(profileImage, user.getId());
                user.setProfileImage(filePath);
                log.info("프로필 이미지 업데이트 완료 (User ID: {}, File Path: {})", userId, filePath);
            }

            userMapper.settingUser(user);
            log.info("사용자 설정 업데이트 완료 (User ID: {})", userId);
        } catch (Exception e) {
            log.error("사용자 설정 업데이트 실패 (Email={}): {}", userEmail, e.getMessage());
            throw new RuntimeException("사용자 설정 업데이트 중 오류 발생", e);
        }
    }

    /**
     * 자동 저장 기능 사용 여부 설정
     *
     * @param userId 사용자 ID
     * @param isUse  자동 저장 사용 여부 (true: 사용, false: 미사용)
     */
    public void settingUserAutoSaveUse(Long userId, Boolean isUse) {
        try {
            userMapper.settingUserAutoSaveUse(userId, isUse);
            log.info("자동 저장 설정 변경 (User ID: {}, AutoSave: {})", userId, isUse);
        } catch (Exception e) {
            log.error("자동 저장 설정 변경 실패 (User ID={}): {}", userId, e.getMessage());
            throw new RuntimeException("자동 저장 설정 변경 중 오류 발생", e);
        }
    }

    /**
     * 사용자 인증 갱신
     *
     * @param userEmail 사용자 이메일
     * @param user      인증 갱신할 사용자 정보
     */
    public void renewVerified(String userEmail, User user) {
        try {
            Long userId = userMapper.findByEmail(userEmail).getId();
            user.setId(userId);
            userMapper.renewVerified(user);
            log.info("사용자 인증 갱신 완료 (User ID: {})", userId);
        } catch (Exception e) {
            log.error("사용자 인증 갱신 실패 (Email={}): {}", userEmail, e.getMessage());
            throw new RuntimeException("사용자 인증 갱신 중 오류 발생", e);
        }
    }

    /**
     * 이메일로 사용자 조회
     *
     * @param userEmail 사용자 이메일
     * @return 조회된 사용자 객체
     */
    public User findUserByEmail(String userEmail) {
        try {
            User user = userMapper.findByEmail(userEmail);
            log.info("사용자 조회 완료 (Email: {})", userEmail);
            return user;
        } catch (Exception e) {
            log.error("사용자 조회 실패 (Email={}): {}", userEmail, e.getMessage());
            throw new RuntimeException("사용자 조회 중 오류 발생", e);
        }
    }

    /**
     * 사용자 ID로 사용자 조회
     *
     * @param userId 사용자 ID
     * @return 조회된 사용자 객체
     */
    public User findUserById(Long userId) {
        try {
            User user = userMapper.findById(userId);
            log.info("사용자 조회 완료 (User ID: {})", userId);
            return user;
        } catch (Exception e) {
            log.error("사용자 조회 실패 (User ID={}): {}", userId, e.getMessage());
            throw new RuntimeException("사용자 조회 중 오류 발생", e);
        }
    }
}
