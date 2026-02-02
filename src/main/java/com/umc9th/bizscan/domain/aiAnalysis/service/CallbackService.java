package com.umc9th.bizscan.domain.aiAnalysis.service;

import com.umc9th.bizscan.domain.aiAnalysis.converter.ActionPlanConverter;
import com.umc9th.bizscan.domain.aiAnalysis.converter.SwotConverter;
import com.umc9th.bizscan.domain.aiAnalysis.dto.request.ActionPlanCallbackReqDTO;
import com.umc9th.bizscan.domain.aiAnalysis.dto.request.SwotCallbackReqDTO;
import com.umc9th.bizscan.domain.aiAnalysis.entity.*;
import com.umc9th.bizscan.domain.aiAnalysis.enums.AnalysisStatus;
import com.umc9th.bizscan.domain.aiAnalysis.enums.RelatedSwotType;
import com.umc9th.bizscan.domain.aiAnalysis.enums.TagType;
import com.umc9th.bizscan.domain.aiAnalysis.repository.*;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.exception.GeneralException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackService {
  private final SwotRepository swotRepository;
  private final AnalysisRequestRepository analysisRequestRepository;
  private final ActionPlanRepository actionPlanRepository;
  private final ActionPlanTagRepository actionPlanTagRepository;
  private final ActionDetailRepository actionDetailRepository;

  /** 1차, 2차 콜백: SWOT 결과 저장 */
  @Transactional
  public void saveSwot(SwotCallbackReqDTO.SwotCallbackDTO request) {
    // 1. 공통: AnalysisRequest 조회
    AnalysisRequest analysisRequest =
        analysisRequestRepository
            .findByRequestId(request.requestId())
            .orElseThrow(() -> new GeneralException(ErrorCode.ANALYSIS_REQUEST_NOT_FOUND));

    String status = request.status();

    // 2. 1차 콜백 처리 (SWOT_PROCESSING)
    if ("SWOT_PROCESSING".equals(status)) {
      analysisRequest.updateStatus(AnalysisStatus.SWOT_PROCESSING);
      return;
    }

    // 3. 2차 콜백 처리 (ACTION_PLAN_PROCESSING)
    if ("ACTION_PLAN_PROCESSING".equals(status)) {
      if (request.result() == null) {
        throw new GeneralException(ErrorCode.INVALID_CALLBACK_DATA); // 데이터 누락 에러 처리
      }

      Analysis analysis = analysisRequest.getAnalysis();
      SwotCallbackReqDTO.SwotReqDTO result = request.result();

      // 3-1. 캐치프레이즈 업데이트
      analysis.updateCatchphrase(result.catchphrase().catchphrase());

      // 3-2. SWOT 리스트 저장
      SwotCallbackReqDTO.SwotResult swot = result.swot();

      // 각 요소를 리스트로 만들어 반복 처리
      List<SwotCallbackReqDTO.SwotItem> swotItems =
          List.of(swot.strengths(), swot.weaknesses(), swot.opportunities(), swot.threats());

      for (SwotCallbackReqDTO.SwotItem item : swotItems) {
        swotRepository.save(SwotConverter.toSwot(item, analysis));
      }

      // 3-3. 상태 업데이트
      analysisRequest.updateStatus(AnalysisStatus.ACTION_PLAN_PROCESSING);
    }
  }

  /** 3차 콜백: 전략 선정 결과 저장 */
  @Transactional
  public void saveActionPlans(ActionPlanCallbackReqDTO.FinalSelectCallbackDTO request) {
    log.info(
        "[saveActionPlans] 3차 콜백 시작 - RequestId: {}, Selections Count: {}",
        request.requestId(),
        request.result().finalSelect().selections().size());

    AnalysisRequest analysisRequest =
        analysisRequestRepository
            .findByRequestId(request.requestId())
            .orElseThrow(
                () -> {
                  log.error(
                      "[saveActionPlans] AnalysisRequest 조회 실패 - RequestId: {}",
                      request.requestId());
                  return new GeneralException(ErrorCode.ANALYSIS_REQUEST_NOT_FOUND);
                });

    Analysis analysis = analysisRequest.getAnalysis();

    for (ActionPlanCallbackReqDTO.FinalSelect selection :
        request.result().finalSelect().selections()) {
      // Enum 변환
      RelatedSwotType swotType = toRelatedSwotType(selection.related_swot());

      // ActionPlan 생성 및 저장
      ActionPlan actionPlan =
          actionPlanRepository.save(
              ActionPlanConverter.toActionPlan(selection, analysis, swotType));

      // 태그 생성 및 저장
      List<String> tags = selection.tags();
      for (int i = 0; i < tags.size(); i++) {
        TagType type = toTagType(i);
        String content = tags.get(i);
        actionPlanTagRepository.save(
            ActionPlanConverter.toActionPlanTag(content, actionPlan, type));
      }
      log.debug(
          "[saveActionPlans] ActionPlan 및 태그 저장 완료 - Plan ID: {}, Tags: {}",
          actionPlan.getId(),
          tags);
    }

    analysisRequest.updateStatus(AnalysisStatus.ACTION_DETAIL_PROCESSING);
    log.info(
        "[saveActionPlans] 3차 콜백 완료 - RequestId: {}, Next Status: {}",
        request.requestId(),
        analysisRequest.getStatus());
  }

  /** 4차 콜백: 상세 실행 계획 저장 */
  @Transactional
  public void saveActionDetails(ActionPlanCallbackReqDTO.ActionDetailCallbackDTO request) {
    log.info("[saveActionDetails] 4차 콜백 시작 - RequestId: {}", request.requestId());

    AnalysisRequest analysisRequest =
        analysisRequestRepository
            .findByRequestId(request.requestId())
            .orElseThrow(
                () -> {
                  log.error(
                      "[saveActionDetails] AnalysisRequest 조회 실패 - RequestId: {}",
                      request.requestId());
                  return new GeneralException(ErrorCode.ANALYSIS_REQUEST_NOT_FOUND);
                });

    Analysis analysis = analysisRequest.getAnalysis();

    for (ActionPlanCallbackReqDTO.ActionPlan planDto : request.result().actionPlan().plans()) {
      ActionPlan actionPlan =
          actionPlanRepository
              .findByAnalysisAndAiRefId(analysis, planDto.id())
              .orElseThrow(
                  () -> {
                    log.warn(
                        "[saveActionDetails] ActionPlan을 찾을 수 없음 - AnalysisId: {}, RefId: {}",
                        analysis.getId(),
                        planDto.id());
                    return new GeneralException(ErrorCode.ACTION_PLAN_NOT_FOUND);
                  });

      // 세부 실행 계획 리스트 저장
      planDto
          .actionDetail()
          .forEach(
              detailDto ->
                  actionDetailRepository.save(
                      ActionPlanConverter.toActionDetail(detailDto, actionPlan)));
      log.debug(
          "[saveActionDetails] 세부 실행 계획 저장 완료 - ActionPlan ID: {}, Detail Count: {}",
          actionPlan.getId(),
          planDto.actionDetail().size());
    }

    analysisRequest.updateStatus(AnalysisStatus.COMPLETED);
    log.info(
        "[saveActionDetails] 4차 콜백 완료 및 분석 종료 - RequestId: {}, Final Status: {}",
        request.requestId(),
        analysisRequest.getStatus());
  }

  // Util
  private RelatedSwotType toRelatedSwotType(List<String> swotList) {
    if (swotList == null || swotList.size() < 2) {
      return null;
    }
    // ["S", "O"] -> "SO"로 합친 후 Enum 변환
    String combined = String.join("", swotList).toUpperCase();
    return RelatedSwotType.valueOf(combined);
  }

  private TagType toTagType(int index) {
    return switch (index) {
      case 0 -> TagType.GOAL;
      case 1 -> TagType.DIFFICULTY;
      case 2 -> TagType.CATEGORY;
      default -> TagType.CATEGORY; // 범위를 벗어날 경우 기본값 혹은 예외 처리
    };
  }
}
