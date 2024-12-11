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

    public void saveApi(Apis apis) {
        apiMapper.saveApi(apis);
    }
}
