package com.umc9th.bizscan.global.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.BaseErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomErrorSend {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static void handleException(
      HttpServletResponse response, BaseErrorCode errorCode, Object errorPoint) throws IOException {

    ApiResponse<Object> apiResponse = ApiResponse.onFailure(errorCode, errorPoint);

    response.setStatus(errorCode.getStatus().value());
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
  }
}
