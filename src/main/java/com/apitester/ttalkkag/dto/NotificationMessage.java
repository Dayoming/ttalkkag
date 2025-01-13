package com.apitester.ttalkkag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationMessage {
    private String type; // 메시지 타입
    private String content; // 메시지 내용
    private Object data; // 추가 데이터
}
