package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.dto.ProxyRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
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
