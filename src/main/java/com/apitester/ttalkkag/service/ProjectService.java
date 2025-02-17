package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.*;
import com.apitester.ttalkkag.log.LoggingFilter;
import com.apitester.ttalkkag.log.LoggingUtil;
import com.apitester.ttalkkag.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 프로젝트 관리 서비스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description 프로젝트 생성, 조회, 삭제 및 프로젝트 내 항목 관리 기능을 제공하는 서비스 클래스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final EnvironmentMapper environmentMapper;
    private final EnvironmentVariableMapper environmentVariableMapper;
    private final DatasetMapper datasetMapper;
    private final DatasetVariableMapper datasetVariableMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    /**
     * 사용자의 프로젝트 목록 조회
     *
     * @param userEmail 사용자 이메일
     * @return 사용자가 속한 프로젝트 리스트
     */
    public List<Project> getProjectsByUserId(String userEmail, boolean includeInfo) {
        String logKey = MDC.get("LOG_KEY");
        Long userId = userMapper.findByEmail(userEmail).getId();
        LoggingUtil.logTransactionStep(logKey, userEmail, "1. 사용자 정보로 사용자가 소유한 프로젝트 조회");
        List<Project> projects = projectMapper.getProjectsByUserId(userId);
        LoggingUtil.logTransactionStep(logKey, userEmail, "1-2. 만약 includeInfo가 true면 프로젝트 하위 정보를 포함한 결과 조회");

        // 프로젝트 하위 정보도 함께 불러오는 경우
        if (includeInfo) {
            for (Project project : projects) {
                System.out.println(projectMapper.findProjectDetailsById(project.getId()));
            }
        }

        LoggingUtil.logTransactionStep(logKey, userEmail, "2. 조회 결과 반환");

        return projects;
    }

    /**
     * 특정 폴더 내 하위 항목 조회
     *
     * @param parentId 부모 폴더 ID
     * @return 하위 프로젝트 항목 리스트
     */
    public List<ProjectItems> getItemsByParentId(Long parentId) {
        return projectMapper.findByParentId(parentId);
    }

    /**
     * 새 프로젝트 생성
     *
     * @param name      프로젝트 이름
     * @param userEmail 사용자 이메일
     * @return 생성된 프로젝트 객체
     */
    @Transactional
    public Project createProject(String name, String userEmail) {
        Long userId = userMapper.findByEmail(userEmail).getId();
        Project project = new Project();
        project.setName(name);
        project.setUserId(userId);
        projectMapper.insertProject(project);

        String logKey = MDC.get("LOG_KEY");

        LoggingUtil.logTransactionStep(logKey, userEmail, "1. 새 프로젝트 " + name + " 생성");

        log.info("새 프로젝트 생성: {} (User ID: {})", name, userId);
        return projectMapper.getProjectByProjectId(project.getId());
    }

    /**
     * 프로젝트 삭제 (연관 데이터 포함)
     *
     * @param projectId 삭제할 프로젝트 ID
     */
    @Transactional
    public void deleteProject(Long projectId) {
        try {
            projectMapper.deleteInviteCode(projectId);
            projectMapper.deleteParticipantByProjectId(projectId);
            datasetVariableMapper.deleteVariableByProjectId(projectId);
            datasetMapper.deleteDatasetsByProjectId(projectId);
            environmentVariableMapper.deleteVariablesByProjectId(projectId);
            environmentMapper.deleteEnvironmentsByProjectId(projectId);
            environmentMapper.deleteSitesByProjectId(projectId);
            projectMapper.deleteApisByProjectId(projectId);
            projectMapper.deleteProjectItemsByProjectId(projectId);
            projectMapper.deleteProject(projectId);
            log.info("프로젝트 삭제 완료: {}", projectId);
        } catch (Exception e) {
            log.error("프로젝트 삭제 실패 (ID={}): {}", projectId, e.getMessage());
            throw new RuntimeException("프로젝트 삭제 중 오류 발생", e);
        }
    }

    /**
     * 폴더 항목 삭제 (하위 항목 포함)
     *
     * @param itemId 삭제할 폴더 ID
     */
    @Transactional
    public void deleteFolderItem(Long itemId) {
        try {
            // 하위 항목 검색
            List<ProjectItems> childItems = projectMapper.findByParentId(itemId);
            List<Long> itemIds = childItems.stream()
                    .map(ProjectItems::getId) // ProjectItems 객체에서 getId() 값을 추출
                    .toList(); // 추출된 값들을 List<Long>으로 변환

            for (Long id : itemIds) {
                projectMapper.deleteApiByItemId(id);
                deleteItemRecursively(id);
            }

            // 현재 항목 삭제
            projectMapper.deleteByItemId(itemId);
            log.info("폴더 삭제 완료: {}", itemId);

            ProjectItems item = projectMapper.getItemByItemId(itemId);

            sendProjectMessage(item.getProjectId(), "UPDATE_PROJECT_ITEM");

        } catch (Exception e) {
            log.error("폴더 삭제 실패 (ID={}): {}", itemId, e.getMessage());
            throw new RuntimeException("폴더 삭제 중 오류 발생", e);
        }
    }

    /**
     * 프로젝트 내 특정 항목 및 하위 항목을 재귀적으로 삭제
     *
     * @param itemId 삭제할 항목 ID
     */
    @Transactional
    private void deleteItemRecursively(Long itemId) {
        try {
            // 하위 항목 검색
            List<ProjectItems> childItems = projectMapper.findByParentId(itemId);

            // 하위 항목 삭제
            for (ProjectItems child : childItems) {
                projectMapper.deleteApiByItemId(child.getId());
                deleteItemRecursively(child.getId());
            }

            // 현재 항목 삭제
            projectMapper.deleteByItemId(itemId);
            log.info("항목 삭제 완료 (Item ID: {})", itemId);
        } catch (Exception e) {
            log.error("항목 삭제 실패 (Item ID={}): {}", itemId, e.getMessage());
            throw new RuntimeException("항목 삭제 중 오류 발생", e);
        }
    }

    /**
     * 새 폴더 추가
     *
     * @param projectItems 생성할 폴더 정보
     */
    @Transactional
    public void addFolder(ProjectItems projectItems) {
        try {
            Integer nextItemOrder = projectMapper.getNextItemOrder(projectItems.getProjectId(), projectItems.getParentId());

            ProjectItems newFolder = new ProjectItems();
            newFolder.setProjectId(projectItems.getProjectId());
            newFolder.setParentId(projectItems.getParentId());
            newFolder.setType("folder");
            newFolder.setName(projectItems.getName());
            newFolder.setDepth(projectItems.getDepth());
            newFolder.setCreateAt(LocalDateTime.now().toString());
            newFolder.setItemOrder(nextItemOrder);

            projectMapper.insertProjectItem(newFolder);
            sendProjectMessage(newFolder.getProjectId(), "UPDATE_PROJECT_ITEM");

            log.info("새 폴더 추가 완료: {} (Project ID: {})", newFolder.getName(), newFolder.getProjectId());
        } catch (Exception e) {
            log.error("폴더 추가 실패: {}", e.getMessage());
            throw new RuntimeException("폴더 추가 중 오류 발생", e);
        }
    }

    /**
     * 프로젝트 내 API 항목 추가
     *
     * @param projectItems API 항목 정보
     * @return 생성된 API 항목 객체
     */
    @Transactional
    public ProjectItems addProjectItemApi(ProjectItems projectItems) {
        try {
            Integer nextItemOrder = projectMapper.getNextItemOrder(projectItems.getProjectId(), projectItems.getParentId());

            ProjectItems newApi = new ProjectItems();
            newApi.setProjectId(projectItems.getProjectId());
            newApi.setParentId(projectItems.getParentId());
            newApi.setType("api");
            newApi.setName(projectItems.getName());
            newApi.setDepth(projectItems.getDepth());
            newApi.setCreateAt(LocalDateTime.now().toString());
            newApi.setItemOrder(nextItemOrder);

            projectMapper.insertProjectItem(newApi);
            log.info("새 API 항목 추가 완료: {} (Project ID: {})", newApi.getName(), newApi.getProjectId());

            return projectMapper.getProjectItemsById(newApi.getId());
        } catch (Exception e) {
            log.error("API 항목 추가 실패: {}", e.getMessage());
            throw new RuntimeException("API 항목 추가 중 오류 발생", e);
        }
    }

    /**
     * 프로젝트 ID로 프로젝트 항목 조회
     *
     * @param projectId 프로젝트 ID
     * @return 프로젝트 항목 리스트
     */
    public List<ProjectItems> getProjectItemsByProjectId(Long projectId) {
        return projectMapper.findByProjectId(projectId);
    }

    /**
     * 프로젝트 내 API 저장
     *
     * @param item 저장할 API 항목
     */
    @Transactional
    public void saveApi(ProjectItems item) {
        try {
            projectMapper.insertApi(item);
            log.info("API 저장 완료: {} (Project ID: {})", item.getName(), item.getProjectId());
        } catch (Exception e) {
            log.error("API 저장 실패: {}", e.getMessage());
            throw new RuntimeException("API 저장 중 오류 발생", e);
        }
    }

    /**
     * 프로젝트 항목 검색
     *
     * @param projectId 프로젝트 ID
     * @param query 검색어
     * @param type 항목 유형
     * @param method 검색 방법
     * @return 검색된 프로젝트 항목 리스트
     */
    public List<ProjectItems> searchProjectItems(Long projectId, String query, String type, String method) {
        return projectMapper.searchProjectItems(projectId, query, type, method);
    }

    /**
     * 프로젝트 ID로 프로젝트 조회
     *
     * @param projectId 프로젝트 ID
     * @return 조회된 프로젝트 객체
     */
    public Project getProjectByProjectId(Long projectId) {
        return projectMapper.getProjectByProjectId(projectId);
    }

    /**
     * 프로젝트 항목 이름 업데이트
     *
     * @param projectItems 업데이트할 프로젝트 항목
     * @return 업데이트된 프로젝트 항목 객체
     */
    @Transactional
    public ProjectItems updateProjectItemName(ProjectItems projectItems) {
        try {
            projectMapper.updateProjectItemName(projectItems);
            sendProjectMessage(projectItems.getProjectId(), "UPDATE_PROJECT_ITEM");
            return projectMapper.getProjectItemsById(projectItems.getId());
        } catch (Exception e) {
            log.error("프로젝트 항목 이름 업데이트 실패: {}", e.getMessage());
            throw new RuntimeException("프로젝트 항목 이름 업데이트 중 오류 발생", e);
        }
    }

    /**
     * 프로젝트 항목 이동 (부모 ID 변경)
     *
     * @param projectItems 이동할 항목 정보
     */
    @Transactional
    public void updateParentId(ProjectItems projectItems) {
        try {
            Long projectId = projectMapper.getItemByItemId(projectItems.getId()).getProjectId();
            Integer itemOrder = projectMapper.getNextItemOrder(projectId, projectItems.getParentId());
            projectItems.setItemOrder(itemOrder);

            projectMapper.updateParentId(projectItems);
            sendProjectMessage(projectId, "UPDATE_PROJECT_ITEM");

            log.info("프로젝트 항목 이동 완료: {} (Project ID: {})", projectItems.getName(), projectId);
        } catch (Exception e) {
            log.error("프로젝트 항목 이동 실패: {}", e.getMessage());
            throw new RuntimeException("프로젝트 항목 이동 중 오류 발생", e);
        }
    }

    /**
     * 프로젝트 항목 삭제
     *
     * @param itemId 삭제할 항목 ID
     */
    @Transactional
    public void deleteItemById(Long itemId, String userEmail) {
        try {
            ProjectItems item = projectMapper.getItemByItemId(itemId);
            projectMapper.deleteApiByItemId(itemId);
            projectMapper.deleteByItemId(itemId);

            String logKey = MDC.get("LOG_KEY");

            LoggingUtil.logTransactionStep(logKey, userEmail, "1. 사용자가 선택한 프로젝트 하위 항목 " + itemId + "번 삭제");

            sendProjectMessage(item.getProjectId(), "UPDATE_PROJECT_ITEM");
            log.info("프로젝트 항목 삭제 완료 (Item ID: {})", itemId);
        } catch (Exception e) {
            log.error("프로젝트 항목 삭제 실패 (Item ID={}): {}", itemId, e.getMessage());
            throw new RuntimeException("프로젝트 항목 삭제 중 오류 발생", e);
        }
    }

    /**
     * 항목 순서 업데이트 (드래그 앤 드롭)
     *
     * @param draggedItemId  이동할 항목 ID
     * @param targetParentId 목표 부모 ID
     * @param targetOrder    목표 순서
     */
    @Transactional
    public void updateItemOrder(Long draggedItemId, Long targetParentId, Integer targetOrder) {
        try {
            ProjectItems item = projectMapper.getItemByItemId(draggedItemId);
            projectMapper.incrementItemOrder(targetParentId, targetOrder, item.getProjectId());
            projectMapper.updateItemOrder(draggedItemId, targetOrder, targetParentId);

            sendProjectMessage(item.getProjectId(), "UPDATE_PROJECT_ITEM");
            log.info("항목 순서 업데이트 완료 (Item ID: {}, Parent ID: {}, Order: {})", draggedItemId, targetParentId, targetOrder);
        } catch (Exception e) {
            log.error("항목 순서 업데이트 실패: {}", e.getMessage());
            throw new RuntimeException("항목 순서 업데이트 중 오류 발생", e);
        }
    }


    /**
     * 항목 ID로 항목 조회
     *
     * @param itemId 항목 ID
     * @return 조회된 항목 객체
     */
    public ProjectItems getItemByItemId(Long itemId) {
        String logKey = MDC.get("LOG_KEY");
        String userEmail = MDC.get("USER_EMAIL");

        LoggingUtil.logTransactionStep(logKey, userEmail, "1. 프로젝트 하위 항목 ID로 하위 항목 조회");
        return projectMapper.getItemByItemId(itemId);
    }

    /**
     * 프로젝트 초대 코드 생성
     *
     * @param projectId 프로젝트 ID
     * @param userEmail 사용자 이메일
     * @return 생성된 초대 코드
     */
    public String generateInviteCode(Long projectId, String userEmail) {
        String inviteCode = UUID.randomUUID().toString().substring(0, 6);
        InviteCode code = new InviteCode();
        code.setProjectId(projectId);
        code.setUserEmail(userEmail);
        code.setCode(inviteCode);
        code.setExpiryTime(LocalDateTime.now().plusHours(1));

        String logKey = MDC.get("LOG_KEY");

        LoggingUtil.logTransactionStep(logKey, userEmail, "1. 랜덤한 초대 코드 UUID 6자리 생성");
        LoggingUtil.logTransactionStep(logKey, userEmail, "2. InviteCode 객체에 초대할 프로젝트 ID, " +
                "초대할 유저 이메일, 생성한 코드, 유효 기간 설정");
        LoggingUtil.logTransactionStep(logKey, userEmail, "3. 초대 코드 생성 완료");

        projectMapper.insertInviteCode(code);
        log.info("초대 코드 생성 (Project ID={}, Code={})", projectId, inviteCode);
        return inviteCode;
    }

    /**
     * 초대 코드 검증
     *
     * @param inviteCode 입력된 초대 코드
     * @param loginEmail 현재 로그인한 사용자 이메일
     * @return 유효성 검증 결과 (Map 형태)
     */
    public Map<String, Object> validateInviteCode(String inviteCode, String loginEmail) {
        Map<String, Object> response = new HashMap<>();
        InviteCode code = projectMapper.findByCode(inviteCode);

        if (code == null || code.isExpired() || !code.isAvailability()) {
            response.put("errorMessage", "유효하지 않은 코드입니다.");
            log.warn("초대 코드 검증 실패: {}", inviteCode);
            return response;
        }

        Long userId = userMapper.findByEmail(code.getUserEmail()).getId();
        Long loginUserId = userMapper.findByEmail(loginEmail).getId();
        Long projectId = code.getProjectId();

        if (projectMapper.getParticipantByProjectIdAndUserId(projectId, userId) != null) {
            response.put("errorMessage", "이미 참여한 프로젝트입니다.");
            return response;
        }

        if (projectMapper.getProjectByProjectId(projectId).getUserId().equals(loginUserId)) {
            response.put("errorMessage", "자신의 프로젝트에는 참여할 수 없습니다.");
            return response;
        }

        code.setAvailability(false);
        projectMapper.updateInviteCode(code);
        response.put("projectId", projectId);
        return response;
    }

    /**
     * 프로젝트에 참여자 추가
     *
     * @param userEmail 사용자 이메일
     * @param projectId 프로젝트 ID
     * @return 추가된 프로젝트 참여자 객체
     */
    @Transactional
    public ProjectParticipants addParticipant(String userEmail, Long projectId) {
        User user = userMapper.findByEmail(userEmail);
        String projectName = projectMapper.getProjectByProjectId(projectId).getName();

        ProjectParticipants participant = new ProjectParticipants();
        participant.setProjectId(projectId);
        participant.setName(projectName);
        participant.setUserId(user.getId());
        participant.setRole("participant");
        participant.setPermissionLevel("read");

        projectMapper.insertParticipant(participant);
        sendProjectMessage(projectId, "NEW_PARTICIPANT");
        log.info("프로젝트 참가자 추가 (User={}, Project={})", userEmail, projectId);
        return projectMapper.getParticipantsById(participant.getId());
    }

    /**
     * 프로젝트 참여자 목록 조회
     *
     * @param projectId 프로젝트 ID
     * @return 프로젝트 참여자 리스트
     */
    public List<ProjectParticipants> getParticipantsByProjectId(Long projectId) {
        String logKey = MDC.get("LOG_KEY");
        String userEmail = MDC.get("USER_EMAIL");

        LoggingUtil.logTransactionStep(logKey, userEmail, "1. 선택 프로젝트 참여자 목록 조회");

        return projectMapper.getParticipantsByProjectId(projectId);
    }

    /**
     * 프로젝트 참여자 정보 업데이트
     *
     * @param projectId 프로젝트 ID
     * @param participants 업데이트할 참여자 리스트
     */
    @Transactional
    public void updateParticipants(Long projectId, List<ProjectParticipants> participants) {
        for (ProjectParticipants participant : participants) {
            projectMapper.updateParticipant(projectId, participant);
            notificationService.notifyProjectParticipants(participant.getUserId(), projectId, "UPDATE_AUTH");
        }
        log.info("프로젝트 참가자 정보 업데이트 완료 (Project ID: {})", projectId);
    }

    /**
     * 특정 사용자와 프로젝트의 참여 정보 조회
     *
     * @param projectId 프로젝트 ID
     * @param userId 사용자 ID
     * @return 프로젝트 참여자 객체
     */
    public ProjectParticipants getParticipantByProjectIdAndUserId(Long projectId, Long userId) {
        return projectMapper.getParticipantByProjectIdAndUserId(projectId, userId);
    }

    /**
     * 프로젝트 참여자 제거
     *
     * @param projectId 프로젝트 ID
     * @param participantId 제거할 참여자 ID
     */
    @Transactional
    public void removeParticipant(Long projectId, Long participantId) {
        try {
            ProjectParticipants participant = projectMapper.getParticipantsById(participantId);
            User user = userMapper.findById(participant.getUserId());
            Project project = projectMapper.getProjectByProjectId(projectId);

            projectMapper.removeParticipant(projectId, participantId);
            notificationService.notifyProjectParticipants(user.getId(), projectId,
                    project.getName() + " 프로젝트에서 강퇴당했습니다.");
            log.info("프로젝트 참가자 제거 완료 (User ID={}, Project ID={})", user.getId(), projectId);
        } catch (Exception e) {
            log.error("프로젝트 참가자 제거 실패: {}", e.getMessage());
            throw new RuntimeException("프로젝트 참가자 제거 중 오류 발생", e);
        }
    }

    /**
     * 프로젝트에서 참가자 탈퇴
     *
     * @param projectId 프로젝트 ID
     * @param userEmail 탈퇴할 사용자 이메일
     */
    @Transactional
    public void exitParticipant(Long projectId, String userEmail) {
        try {
            User user = userMapper.findByEmail(userEmail);
            ProjectParticipants participant = projectMapper.getParticipantByProjectIdAndUserId(projectId, user.getId());
            projectMapper.removeParticipant(projectId, participant.getId());

            sendProjectMessage(projectId, "EXIT_PROJECT");
            log.info("사용자 프로젝트 탈퇴 완료 (User={}, Project ID={})", userEmail, projectId);
        } catch (Exception e) {
            log.error("프로젝트 탈퇴 실패: {}", e.getMessage());
            throw new RuntimeException("프로젝트 탈퇴 중 오류 발생", e);
        }
    }

    private void sendProjectMessage(Long projectId, String message) {
        List<ProjectParticipants> participants = projectMapper.getParticipantsByProjectId(projectId);

        for (ProjectParticipants participant : participants) {
            notificationService.notifyProjectParticipants(participant.getUserId(), projectId, message);
        }

        Project project = projectMapper.getProjectByProjectId(projectId);
        Long ownerId = project.getUserId();

        notificationService.notifyProjectParticipants(ownerId, projectId, message);
    }
}
