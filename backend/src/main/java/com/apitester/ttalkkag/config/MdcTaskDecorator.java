package com.apitester.ttalkkag.config;

import com.apitester.ttalkkag.log.LoggingUtil;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap(); // 현재 스레드의 MDC 복사
        String logKey = MDC.get("LOG_KEY");
        String userEmail = MDC.get("USER_EMAIL");

        return () -> {
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                LoggingUtil.logTransactionStep(logKey, userEmail, "[Async] 비동기 스레드 종료 - MDC 제거 시작: " + MDC.getCopyOfContextMap());
                MDC.clear();
                LoggingUtil.logTransactionStep(logKey, userEmail, "[Async] 비동기 스레드 종료 - MDC 제거 완료: " + MDC.getCopyOfContextMap());
            }
        };
    }
}
