package com.umc9th.bizscan.global.security.filter;

import static com.umc9th.bizscan.global.security.consts.StaticVariable.HEALTH_CHECK_ENDPOINT;
import static com.umc9th.bizscan.global.security.consts.StaticVariable.REISSUE_ENDPOINT;

import com.umc9th.bizscan.global.security.exception.CustomErrorSend;
import com.umc9th.bizscan.global.security.exception.JwtAuthenticationException;
import com.umc9th.bizscan.global.security.exception.JwtAuthenticationExpiredException;
import com.umc9th.bizscan.global.security.exception.SecurityErrorStatus;
import com.umc9th.bizscan.global.security.jwt.service.RedisService;
import com.umc9th.bizscan.global.security.jwt.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final TokenService tokenService;
  private final RedisService redisService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    // HttpServletRequest에서 JWT 토큰 추출
    HttpServletRequest httpServletRequest = ((HttpServletRequest) request);

    String requestURI = httpServletRequest.getRequestURI();
    log.info("requestURI: {}", requestURI);
    String token = null;
    token = resolveToken(request);
    log.info("Token: {}", token);

    if (token != null) {
      // 만료 케이스만 해당 필터에서 처리. 나머지는 JwtExceptionFilter 에서 처리
      try {
        if (redisService.isBlacklisted(token)) {
          CustomErrorSend.handleException(
              response,
              SecurityErrorStatus.AUTH_LOGGED_OUT_TOKEN,
              SecurityErrorStatus.AUTH_LOGGED_OUT_TOKEN.name());
          return;
        }
        tokenService.validateToken(token);
        // 토큰이 유효할 경우 토큰에서 Authentication 객체를 가지고 와서 SecurityContext에 저장
        Authentication authentication = tokenService.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.setAttribute("username", authentication.getName());
        log.info(
            "set Authentication to security context for '{}', uri: '{}', Role '{}'",
            authentication.getName(),
            requestURI,
            authentication.getAuthorities());
      } catch (JwtAuthenticationExpiredException e) {
        log.info("Token has expired");
        if (!requestURI.equals(REISSUE_ENDPOINT)) {
          CustomErrorSend.handleException(
              response,
              SecurityErrorStatus.AUTH_TOKEN_HAS_EXPIRED,
              SecurityErrorStatus.AUTH_TOKEN_HAS_EXPIRED.name());
          return;
        }
        log.debug("토큰 만료지만 재발급 시도이므로 통과합니다.");

      } catch (JwtAuthenticationException e) {
        SecurityErrorStatus status;
        try {
          status = SecurityErrorStatus.valueOf(e.getMessage());
        } catch (Exception ex) {
          log.info("token error: {}", e.getMessage());
          status = SecurityErrorStatus.AUTH_INVALID_TOKEN;
        }

        CustomErrorSend.handleException(response, status, status.name());
        return;
      }
    } else {
      if (!requestURI.equals(HEALTH_CHECK_ENDPOINT)) {
        log.info("no valid JWT token found, uri: {}", requestURI);
      }
    }

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    if (path.startsWith("/actuator")) return true;
    return path.equals(REISSUE_ENDPOINT)
        || path.equals("/api/tokens/login")
        || path.equals("/health");
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");

    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
