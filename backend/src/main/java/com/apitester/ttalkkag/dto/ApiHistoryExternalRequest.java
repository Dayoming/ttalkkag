package com.apitester.ttalkkag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiHistoryExternalRequest implements Serializable {
    private String jwtToken;
    private Long apiId;
}
