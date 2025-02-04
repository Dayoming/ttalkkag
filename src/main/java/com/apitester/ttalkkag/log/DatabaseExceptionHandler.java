package com.apitester.ttalkkag.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.sql.SQLException;

/**
 * 글로벌 예외 처리 (Database 관련 예외)
 *
 * @author 정다영
 * @date 2025-02-01
 * @description 데이터베이스 관련 예외 및 일반 예외를 처리하는 컨트롤러 어드바이스 클래스.
 */
@ControllerAdvice
public class DatabaseExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseExceptionHandler.class);

    /**
     * SQL 관련 예외 처리
     * 데이터베이스 연결 유실 또는 SQL 실행 중 발생하는 오류를 처리
     * 500 INTERNAL_SERVER_ERROR 상태 코드를 반환
     *
     * @param ex SQLException 객체
     */
    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleSQLException(SQLException ex) {
        logger.error("DB 연결이 유실되었습니다: {}", ex.getMessage());
    }

    /**
     * 일반 예외 처리
     * 예상치 못한 서버 오류가 발생했을 때 처리
     * 500 INTERNAL_SERVER_ERROR 상태 코드를 반환
     *
     * @param ex Exception 객체
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleGeneralException(Exception ex) {
        logger.error("예상치 못한 오류가 발생했습니다: {}", ex.getMessage());
    }
}
