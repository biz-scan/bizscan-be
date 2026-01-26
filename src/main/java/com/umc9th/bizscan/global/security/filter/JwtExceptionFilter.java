package com.umc9th.bizscan.global.security.filter;

import com.umc9th.bizscan.global.security.exception.CustomErrorSend;
import com.umc9th.bizscan.global.security.exception.JwtAuthenticationException;
import com.umc9th.bizscan.global.security.exception.SecurityErrorStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtExceptionFilter  extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtAuthenticationException authException) {
            String errorCodeName = authException.getMessage();
            SecurityErrorStatus errorStatus = SecurityErrorStatus.valueOf(errorCodeName);
            CustomErrorSend.handleException(response, errorStatus, errorCodeName);
        }
    }
}
