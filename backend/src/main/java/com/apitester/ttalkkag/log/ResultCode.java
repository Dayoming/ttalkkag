package com.apitester.ttalkkag.log;

/**
 * HTTP 상태 코드와 매칭되는 결과 코드 Enum
 *
 * @author 정다영
 * @date 2025-02-01
 * @description HTTP 응답 코드와 내부 시스템 결과 코드를 매핑하는 Enum 클래스.
 *              주어진 HTTP 상태 코드에 대응하는 ResultCode를 제공하며,
 *              정의되지 않은 상태 코드의 경우 기본적으로 "UNKNOWN"을 반환
 */
public enum ResultCode {
    SUCCESS(200, "20000000"),
    NO_CONTENT(204, "20000001"),
    BAD_REQUEST(400, "31000601"),
    UNAUTHORIZED(401, "31000602"),
    NOT_FOUND(404, "31000603"),
    INTERNAL_SERVER_ERROR(500, "31000604"),
    UNKNOWN(0, "99999999");

    private final int httpStatus;
    private final String code;

    ResultCode(int httpStatus, String code) {
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    /**
     * HTTP 상태 코드로 ResultCode를 찾는 메서드
     * @param status HTTP 상태 코드
     * @return 매칭되는 ResultCode, 없으면 UNKNOWN
     */
    public static ResultCode fromHttpStatus(int status) {
        for (ResultCode resultCode : values()) {
            if (resultCode.getHttpStatus() == status) {
                return resultCode;
            }
        }
        return UNKNOWN;
    }
}
