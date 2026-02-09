package com.umc9th.bizscan.domain.aiAnalysis.controller;

import com.umc9th.bizscan.domain.aiAnalysis.dto.request.AnalysisReqDTO;
import com.umc9th.bizscan.domain.aiAnalysis.dto.response.*;
import com.umc9th.bizscan.domain.aiAnalysis.enums.SwotType;
import com.umc9th.bizscan.domain.aiAnalysis.service.AiAnalysisService;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.code.SuccessCode;
import com.umc9th.bizscan.global.config.swagger.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI Analysis", description = "AI 기반 SWOT 분석 및 실행 전략 추천 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
public class AiAnalysisController {

  private final AiAnalysisService aiAnalysisService;

  // 분석 요청 (프론트에서 최초 1회 호출)
  @Operation(
          summary = "매장 AI 분석 요청",
          description = """
            특정 매장(storeId)에 대해 AI 분석을 요청합니다.
            
            - 분석 요청 시 비동기 방식으로 AI 서버(FastAPI)에 분석을 전달합니다.
            - 분석 요청이 성공하면 requestId를 반환합니다.
            - 반환된 requestId는 이후 분석 상태 조회 및 결과 조회에 사용됩니다.
            
            **[주의사항]**
            1. 이미 분석이 완료된 경우 `ANALYSIS_ALREADY_IN_COMPLETED` 에러가 발생합니다. 재시도를 원할 경우 `retry: true`로 요청하세요.
            2. 이미 분석이 진행 중인 경우 `ANALYSIS_ALREADY_IN_PROGRESS` 에러가 발생합니다.
            """)
  @ApiErrorCodeExamples({ErrorCode.STORE_NOT_FOUND, ErrorCode.ANALYSIS_ALREADY_IN_COMPLETED, ErrorCode.ANALYSIS_ALREADY_IN_PROGRESS, ErrorCode.ANALYSIS_SERVER_ERROR})
  @PostMapping
  public ApiResponse<AnalysisRequestResponse> analyze(@RequestBody AnalysisReqDTO.AiAnalysisDTO request) {
    return ApiResponse.onSuccess(SuccessCode.OK, aiAnalysisService.analyzeStore(request));
  }

  // 분석 상태 조회 (폴링용)
  @Operation(
      summary = "AI 분석 상태 조회",
      description =
          """
    requestId를 기준으로 AI 분석 진행 상태를 조회합니다.

    - 프론트엔드에서 주기적으로 호출하는 폴링용 API입니다.
    - 분석 상태는 WAITING / PROCESSING / COMPLETED / FAILED 등으로 구분됩니다.
    """)
  @GetMapping("/{requestId}/status")
  public ApiResponse<AnalysisStatusResponse> getStatus(@PathVariable String requestId) {
    return ApiResponse.onSuccess(SuccessCode.OK, aiAnalysisService.getAnalysisStatus(requestId));
  }

  // AI 캐치프레이즈 badge
  @Operation(
      summary = "AI 캐치프레이즈 조회",
      description =
          """
              특정 매장의 AI 분석 결과에서
              AI가 생성한 캐치프레이즈를 조회합니다.

              - 대시보드 상단 환영 영역의 뱃지(Badge)에 사용됩니다.
              - AI 분석이 완료된 이후 조회 가능합니다.
              - 캐치프레이즈는 매장의 핵심 정체성을 요약한 문구입니다.
              - 최대 15자 이내의 텍스트로 제공됩니다.

              [예외 처리]
              - 캐치프레이즈가 존재하지 않는 경우 null이 반환될 수 있습니다.
              - 프론트엔드에서는 catchphrase 값이 null일 경우
                해당 뱃지 UI를 렌더링하지 않습니다.
              """)
  @ApiErrorCodeExamples({ErrorCode.ANALYSIS_NOT_FOUND})
  @GetMapping("/catchphrase")
  public ApiResponse<CatchphraseResponse> getCatchphrase(
      @Parameter(description = "매장 ID", example = "1") @RequestParam Long storeId) {
    return ApiResponse.onSuccess(SuccessCode.OK, aiAnalysisService.getLatestCatchphrase(storeId));
  }

