package com.apitester.ttalkkag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiUsage {
    private Long itemId; // 선택된 Item ID
    private String email; // 사용자 이메일
    private String profileImageUrl; // 사용자 프로필 이미지 URL
}
