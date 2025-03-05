package com.apitester.ttalkkag.exception;

import lombok.Getter;

@Getter
public class JwtAuthenticationException extends SecurityException {
    public JwtAuthenticationException(String message) {
        super(message);
    }
}
