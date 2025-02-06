package com.apitester.ttalkkag.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Project {
    @NotNull(message = "프로젝트 ID는 필수입니다.")
    private Long id; // 프로젝트 ID
    @NotNull(message = "소유자 ID는 필수입니다.")
    private Long userId; // 소유자 ID
    @NotNull(message = "프로젝트 이름은 필수입니다.")
    private String name; // 프로젝트 이름
    private String createAt; // 생성일자

    // 추가된 참여자 정보
    private Long participantUserId; // 참여자 ID
    private String participantRole; // 참여자 역할 (예: 소유자, 참여자)
    private String permissionLevel; // 권한 수준 (예: 읽기 전용, 쓰기 가능)
}
