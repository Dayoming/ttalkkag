package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.User;
import com.apitester.ttalkkag.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * OAuth2 사용자 서비스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description OAuth2 로그인 요청을 처리하고, 사용자 정보를 가져와 DB에 저장하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserMapper userMapper;

    /**
     * OAuth2 사용자 정보를 로드하여 데이터베이스에 저장
     *
     * @param userRequest OAuth2 로그인 요청 정보
     * @return OAuth2User 객체 (사용자 정보 포함)
     * @throws OAuth2AuthenticationException OAuth2 인증 예외 발생 시
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 기본 OAuth2UserService를 사용하여 OAuth2User 객체 로드
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        // 카카오 사용자 정보 가져오기
        Map<String, Object> attributes = oauth2User.getAttributes();
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = (String) kakaoAccount.get("email");

        User existingUser = userMapper.findByEmail(email);
        if (existingUser == null) {
            userMapper.insertSocialUser(createSocialUser(email, "kakao"));
        }
        // OAuth2User 반환
        return oauth2User;
    }

    /**
     * 소셜 로그인 사용자의 기본 정보를 생성
     *
     * @param email 사용자 이메일
     * @param provider 소셜 로그인 제공자
     * @return 초기화된 User 객체
     */
    private User createSocialUser(String email, String provider) {
        User user = new User();
        user.setEmail(email);
        user.setVerified(false);        // 이메일 인증 여부 기본값: false
        user.setAutoSaveUse(false);     // 자동 저장 기능 기본값: false
        user.setAutoSaveTime(60);       // 자동 저장 시간 기본값: 60분
        user.setAutoSaveTerm(5);        // 자동 저장 간격 기본값: 5분
        user.setShowResponse(false);    // 응답 보기 설정 기본값: false
        user.setSocialProvider(provider); // 소셜 로그인 제공자 설정
        return user;
    }
}
