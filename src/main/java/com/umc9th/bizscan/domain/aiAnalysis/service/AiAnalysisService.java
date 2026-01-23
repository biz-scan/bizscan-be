package com.umc9th.bizscan.domain.aiAnalysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc9th.bizscan.domain.aiAnalysis.dto.request.AiAnalysisRequest;
import com.umc9th.bizscan.domain.aiAnalysis.dto.response.FastApiAiAnalysisResponse;
import com.umc9th.bizscan.domain.aiAnalysis.dto.response.SwotResponse;
import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlan;
import com.umc9th.bizscan.domain.aiAnalysis.entity.Swot;
import com.umc9th.bizscan.domain.aiAnalysis.enums.ActionCategory;
import com.umc9th.bizscan.domain.aiAnalysis.repository.ActionPlanRepository;
import com.umc9th.bizscan.domain.aiAnalysis.repository.SwotRepository;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.exception.GeneralException;
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
  private final ObjectMapper objectMapper;

  private final RestTemplate restTemplate = new RestTemplate();

  // AI 분석 실행 (SWOT + ActionPlan)
  @Transactional
  public void analyzeStore(Long storeId) {

    // 1. FastAPI 호출
    FastApiAiAnalysisResponse aiResponse = callFastApi(storeId);

    // 2. SWOT 저장
    Swot swot =
        Swot.builder()
            .storeId(storeId)
            .sTitle(aiResponse.getSwot().getSTitle())
            .sDetail(aiResponse.getSwot().getSDetail())
            .wTitle(aiResponse.getSwot().getWTitle())
            .wDetail(aiResponse.getSwot().getWDetail())
            .oTitle(aiResponse.getSwot().getOTitle())
            .oDetail(aiResponse.getSwot().getODetail())
            .tTitle(aiResponse.getSwot().getTTitle())
            .tDetail(aiResponse.getSwot().getTDetail())
            .build();

    swotRepository.save(swot);

    // 3️. ActionPlan 저장
    for (FastApiAiAnalysisResponse.ActionPlanPart planDto : aiResponse.getActionPlans()) {

      String tagsJson;
      try {
        tagsJson = objectMapper.writeValueAsString(planDto.getTags());
      } catch (Exception e) {
        throw new RuntimeException("tags JSON 변환 실패", e);
      }

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
  }

  // 최신 SWOT 조회
  public SwotResponse getLatestSwot(Long storeId) {
    Swot swot =
        swotRepository
            .findTopByStoreIdOrderByCreatedAtDesc(storeId)
            .orElseThrow(() -> new GeneralException(ErrorCode.SWOT_NOT_FOUND));

    return SwotResponse.from(swot);
  }

  // FastAPI 호출
  private FastApiAiAnalysisResponse callFastApi(Long storeId) {
    String url = "http://localhost:8000/ai-analysis";

    return restTemplate.postForObject(
        url, new AiAnalysisRequest(storeId), FastApiAiAnalysisResponse.class);
  }
}
