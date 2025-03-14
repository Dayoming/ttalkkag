package com.apitester.history.kafka;

import com.apitester.history.dto.ApiChangeHistoryMessage;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtValidationCache {
    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    public void storeRequest(String jwtToken, Object requestObject) {
        cache.put(jwtToken, requestObject);
    }

    // JWT 토큰을 기반으로 원본 객체를 가져오고, 가져온 후 삭제 (1회용 캐시)
    public <T> T getRequest(String jwtToken, Class<T> type) {
        Object obj = cache.remove(jwtToken);
        if (type.isInstance(obj)) {
            return type.cast(obj);
        }
        return null; // 타입이 맞지 않으면 null 반환
    }
}
