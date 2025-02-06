package com.apitester.ttalkkag.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class ProxyRequest {
    @NotNull(message = "요청 URL은 필수입니다.")
    private String url; // 외부 URL
    @NotNull(message = "요청 Method는 필수입니다.")
    private String method; // HTTP Method (GET, POST, PUT, DELETE, PATCH)
    private Map<String, String> headers; // 요청 헤더
    private Object body; // 요청 바디
}

