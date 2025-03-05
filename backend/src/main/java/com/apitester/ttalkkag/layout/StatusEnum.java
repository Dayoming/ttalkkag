package com.apitester.ttalkkag.layout;

public enum StatusEnum {

    OK(200, "20000000"),
    NO_CONTENT(204, "20000001"),
    BAD_REQUEST(400, "31000601"),
    UNAUTHORIZED(401, "31000602"),
    FORBIDDEN(403, "31000603"),
    NOT_FOUND(404, "31000604"),
    INTERNAL_SERVER_ERROR(500, "31000605");

    int statusCode;
    String code;

    StatusEnum(int statusCode, String code) {
        this.statusCode = statusCode;
        this.code = code;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getCode() {
        return code;
    }
}
