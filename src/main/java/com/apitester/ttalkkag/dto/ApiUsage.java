package com.apitester.ttalkkag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiUsage implements Serializable {
    private static final long serialVersionUID = 1L; // 버전 관리 ID
    private Long itemId; // 선택된 Item ID
    private String email; // 사용자 이메일
    private String profileImageUrl; // 사용자 프로필 이미지 URL
}
