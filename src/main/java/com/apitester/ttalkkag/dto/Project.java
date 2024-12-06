package com.apitester.ttalkkag.dto;

import lombok.Data;

@Data
public class Project {
    private Long id;
    private Long userId;
    private String name;
    private String createAt;
}
