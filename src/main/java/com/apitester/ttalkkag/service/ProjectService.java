package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.Project;
import com.apitester.ttalkkag.dto.ProjectItems;
import com.apitester.ttalkkag.mapper.ProjectMapper;
import com.apitester.ttalkkag.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;

    // 사용자별 프로젝트 조회
    public List<Project> getProjectsByUserId(String userEmail) {
        Long userId = userMapper.findByEmail(userEmail).getId();
        return projectMapper.getProjectsByUserId(userId);
    }

    // 하위 폴더 조회
    public List<ProjectItems> getItemsByParentId(Long parentId) {
        return projectMapper.findByParentId(parentId);
    }

    // 프로젝트 생성
    public Project createProject(String name, String userEmail) {
        Long userId = userMapper.findByEmail(userEmail).getId();
        Project project = new Project();
        project.setName(name);
        project.setUserId(userId);
        projectMapper.insertProject(project);
        return project;
    }

    // 프로젝트 삭제
    public void deleteProject(Long projectId) {
        projectMapper.deleteProject(projectId);
    }

    // 폴더 선택 삭제
    @Transactional
    public void deleteItems(List<Long> itemIds) {
        for (Long itemId : itemIds) {
            deleteItemRecursively(itemId);
        }
    }

    private void deleteItemRecursively(Long itemId) {
        // 하위 항목 검색
        List<ProjectItems> childItems = projectMapper.findByParentId(itemId);

        // 하위 항목 삭제
        for (ProjectItems child : childItems) {
            deleteItemRecursively(child.getId());
        }

        // 현재 항목 삭제
        projectMapper.deleteByItemId(itemId);
    }

    public void addFolder(ProjectItems projectItems) {
        ProjectItems newFolder = new ProjectItems();
        newFolder.setProjectId(projectItems.getProjectId());
        newFolder.setParentId(projectItems.getParentId());
        newFolder.setType("folder");
        newFolder.setName(projectItems.getName());
        newFolder.setDepth(projectItems.getDepth());
        newFolder.setCreateAt(LocalDateTime.now().toString());

        projectMapper.insertProjectItem(newFolder);
    }

    public ProjectItems addProjectItemApi(ProjectItems projectItems) {
        ProjectItems newApi = new ProjectItems();
        newApi.setProjectId(projectItems.getProjectId());
        newApi.setParentId(projectItems.getParentId());
        newApi.setType("api");
        newApi.setName(projectItems.getName());
        newApi.setDepth(projectItems.getDepth());
        newApi.setCreateAt(LocalDateTime.now().toString());
        System.out.println(newApi);
        projectMapper.insertProjectItem(newApi);
        return projectMapper.getProjectItemsById(newApi.getId());
    }

    public List<ProjectItems> getProjectItemsByProjectId(Long projectId) {
        return projectMapper.findByProjectId(projectId);
    }

    public void saveApi(ProjectItems item) {
        projectMapper.insertApi(item);
    }

    public List<ProjectItems> searchProjectItems(Long projectId, String query, String type, String method) {
        return projectMapper.searchProjectItems(projectId, query, type, method);
    }

    public Project getProjectByProjectId(Long projectId) {
        return projectMapper.getProjectByProjectId(projectId);
    }

    public void updateProjectItemName(ProjectItems projectItems) {
        projectMapper.updateProjectItemName(projectItems);
    }

    public void updateParentId(ProjectItems projectItems) {
        projectMapper.updateParentId(projectItems);
    }

    public void deleteItemById(Long itemId) {
        projectMapper.deleteByItemId(itemId);
    }
}
