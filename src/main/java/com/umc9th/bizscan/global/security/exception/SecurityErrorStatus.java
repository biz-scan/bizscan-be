package com.umc9th.bizscan.global.security.exception;

import com.umc9th.bizscan.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SecurityErrorStatus implements BaseErrorCode {

  // 인증 관련 오류(4200~4249)
  AUTH_INVALID_TOKEN(HttpStatus.BAD_REQUEST, "4350", "유효하지 않은 토큰입니다."),
  AUTH_INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "4351", "유효하지 않은 리프레시 토큰입니다."),
  AUTH_TOKEN_HAS_EXPIRED(HttpStatus.BAD_REQUEST, "4352", "토큰의 유효기간이 만료되었습니다."),
  AUTH_TOKEN_IS_UNSUPPORTED(HttpStatus.BAD_REQUEST, "4353", "서버에서 지원하지 않는 토큰 형식입니다."),
  AUTH_IS_NULL(HttpStatus.BAD_REQUEST, "4354", "토큰 값이 존재하지 않습니다."),
  AUTH_OAUTH2_EMAIL_NOT_FOUND_FROM_PROVIDER(HttpStatus.NOT_FOUND, "4355", "카카오 이메일이 존재하지 않습니다."),
  AUTH_MUST_AUTHORIZED_URI(HttpStatus.BAD_REQUEST, "4356", "인증이 필요한 URI입니다."),
  AUTH_ROLE_CANNOT_EXECUTE_URI(HttpStatus.BAD_REQUEST, "4357", "해당 권한으로는 요청을 처리할 수 없습니다."),
  AUTH_UNAUTHORIZED_LOGIN_DATA_RETRIEVAL_ERROR(HttpStatus.BAD_REQUEST, "4358", "로그인이 필요없는 API입니다."),
  AUTH_ASSIGNABLE_PARAMETER(HttpStatus.BAD_REQUEST, "4359", "인증타입이 잘못되어 할당이 불가능합니다."),
  AUTH_INVALID_ROLE(HttpStatus.FORBIDDEN, "4360", "유효하지 않은 역할(Role)입니다."),
  AUTH_WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "4361", "패스워드가 잘못되었습니다."),
  AUTH_LOGGED_OUT_TOKEN(HttpStatus.UNAUTHORIZED, "4362", "로그아웃된 토큰입니다."),
  INTERNAL_SECURITY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH5000", "보안 시스템 내부 오류가 발생했습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public HttpStatus getStatus() {
    return httpStatus;
  }
}
