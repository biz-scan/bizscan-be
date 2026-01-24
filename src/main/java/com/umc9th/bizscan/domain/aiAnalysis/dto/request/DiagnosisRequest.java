package com.umc9th.bizscan.domain.aiAnalysis.dto.request;

import java.util.Map;
import lombok.Getter;

@Getter
public class DiagnosisRequest {

  private String swotType; // S, W, O, T
  private String keyword; // "가격 경쟁력 우수"
  private String description; // "객단가가 주변보다 낮아요"
  private Map<String, String> evidenceData;
}
