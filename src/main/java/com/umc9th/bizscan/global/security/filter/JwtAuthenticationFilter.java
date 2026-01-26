package com.umc9th.bizscan.global.security.filter;

import static com.umc9th.bizscan.global.common.consts.StaticVariable.HEALTH_CHECK_ENDPOINT;
import static com.umc9th.bizscan.global.common.consts.StaticVariable.REISSUE_ENDPOINT;

import com.umc9th.bizscan.global.security.exception.JwtAuthenticationException;
import com.umc9th.bizscan.global.security.exception.JwtAuthenticationExpiredException;
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

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    // HttpServletRequest에서 JWT 토큰 추출
    HttpServletRequest httpServletRequest = ((HttpServletRequest) request);

    String requestURI = httpServletRequest.getRequestURI();
    String token = null;
    token = resolveToken(request);

    if (token != null) {
      // 만료 케이스만 해당 필터에서 처리. 나머지는 JwtExceptionFilter 에서 처리
      try {
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
        if (!requestURI.equals(REISSUE_ENDPOINT)) throw JwtAuthenticationException.TOKEN_IS_EXPIRED;
        log.debug("토큰 만료지만 재발급 시도이므로 통과합니다.");
      }
    } else {
      if (!requestURI.equals(HEALTH_CHECK_ENDPOINT)) {
        log.info("no valid JWT token found, uri: {}", requestURI);
      }
    }

    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");

    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
