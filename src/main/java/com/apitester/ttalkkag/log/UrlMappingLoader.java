package com.apitester.ttalkkag.log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
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
@Slf4j
@Component
public class UrlMappingLoader {

    @Value("${url.mapping.file.path}")
    private String mappingFilePath;

    /**
     * -- GETTER --
     *  URL 매핑 리스트 반환 (캐싱된 데이터 사용)
     *
     * @return URL 매핑 리스트
     */
    @Getter
    private List<UrlMapping> urlMappings; // 캐싱된 URL 매핑 리스트

    /**
     * 애플리케이션 시작 시 URL 매핑을 로드
     */
    @PostConstruct
    public void init() {
        try {
            this.urlMappings = loadMappings();
            log.info("✅ URL 매핑 파일 로드 성공 (총 {}개 항목)", urlMappings.size());
        } catch (IOException e) {
            log.error("❌ URL 매핑 파일 로드 실패: {}", e.getMessage());
            throw new RuntimeException("URL 매핑 파일을 불러오는 중 오류 발생", e);
        }
    }

    /**
     * URL 매핑 정보를 JSON 파일에서 로드
     *
     * @return JSON 파일에서 로드한 URL 매핑 리스트
     * @throws IOException 파일을 읽는 중 오류 발생 시 예외 발생
     */
    List<UrlMapping> loadMappings() throws IOException {
        // JSON 파일 읽기
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(mappingFilePath);

        if (!file.exists()) {
            throw new IOException("URL 매핑 파일이 존재하지 않습니다: " + mappingFilePath);
        }

        return objectMapper.readValue(file, new TypeReference<List<UrlMapping>>() {});
    }
}
