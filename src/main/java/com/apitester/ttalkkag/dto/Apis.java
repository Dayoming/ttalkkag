package com.apitester.ttalkkag.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Apis {
    private Long id;
    @NotNull(message = "item ID는 필수입니다.")
    private Long itemId;
    private Long environmentId;
    @NotNull(message = "API 이름은 필수입니다.")
    private String name;
    @NotNull(message = "method는 필수입니다.")
    private String method;
    private String url;
    private String headers;
    private String queryParameters;
    private String formParameters;
    private String file;
    private String selectedBodyType;
    private String createAt;
}
