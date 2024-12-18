package com.apitester.ttalkkag.mapper;

import com.apitester.ttalkkag.dto.Environment;
import com.apitester.ttalkkag.dto.Site;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EnvironmentMapper {
    List<Site> getSitesByProjectId(Long projectId);
    List<Environment> getEnvironments(Long siteId);
    Environment getEnvironmentById(Long id);
    void insertEnvironment(Environment environment);
    void createSite(Site site);
    void deleteEnvironment(Long id);
    void deleteSite(Long id);
}
