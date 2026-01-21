package com.umc9th.bizscan.global.config.swagger;

import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.*;

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

        // 2. 어노테이션의 에러 코드 배열을 리스트로 변환
        List<ErrorCode> errorCodes = Arrays.asList(annotation.value());

        // 2. Operation에서 Responses 확보 (null 방어)
        ApiResponses responses = Optional.ofNullable(operation.getResponses()).orElseGet(ApiResponses::new);
        operation.setResponses(responses);

        // 3. 에러 코드 주입
        errorCodes.forEach(errorCode -> addErrorCodeExample(responses, errorCode));

        return operation;
    }

    private void addErrorCodeExample(ApiResponses responses, ErrorCode errorCode) {
        String statusCode = String.valueOf(errorCode.getStatus());
        ExampleHolder holder = createExampleHolder(errorCode);

        // 4. ApiResponse 및 Content 설정 과정을 체이닝과 computeIfAbsent로 간소화
        ApiResponse apiResponse = responses.computeIfAbsent(statusCode, code -> new ApiResponse());

        Content content = Optional.ofNullable(apiResponse.getContent()).orElseGet(Content::new);
        apiResponse.setContent(content);

        MediaType mediaType = Optional.ofNullable(content.get(JSON_MEDIA_TYPE)).orElseGet(() -> {
            MediaType mt = new MediaType();
            mt.setSchema(new Schema<>().$ref(RESPONSE_DTO_REF));
            content.addMediaType(JSON_MEDIA_TYPE, mt);
            return mt;
        });

        mediaType.addExamples(holder.name(), holder.holder());
    }

    private ExampleHolder createExampleHolder(ErrorCode errorCode) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("isSuccess", false);
        body.put("code", errorCode.getCode());
        body.put("message", errorCode.getMessage());
        body.put("result", null);

        // Swagger Example 객체 생성
        Example example = new Example();
        example.setSummary(errorCode.name());    // 드롭다운에 표시될 이름
        example.setValue(body);                  // 위에서 만든 Map이 JSON으로 출력됨

        return ExampleHolder.builder()
                .name(errorCode.name())
                .status(errorCode.getStatus().value()) // HttpStatus의 숫자값 (예: 400)
                .code(errorCode.getCode())
                .holder(example)
                .build();
    }
}