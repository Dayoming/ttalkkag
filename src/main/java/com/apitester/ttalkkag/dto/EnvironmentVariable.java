package com.apitester.ttalkkag.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnvironmentVariable {
    @NotNull
    private Long id;
    @NotNull
    private Long environmentId;
    private String key;
    private String value;
}
