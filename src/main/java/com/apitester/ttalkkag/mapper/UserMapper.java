package com.apitester.ttalkkag.mapper;

import com.apitester.ttalkkag.dto.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User findByEmail(String email);
    void insertUser(User user);
    void insertSocialUser(User user);
    void settingUser(User user);
    void renewVerified(User user);
}
