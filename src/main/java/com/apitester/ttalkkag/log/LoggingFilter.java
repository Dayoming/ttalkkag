package com.apitester.ttalkkag.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * HTTP 요청/응답 로깅 필터
 *
 * @author 정다영
 * @date 2025-02-01
 * @description 모든 HTTP 요청 및 응답을 로깅하는 필터 클래스.
 *              요청 URL, 헤더, 본문 및 응답 상태 코드를 기록하며, TLO 로그를 생성
 */
@Component
public class LoggingFilter implements Filter {

    private static final Logger CALL_LOGGER = LoggerFactory.getLogger("com.apitester.ttalkkag.log.CALL");
    private static final Logger TLO_LOGGER = LoggerFactory.getLogger("com.apitester.ttalkkag.log.TLO");

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private final UrlMappingResolver urlMappingResolver;

    private String logKey = "";
    private Tlo tlo;

    /**
     * LoggingFilter 생성자
     *
     * @param urlMappingResolver URL과 기능 ID 매핑을 위한 Resolver
     */
    @Autowired
    public LoggingFilter(UrlMappingResolver urlMappingResolver) {
        this.urlMappingResolver = urlMappingResolver;
    }

    /**
     * HTTP 요청 및 응답을 로깅하는 필터 메서드
     *
     * @param request  요청 객체
     * @param response 응답 객체
     * @param chain    필터 체인
     * @throws IOException      입출력 예외
     * @throws ServletException 서블릿 예외
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        // WebSocket 요청 감지
        if (isWebSocketRequest(servletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        // ContentCachingWrapper로 요청 및 응답을 래핑
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(servletRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(servletResponse);

        logKey = generateLogKey();
        String requestTime = LocalDateTime.now().format(formatter);

        createTlo(requestWrapper);

        // URL 및 HTTP Method 기반 매핑 처리
        Optional<UrlMapping> mapping = urlMappingResolver.resolveMapping(servletRequest);
        mapping.ifPresent(m -> {
            tlo.setFuncId(m.getFuncId());
            tlo.setMid(m.getMid());
        });

        logStart(logKey, requestTime, requestWrapper);

        try {
            // 필터 체인 호출
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            // 응답 본문 로깅
            String responseTime = LocalDateTime.now().format(formatter);

            logEnd(logKey, responseTime, responseWrapper);

            // 응답 데이터를 클라이언트로 전달
            responseWrapper.copyBodyToResponse();
        }
    }

    /**
     * 요청 정보를 기반으로 TLO 객체 생성
     *
     * @param request HTTP 요청 객체
     */
    private void createTlo(ContentCachingRequestWrapper request) {
        tlo = new Tlo();
        String clientIp = request.getRemoteAddr();
        String seqId = generateLogKey();
        String logTime = LocalDateTime.now().format(formatter);
        // 요청 헤더에서 MID 읽기
        String midFromHeader = request.getHeader("MID");

        // TLO LOG
        tlo.setSeqId(seqId);
        tlo.setLogTime(logTime);
        tlo.setLogType("SVC");
        tlo.setReqTime(logTime);
        tlo.setSid("");
        tlo.setRspTime(logTime);
        tlo.setClientIp(clientIp);
        tlo.setDevInfo("Chrome");
        tlo.setOsInfo(parseOsInfo(request.getHeader("User-Agent"))); // OS 정보 추출
        tlo.setNwInfo("ETC");
        tlo.setSvcName("ttalkkag");
        tlo.setDevModel("");
        tlo.setCarrierType("E");
        tlo.setFuncId(""); // URL 또는 헤더에서 함수 ID 결정
        tlo.setLogKey(logKey); // Seq ID와 동일하게 사용
        tlo.setChannelType("IN"); // 고정 값
        tlo.setUrl(request.getRequestURI()); // 요청 URI
        tlo.setMid(midFromHeader != null ? midFromHeader : ""); // 헤더에서 MID 설정
    }

