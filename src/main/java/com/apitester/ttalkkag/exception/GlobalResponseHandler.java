package com.apitester.ttalkkag.exception;

import com.apitester.ttalkkag.config.MessageUtil;
import com.apitester.ttalkkag.layout.Message;
import com.apitester.ttalkkag.layout.StatusEnum;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 응답 처리 및 예외 핸들러
 *
 * @author 정다영
 * @date 2025-03-04
 * @description 모든 API 응답을 표준 포맷으로 감싸고, 예외를 처리하는 글로벌 핸들러.
 */
@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    private final MessageUtil messageUtil;

    public GlobalResponseHandler(MessageUtil messageUtil) {
        this.messageUtil = messageUtil;
    }

    /**
     * API 응답을 감싸는 여부 설정
     * Message 객체는 이미 감싸져 있으므로 제외
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return !returnType.getParameterType().equals(Message.class);
    }

    /**
     * API 응답을 표준 메시지 형식으로 감싸기
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, org.springframework.http.MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  org.springframework.http.server.ServerHttpRequest request,
                                  org.springframework.http.server.ServerHttpResponse response) {

        if (body instanceof Message) {
            return body;
        }

        if (body == null) {
            return new Message(StatusEnum.NO_CONTENT.getCode(), messageUtil.getMessage("response.no_content"), null);
        }

        return new Message(StatusEnum.OK.getCode(), "response.ok", body);
    }

    /**
     * `@Valid` 검증 실패 시 발생하는 예외를 처리
     *
     * @param ex `MethodArgumentNotValidException` 예외 객체
     * @return 유효성 검증 실패 메시지
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Bad Request 반환
    public ResponseEntity<Message> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // 발생한 유효성 검증 오류를 필드별로 매핑
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Message(
                        StatusEnum.BAD_REQUEST.getCode(),
                        messageUtil.getMessage("error.bad_request"),
                        errors
                ));
    }

    /**
     * `@RequestParam`, `@PathVariable`, `@NotBlank` 등의 검증 실패 예외 처리
     *
     * @param ex `ConstraintViolationException` 예외 객체
     * @return 검증 실패 메시지
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Bad Request 반환
    public ResponseEntity<Message> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Message(
                        StatusEnum.BAD_REQUEST.getCode(),
                        messageUtil.getMessage("error.bad_request"),
                        errors
                ));
    }

    /**
     * 접근 거부 오류 (HTTP 403)
     */
    @ExceptionHandler(AccessDeniedException.class) // Spring Security 등에서 발생하는 권한 부족 예외
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Message> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new Message(
                        StatusEnum.FORBIDDEN.getCode(),
                        messageUtil.getMessage("error.forbidden"),
                        null
                ));
    }

    /**
     * 리소스를 찾을 수 없는 경우 (HTTP 404)
     */
    @ExceptionHandler(NoResourceFoundException.class) // 사용자가 요청한 데이터가 없을 때 발생하는 예외
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Message> handleResourceNotFoundException(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Message(
                        StatusEnum.NOT_FOUND.getCode(),
                        messageUtil.getMessage("error.not_found"),
                        null
                ));
    }

    /**
     * 기타 모든 예외 처리
     *
     * @param ex 처리되지 않은 예외
     * @return 오류 메시지
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500 Internal Server Error 반환
    public ResponseEntity<Message> handleAllExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Message(
                        StatusEnum.INTERNAL_SERVER_ERROR.getCode(),
                        messageUtil.getMessage("error.internal_server"),
                        null));
    }
}
