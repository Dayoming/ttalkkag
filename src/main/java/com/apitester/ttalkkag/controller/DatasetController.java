package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.dto.Dataset;
import com.apitester.ttalkkag.dto.DatasetVariable;
import com.apitester.ttalkkag.service.DatasetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dataset")
@RequiredArgsConstructor
@Validated
public class DatasetController {

    private final DatasetService datasetService;

    @GetMapping("/getAllDatasets")
    public List<Dataset> getAllDatasets(@AuthenticationPrincipal @NotBlank String userEmail) {
        return datasetService.getAllDatasets(userEmail);
    }

    @GetMapping("/getAllDatasetsWithVariables/{projectId}")
    public List<Dataset> getAllDatasetsWithVariables(@PathVariable Long projectId) {
        return datasetService.getAllDatasetsWithVariables(projectId);
    }

    @GetMapping("/variables/{datasetId}")
    public List<DatasetVariable> getDatasetVariablesByDatasetId(@PathVariable Long datasetId) {
        return datasetService.getDatasetVariablesByDatasetId(datasetId);
    }

    @PostMapping("/addDataset")
    public Map<String, Object> addDataset(@RequestBody Dataset dataset) {
        return datasetService.addDataset(dataset);
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
    public Map<String, Object> updateDataset(@PathVariable Long id, @Valid @RequestBody Dataset dataset) {
        return datasetService.updateDataset(id, dataset);
    }

    @GetMapping("/search")
    public List<Dataset> searchDatasets(@RequestParam("query") String query) {
        return datasetService.searchDatasets(query);
    }

}
