package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.User;
import com.apitester.ttalkkag.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public void settingUser(String userEmail, User user) {
        Long userId = userMapper.findByEmail(userEmail).getId();
        user.setId(userId);
        userMapper.settingUser(user);
    }

    public void renewVerified(String userEmail, User user) {
        Long userId = userMapper.findByEmail(userEmail).getId();
        user.setId(userId);
        userMapper.renewVerified(user);
    }

    public User findUserByEmail(String userEmail) {
        return userMapper.findByEmail(userEmail);
    }
}
