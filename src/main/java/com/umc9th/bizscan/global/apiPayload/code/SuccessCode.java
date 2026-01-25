package com.umc9th.bizscan.global.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode implements BaseSuccessCode {
  OK(HttpStatus.OK, "COMMON200_1", "요청이 정상적으로 처리되었습니다."),
  // Member
  MEMBER_GET_SUCCESS(HttpStatus.OK, "MEMBER200_1", "회원 정보 조회가 정상적으로 처리되었습니다."),
  MEMBER_REGISTER_SUCCESS(HttpStatus.CREATED, "MEMBER201_1", "회원가입이 정상적으로 처리되었습니다.");


  private final HttpStatus status;
  private final String code;
  private final String message;
}
