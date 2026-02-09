package com.umc9th.bizscan.global.security.controller;

import com.umc9th.bizscan.domain.member.service.MemberQueryService;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.code.SuccessCode;
import com.umc9th.bizscan.global.config.swagger.ApiErrorCodeExamples;
import com.umc9th.bizscan.global.security.exception.SecurityErrorStatus;
import com.umc9th.bizscan.global.security.jwt.dto.AccessTokenResponse;
import com.umc9th.bizscan.global.security.jwt.dto.JwtToken;
import com.umc9th.bizscan.global.security.jwt.dto.MemberLoginRequestDto;
import com.umc9th.bizscan.global.security.jwt.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Token API", description = "JWT 토큰 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tokens")
public class TokenApiController {

  private final TokenService tokenService;
  private final MemberQueryService memberQueryService;

  @Operation(summary = "이메일로 JWT 토큰 발급")
  @ApiErrorCodeExamples(
          value = {ErrorCode.MEMBER_NOT_FOUND},
          security = {SecurityErrorStatus.AUTH_WRONG_PASSWORD}
  )
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AccessTokenResponse>> login(
      @RequestBody MemberLoginRequestDto memberLoginRequestDto, HttpServletResponse response) {
    JwtToken token = tokenService.login(memberLoginRequestDto);

    // Refresh Token → HttpOnly Cookie
    Cookie refreshTokenCookie = new Cookie("refreshToken", token.getRefreshToken());
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setSecure(true); // HTTPS 환경
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge(14 * 24 * 60 * 60); // 14일

    response.addCookie(refreshTokenCookie);

    // Access Token → Header
    response.setHeader("Authorization", "Bearer " + token.getAccessToken());

    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessCode.MEMBER_LOGIN_SUCCESS, null));
  }

  @Operation(
      summary = "Access Token 재발급",
      description = "HttpOnly Cookie에 저장된 Refresh Token을 이용해 Access Token을 재발급합니다.")
  @ApiErrorCodeExamples(
          value = {ErrorCode.MEMBER_NOT_FOUND},
          security = {
                  SecurityErrorStatus.AUTH_INVALID_REFRESH_TOKEN,
                  SecurityErrorStatus.AUTH_TOKEN_HAS_EXPIRED,
                  SecurityErrorStatus.AUTH_INVALID_TOKEN
          }
  )
  @PostMapping("/reissue")
  public ResponseEntity<ApiResponse<AccessTokenResponse>> reissue(
      @CookieValue(value = "refreshToken", required = false) String refreshToken,
      HttpServletResponse response) {
    JwtToken token = tokenService.issueTokens(refreshToken);

    // Access Token → Header
    response.setHeader("Authorization", "Bearer " + token.getAccessToken());

    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessCode.TOKEN_ISSUE_SUCCESS, null));
  }

  @Operation(summary = "로그아웃", description = "사용자를 로그아웃 처리합니다.")
  @ApiErrorCodeExamples(
          security = {
                  SecurityErrorStatus.AUTH_MUST_AUTHORIZED_URI,
                  SecurityErrorStatus.AUTH_TOKEN_HAS_EXPIRED,
                  SecurityErrorStatus.AUTH_INVALID_TOKEN
          }
  )
  @PostMapping("/logout")
  public ApiResponse<Void> logout(
      @AuthenticationPrincipal User user,
      @RequestHeader(value = "Authorization", required = false) String authorization,
      HttpServletResponse response) {

    tokenService.logout(user.getUsername(), authorization);

    // RefreshToken Cookie 삭제
    Cookie deleteCookie = new Cookie("refreshToken", null);
    deleteCookie.setHttpOnly(true);
    deleteCookie.setSecure(true);
    deleteCookie.setPath("/");
    deleteCookie.setMaxAge(0); // 즉시 삭제
    response.addCookie(deleteCookie);

    return ApiResponse.onSuccess(SuccessCode.MEMBER_LOGOUT_SUCCESS, null);
  }

  @Operation(summary = "이메일 중복 검증", description = "이미 존재하면 true, 사용 가능하면 false를 반환합니다.")
  @GetMapping("/duplication/login-id")
  public ResponseEntity<ApiResponse<Boolean>> idValidator(@RequestParam String email) {
    return ResponseEntity.ok(
        ApiResponse.onSuccess(
            SuccessCode.MEMBER_DUPLICATION_CHECK_SUCCESS,
            memberQueryService.validateExistEmail(email)));
  }

  @Operation(summary = "닉네임 중복 검증", description = "이미 존재하면 true, 사용 가능하면 false를 반환합니다.")
  @GetMapping("/duplication/nickname")
  public ResponseEntity<ApiResponse<Boolean>> nicknameValidator(@RequestParam String nickname) {
    return ResponseEntity.ok(
        ApiResponse.onSuccess(
            SuccessCode.MEMBER_DUPLICATION_CHECK_SUCCESS,
            memberQueryService.validateExistNickname(nickname)));
  }
}
