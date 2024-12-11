package com.apitester.ttalkkag.mapper;

import com.apitester.ttalkkag.dto.DatasetVariable;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DatasetVariableMapper {
    void insertVariable(DatasetVariable variable);
    void deleteVariable(Long id);
    void deleteVariableByDatasetId(Long id);
}
