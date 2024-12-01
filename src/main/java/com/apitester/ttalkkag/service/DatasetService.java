package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.entity.Dataset;
import com.apitester.ttalkkag.entity.DatasetVariable;
import com.apitester.ttalkkag.mapper.DatasetMapper;
import com.apitester.ttalkkag.mapper.DatasetVariableMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DatasetService {

    private final DatasetMapper datasetMapper;
    private final DatasetVariableMapper datasetVariableMapper;

    public List<Dataset> getAllDatasets() {
        return datasetMapper.findAllDatasets();
    }

    public Map<String, Object> getDatasetById(Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("dataset", datasetMapper.findDatasetById(id));
        return response;
    }

    public List<Dataset> getAllDatasetsWithVariables() {
        List<Dataset> datasets = datasetMapper.findAllDatasets();
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

    public Map<String, Object> addDataset(Dataset dataset, List<DatasetVariable> variables) {
        Map<String, Object> response = new HashMap<>();
        datasetMapper.insertDataset(dataset);

        for (DatasetVariable variable : variables) {
            variable.setDatasetId(dataset.getId());
            datasetVariableMapper.insertVariable(variable);
        }

        response.put("message", "Dataset이 정상적으로 추가되었습니다.");
        return response;
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

}
