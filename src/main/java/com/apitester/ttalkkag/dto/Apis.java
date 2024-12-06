package com.apitester.ttalkkag.dto;

import lombok.Data;

@Data
public class Apis {
    private Long id;
    private Long itemId;
    private String name;
    private String method;
    private String url;
    private Object headers;
    private Object queryParameters;
    private Object formParameters;
    private Object file;
    private String selectedBodyType;
    private String selectedEnvironment;
}
