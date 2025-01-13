package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.User;
import com.apitester.ttalkkag.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final FileService fileService;

    public void settingUser(String userEmail, User user, MultipartFile profileImage) {
        Long userId = userMapper.findByEmail(userEmail).getId();
        user.setId(userId);
        if (profileImage != null && !profileImage.isEmpty()) {
            String filePath = fileService.saveProfileImage(profileImage, user.getId());
            user.setProfileImage(filePath);
        }
        userMapper.settingUser(user);
    }

    public void settingUserAutoSaveUse(Long userId, Boolean isUse) {
        userMapper.settingUserAutoSaveUse(userId, isUse);
    }

    public void renewVerified(String userEmail, User user) {
        Long userId = userMapper.findByEmail(userEmail).getId();
        user.setId(userId);
        userMapper.renewVerified(user);
    }

    public User findUserByEmail(String userEmail) {
        return userMapper.findByEmail(userEmail);
    }

    public User findUserById(Long userId) {
        return userMapper.findById(userId);
    }
}
