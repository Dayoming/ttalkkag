package com.apitester.ttalkkag.dto;

import lombok.Data;

@Data
public class ProjectItems {
    private Long id;
    private Long projectId;
    private Long parentId;
    private String type;
    private String name;
    private int depth;
    private String createAt;
}
