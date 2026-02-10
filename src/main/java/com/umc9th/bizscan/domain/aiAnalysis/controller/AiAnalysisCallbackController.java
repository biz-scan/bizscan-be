package com.umc9th.bizscan.domain.aiAnalysis.controller;

import com.umc9th.bizscan.domain.aiAnalysis.dto.request.ActionPlanCallbackReqDTO;
import com.umc9th.bizscan.domain.aiAnalysis.dto.request.FailCallbackReqDTO;
import com.umc9th.bizscan.domain.aiAnalysis.dto.request.SwotCallbackReqDTO;
import com.umc9th.bizscan.domain.aiAnalysis.service.CallbackService;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.code.SuccessCode;
import com.umc9th.bizscan.global.config.swagger.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI Analysis - Callback", description = "AI 서버(FastAPI)에서 호출하는 분석 결과 콜백 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis/callback")
public class AiAnalysisCallbackController {
  private final CallbackService callbackService;

  @Operation(
      summary = "SWOT 분석 결과 콜백 (1, 2차)",
      description =
          """
          AI 서버로부터 SWOT 분석 단계의 결과를 수신합니다.

          - **1차 콜백 (SWOT_PROCESSING)**: SWOT 분석이 시작되었음을 알립니다.
          - **2차 콜백 (ACTION_PLAN_PROCESSING)**: 캐치프레이즈 및 4개 영역(S,W,O,T)의 분석 데이터가 포함됩니다.
          - 수신 성공 시 상태가 '실행 계획 생성 중'으로 변경됩니다.
          """)
  @ApiErrorCodeExamples({ErrorCode.ANALYSIS_REQUEST_NOT_FOUND, ErrorCode.INVALID_CALLBACK_DATA})
  @PostMapping("/swots")
  public ApiResponse<Void> callbackSwot(@RequestBody SwotCallbackReqDTO.SwotCallbackDTO request) {
    callbackService.saveSwot(request);
    return ApiResponse.onSuccess(SuccessCode.OK, null);
  }

  @Operation(
      summary = "맞춤 전략(Action Plan) 선정 결과 콜백 (3차)",
      description =
          """
          AI 서버로부터 선정된 실행 전략(Action Plan) 리스트와 관련 태그 정보를 수신합니다.

          - 수신된 전략들은 DB에 저장되며, 분석 상태가 '상세 계획 생성 중'으로 변경됩니다.
          - 관련 SWOT 타입(S, W, O, T)과 전략 태그(목표, 난이도 등)가 함께 저장됩니다.
          """)
  @ApiErrorCodeExamples({ErrorCode.ANALYSIS_REQUEST_NOT_FOUND})
  @PostMapping("/action-plans")
  public ApiResponse<Void> callbackActionPlan(
      @RequestBody ActionPlanCallbackReqDTO.FinalSelectCallbackDTO request) {

    callbackService.saveActionPlans(request);
    return ApiResponse.onSuccess(SuccessCode.OK, null);
  }

  @Operation(
      summary = "세부 실행 지침(Action Detail) 결과 콜백 (4차)",
      description =
          """
          AI 서버로부터 각 전략에 대한 구체적인 단계별 실행 지침을 수신합니다.

          - 모든 데이터 저장이 완료되면 전체 분석 프로세스의 상태가 **COMPLETED**로 변경됩니다.
          - 이후 사용자는 대시보드에서 최종 결과를 조회할 수 있습니다.
          """)
  @ApiErrorCodeExamples({ErrorCode.ANALYSIS_REQUEST_NOT_FOUND, ErrorCode.ACTION_PLAN_NOT_FOUND})
  @PostMapping("/action-details")
  public ApiResponse<Void> callbackActionDetail(
      @RequestBody ActionPlanCallbackReqDTO.ActionDetailCallbackDTO request) {
    callbackService.saveActionDetails(request);
    return ApiResponse.onSuccess(SuccessCode.OK, null);
  }

  @Operation(
      summary = "AI 분석 실패 알림 콜백",
      description =
          """
          AI 서버 내부에서 분석 도중 오류가 발생한 경우 호출됩니다.

          - 해당 요청(requestId)의 상태를 **FAILED**로 변경하고 실패 사유를 기록합니다.
          - 폴링 중인 클라이언트는 이 상태를 확인하여 사용자에게 에러 UI를 노출할 수 있습니다.
          """)
  @ApiErrorCodeExamples({ErrorCode.ANALYSIS_REQUEST_NOT_FOUND})
  @PostMapping("/fail")
  public ApiResponse<Void> callbackFail(@RequestBody FailCallbackReqDTO request) {
    callbackService.failAnalysis(request);
    return ApiResponse.onSuccess(SuccessCode.OK, null);
  }
}
