package com.apitester.ttalkkag.dto;

import lombok.Data;

@Data
public class DatasetVariable {
    private Long id;
    private Long datasetId;
    private String type;
    private String name;
    private String description;
    private String createdAt;
}
