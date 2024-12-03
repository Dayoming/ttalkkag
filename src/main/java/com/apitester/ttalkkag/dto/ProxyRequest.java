package com.apitester.ttalkkag.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ProxyRequest {
    private String url; // 외부 URL
    private String method; // HTTP Method (GET, POST, PUT, DELETE, PATCH)
    private Map<String, String> headers; // 요청 헤더
    private Object body; // 요청 바디
}

