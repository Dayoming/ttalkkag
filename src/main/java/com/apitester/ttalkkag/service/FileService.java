package com.apitester.ttalkkag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

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

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");
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

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
            }
            String extension = getFileExtension(originalFilename);
            if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                log.error("지원되지 않는 파일 확장자: {}", extension);
                throw new IllegalArgumentException("지원되지 않는 파일 형식입니다. PNG, JPG, JPEG, GIF, WEBP만 가능합니다.");
            }

            // 고유한 파일명 생성
            String filename = "profile_" + userId + "_" + UUID.randomUUID() + "." + extension;
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

    /**
     * 파일 이름에서 확장자를 추출하는 메서드
     *
     * @param filename 원본 파일명
     * @return 확장자 (예: png, jpg)
     */
    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf(".");
        if (lastIndex == -1 || lastIndex == filename.length() - 1) {
            throw new IllegalArgumentException("확장자가 없는 파일입니다.");
        }
        return filename.substring(lastIndex + 1);
    }
}
