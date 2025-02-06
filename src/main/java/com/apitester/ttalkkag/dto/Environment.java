package com.apitester.ttalkkag.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Environment {
    @NotNull(message = "환경 ID는 필수입니다.")
    private Long id;
    @NotNull(message = "사이트 ID는 필수입니다.")
    private Long siteId;
    private String name;
}
