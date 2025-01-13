package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.dto.NotificationMessage;
import com.apitester.ttalkkag.dto.User;
import com.apitester.ttalkkag.service.FileService;
import com.apitester.ttalkkag.service.NotificationService;
import com.apitester.ttalkkag.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@Transactional
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileService fileService;
    private final NotificationService notificationService;

    @PatchMapping(value = "/setting", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> settingUser(@RequestParam("showResponse") boolean showResponse,
                                           @RequestParam("autoSaveUse") boolean autoSaveUse,
                                           @RequestParam("autoSaveTime") int autoSaveTime,
                                           @RequestParam("autoSaveTerm") int autoSaveTerm,
                                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                           @AuthenticationPrincipal String userEmail) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = new User();
            user.setShowResponse(showResponse);
            user.setAutoSaveUse(autoSaveUse);
            user.setAutoSaveTime(autoSaveTime);
            user.setAutoSaveTerm(autoSaveTerm);
            userService.settingUser(userEmail, user, profileImage);
            response.put("message", "설정이 완료되었습니다.");
        } catch (Exception e) {
            response.put("errorMessage", "설정에 오류가 발생했습니다. 다시 시도해주세요.");
        }
        return response;
    }

    @PostMapping("/upload-profile")
    public ResponseEntity<Map<String, String>> uploadProfileImage(@RequestParam("profileImage") MultipartFile file, @RequestParam Long userId) {
        String profileImageUrl = fileService.saveProfileImage(file, userId);
        Map<String, String> response = new HashMap<>();
        response.put("profileImageUrl", profileImageUrl);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/setting/{userId}")
    public void settingUserAutoSaveUse(@PathVariable Long userId, @RequestBody Map<String, Boolean> body) {
        System.out.println(userId + ", " + body.get("autoSaveUse"));
        userService.settingUserAutoSaveUse(userId, body.get("autoSaveUse"));
        notificationService.notifyUser(userId, new NotificationMessage("API", "ENTER_OWNER", null));
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

    @GetMapping("/findById/{id}")
    public Map<String, Object> findUserById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        User user = userService.findUserById(id);
        response.put("user", user);
        return response;
    }
}
