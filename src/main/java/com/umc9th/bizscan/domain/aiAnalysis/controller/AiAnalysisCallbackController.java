package com.umc9th.bizscan.domain.aiAnalysis.controller;

import com.umc9th.bizscan.domain.aiAnalysis.dto.request.ActionPlanCallbackReqDTO;
import com.umc9th.bizscan.domain.aiAnalysis.dto.request.FailCallbackReqDTO;
import com.umc9th.bizscan.domain.aiAnalysis.dto.request.SwotCallbackReqDTO;
import com.umc9th.bizscan.domain.aiAnalysis.service.CallbackService;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.SuccessCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI Analysis - Callback", description = "AI 서버(FastAPI)에서 호출하는 분석 결과 콜백 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis/callback")
public class AiAnalysisCallbackController {
  private final CallbackService callbackService;

  @PostMapping("/swots")
  public ApiResponse<Void> callbackSwot(@RequestBody SwotCallbackReqDTO.SwotCallbackDTO request) {
    callbackService.saveSwot(request);
    return ApiResponse.onSuccess(SuccessCode.OK, null);
  }

  @PostMapping("/action-plans")
  public ApiResponse<Void> callbackActionPlan(
      @RequestBody ActionPlanCallbackReqDTO.FinalSelectCallbackDTO request) {

    callbackService.saveActionPlans(request);
    return ApiResponse.onSuccess(SuccessCode.OK, null);
  }

  @PostMapping("/action-details")
  public ApiResponse<Void> callbackActionDetail(
      @RequestBody ActionPlanCallbackReqDTO.ActionDetailCallbackDTO request) {
    callbackService.saveActionDetails(request);
    return ApiResponse.onSuccess(SuccessCode.OK, null);
  }

  @PostMapping("/fail")
  public ApiResponse<Void> callbackFail(@RequestBody FailCallbackReqDTO request) {
    callbackService.failAnalysis(request);
    return ApiResponse.onSuccess(SuccessCode.OK, null);
  }
}
