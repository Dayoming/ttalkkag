package com.apitester.ttalkkag.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 글로벌 예외 처리 핸들러
 *
 * @author 정다영
 * @date 2025-02-06
 * @description `@Valid` 유효성 검증 및 기타 예외를 처리하는 글로벌 핸들러 클래스.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * `@Valid` 검증 실패 시 발생하는 예외를 처리
     *
     * @param ex `MethodArgumentNotValidException` 예외 객체
     * @return 유효성 검증 실패 메시지
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Bad Request 반환
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // 발생한 유효성 검증 오류를 필드별로 매핑
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * `@RequestParam`, `@PathVariable`, `@NotBlank` 등의 검증 실패 예외 처리
     *
     * @param ex `ConstraintViolationException` 예외 객체
     * @return 검증 실패 메시지
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Bad Request 반환
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        });

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * 기타 모든 예외 처리
     *
     * @param ex 처리되지 않은 예외
     * @return 오류 메시지
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500 Internal Server Error 반환
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("errorMessage", "서버 내부 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
