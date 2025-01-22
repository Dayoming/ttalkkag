package com.apitester.ttalkkag.log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Component
public class UrlMappingLoader {

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
