package com.umc9th.bizscan.domain.analysis.exception;

import com.umc9th.bizscan.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SwotAnalysisErrorCode implements BaseErrorCode {
  REGION_ADDRESS_NOT_FOUND("REGION404_1", "입력하신 주소를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  REGION_DATA_NOT_FOUND("REGION404_2", "기본 상권 데이터를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
