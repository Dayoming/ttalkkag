package com.apitester.ttalkkag.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Environment {
    @NotNull
    private Long id;
    @NotNull
    private Long siteId;
    private String name;
}
