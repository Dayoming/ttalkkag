package com.apitester.ttalkkag.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApiHistory {
    @NotNull(message = "요청 기록 ID는 필수입니다.")
    private Long id;
    @NotNull(message = "요청한 유저 ID는 필수입니다.")
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
