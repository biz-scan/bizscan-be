package com.umc9th.bizscan.global.apiPayload.handler;

import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.BaseErrorCode;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.exception.GeneralException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GeneralExceptionAdvice {

  // 애플리케이션에서 발생하는 커스텀 예외를 처리
  @ExceptionHandler(GeneralException.class)
  public ResponseEntity<ApiResponse<Void>> handleException(GeneralException ex) {
    return ResponseEntity.status(ex.getCode().getStatus())
        .body(ApiResponse.onFailure(ex.getCode(), null));
  }

  // 컨트롤러 메서드에서 @Valid 어노테이션을 사용하여 DTO의 유효성 검사를 수행
  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    // 검사에 실패한 필드와 그에 대한 메시지를 저장하는 Map
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    ErrorCode code = ErrorCode.INVALID_TYPE_VALUE;
    ApiResponse<Map<String, String>> errorResponse = ApiResponse.onFailure(code, errors);

    // 에러 코드, 메시지와 함께 errors를 반환
    return ResponseEntity.status(code.getStatus()).body(errorResponse);
  }

  // 그 외의 정의되지 않은 모든 예외 처리
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<String>> handleException(
      Exception ex, HttpServletRequest request) {

    // 오류난 url과 httpMethod 가져오기
    String url = request.getRequestURI();
    String method = request.getMethod();

    // 로그 출력
    log.error("[500 ERROR] {} {} - {}", method, url, ex.getMessage(), ex);

    BaseErrorCode code = ErrorCode.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(code.getStatus()).body(ApiResponse.onFailure(code, null));
  }
}
