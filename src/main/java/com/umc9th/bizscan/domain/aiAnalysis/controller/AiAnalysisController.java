package com.umc9th.bizscan.domain.aiAnalysis.controller;

import com.umc9th.bizscan.domain.aiAnalysis.dto.response.AnalysisStatusResponse;
import com.umc9th.bizscan.domain.aiAnalysis.dto.response.DashboardSwotResponse;
import com.umc9th.bizscan.domain.aiAnalysis.entity.Swot;
import com.umc9th.bizscan.domain.aiAnalysis.service.AiAnalysisService;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-analysis")
public class AiAnalysisController {

  private final AiAnalysisService aiAnalysisService;

  // 분석 요청 (프론트에서 최초 1회 호출)
  @PostMapping
  public ApiResponse<String> analyze(@RequestParam Long storeId) {
    String requestId = aiAnalysisService.analyzeStore(storeId);
    return ApiResponse.onSuccess(SuccessCode.OK, requestId);
  }

  // 분석 상태 조회 (폴링용)
  @GetMapping("/{requestId}/status")
  public ApiResponse<AnalysisStatusResponse> getStatus(@PathVariable String requestId) {
    return ApiResponse.onSuccess(SuccessCode.OK, aiAnalysisService.getAnalysisStatus(requestId));
  }

  @GetMapping("/dashboard/swot")
  public ApiResponse<DashboardSwotResponse> getDashboardSwot(@RequestParam Long storeId) {
    Swot swot = aiAnalysisService.getLatestSwotEntity(storeId);

    return ApiResponse.onSuccess(SuccessCode.OK, DashboardSwotResponse.from(swot));
  }
}
