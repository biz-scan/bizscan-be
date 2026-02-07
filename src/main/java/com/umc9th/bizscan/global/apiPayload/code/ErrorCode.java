package com.umc9th.bizscan.global.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseErrorCode {
  // Common
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400_1", "잘못된 입력 값입니다."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON405_1", "허용되지 않은 메서드입니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404_1", "요청한 리소스를 찾을 수 없습니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500_1", "예기치 않은 서버 에러가 발생했습니다."),
  INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "COMMON400_2", "유효하지 않은 타입 값입니다."),

  // JWT
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH401_1", "인증이 필요합니다."),
  FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH403_1", "요청이 거부되었습니다."),
  EXPIRED_JWT(HttpStatus.UNAUTHORIZED, "AUTH401_2", "만료된 JWT 토큰입니다."),
  UNSUPPORTED_JWT(HttpStatus.UNAUTHORIZED, "AUTH401_3", "지원되지 않는 JWT 토큰입니다."),
  SIGNATURE_INVALID_JWT(HttpStatus.UNAUTHORIZED, "AUTH401_4", "유효하지 않은 JWT 시그니처입니다."),
  JWT_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH401_5", "JWT 토큰을 찾을 수 없습니다."),
  AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH401_6", "인증에 실패했습니다."),

  // Member
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404_1", "사용자를 찾을 수 없습니다."),
  MEMBER_ALREADY_REGISTERED(HttpStatus.CONFLICT, "MEMBER409_1", "이미 가입된 사용자입니다."),
  EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER409_2", "이미 사용 중인 이메일입니다."),
  NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER409_3", "이미 사용 중인 닉네임입니다."),
  MEMBER_NAME_BAD_REQUEST(HttpStatus.BAD_REQUEST, "MEMBER400_1", "이름의 형식이 맞지 않습니다."),
  INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "MEMBER400_2", "현재 비밀번호가 일치하지 않습니다."),
  SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "MEMBER400_3", "새 비밀번호는 기존 비밀번호와 다르게 설정해야 합니다."),

  // AiAnalysis
  SWOT_NOT_FOUND(HttpStatus.NOT_FOUND, "SWOT404", "해당 매장의 SWOT 분석 결과가 존재하지 않습니다."),
  ANALYSIS_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "ANALYSIS404_1", "해당 분석 요청을 찾을 수 없습니다."),
  ACTION_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "ACTION_PLAN_404", "실행 전략을 찾을 수 없습니다."),
  INVALID_CALLBACK_DATA(
      HttpStatus.BAD_REQUEST, "ANALYSIS400_1", "AI 분석 콜백 데이터가 유효하지 않거나 결과 값이 누락되었습니다."),
  ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "ANALYSIS404_2", "해당 매장이 진행한 분석이 없습니다."),
    ANALYSIS_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "ANALYSIS409_1", "이미 분석이 진행 중입니다."),

    ANALYSIS_ALREADY_IN_COMPLETED(HttpStatus.BAD_REQUEST, "ANALYSIS400_1", "이미 완료된 분석 결과가 존재합니다."),
    ANALYSIS_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ANALYSIS500_1", "AI 분석 서버와의 통신 중 오류가 발생했습니다."),

  // Store
  STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE404_1", "해당 매장을 찾을 수 없습니다."),

  // ActionNote
  ACTION_NOTE_ALREADY_EXISTS(HttpStatus.CONFLICT, "ACTION_NOTE409_1", "이미 등록된 실행노트가 존재합니다."),
  ACTION_NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "ACTION_NOTE404_1", "실행 노트를 찾을 수 없습니다."),
  ACTION_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "ACTION_DETAIL404_1", "세부 실행 전략을 찾을 수 없습니다."),
  PAGE_INVALID(HttpStatus.BAD_REQUEST, "PAGE400_1", "유효하지 않은 페이지 범위입니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
