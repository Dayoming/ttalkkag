package com.apitester.ttalkkag.dto;

import lombok.Data;

@Data
public class Environment {
    private Long id;
    private Long userId;
    private String name;
    private Boolean isDefault;
}
