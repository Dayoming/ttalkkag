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
    List<ProjectItems> searchProjectItems(Long projectId, String query, String type, String method);
    Project getProjectByProjectId(Long projectId);
    ProjectItems getProjectItemsById(Long id);
    void insertProject(Project project);
    void insertApi(ProjectItems item);
    void deleteProject(Long id);
    void deleteByItemId(Long itemId);
    void insertProjectItem(ProjectItems items);
    void updateProjectItemName(ProjectItems projectItems);
    void updateParentId(ProjectItems projectItems);
}
