package com.apitester.ttalkkag.config;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Properties;

/**
 * 메시지 소스를 쉽게 가져오기 위한 유틸리티 클래스
 */
@Component
public class MessageUtil {

    private final Properties yamlProperties;

    public MessageUtil(Properties yamlProperties) {
        this.yamlProperties = yamlProperties;
    }

    /**
     * 메시지 키를 이용하여 메시지를 가져온다.
     *
     * @param key 메시지 키
     * @return 해당 키의 메시지
     */
    public String getMessage(String key) {
        return yamlProperties.getProperty(key, "메시지를 찾을 수 없습니다.");
    }
}
