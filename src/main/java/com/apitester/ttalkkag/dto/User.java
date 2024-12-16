package com.apitester.ttalkkag.dto;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String email;
    private String password;
    private Integer autoSaveTerm;
    private Integer autoSaveTime;
    private Long autoSavePath;
    private boolean showResponse;
    private boolean verified;
}
