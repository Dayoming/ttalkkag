package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.Environment;
import com.apitester.ttalkkag.dto.EnvironmentVariable;
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

    public void createEnvironment(String userEmail, Environment environment) {
        User user = userMapper.findByEmail(userEmail);
        environmentMapper.insertEnvironment(environment);
    }

    public void deleteEnvironment(Long id) {
        environmentMapper.deleteEnvironment(id);
    }

    public List<EnvironmentVariable> findVariablesByEnvironmentId(Long environmentId) {
        return variableMapper.findByEnvironmentId(environmentId);
    }

    public void createVariable(EnvironmentVariable variable) {
        System.out.println("variable: " + variable);
        variableMapper.insertVariable(variable);
    }

    public void updateVariable(EnvironmentVariable variable) {
        variableMapper.updateVariable(variable);
    }

    public void deleteVariable(Long id) {
        variableMapper.deleteVariable(id);
    }
}
