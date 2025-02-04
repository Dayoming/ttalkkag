package com.apitester.ttalkkag.mapper;

import com.apitester.ttalkkag.dto.Apis;
import com.apitester.ttalkkag.dto.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ApiMapper {
    void saveApi(Apis api);
    Apis loadApi(Long itemId);
    Apis findById(Long id);
    void updateApi(Apis apis);
}
