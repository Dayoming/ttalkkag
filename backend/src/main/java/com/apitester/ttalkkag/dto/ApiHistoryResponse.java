package com.apitester.ttalkkag.dto;

import lombok.Data;

@Data
public class ApiHistoryResponse {
    private Long id;
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
    private String requestUserEmail;
    private String projectName;
    private String projectOwnerEmail;
    private String siteName;
    private String environmentName;
}
