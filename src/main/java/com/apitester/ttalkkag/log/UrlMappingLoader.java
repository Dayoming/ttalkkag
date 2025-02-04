package com.apitester.ttalkkag.log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * URL 매핑 로더 클래스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description `url-mapping.json` 파일을 읽어 URL 매핑 정보를 로드하는 클래스.
 *              JSON 파일을 `List<UrlMapping>` 객체로 변환하여 반환
 */
@Component
public class UrlMappingLoader {

    /**
     * URL 매핑 정보를 JSON 파일에서 로드
     *
     * @return JSON 파일에서 로드한 URL 매핑 리스트
     * @throws IOException 파일을 읽는 중 오류 발생 시 예외 발생
     */
    List<UrlMapping> loadMappings() throws IOException {
        // JSON 파일 읽기
        ObjectMapper objectMapper = new ObjectMapper();
        // ClassPathResource를 통해 파일 읽기
        return objectMapper.readValue(
                new ClassPathResource("url-mapping.json").getInputStream(),
                new TypeReference<List<UrlMapping>>() {}
        );
    }
}
