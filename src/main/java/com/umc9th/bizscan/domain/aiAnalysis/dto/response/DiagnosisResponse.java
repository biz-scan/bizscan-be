package com.umc9th.bizscan.domain.aiAnalysis.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DiagnosisResponse {
  @Schema(
      description = "AI 정밀 진단 내용",
      example = "현재 해당 지역의 유동인구가 증가하는 추세이며, 이는 매장의 접근성 강점과 결합되어 매출 증대 가능성이 매우 높습니다.")
  private String diagnosis;
}
