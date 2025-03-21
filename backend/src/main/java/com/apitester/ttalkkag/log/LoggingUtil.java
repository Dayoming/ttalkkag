package com.apitester.ttalkkag.log;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@AllArgsConstructor
public class LoggingUtil {
    private static final Logger TRANSACTION_LOGGER = LoggerFactory.getLogger("com.apitester.ttalkkag.log.TRANSACTION");
    private static final Logger CALL_LOGGER = LoggerFactory.getLogger("com.apitester.ttalkkag.log.CALL");

    /**
     * 트랜잭션 단위 로그 기록 메서드
     *
     * @param logKey    로그 키
     * @param userEmail    사용자 Email
     * @param message   로그 메시지
     */
    public static void logTransactionStep(String logKey, String userEmail, String message) {
        String formattedLog = String.format("[%s][%s] %s", logKey, userEmail, message);
        TRANSACTION_LOGGER.info(formattedLog);
    }

    public static void logExternalCallStep(String logKey, String target, Object data) {
        String formattedLog = String.format("[%s]     [EXTERNAL CALL] Target: %s, Data: %s", logKey, target, data);
        CALL_LOGGER.info(formattedLog);
    }
}
