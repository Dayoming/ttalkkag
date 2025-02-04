package com.apitester.ttalkkag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 파일 관리 서비스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description 프로필 이미지 업로드 및 저장 기능을 제공하는 서비스 클래스.
 */
@Slf4j
@Service
public class FileService {

    private final String uploadDir;

    /**
     * 파일 저장 디렉토리 설정
     *
     * @param uploadDir 환경 변수에서 설정된 업로드 디렉토리 경로
     */
    public FileService(@Value("${file.upload.dir}") String uploadDir) {
        this.uploadDir = uploadDir; // 환경 변수에서 경로를 읽음
    }

    /**
     * 사용자의 프로필 이미지를 저장
     *
     * @param file 업로드된 이미지 파일
     * @param userId 사용자 ID
     * @return 저장된 이미지 파일의 URL 경로
     */
    public String saveProfileImage(MultipartFile file, Long userId) {
        try {
            // 고유한 파일명 생성
            String filename = "profile_" + userId + "_" + System.currentTimeMillis() + ".png";
            Path filePath = Paths.get(uploadDir, filename);

            // 디렉토리가 존재하지 않으면 생성
            Files.createDirectories(filePath.getParent());

            // 파일 저장
            Files.write(filePath, file.getBytes());

            // 저장된 파일 경로 로깅
            log.info("Profile image saved: {}", filePath);

            // 클라이언트가 접근할 수 있는 URL 반환
            return "/uploads/profiles/" + filename;
        } catch (IOException e) {
            log.error("Failed to save profile image for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to save profile image", e);
        }
    }
}
