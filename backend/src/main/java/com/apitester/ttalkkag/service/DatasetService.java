package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.Dataset;
import com.apitester.ttalkkag.dto.DatasetVariable;
import com.apitester.ttalkkag.mapper.DatasetMapper;
import com.apitester.ttalkkag.mapper.DatasetVariableMapper;
import com.apitester.ttalkkag.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 데이터셋 관리 서비스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description 데이터셋 생성, 조회, 수정, 삭제 및 관련 변수 관리 기능을 제공하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
public class DatasetService {

    private final DatasetMapper datasetMapper;
    private final DatasetVariableMapper datasetVariableMapper;
    private final UserMapper userMapper;

    /**
     * 사용자가 소유한 모든 데이터셋 조회
     *
     * @param userEmail 사용자 이메일
     * @return 사용자의 데이터셋 목록
     */
    public List<Dataset> getAllDatasets(String userEmail) {
        Long userId = userMapper.findByEmail(userEmail).getId();
        return datasetMapper.findAllDatasets(userId);
    }

    /**
     * 특정 데이터셋 조회
     *
     * @param id 데이터셋 ID
     * @return 데이터셋 정보 (Map 형태)
     */
    public Map<String, Object> getDatasetById(Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("dataset", datasetMapper.findDatasetById(id));
        return response;
    }

    /**
     * 프로젝트 내 모든 데이터셋과 해당 변수 조회
     *
     * @param projectId 프로젝트 ID
     * @return 데이터셋 목록 (각 데이터셋에 변수 포함)
     */
    public List<Dataset> getAllDatasetsWithVariables(Long projectId) {
        List<Dataset> datasets = datasetMapper.findAllDatasets(projectId);
        for (Dataset dataset : datasets) {
            List<DatasetVariable> variables = datasetMapper.findVariablesByDatasetId(dataset.getId());
            dataset.setVariables(variables);
        }
        return datasets;
    }

    /**
     * 데이터셋 검색
     *
     * @param query 검색어
     * @return 검색된 데이터셋 목록
     */
    public List<Dataset> searchDatasets(String query) {
        return datasetMapper.searchDatasets(query);
    }

    /**
     * 새로운 데이터셋 추가
     *
     * @param dataset 추가할 데이터셋 객체
     * @return 성공 또는 실패 메시지를 포함한 Map
     */
    @Transactional
    public Map<String, Object> addDataset(Dataset dataset) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 데이터셋 정보 설정
            datasetMapper.insertDataset(dataset);

            // 변수 리스트 추가
            for (DatasetVariable variable : dataset.getVariables()) {
                variable.setDatasetId(dataset.getId());
                datasetVariableMapper.insertVariable(variable);
            }

            response.put("message", "Dataset " + dataset.getName() + "이(가) 정상적으로 추가되었습니다.");
        } catch (Exception e) {
            response.put("errorMessage", "Error: " + e.getMessage());
        }
        return response;
    }

    /**
     * 데이터셋 삭제
     *
     * @param id 삭제할 데이터셋 ID
     * @return 성공 또는 실패 메시지를 포함한 Map
     */
    @Transactional
    public Map<String, Object> deleteDataset(Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 데이터셋 변수 삭제
            datasetVariableMapper.deleteVariableByDatasetId(id);

            // 데이터셋 삭제
            datasetMapper.deleteDataset(id);
            response.put("message", "Dataset이 정상적으로 삭제되었습니다.");
        } catch (Exception e) {
            response.put("errorMessage", "Dataset 삭제 중 오류가 발생했습니다.");
        }
        return response;
    }

    /**
     * 데이터셋 수정
     *
     * @param id 데이터셋 ID
     * @param dataset 업데이트할 데이터셋 객체
     * @return 성공 또는 실패 메시지를 포함한 Map
     */
    @Transactional
    public Map<String, Object> updateDataset(Long id, Dataset dataset) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 기존 데이터셋 조회 및 업데이트
            Dataset existingDataset = datasetMapper.findDatasetById(id);
            existingDataset.setName(dataset.getName());
            existingDataset.setDescription(dataset.getDescription());
            datasetMapper.updateDataset(existingDataset);

            // 기존 변수 삭제 후 새 변수 추가
            datasetVariableMapper.deleteVariableByDatasetId(id);
            for (DatasetVariable variable : dataset.getVariables()) {
                variable.setDatasetId(id);
                datasetVariableMapper.insertVariable(variable);
            }

            response.put("message", "정상적으로 수정되었습니다.");
        } catch (Exception e) {
            response.put("errorMessage", "Dataset 수정 중 오류가 발생했습니다.");
        }

        return response;
    }

    /**
     * 특정 데이터셋의 변수 목록 조회
     *
     * @param datasetId 데이터셋 ID
     * @return 해당 데이터셋의 변수 목록
     */
    public List<DatasetVariable> getDatasetVariablesByDatasetId(Long datasetId) {
        return datasetMapper.findVariablesByDatasetId(datasetId);
    }
}