    /**
     * 요청 시작 로그 기록
     *
     * @param logKey  요청 식별 키
     * @param requestTime 요청 시간
     * @param request HTTP 요청 객체
     */
    private void logStart(String logKey, String requestTime, ContentCachingRequestWrapper request) {
        // CALL LOG
        CALL_LOGGER.info(String.format("[%s]==   START CALL LOG  ==================================================", logKey));
        CALL_LOGGER.info(String.format("[%s][REQUEST] [%s] URI : %s", logKey, requestTime, request.getRequestURI()));
        CALL_LOGGER.info(String.format("[%s]     [QUERY STRING] %s", logKey, request.getQueryString()));
        // HTTP Header 출력
        try {
            String headersJson = getHeadersAsJson(request);
            CALL_LOGGER.info(String.format("[%s]     [HTTP HEADER] %s", logKey, headersJson));
        } catch (Exception e) {
            CALL_LOGGER.error(String.format("[%s] Error while logging headers", logKey), e);
        }
        // HTTP Body 출력
        String requestBody = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
        CALL_LOGGER.info(String.format("[%s]     [HTTP BODY] %s", logKey, requestBody));
    }

    /**
     * 요청 종료 로그 기록
     *
     * @param logKey       요청 식별 키
     * @param responseTime 응답 시간
     * @param response     HTTP 응답 객체
     */
    private void logEnd(String logKey, String responseTime, ContentCachingResponseWrapper response) {
        int statusCode = response.getStatus(); // HTTP 상태 코드
        String responseBody = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);

        // 상태 코드 기반 ResultCode 설정
        ResultCode resultCode = ResultCode.fromHttpStatus(statusCode);
        if (responseBody.isEmpty() && statusCode == 204) {
            resultCode = ResultCode.NO_CONTENT; // 빈 응답 처리
        }

        tlo.setResultCode(resultCode.getCode()); // Tlo의 RESULT_CODE 설정

        // 성공 또는 실패 여부 결정
        String statusMessage = (statusCode >= 200 && statusCode < 300) ? "성공" : "실패";

        // TLO 로그 기록
        TLO_LOGGER.info(tlo.toString());

        // CALL 로그 기록 (응답 본문 및 성공/실패 메시지 포함)
        CALL_LOGGER.info(String.format("[%s][RESPONSE] [%s] %d %s %s", logKey, responseTime, statusCode, statusMessage, responseBody));
        CALL_LOGGER.info(String.format("[%s]==   END CALL LOG    ==================================================", logKey));
    }

    /**
     * WebSocket 요청 여부 확인
     *
     * @param request HTTP 요청 객체
     * @return WebSocket 요청 여부 (true/false)
     */
    private boolean isWebSocketRequest(HttpServletRequest request) {
        String connectionHeader = request.getHeader("Connection");
        String upgradeHeader = request.getHeader("Upgrade");
        return connectionHeader != null && connectionHeader.equalsIgnoreCase("Upgrade")
                && upgradeHeader != null && upgradeHeader.equalsIgnoreCase("websocket");
    }

    /**
     * 로그 키 생성 (YYYYMMDDHHMMSSSSS + 랜덤 8자리 문자열)
     *
     * @return 로그 키 문자열
     */
    private String generateLogKey() {
        // 랜덤 4자리 문자열 생성
        String randomPart1 = generateRandomString(4);
        String randomPart2 = generateRandomString(4);

        return LocalDateTime.now().format(formatter) + randomPart1 + randomPart2;
    }

    /**
     * 랜덤 문자열 생성 함수
     * @param length 생성할 문자열 길이
     * @return 랜덤 문자열
     */
    private static String generateRandomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALPHANUMERIC_CHARS.length());
            sb.append(ALPHANUMERIC_CHARS.charAt(index));
        }

        return sb.toString();
    }

    private String parseOsInfo(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        if (userAgent.contains("Windows")) {
            return "Windows NT";
        }
        if (userAgent.contains("Mac")) {
            return "Mac OS";
        }
        if (userAgent.contains("Linux")) {
            return "Linux";
        }
        return "Other";
    }

    private String getHeadersAsJson(HttpServletRequest request) throws Exception {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        return OBJECT_MAPPER.writeValueAsString(headers);
    }
}