  // SWOT 대시보드
  @Operation(
      summary = "SWOT 대시보드 조회",
      description =
          """
          특정 매장의 최신 AI SWOT 분석 결과를 조회합니다.

          - AI 분석이 완료된 이후 조회 가능합니다.
          - 대시보드 화면에 사용되는 핵심 SWOT 요약 정보(강점, 약점, 기회, 위협)입니다.
          """)
  @ApiErrorCodeExamples({ErrorCode.ANALYSIS_NOT_FOUND})
  @GetMapping("/swots")
  public ApiResponse<List<AnalysisResDTO.SwotDTO>> getDashboardSwot(
      @Parameter(description = "매장 ID", example = "1") @RequestParam Long storeId) {
    return ApiResponse.onSuccess(SuccessCode.OK, aiAnalysisService.getSwots(storeId));
  }

  // AI 정밀 진단
  @Operation(
      summary = "SWOT 정밀 진단 조회",
      description =
          """
          매장 정보를 기반으로 생성된 특정 SWOT 항목의 AI 정밀 진단 결과를 조회합니다.

          - 특정 강점이나 약점 등에 대한 AI의 심층 분석 내용을 제공합니다.
          - SWOT 아이템 ID를 경로 변수로 받습니다.
          """)
  @ApiErrorCodeExamples({ErrorCode.SWOT_NOT_FOUND})
  @GetMapping("/swots/{swotId}/diagnosis")
  public ApiResponse<DiagnosisResponse> getDiagnose(
      @Parameter(description = "SWOT 아이템 ID", example = "10") @PathVariable Long swotId) {
    return ApiResponse.onSuccess(SuccessCode.OK, aiAnalysisService.getDiagnosis(swotId));
  }

  // 맞춤 실행 전략
  @Operation(
      summary = "실행 전략 목록 조회",
      description =
          """
          AI 분석 결과를 기반으로 생성된 실행 전략(Action Plan) 목록을 조회합니다.

          - 매장 특성과 SWOT 분석 결과를 반영한 전략 리스트입니다.
          - 각 전략은 제목과 관련 태그(예: 마케팅, 운영 등)를 포함합니다.
          """)
  @ApiErrorCodeExamples({ErrorCode.ANALYSIS_NOT_FOUND})
  @GetMapping("/action-plans")
  public ApiResponse<List<AnalysisResDTO.ActionPlanDTO>> getActionPlans(
      @Parameter(description = "매장 ID", example = "1") @RequestParam Long storeId,
      @Parameter(description = "필터링할 SWOT 타입 (S, W, O, T)", example = "S")
          @RequestParam(required = false)
          SwotType swotType) {
    return ApiResponse.onSuccess(
        SuccessCode.OK, aiAnalysisService.getActionPlans(storeId, swotType));
  }

  @Operation(
      summary = "실행 전략 상세 조회",
      description =
          """
          AI가 생성한 특정 실행 전략(ActionPlan)의 전체 내용과 단계별 상세 지침(ActionDetail)을 조회합니다.

          - 전략의 수립 배경(Reason)과 관련 태그 정보를 포함합니다.
          - 상세 지침(details)은 실행 단계(step) 순으로 제공됩니다.
          """)
  @ApiErrorCodeExamples({ErrorCode.ACTION_PLAN_NOT_FOUND})
  @GetMapping("/action-plans/{actionPlanId}")
  public ApiResponse<AnalysisResDTO.ActionPlanDetailDTO> getActionPlanDetail(
      @Parameter(description = "조회할 실행 전략 ID", example = "1") @PathVariable Long actionPlanId) {
    return ApiResponse.onSuccess(
        SuccessCode.OK, aiAnalysisService.getActionPlanDetail(actionPlanId));
  }
}
