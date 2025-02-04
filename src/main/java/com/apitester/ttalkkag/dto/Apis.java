package com.apitester.ttalkkag.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Apis {
    @NotNull
    private Long id;
    @NotNull
    private Long itemId;
    private Long environmentId;
    @NotNull
    private String name;
    @NotNull
    private String method;
    private String url;
    private String headers;
    private String queryParameters;
    private String formParameters;
    private String file;
    private String selectedBodyType;
    private String createAt;
}
