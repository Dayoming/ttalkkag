package com.apitester.ttalkkag.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final RestTemplate restTemplate;

    public TestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 100번대 테스트 (컨티뉴 응답)
    @GetMapping("/100")
    public ResponseEntity<String> test100() {
        return ResponseEntity.status(HttpStatus.CONTINUE).body("100 CONTINUE");
    }

    // 200번대 테스트 (성공 응답)
    @GetMapping("/200")
    public ResponseEntity<String> test200() {
        return ResponseEntity.ok("200 OK");
    }

    // 300번대 테스트 (리다이렉션 응답)
    @GetMapping("/300")
    public ResponseEntity<String> test300() {
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).body("300 Multiple Choice");
    }

    // 400번대 테스트 (클라이언트 오류)
    @GetMapping("/400")
    public ResponseEntity<Map<String, String>> test400() {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "400 BAD REQUEST");
        errorResponse.put("message", "잘못된 요청입니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // 500번대 테스트 (서버 오류)
    @GetMapping("/500")
    public ResponseEntity<String> test500() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("500 INTERNAL SERVER ERROR");
    }

    @PostMapping
    public ResponseEntity<?> echoRequest(HttpEntity<Map<String, Object>> httpEntity) {
        try {
            // Body 데이터 추출
            Map<String, Object> body = httpEntity.getBody();

            // Headers 데이터 추출
            HttpHeaders headers = httpEntity.getHeaders();

            // 출력 (디버깅용)
            System.out.println("Body: " + body);
            System.out.println("Headers: " + headers);

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("body", body);
            response.put("headers", headers);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process entity: " + e.getMessage()));
        }
    }

    // 파일 업로드 테스트
    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestPart(value = "file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("파일이 비어 있습니다.");
            }

            System.out.println("handleFileUpload: " + file.getOriginalFilename());
            String fileInfo = String.format("파일명: %s, 크기: %d bytes", file.getOriginalFilename(), file.getSize());

            return ResponseEntity.ok("파일 업로드 성공: " + fileInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("업로드 처리 중 오류 발생: " + e.getMessage());
        }
    }
}
