package com.apitester.ttalkkag.mapper;

import com.apitester.ttalkkag.dto.Environment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EnvironmentMapper {
    List<Environment> findByUserId(Long id);
    void insertEnvironment(Environment environment);
    void deleteEnvironment(Long id);
}
