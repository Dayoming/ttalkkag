package com.apitester.ttalkkag.layout;

public enum StatusEnum {

    OK(200, "20000000"),
    NO_CONTENT(204, "20000001"),
    BAD_BEQUEST(400, "31000601"),
    UNAAUTHORIZED(401, "31000602"),
    NOT_FOUND(404, "31000603"),
    INTERNAL_SERVER_ERROR(500, "31000604");

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
