package com.apitester.ttalkkag.entity;

import lombok.Data;

import java.util.List;

@Data
public class Dataset {
    private Long id;
    private String name;
    private String description;
    private String createdAt;
    private List<DatasetVariable> variables;
}
