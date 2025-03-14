package com.apitester.history.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtValidationResponse implements Serializable {
    private String jwtToken;
    private boolean isValid;
}
