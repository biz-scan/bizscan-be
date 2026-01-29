package com.umc9th.bizscan.domain.aiAnalysis.controller;

import com.umc9th.bizscan.domain.aiAnalysis.dto.request.DiagnosisRequest;
import com.umc9th.bizscan.domain.aiAnalysis.dto.response.*;
import com.umc9th.bizscan.domain.aiAnalysis.entity.Swot;
import com.umc9th.bizscan.domain.aiAnalysis.service.AiAnalysisService;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI Analysis", description = "AI 기반 SWOT 분석 및 실행 전략 추천 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-analysis")
public class AiAnalysisController {

  private final AiAnalysisService aiAnalysisService;

  // 분석 요청 (프론트에서 최초 1회 호출)
  @Operation(
      summary = "매장 AI 분석 요청",
      description =
          """
    특정 매장(storeId)에 대해 AI 분석을 요청합니다.

    - 분석 요청 시 비동기 방식으로 AI 서버에 분석을 전달합니다.
    - 분석 요청이 성공하면 requestId를 반환합니다.
    - 반환된 requestId는 이후 분석 상태 조회 및 결과 조회에 사용됩니다.
    """)
  @PostMapping
  public ApiResponse<String> analyze(@RequestParam Long storeId) {
    String requestId = aiAnalysisService.analyzeStore(storeId);
    return ApiResponse.onSuccess(SuccessCode.OK, requestId);
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
              특정 매장의 최신 AI 분석 결과에서
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
  @GetMapping("/{storeId}/catchphrase")
  public ApiResponse<CatchphraseResponse> getCatchphrase(@PathVariable Long storeId) {
    return ApiResponse.onSuccess(SuccessCode.OK, aiAnalysisService.getLatestCatchphrase(storeId));
  }

  // SWOT 대시보드
  @Operation(
      summary = "SWOT 대시보드 조회",
      description =
          """
    특정 매장의 최신 AI SWOT 분석 결과를 조회합니다.

    - AI 분석이 완료된 이후 조회 가능합니다.
    - 대시보드 화면에 사용되는 핵심 SWOT 요약 정보입니다.
    """)
  @GetMapping("/dashboard/swot")
  public ApiResponse<DashboardSwotResponse> getDashboardSwot(@RequestParam Long storeId) {
    Swot swot = aiAnalysisService.getLatestSwotEntity(storeId);

    return ApiResponse.onSuccess(SuccessCode.OK, DashboardSwotResponse.from(swot));
  }

  // AI 정밀 진단
  @Operation(
      summary = "AI 정밀 진단 생성",
      description =
          """
    SWOT 분석 결과 및 매장 정보를 기반으로
    AI 정밀 진단 결과를 생성합니다.

    - 사용자의 선택 또는 추가 입력을 반영하여 진단을 수행합니다.
    - 단순 요약이 아닌 해석 및 개선 방향 중심의 진단 결과를 제공합니다.
    """)
  @PostMapping("/diagnosis")
  public ApiResponse<DiagnosisResponse> diagnose(@RequestBody DiagnosisRequest request) {
    return ApiResponse.onSuccess(SuccessCode.OK, aiAnalysisService.generateDiagnosis(request));
  }

  // 맞춤 실행 전략
  @Operation(
      summary = "맞춤 실행 전략 목록 조회",
      description =
          """
    AI 분석 결과를 기반으로 생성된 맞춤 실행 전략(Action Plan) 목록을 조회합니다.

    - 매장 특성과 SWOT 분석 결과를 반영한 전략 리스트입니다.
    """)
  @GetMapping("/action-plans")
  public ApiResponse<ActionPlanListResponse> getActionPlans(@RequestParam Long storeId) {
    return ApiResponse.onSuccess(SuccessCode.OK, aiAnalysisService.getActionPlans(storeId));
  }

  @GetMapping("/action-plans/{solutionId}")
  @Operation(
      summary = "맞춤 실행 전략 상세 조회",
      description =
          """
    특정 실행 전략(solutionId)의 상세 정보를 조회합니다.

    - 실행 전략의 배경, 기대 효과, 실행 방법 등의 상세 정보를 제공합니다.
    """)
  public ApiResponse<ActionPlanDetailResponse> getActionPlanDetail(@PathVariable Long solutionId) {
    return ApiResponse.onSuccess(SuccessCode.OK, aiAnalysisService.getActionPlanDetail(solutionId));
  }
}
