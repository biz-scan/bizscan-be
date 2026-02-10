package com.umc9th.bizscan.global.apiPayload.handler;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.BaseErrorCode;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.exception.GeneralException;
import com.umc9th.bizscan.global.security.exception.JwtAuthenticationException;
import com.umc9th.bizscan.global.security.exception.JwtAuthenticationExpiredException;
import com.umc9th.bizscan.global.security.exception.SecurityErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

  // @RequestParam / @PathVariable enum, 숫자 타입 등 변환 실패 처리 예: category=CAFE (존재하지 않는 enum)

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  protected ResponseEntity<ApiResponse<Map<String, String>>>
      handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {

    Map<String, String> errors = new HashMap<>();

    String paramName = ex.getName();
    Object value = ex.getValue();
    Class<?> requiredType = ex.getRequiredType();

    if (requiredType != null && requiredType.isEnum()) {
      String allowed =
          java.util.Arrays.stream(requiredType.getEnumConstants())
              .map(Object::toString)
              .collect(Collectors.joining(", "));

      errors.put(paramName, "잘못된 값입니다. 입력값=" + value + " / 허용값=[" + allowed + "]");
    } else {
      errors.put(paramName, "잘못된 값입니다. 입력값=" + value);
    }

    ErrorCode code = ErrorCode.INVALID_TYPE_VALUE;
    return ResponseEntity.status(code.getStatus()).body(ApiResponse.onFailure(code, errors));
  }

  // @RequestBody(JSON) 파싱 실패 처리 (InvalidFormatException 포함) 예: { "category": "CAFE" } 처럼 enum 값이 잘못
  // 들어온 경우

  @ExceptionHandler(HttpMessageNotReadableException.class)
  protected ResponseEntity<ApiResponse<Map<String, String>>> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex) {

    Map<String, String> errors = new HashMap<>();

    Throwable cause = ex.getCause();
    if (cause instanceof InvalidFormatException ife) {
      String fieldPath =
          ife.getPath().stream()
              .map(JsonMappingException.Reference::getFieldName)
              .collect(Collectors.joining("."));

      Class<?> targetType = ife.getTargetType();
      Object value = ife.getValue();

      if (targetType != null && targetType.isEnum()) {
        String allowed =
            java.util.Arrays.stream(targetType.getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        errors.put(fieldPath, "잘못된 enum 값입니다. 입력값=" + value + " / 허용값=[" + allowed + "]");
      } else {
        errors.put(fieldPath, "잘못된 형식의 값입니다. 입력값=" + value);
      }

      ErrorCode code = ErrorCode.INVALID_TYPE_VALUE;
      return ResponseEntity.status(code.getStatus()).body(ApiResponse.onFailure(code, errors));
    }

    // InvalidFormatException이 아닌 일반 JSON 파싱 오류(콤마/따옴표 깨짐 등)
    errors.put("body", "요청 JSON 형식이 올바르지 않습니다.");
    ErrorCode code = ErrorCode.INVALID_TYPE_VALUE;
    return ResponseEntity.status(code.getStatus()).body(ApiResponse.onFailure(code, errors));
  }

  // @RequestParam 등에 붙는 Validation 실패 처리 예: @Min, @NotNull, @Positive 등 (Query Param 검증)
  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
      ConstraintViolationException ex) {

    Map<String, String> errors = new HashMap<>();

    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      String path = violation.getPropertyPath().toString();
      String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
      errors.put(field, violation.getMessage());
    }

    ErrorCode code = ErrorCode.INVALID_TYPE_VALUE;
    return ResponseEntity.status(code.getStatus()).body(ApiResponse.onFailure(code, errors));
  }

  // 정적 리소스 또는 매핑되지 않은 URL을 500이 아닌 404(Not Found)로 응답하도록 처리
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(
      NoResourceFoundException ex) {

    return ResponseEntity.status(ErrorCode.NOT_FOUND.getStatus())
        .body(ApiResponse.onFailure(ErrorCode.NOT_FOUND, null));
  }

  @ExceptionHandler({JwtAuthenticationException.class, JwtAuthenticationExpiredException.class})
  public ResponseEntity<ApiResponse<Void>> handleJwtAuthenticationException(
      JwtAuthenticationException ex) {
    // 예외 메시지에 담긴 "AUTH_WRONG_PASSWORD" 등을 통해 Enum을 찾음
    SecurityErrorStatus errorStatus = SecurityErrorStatus.valueOf(ex.getMessage());

    return ResponseEntity.status(errorStatus.getStatus())
        .body(ApiResponse.onFailure(errorStatus, null));
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
