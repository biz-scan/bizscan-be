package com.umc9th.bizscan.domain.aiAnalysis.controller;

import com.umc9th.bizscan.domain.aiAnalysis.dto.request.AiAnalysisCallbackRequest;
import com.umc9th.bizscan.domain.aiAnalysis.service.AiAnalysisService;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
public class AiAnalysisCallbackController {
  private final AiAnalysisService aiAnalysisService;

  @PostMapping("/callback")
  public ApiResponse<Void> callback(@RequestBody AiAnalysisCallbackRequest request) {
    aiAnalysisService.completeAnalysis(request);
    return ApiResponse.onSuccess(SuccessCode.OK, null);
  }
}
