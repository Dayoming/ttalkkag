package com.apitester.ttalkkag.mapper;

import com.apitester.ttalkkag.dto.Project;
import com.apitester.ttalkkag.dto.ProjectItems;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectMapper {
    List<Project> getProjectsByUserId(Long userId);
    List<ProjectItems> findByProjectId(Long projectId);
    List<ProjectItems> findByParentId(Long parentId);
    void insertProject(Project project);
    void deleteProject(Long id);
    void deleteByItemId(Long itemId);
    void insertFolder(ProjectItems items);
}
