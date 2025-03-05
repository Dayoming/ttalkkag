package com.apitester.ttalkkag.service;

import com.apitester.ttalkkag.layout.Message;
import com.apitester.ttalkkag.layout.StatusEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ResponseService {
    public ResponseEntity<Message> success(Object data) {
        return new ResponseEntity<>(new Message(StatusEnum.OK.getCode(), "요청이 성공적으로 처리되었습니다.", data),
                HttpStatus.OK);
    }

    public ResponseEntity<Message> successNoContent() {
        return new ResponseEntity<>(new Message(StatusEnum.NO_CONTENT.getCode(), "요청은 성공적으로 처리되었으나 반환 값이 없는 요청입니다.", null),
                HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<Message> error(StatusEnum status, String errorMessage) {
        return new ResponseEntity<>(new Message(status.getCode(), errorMessage, null),
                HttpStatus.valueOf(status.getStatusCode()));
    }
}
