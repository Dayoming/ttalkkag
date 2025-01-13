package com.apitester.ttalkkag.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileService {

    private final String uploadDir;

    public FileService(@Value("${file.upload.dir}") String uploadDir) {
        this.uploadDir = uploadDir; // 환경 변수에서 경로를 읽음
    }

    public String saveProfileImage(MultipartFile file, Long userId) {
        try {
            String filename = "profile_" + userId + "_" + System.currentTimeMillis() + ".png";
            Path filePath = Paths.get(uploadDir, filename);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());
            return "/uploads/profiles/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save profile image", e);
        }
    }
}
