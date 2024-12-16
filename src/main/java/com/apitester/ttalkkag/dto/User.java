package com.apitester.ttalkkag.dto;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String email;
    private String password;
    private boolean autoSaveUse; // 자동 저장 사용 여부
    private Integer autoSaveTime; // 사용자가 입력 중 자동으로 저장되는 시간
    private Integer autoSaveTerm; // 사용자가 입력을 멈추고 자동으로 저장되는 시간
    private Long autoSavePath;
    private boolean showResponse;
    private boolean verified;
}
