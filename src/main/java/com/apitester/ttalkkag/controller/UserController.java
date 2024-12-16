package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.dto.User;
import com.apitester.ttalkkag.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Transactional
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/setting")
    public Map<String, Object> settingUser(@AuthenticationPrincipal String userEmail, @RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.settingUser(userEmail, user);
            response.put("message", "설정이 완료되었습니다.");
        } catch (Exception e) {
            response.put("errorMessage", "설정에 오류가 발생했습니다. 다시 시도해주세요.");
        }
        return response;
    }

    @PostMapping("/renewVerified")
    public void renewVerified(@AuthenticationPrincipal String userEmail, @RequestBody User user) {
        userService.renewVerified(userEmail, user);
    }

    @GetMapping("/findUserByEmail")
    public Map<String, Object> findUserByEmail(@AuthenticationPrincipal String userEmail) {
        Map<String, Object> response = new HashMap<>();
        User user = userService.findUserByEmail(userEmail);
        response.put("user", user);
        return response;
    }
}
