package com.apitester.ttalkkag.mapper;

import com.apitester.ttalkkag.dto.Dataset;
import com.apitester.ttalkkag.dto.DatasetVariable;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DatasetMapper {
    List<Dataset> findAllDatasets(Long projectId);
    List<DatasetVariable> findVariablesByDatasetId(Long id);
    List<Dataset> searchDatasets(String query);
    List<Dataset> findDatasetsByProjectId(Long projectId);
    Dataset findDatasetById(Long id);
    void insertDataset(Dataset dataset);
    void deleteDataset(Long id);
    void deleteDatasetsByProjectId(Long projectId);
    void updateDataset(Dataset dataset);
}
