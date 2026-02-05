package com.umc9th.bizscan.domain.store.exception;

import com.umc9th.bizscan.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StoreErrorCode implements BaseErrorCode {
  FORBIDDEN("STORE403_1", "가게 소유자만 수정할 수 있습니다.", HttpStatus.FORBIDDEN),

  MEMBER_NOT_FOUND("STORE404_1", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  STORE_NOT_FOUND("STORE404_2", "가게를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

  TAG_NOT_FOUND("TAG404_1", "태그를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  TAG_REQUIRED("TAG400_0", "태그는 1개 이상 필수입니다.", HttpStatus.BAD_REQUEST),
  TAG_LIMIT_EXCEEDED("TAG400_1", "태그는 최대 3개까지 선택할 수 있습니다.", HttpStatus.BAD_REQUEST),
  TAG_DUPLICATED("TAG400_2", "중복된 태그가 포함되어 있습니다.", HttpStatus.BAD_REQUEST),
  TAG_CODE_INVALID("TAG400_3", "유효하지 않은 태그 코드가 포함되어 있습니다.", HttpStatus.BAD_REQUEST),

  ADDRESS_INVALID("STORE400_1", "유효하지 않은 주소입니다. 주소를 다시 확인해주세요.", HttpStatus.BAD_REQUEST),
  ADDRESS_DUPLICATED("STORE400_2", "이미 등록된 주소입니다.", HttpStatus.BAD_REQUEST),
  INVALID_REQUEST_FORMAT("STORE400_3", "요청 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
  INVALID_ENUM_VALUE("STORE400_4", "요청 값(enum)이 올바르지 않습니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
