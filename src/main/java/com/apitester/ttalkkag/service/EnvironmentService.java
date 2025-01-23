package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.Environment;
import com.apitester.ttalkkag.dto.EnvironmentVariable;
import com.apitester.ttalkkag.dto.Site;
import com.apitester.ttalkkag.dto.User;
import com.apitester.ttalkkag.mapper.EnvironmentMapper;
import com.apitester.ttalkkag.mapper.EnvironmentVariableMapper;
import com.apitester.ttalkkag.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EnvironmentService {

    private final EnvironmentMapper environmentMapper;
    private final EnvironmentVariableMapper variableMapper;
    private final UserMapper userMapper;

    public Environment createEnvironment(Environment environment) {
        environmentMapper.insertEnvironment(environment);
        return environmentMapper.getEnvironmentById(environment.getId());
    }

    public void deleteEnvironment(Long id) {
        environmentMapper.deleteEnvironment(id);
    }

    public List<EnvironmentVariable> findVariablesByEnvironmentId(Long environmentId) {
        return variableMapper.findByEnvironmentId(environmentId);
    }

    public void createVariable(EnvironmentVariable variable) {
        variableMapper.insertVariable(variable);
    }

    public void updateVariable(EnvironmentVariable variable) {
        variableMapper.updateVariable(variable);
    }

    public void deleteVariable(Long id) {
        variableMapper.deleteVariable(id);
    }

    public List<Site> getSites(Long projectId) {
        return environmentMapper.getSitesByProjectId(projectId);
    }

    public List<Environment> getEnvironments(Long siteId) {
        return environmentMapper.getEnvironments(siteId);
    }

    public List<Environment> getEnvironmentsBySiteId(Long siteId) {
        return environmentMapper.getEnvironmentsBySiteId(siteId);
    }

    public Site createSite(Site site) {
        environmentMapper.createSite(site);
        return environmentMapper.getSiteById(site.getId());
    }

    public void deleteSite(Long id) {
        environmentMapper.deleteSite(id);
    }

    public void deleteVariableByKey(String key) {
        variableMapper.deleteVariableByKey(key);
    }

    public void deleteVariableByEnvironmentId(Long environmentId) {
        variableMapper.deleteVariableByEnvironmentId(environmentId);
    }

    public Environment getEnvironmentById(Long id) {
        return environmentMapper.getEnvironmentById(id);
    }
}
