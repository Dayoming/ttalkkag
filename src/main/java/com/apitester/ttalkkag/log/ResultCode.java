package com.apitester.ttalkkag.log;

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
