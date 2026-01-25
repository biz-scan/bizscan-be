package com.umc9th.bizscan.global.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode implements BaseSuccessCode {
  OK(HttpStatus.OK, "COMMON200_1", "요청이 정상적으로 처리되었습니다."),
  // Member
  MEMBER_REGISTER_SUCCESS(HttpStatus.CREATED, "MEMBER201_1", "회원가입이 정상적으로 처리되었습니다."),
  MEMBER_LOGIN_SUCCESS(HttpStatus.OK, "MEMBER200_1", "로그인이 정상적으로 처리되었습니다."),
  MEMBER_GET_SUCCESS(HttpStatus.OK, "MEMBER200_2", "회원 정보 조회가 정상적으로 처리되었습니다."),
  MEMBER_LIST_SUCCESS(HttpStatus.OK, "MEMBER200_3", "회원 목록 조회가 정상적으로 처리되었습니다."),
  MEMBER_UPDATE_SUCCESS(HttpStatus.OK, "MEMBER200_4", "회원 정보 수정이 정상적으로 처리되었습니다."),
  MEMBER_DELETE_SUCCESS(HttpStatus.OK, "MEMBER200_5", "회원 삭제가 정상적으로 처리되었습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
