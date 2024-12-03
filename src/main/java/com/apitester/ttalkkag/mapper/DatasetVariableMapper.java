package com.apitester.ttalkkag.mapper;

import com.apitester.ttalkkag.dto.DatasetVariable;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DatasetVariableMapper {
    void insertVariable(DatasetVariable variable);
    void deleteVariable(Long id);
    void deleteVariableByDatasetId(Long id);
}
