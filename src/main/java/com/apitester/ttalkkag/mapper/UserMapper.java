package com.apitester.ttalkkag.mapper;

import com.apitester.ttalkkag.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User findByEmail(String email);
    void insertUser(User user);
}
