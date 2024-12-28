package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.dto.Apis;
import com.apitester.ttalkkag.mapper.ApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ApiService {

    private final ApiMapper apiMapper;

    public Apis saveApi(Apis apis) {
        apiMapper.saveApi(apis);
        
        // 생성된 ID로 데이터 조회
        return apiMapper.findById(apis.getId());
    }

    public Apis loadApi(Long itemId) {
        return apiMapper.loadApi(itemId);
    }

    public void updateApi(Apis apis) {
        apiMapper.updateApi(apis);
    }
}
