package com.umc9th.bizscan.domain.aiAnalysis.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnalysisRequestResponse {
  @Schema(
      description = "폴링 상태 조회 API(GET /api/analysis/{requestId}/status)에 사용되는 요청 ID(requestId)",
      example = "550e8400-e29b-41d4-a716-446655440000")
  private String requestId;
}
