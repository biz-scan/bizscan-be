package com.umc9th.bizscan.global.config.swagger;

import com.umc9th.bizscan.global.apiPayload.code.BaseErrorCode;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.exception.GeneralException;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.lang.reflect.Method;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Slf4j
@Component
public class ApiErrorCodeOperationCustomizer implements OperationCustomizer {

  private static final String JSON_MEDIA_TYPE = "application/json";
  private static final String RESPONSE_DTO_REF = "#/components/schemas/ApiResponse";

  @Override
  public Operation customize(Operation operation, HandlerMethod handlerMethod) {
    // 1. ApiErrorCodeExamples 어노테이션을 찾고, 없으면 즉시 종료
    ApiErrorCodeExamples annotation = handlerMethod.getMethodAnnotation(ApiErrorCodeExamples.class);
    if (annotation == null) {
      return operation;
    }

    // 2. Operation에서 Responses 확보 (null 방어)
      ApiResponses responses =
              Optional.ofNullable(operation.getResponses()).orElseGet(ApiResponses::new);
      operation.setResponses(responses);

      // 3. 에러 코드 추출 및 예시 추가
      List<BaseErrorCode> errorCodes = extractErrorCodes(annotation);

    // 4. 에러 코드 주입
      errorCodes.forEach(errorCode -> addErrorCodeExample(responses, errorCode));

    return operation;
  }

    /**
     * 리플렉션을 통해 어노테이션의 모든 필드에서 BaseErrorCode[]를 추출
     */
    private List<BaseErrorCode> extractErrorCodes(ApiErrorCodeExamples annotation) {
        List<BaseErrorCode> errorCodes = new ArrayList<>();

        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            // 반환 타입이 BaseErrorCode 배열인지 체크 (안전성 강화)
            if (BaseErrorCode[].class.isAssignableFrom(method.getReturnType())) {
                try {
                    BaseErrorCode[] result = (BaseErrorCode[]) method.invoke(annotation);
                    if (result != null) {
                        errorCodes.addAll(Arrays.asList(result));
                    }
                } catch (Exception e) {
                    log.error("Swagger 예시 생성 중 리플렉션 오류 발생: {}", method.getName(), e);
                    throw new GeneralException(ErrorCode.SWAGGER_ANNOTATION_ERROR);
                }
            }
        }
        return errorCodes;
    }

  private void addErrorCodeExample(ApiResponses responses, BaseErrorCode errorCode) {
    String statusCode = String.valueOf(errorCode.getStatus());
    ExampleHolder holder = createExampleHolder(errorCode);

    ApiResponse apiResponse = responses.computeIfAbsent(statusCode, code -> new ApiResponse());

    Content content = Optional.ofNullable(apiResponse.getContent()).orElseGet(Content::new);
    apiResponse.setContent(content);

    MediaType mediaType =
        Optional.ofNullable(content.get(JSON_MEDIA_TYPE))
            .orElseGet(
                () -> {
                  MediaType mt = new MediaType();
                  mt.setSchema(new Schema<>().$ref(RESPONSE_DTO_REF));
                  content.addMediaType(JSON_MEDIA_TYPE, mt);
                  return mt;
                });

    mediaType.addExamples(holder.name(), holder.holder());
  }

  private ExampleHolder createExampleHolder(BaseErrorCode errorCode) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("isSuccess", false);
    body.put("code", errorCode.getCode());
    body.put("message", errorCode.getMessage());
    body.put("result", null);

      String errorName = errorCode.toString();

    // Swagger Example 객체 생성
    Example example = new Example();
    example.setSummary(errorName); // 드롭다운에 표시될 이름
    example.setValue(body); // 위에서 만든 Map이 JSON으로 출력됨

    return ExampleHolder.builder()
        .name(errorName)
        .status(errorCode.getStatus().value()) // HttpStatus의 숫자값 (예: 400)
        .code(errorCode.getCode())
        .holder(example)
        .build();
  }
}
