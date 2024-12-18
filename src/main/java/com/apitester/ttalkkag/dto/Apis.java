package com.apitester.ttalkkag.dto;

import lombok.Data;

@Data
public class Apis {
    private Long id;
    private Long itemId;
    private Long environmentId;
    private String name;
    private String method;
    private String url;
    private String headers;
    private String queryParameters;
    private String formParameters;
    private String file;
    private String selectedBodyType;
    private String createAt;
}
