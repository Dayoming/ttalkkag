package com.apitester.ttalkkag.mapper;

import com.apitester.ttalkkag.dto.Apis;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiMapper {
    void saveApi(Apis api);
    Apis loadApi(Long itemId);
}
