package com.apitester.ttalkkag.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Apis {
    private Long id;
    private Long itemId;
    private String name;
    private String method;
    private String url;
    private String headers;
    private String queryParameters;
    private String formParameters;
    private String file;
    private String selectedBodyType;
    private Long selectedEnvironment;
    private String createAt;
}
