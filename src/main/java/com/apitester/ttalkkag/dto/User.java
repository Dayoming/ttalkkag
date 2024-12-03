package com.apitester.ttalkkag.dto;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String email;
    private String password;
    private boolean verified;
}
