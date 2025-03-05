package com.apitester.ttalkkag.dto;

import lombok.Data;

@Data
public class ProjectParticipants {
    private Long id;
    private Long projectId;
    private String name;
    private Long userId;
    private String role;
    private String permissionLevel;
}
