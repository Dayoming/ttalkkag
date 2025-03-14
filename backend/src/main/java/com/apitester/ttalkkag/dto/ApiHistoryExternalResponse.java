package com.apitester.ttalkkag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiHistoryExternalResponse implements Serializable {
    private String jwtToken;
    private Long apiId;
    private boolean isValid;
    private List<ApiChangeHistory> histories;
}
