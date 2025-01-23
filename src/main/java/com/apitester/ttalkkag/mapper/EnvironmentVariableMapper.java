package com.apitester.ttalkkag.mapper;

import com.apitester.ttalkkag.dto.EnvironmentVariable;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EnvironmentVariableMapper {
    List<EnvironmentVariable> findByEnvironmentId(Long id);
    void insertVariable(EnvironmentVariable environmentVariable);
    void updateVariable(EnvironmentVariable environmentVariable);
    void deleteVariable(Long id);
    void deleteVariableByEnvironmentId(Long environmentId);
    void deleteVariableByKey(String key);
}
