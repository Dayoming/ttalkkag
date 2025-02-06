package com.apitester.ttalkkag.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnvironmentVariable {
    @NotNull(message = "환경 변수 ID는 필수입니다.")
    private Long id;
    @NotNull(message = "환경 ID는 필수입니다.")
    private Long environmentId;
    private String key;
    private String value;
}
