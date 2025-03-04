package com.apitester.ttalkkag.exception;

import com.apitester.ttalkkag.layout.Message;
import com.apitester.ttalkkag.layout.StatusEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@RequiredArgsConstructor
@Component
public class JwtExceptionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtAuthenticationException ex) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            Message errorResponse = new Message(
                    StatusEnum.UNAUTHORIZED.getCode(),
                    ex.getMessage(),
                    null
            );

            PrintWriter out = response.getWriter();
            ObjectMapper objectMapper = new ObjectMapper();
            out.print(objectMapper.writeValueAsString(errorResponse));
            out.flush();
        }
    }
}
