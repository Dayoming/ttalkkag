package com.apitester.history.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class ApiChangeHistory {
    private Long id;
    @NotNull(message = "apiId는 필수입니다.")
    private Long apiId;
    @NotNull(message = "userId는 필수입니다.")
    private Long userId;
    private String name;
    private String method;
    private String url;
    private String headers;
    private String queryParameters;
    private String formParameters;
    private String file;
    private String selectedBodyType;
    private String savedAt;
}
