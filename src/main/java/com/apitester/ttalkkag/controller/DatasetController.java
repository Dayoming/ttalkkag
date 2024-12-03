package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.dto.Dataset;
import com.apitester.ttalkkag.dto.DatasetVariable;
import com.apitester.ttalkkag.service.DatasetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dataset")
@RequiredArgsConstructor
public class DatasetController {

    private final DatasetService datasetService;

    @GetMapping("/getAllDatasets")
    public List<Dataset> getAllDatasets() {
        return datasetService.getAllDatasets();
    }

    @GetMapping("/getAllDatasetsWithVariables")
    public List<Dataset> getAllDatasetsWithVariables() {
        return datasetService.getAllDatasetsWithVariables();
    }

    @PostMapping("/addDataset")
    public Map<String, Object> addDataset(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            // RequestBody에서 Dataset 정보와 변수 리스트를 추출
            Dataset dataset = new Dataset();
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

            datasetService.addDataset(dataset, variables);
            response.put("message", "Dataset " + dataset.getName() + "이(가) 정상적으로 추가되었습니다.");
            return response;
        } catch (Exception e) {
            response.put("errorMessage", "Error: " + e.getMessage());
            return response;
        }
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteDataset(@PathVariable Long id) {
        return datasetService.deleteDataset(id);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getDatasetById(@PathVariable Long id) {
        return datasetService.getDatasetById(id);
    }

    @PutMapping("/update/{id}")
    public Map<String, Object> updateDataset(@PathVariable Long id, @RequestBody Dataset dataset) {
        return datasetService.updateDataset(id, dataset);
    }

    @GetMapping("/search")
    public List<Dataset> searchDatasets(@RequestParam("query") String query) {
        return datasetService.searchDatasets(query);
    }

}
