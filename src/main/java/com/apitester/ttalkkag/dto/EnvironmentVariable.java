package com.apitester.ttalkkag.dto;

import lombok.Data;

@Data
public class EnvironmentVariable {
    private Long id;
    private Long environmentId;
    private String key;
    private String value;
}
