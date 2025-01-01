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

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserMapper userMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 기본 OAuth2UserService 호출
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        // 카카오 사용자 정보 가져오기
        Map<String, Object> attributes = oauth2User.getAttributes();
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = (String) kakaoAccount.get("email");
        String name = (String) ((Map<String, Object>) attributes.get("properties")).get("nickname");

        // DB에 사용자 정보 저장
        userMapper.insertSocialUser(createSocialUser(email, "kakao"));

        // 사용자 정보 반환
        return oauth2User;
    }

    private User createSocialUser(String email, String provider) {
        User user = new User();
        user.setEmail(email);
        user.setVerified(false);
        user.setAutoSaveUse(false);
        user.setAutoSaveTime(60);
        user.setAutoSaveTerm(5);
        user.setShowResponse(false);
        user.setSocialProvider(provider);
        return user;
    }
}
