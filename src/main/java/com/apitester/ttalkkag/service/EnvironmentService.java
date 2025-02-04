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

/**
 * 환경(Environment) 관리 서비스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description 사이트 및 환경을 관리하고, 환경 변수의 생성/조회/수정/삭제 기능을 제공하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
public class EnvironmentService {

    private final EnvironmentMapper environmentMapper;
    private final EnvironmentVariableMapper variableMapper;

    /**
     * 새로운 환경(Environment) 생성
     *
     * @param environment 생성할 환경 객체
     * @return 생성된 환경 객체
     */
    @Transactional
    public Environment createEnvironment(Environment environment) {
        environmentMapper.insertEnvironment(environment);
        return environmentMapper.getEnvironmentById(environment.getId());
    }

    /**
     * 환경 삭제
     *
     * @param id 삭제할 환경 ID
     */
    @Transactional
    public void deleteEnvironment(Long id) {
        variableMapper.deleteVariableByEnvironmentId(id);
        environmentMapper.deleteEnvironment(id);
    }

    /**
     * 특정 환경(Environment)에 속한 변수 목록 조회
     *
     * @param environmentId 환경 ID
     * @return 환경 변수 목록
     */
    public List<EnvironmentVariable> findVariablesByEnvironmentId(Long environmentId) {
        return variableMapper.findByEnvironmentId(environmentId);
    }

    /**
     * 환경 변수 생성
     *
     * @param variable 생성할 환경 변수 객체
     */
    @Transactional
    public void createVariable(EnvironmentVariable variable) {
        variableMapper.insertVariable(variable);
    }

    /**
     * 환경 변수 업데이트
     *
     * @param variable 업데이트할 환경 변수 객체
     */
    @Transactional
    public void updateVariable(EnvironmentVariable variable) {
        variableMapper.updateVariable(variable);
    }

    /**
     * 환경 변수 삭제
     *
     * @param id 삭제할 환경 변수 ID
     */
    @Transactional
    public void deleteVariable(Long id) {
        variableMapper.deleteVariable(id);
    }

    /**
     * 프로젝트에 속한 모든 사이트(Site) 조회
     *
     * @param projectId 프로젝트 ID
     * @return 프로젝트 내 사이트 목록
     */
    public List<Site> getSites(Long projectId) {
        return environmentMapper.getSitesByProjectId(projectId);
    }

    /**
     * 특정 사이트(Site)에 속한 환경(Environment) 목록 조회
     *
     * @param siteId 사이트 ID
     * @return 해당 사이트의 환경 목록
     */
    public List<Environment> getEnvironments(Long siteId) {
        return environmentMapper.getEnvironments(siteId);
    }

    /**
     * 새로운 사이트(Site) 생성
     *
     * @param site 생성할 사이트 객체
     * @return 생성된 사이트 객체
     */
    @Transactional
    public Site createSite(Site site) {
        environmentMapper.createSite(site);
        return environmentMapper.getSiteById(site.getId());
    }

    /**
     * 특정 키(Key)를 가진 환경 변수 삭제
     *
     * @param key 삭제할 환경 변수의 키
     */
    @Transactional
    public void deleteVariableByKey(String key) {
        variableMapper.deleteVariableByKey(key);
    }

    /**
     * 특정 환경(Environment)에 속한 모든 환경 변수 삭제
     *
     * @param environmentId 환경 ID
     */
    @Transactional
    public void deleteVariableByEnvironmentId(Long environmentId) {
        variableMapper.deleteVariableByEnvironmentId(environmentId);
    }

    /**
     * 특정 환경(Environment) 조회
     *
     * @param id 환경 ID
     * @return 환경 객체
     */
    public Environment getEnvironmentById(Long id) {
        return environmentMapper.getEnvironmentById(id);
    }

    /**
     * 사이트 및 관련 환경과 환경 변수 삭제
     *
     * @param siteId 삭제할 사이트 ID
     */
    @Transactional
    public void deleteSiteAndEnvironments(Long siteId) {
        variableMapper.deleteVariablesBySiteId(siteId);
        environmentMapper.deleteEnvironmentsBySiteId(siteId);
        environmentMapper.deleteSite(siteId);
    }
}
