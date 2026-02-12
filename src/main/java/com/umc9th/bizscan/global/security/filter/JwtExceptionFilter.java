package com.umc9th.bizscan.global.security.filter;

import static com.umc9th.bizscan.global.security.consts.StaticVariable.REISSUE_ENDPOINT;

import com.umc9th.bizscan.global.security.exception.CustomErrorSend;
import com.umc9th.bizscan.global.security.exception.SecurityErrorStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtExceptionFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      filterChain.doFilter(request, response);
    } catch (AuthenticationException authException) {
      // JwtAuthenticationException, JwtAuthenticationExpiredException의 인증 관련 예외 처리
      log.info(authException.getMessage(), authException);
      String errorCodeName = authException.getMessage();
      SecurityErrorStatus errorStatus = SecurityErrorStatus.valueOf(errorCodeName);
      CustomErrorSend.handleException(response, errorStatus, errorCodeName);
    } catch (Exception e) {
      // 그 외의 필터 단 예외 처리 (예: DB 연결 오류, 예상치 못한 런타임 에러 등)
      log.info("Filter Error: {}", e.getMessage());
      CustomErrorSend.handleException(
          response, SecurityErrorStatus.INTERNAL_SECURITY_ERROR, e.getMessage());
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    if (path.startsWith("/actuator")) return true;
    return path.equals(REISSUE_ENDPOINT)
        || path.equals("/api/tokens/login")
        || path.equals("/health");
  }
}
