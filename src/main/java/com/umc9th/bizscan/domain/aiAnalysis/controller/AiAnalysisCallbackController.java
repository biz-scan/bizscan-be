package com.umc9th.bizscan.domain.aiAnalysis.controller;

import com.umc9th.bizscan.domain.aiAnalysis.dto.request.AiAnalysisCallbackRequest;
import com.umc9th.bizscan.domain.aiAnalysis.service.AiAnalysisService;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI Analysis - Callback", description = "AI 서버(FastAPI)에서 호출하는 분석 결과 콜백 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
public class AiAnalysisCallbackController {
  private final AiAnalysisService aiAnalysisService;

  @Operation(
      summary = "AI 분석 결과 콜백",
      description =
          """
    AI 서버에서 분석 완료 시 결과를 전달하는 내부 콜백 API입니다.

    - FastAPI 기반 AI 서버가 분석 완료 후 호출합니다.
    - 프론트엔드에서는 직접 호출하지 않습니다.
    - requestId를 기준으로 분석 상태를 완료 처리하고,
      SWOT 및 실행 전략(ActionPlan) 결과를 DB에 저장합니다.
    """)
  @PostMapping("/callback")
  public ApiResponse<Void> callback(@RequestBody AiAnalysisCallbackRequest request) {
    aiAnalysisService.completeAnalysis(request);
    return ApiResponse.onSuccess(SuccessCode.OK, null);
  }
}
