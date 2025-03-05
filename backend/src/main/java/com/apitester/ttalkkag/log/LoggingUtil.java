package com.apitester.ttalkkag.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingUtil {
    private static final Logger TRANSACTION_LOGGER = LoggerFactory.getLogger("com.apitester.ttalkkag.log.TRANSACTION");

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
}
