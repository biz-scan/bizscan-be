package com.umc9th.bizscan.domain.aiAnalysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc9th.bizscan.domain.aiAnalysis.dto.request.ActionPlanAnalyzeRequest;
import com.umc9th.bizscan.domain.aiAnalysis.dto.response.FastApiActionPlanResponse;
import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlan;
import com.umc9th.bizscan.domain.aiAnalysis.entity.Swot;
import com.umc9th.bizscan.domain.aiAnalysis.enums.ActionCategory;
import com.umc9th.bizscan.domain.aiAnalysis.repository.ActionPlanRepository;
import com.umc9th.bizscan.domain.aiAnalysis.repository.SwotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActionPlanService {

    private final ActionPlanRepository actionPlanRepository;
    private final SwotRepository swotRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Transactional
    public void generateActionPlan(Long swotId) {

        // 1. SWOT 조회
        Swot swot = swotRepository.findById(swotId)
                .orElseThrow(() -> new IllegalArgumentException("SWOT 없음"));

        // 2. FastAPI 요청
        ActionPlanAnalyzeRequest aiRequest = ActionPlanAnalyzeRequest.builder()
                .storeId(swot.getStoreId())
                .build();

        FastApiActionPlanResponse aiResponse = callFastApi(aiRequest);

// 3. ActionPlan 저장
        for (FastApiActionPlanResponse.ActionPlanItem item : aiResponse.getPlans()) {

            String tagsJson;
            try {
                tagsJson = objectMapper.writeValueAsString(item.getTags());
            } catch (Exception e) {
                throw new RuntimeException("tags JSON 변환 실패", e);
            }

            ActionPlan plan = ActionPlan.builder()
                    .swot(swot)
                    .title(item.getTitle())
                    .category(ActionCategory.valueOf(item.getCategory()))
                    .tags(tagsJson)
                    .reason(item.getReason())
                    .build();

            actionPlanRepository.save(plan);
        }

    }

    private FastApiActionPlanResponse callFastApi(ActionPlanAnalyzeRequest request) {
        String url = "http://localhost:8000/action-plan";
        return restTemplate.postForObject(url, request, FastApiActionPlanResponse.class);
    }
}
