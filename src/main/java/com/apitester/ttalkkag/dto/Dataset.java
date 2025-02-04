package com.apitester.ttalkkag.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class Dataset {
    @NotNull
    private Long id;
    @NotNull
    private Long projectId;
    private String name;
    private String description;
    private String createdAt;
    private List<DatasetVariable> variables;
}
