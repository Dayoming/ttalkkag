package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.dto.ProxyRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/proxy")
@Validated
public class ProxyController {

    private final RestTemplate restTemplate;

    public ProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping
    public ResponseEntity<?> proxyRequest(@Valid @RequestPart(value = "request") ProxyRequest request,
                                          @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (request.getHeaders() != null) {
                request.getHeaders().forEach(headers::set);
            }

            HttpEntity<?> entity;

            // 파일이 존재하는 경우 Multipart 요청 구성
            if (file != null) {
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("file", new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename(); // 원본 파일명 유지
                    }
                });

                // JSON 데이터도 함께 전송
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonRequest = objectMapper.writeValueAsString(request);
                body.add("request", new HttpEntity<>(jsonRequest, headers));

                entity = new HttpEntity<>(body, headers);
            } else {
                // 파일이 없으면 기존 방식 유지
                entity = new HttpEntity<>(request.getBody(), headers);
            }

            ResponseEntity<String> response = restTemplate.exchange(
                    request.getUrl(),
                    HttpMethod.valueOf(request.getMethod()),
                    entity,
                    String.class
            );

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Client error: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Server error: " + e.getMessage());
        } catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid URL: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Proxy request failed: " + e.getMessage());
        }
    }
}

