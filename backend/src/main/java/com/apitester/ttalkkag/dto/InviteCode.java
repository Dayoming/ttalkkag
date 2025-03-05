package com.apitester.ttalkkag.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InviteCode {
    private Long id;
    private Long projectId;
    private String userEmail;
    private String code;
    private boolean availability;
    private LocalDateTime expiryTime;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
