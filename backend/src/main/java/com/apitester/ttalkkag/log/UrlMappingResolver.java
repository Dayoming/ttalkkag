package com.apitester.ttalkkag.log;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * URL 매핑 해석기 (Resolver)
 *
 * @author 정다영
 * @date 2025-02-01
 * @description HTTP 요청의 URL과 메서드를 기반으로 `url-mapping.json`에 정의된 매핑 정보를 찾아 반환하는 클래스.
 *              `AntPathMatcher`를 사용하여 경로 패턴을 비교
 */
@Component
public class UrlMappingResolver {

    private final List<UrlMapping> urlMappings;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * URL 매핑 해석기 생성자
     *
     * @param urlMappingLoader JSON에서 로드한 URL 매핑 리스트를 제공하는 로더 클래스
     * @throws IOException `url-mapping.json` 로딩 중 오류 발생 시 예외 처리
     */
    @Autowired
    public UrlMappingResolver(UrlMappingLoader urlMappingLoader) throws IOException {
        this.urlMappings = urlMappingLoader.loadMappings();
    }

    /**
     * HTTP 요청에 해당하는 URL 매핑 정보 찾기
     *
     * @param request HTTP 요청 객체
     * @return 매칭된 URL 매핑 객체 (`Optional<UrlMapping>`)
     */
    public Optional<UrlMapping> resolveMapping(HttpServletRequest request) {
        String requestUrl = request.getRequestURI();
        String requestMethod = request.getMethod();

        return urlMappings.stream()
                .filter(mapping -> pathMatcher.match(mapping.getUrl(), requestUrl)
                        && mapping.getMethod().equalsIgnoreCase(requestMethod))
                .findFirst();
    }
}