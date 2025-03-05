package com.apitester.ttalkkag.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class Dataset {
    @NotNull(message = "데이터셋 ID는 필수입니다.")
    private Long id;
    @NotNull(message = "프로젝트 ID는 필수입니다.")
    private Long projectId;
    private String name;
    private String description;
    private String createdAt;
    private List<DatasetVariable> variables;
}
