package com.umc9th.bizscan.domain.aiAnalysis.dto.request;

import com.umc9th.bizscan.domain.aiAnalysis.dto.response.FastApiAiAnalysisResponse;
import lombok.Getter;

@Getter
public class AiAnalysisCallbackRequest {

  private String requestId;

  // FastAPI가 분석 결과 그대로 넘겨줌
  private FastApiAiAnalysisResponse result;
}
