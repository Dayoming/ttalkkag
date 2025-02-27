package com.apitester.ttalkkag.layout;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // Message 타입이 아닐 경우에만 적용 (이미 Message로 감싼 경우 제외)
        return !returnType.getParameterType().equals(Message.class);
    }

    @Override
    @ResponseBody
    public Object beforeBodyWrite(Object body, MethodParameter returnType, org.springframework.http.MediaType selectedContentType,
                                  Class selectedConverterType, org.springframework.http.server.ServerHttpRequest request,
                                  org.springframework.http.server.ServerHttpResponse response) {

        // body가 null이면 NO_CONTENT 응답
        if (body == null) {
            return new Message(StatusEnum.NO_CONTENT.getCode(), "컨텐츠 없음.", null);
        }

        // body가 Exception이면 INTERNAL_SERVER_ERROR 응답
        if (body instanceof Exception) {
            return new Message(StatusEnum.INTERNAL_SERVER_ERROR.getCode(), "서버 내부 오류가 발생했습니다.", null);
        }

        return new Message(StatusEnum.OK.getCode(), "요청이 성공적으로 처리되었습니다.", body);
    }
}