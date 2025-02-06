package com.apitester.ttalkkag.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Site {
    @NotNull(message = "사이트 ID는 필수입니다.")
    private Long id;
    @NotNull(message = "프로젝트 ID는 필수입니다.")
    private Long projectId;
    private String name;
    private String createAt;
}
