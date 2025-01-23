package com.apitester.ttalkkag.mapper;

import com.apitester.ttalkkag.dto.InviteCode;
import com.apitester.ttalkkag.dto.Project;
import com.apitester.ttalkkag.dto.ProjectItems;
import com.apitester.ttalkkag.dto.ProjectParticipants;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProjectMapper {
    List<Project> getProjectsByUserId(Long userId);
    List<ProjectItems> findByProjectId(Long projectId);
    List<ProjectItems> findByParentId(Long parentId);
    List<ProjectItems> searchProjectItems(Long projectId, String query, String type, String method);
    Project getProjectByProjectId(Long projectId);
    ProjectItems getProjectItemsById(Long id);
    List<ProjectItems> getProjectItemsByProjectId(Long projectId);
    InviteCode getInviteCodeByProjectId(Long projectId);
    InviteCode findByCode(String code);
    ProjectParticipants getParticipantsById(Long id);
    ProjectParticipants getParticipantByProjectIdAndUserId(Long projectId, Long userId);
    List<ProjectParticipants> getParticipantsByProjectId(Long projectId);
    void insertProject(Project project);
    void insertApi(ProjectItems item);
    void deleteProject(Long id);
    void deleteByItemId(Long itemId);
    void deleteApiByItemId(Long itemId);
    void deleteInviteCode(Long projectId);
    void deleteParticipantByProjectId(Long projectId);
    void insertProjectItem(ProjectItems items);
    void insertInviteCode(InviteCode code);
    void insertParticipant(ProjectParticipants participant);
    void updateProjectItemName(ProjectItems projectItems);
    void updateParentId(ProjectItems projectItems);
    void updateInviteCode(InviteCode code);
    void updateParticipant(Long projectId, ProjectParticipants participant);
    Integer getNextItemOrder(Long projectId, Long parentId);
    ProjectItems getItemByItemId(Long itemId);
    void incrementItemOrder(Long targetParentId, Integer targetOrder, Long projectId);
    void updateItemOrder(Long draggedItemId, Integer targetOrder, Long targetParentId);
    void removeParticipant(Long projectId, Long participantId);
}
