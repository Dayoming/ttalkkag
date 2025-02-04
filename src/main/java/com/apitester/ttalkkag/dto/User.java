package com.apitester.ttalkkag.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class User {
    @NotNull
    private Long id;
    @NotNull
    private String email;
    private String password;
    private boolean autoSaveUse; // 자동 저장 사용 여부
    private Integer autoSaveTime; // 사용자가 입력 중 자동으로 저장되는 시간
    private Integer autoSaveTerm; // 사용자가 입력을 멈추고 자동으로 저장되는 시간
    private Long autoSavePath;
    private boolean showResponse;
    private boolean verified;
    private String socialProvider; // 소셜 로그인 제공자
    private String profileImage; // 프로필 이미지
}
