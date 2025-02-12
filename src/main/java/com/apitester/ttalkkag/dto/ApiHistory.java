package com.apitester.ttalkkag.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApiHistory {
    private Long id;
    private Long userId;
    private Long projectId;
    private Long environmentId;
    private Long siteId;
    private String method;
    private String url;
    private Integer responseCode;
    private Integer responseTime;
    private String loggedTime;
    private String header;
    private String parameter;
    private String formParameter;
    private String responseBody;
    private String responseHeader;
}
