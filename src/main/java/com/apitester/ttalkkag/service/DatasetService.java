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
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DatasetService {

    private final DatasetMapper datasetMapper;
    private final DatasetVariableMapper datasetVariableMapper;
    private final UserMapper userMapper;

    public List<Dataset> getAllDatasets(String userEmail) {
        Long userId = userMapper.findByEmail(userEmail).getId();
        return datasetMapper.findAllDatasets(userId);
    }

    public Map<String, Object> getDatasetById(Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("dataset", datasetMapper.findDatasetById(id));
        return response;
    }

    public List<Dataset> getAllDatasetsWithVariables(String userEmail) {
        Long userId = userMapper.findByEmail(userEmail).getId();
        List<Dataset> datasets = datasetMapper.findAllDatasets(userId);
        for (Dataset dataset : datasets) {
            List<DatasetVariable> variables = datasetMapper.findVariablesByDatasetId(dataset.getId());
            System.out.println(variables);
            dataset.setVariables(variables);
        }
        return datasets;
    }

    public List<Dataset> searchDatasets(String query) {
        return datasetMapper.searchDatasets(query);
    }

    public Map<String, Object> addDataset(String userEmail, Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = userMapper.findByEmail(userEmail).getId();
            // RequestBody에서 Dataset 정보와 변수 리스트를 추출
            Dataset dataset = new Dataset();
            dataset.setUserId(userId);
            dataset.setName((String) requestData.get("name"));
            dataset.setDescription((String) requestData.get("description"));

            List<DatasetVariable> variables = ((List<Map<String, String>>) requestData.get("variables"))
                    .stream()
                    .map(variableData -> {
                        DatasetVariable variable = new DatasetVariable();
                        variable.setType(variableData.get("type"));
                        variable.setName(variableData.get("name"));
                        variable.setDescription(variableData.get("description"));
                        return variable;
                    })
                    .collect(Collectors.toList());

            datasetMapper.insertDataset(dataset);

            for (DatasetVariable variable : variables) {
                variable.setDatasetId(dataset.getId());
                datasetVariableMapper.insertVariable(variable);
            }

            response.put("message", "Dataset " + dataset.getName() + "이(가) 정상적으로 추가되었습니다.");
            return response;
        } catch (Exception e) {
            response.put("errorMessage", "Error: " + e.getMessage());
            return response;
        }
    }

    public Map<String, Object> deleteDataset(Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Dataset이 정상적으로 삭제되었습니다.");
        datasetMapper.deleteDataset(id);
        return response;
    }

    public Map<String, Object> updateDataset(Long id, Dataset dataset) {
        Map<String, Object> response = new HashMap<>();
        // 기존 데이터셋 조회
        Dataset existingDataset = datasetMapper.findDatasetById(id);

        // 데이터셋 정보 업데이트
        existingDataset.setName(dataset.getName());
        existingDataset.setDescription(dataset.getDescription());
        datasetMapper.updateDataset(existingDataset);

        // 기존 변수 삭제
        datasetVariableMapper.deleteVariableByDatasetId(id);

        // 새로운 변수 추가
        for (DatasetVariable variable : dataset.getVariables()) {
            variable.setDatasetId(id); // 외래키 설정
            datasetVariableMapper.insertVariable(variable);
        }
        response.put("message", "정상적으로 수정되었습니다.");
        return response;
    }

    public List<DatasetVariable> getDatasetVariablesByDatasetId(Long datasetId) {
        return datasetMapper.findVariablesByDatasetId(datasetId);
    }
}
