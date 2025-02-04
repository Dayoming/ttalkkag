package com.apitester.ttalkkag.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Site {
    @NotNull
    private Long id;
    @NotNull
    private Long projectId;
    private String name;
    private String createAt;
}
