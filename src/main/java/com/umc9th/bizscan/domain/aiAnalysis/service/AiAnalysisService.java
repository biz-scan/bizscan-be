package com.umc9th.bizscan.domain.aiAnalysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc9th.bizscan.domain.aiAnalysis.dto.request.AiAnalysisCallbackRequest;
import com.umc9th.bizscan.domain.aiAnalysis.dto.request.AiAnalysisRequest;
import com.umc9th.bizscan.domain.aiAnalysis.dto.response.AnalysisStatusResponse;
import com.umc9th.bizscan.domain.aiAnalysis.dto.response.FastApiAiAnalysisResponse;
import com.umc9th.bizscan.domain.aiAnalysis.dto.response.SwotResponse;
import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlan;
import com.umc9th.bizscan.domain.aiAnalysis.entity.AnalysisRequest;
import com.umc9th.bizscan.domain.aiAnalysis.entity.Swot;
import com.umc9th.bizscan.domain.aiAnalysis.enums.ActionCategory;
import com.umc9th.bizscan.domain.aiAnalysis.enums.AnalysisStatus;
import com.umc9th.bizscan.domain.aiAnalysis.repository.ActionPlanRepository;
import com.umc9th.bizscan.domain.aiAnalysis.repository.AnalysisRequestRepository;
import com.umc9th.bizscan.domain.aiAnalysis.repository.SwotRepository;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.exception.GeneralException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiAnalysisService {

  private final SwotRepository swotRepository;
  private final ActionPlanRepository actionPlanRepository;
  private final AnalysisRequestRepository analysisRequestRepository;
  private final ObjectMapper objectMapper;

  private final RestTemplate restTemplate = new RestTemplate();

  /** AI 분석 요청 (프론트에서 최초 1회 호출) requestId 반환 → 프론트에서 폴링 */
  @Transactional
  public String analyzeStore(Long storeId) {

    // 1. requestId 생성
    String requestId = UUID.randomUUID().toString();

    // 2. 분석 요청 상태 저장
    AnalysisRequest request =
        AnalysisRequest.builder()
            .requestId(requestId)
            .storeId(storeId)
            .status(AnalysisStatus.PROCESSING)
            .progressMessage("매장 정보를 분석 중입니다.")
            .build();

    analysisRequestRepository.save(request);

    try {
      // 3. FastAPI 호출 (현재는 동기)
      FastApiAiAnalysisResponse aiResponse = callFastApi(storeId);

      // 4. SWOT 저장
      Swot swot =
          new Swot(
              storeId,
              aiResponse.getSwot().getSTitle(),
              aiResponse.getSwot().getSDetail(),
              aiResponse.getSwot().getWTitle(),
              aiResponse.getSwot().getWDetail(),
              aiResponse.getSwot().getOTitle(),
              aiResponse.getSwot().getODetail(),
              aiResponse.getSwot().getTTitle(),
              aiResponse.getSwot().getTDetail());
      swotRepository.save(swot);

      // 5. ActionPlan 저장
      for (FastApiAiAnalysisResponse.ActionPlanPart planDto : aiResponse.getActionPlans()) {

        String tagsJson = objectMapper.writeValueAsString(planDto.getTags());

        ActionPlan plan =
            ActionPlan.builder()
                .swot(swot)
                .title(planDto.getTitle())
                .category(ActionCategory.valueOf(planDto.getCategory()))
                .tags(tagsJson)
                .reason(planDto.getReason())
                .build();

        actionPlanRepository.save(plan);
      }

      // 6. 완료 처리
      request.complete();

    } catch (Exception e) {
      request.fail("분석 중 오류가 발생했습니다.");
      throw new RuntimeException(e);
    }

    // 7. requestId 반환 (폴링용)
    return requestId;
  }

  /** 분석 상태 조회 (폴링 API) */
  public AnalysisStatusResponse getAnalysisStatus(String requestId) {
    AnalysisRequest request =
        analysisRequestRepository
            .findByRequestId(requestId)
            .orElseThrow(() -> new GeneralException(ErrorCode.ANALYSIS_REQUEST_NOT_FOUND));

    return new AnalysisStatusResponse(request.getStatus(), request.getProgressMessage());
  }

  /** 최신 SWOT 조회 (대시보드용) */
  public SwotResponse getLatestSwot(Long storeId) {
    Swot swot =
        swotRepository
            .findTopByStoreIdOrderByCreatedAtDesc(storeId)
            .orElseThrow(() -> new GeneralException(ErrorCode.SWOT_NOT_FOUND));

    return SwotResponse.from(swot);
  }

  /** FastAPI 호출 */
  private FastApiAiAnalysisResponse callFastApi(Long storeId) {
    String url = "http://localhost:8000/ai-analysis";

    return restTemplate.postForObject(
        url, new AiAnalysisRequest(storeId), FastApiAiAnalysisResponse.class);
  }

  /** FastAPI 콜백 처리 (비동기 전환 대비) */
  @Transactional
  public void completeAnalysis(AiAnalysisCallbackRequest callback) {

    AnalysisRequest request =
        analysisRequestRepository
            .findByRequestId(callback.getRequestId())
            .orElseThrow(() -> new GeneralException(ErrorCode.ANALYSIS_REQUEST_NOT_FOUND));

    try {
      FastApiAiAnalysisResponse result = callback.getResult();

      Swot swot =
          new Swot(
              request.getStoreId(),
              result.getSwot().getSTitle(),
              result.getSwot().getSDetail(),
              result.getSwot().getWTitle(),
              result.getSwot().getWDetail(),
              result.getSwot().getOTitle(),
              result.getSwot().getODetail(),
              result.getSwot().getTTitle(),
              result.getSwot().getTDetail());
      swotRepository.save(swot);

      for (FastApiAiAnalysisResponse.ActionPlanPart planDto : result.getActionPlans()) {

        String tagsJson = objectMapper.writeValueAsString(planDto.getTags());

        ActionPlan plan =
            ActionPlan.builder()
                .swot(swot)
                .title(planDto.getTitle())
                .category(ActionCategory.valueOf(planDto.getCategory()))
                .tags(tagsJson)
                .reason(planDto.getReason())
                .build();

        actionPlanRepository.save(plan);
      }

      request.complete();

    } catch (Exception e) {
      request.fail("분석 중 오류가 발생했습니다.");
      throw new RuntimeException(e);
    }
  }
}
