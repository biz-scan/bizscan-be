package com.umc9th.bizscan.domain.aiAnalysis.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ActionPlanCallbackReqDTO {

    // --- 3차 콜백: 전략 선정 결과 (FinalSelectCallbackDTO) ---
    public record FinalSelectCallbackDTO(
            boolean isSuccess,
            String code,
            String message,
            @JsonProperty("request_id") String requestId,
            String status,
            FinalSelectReqDTO result
    ) implements CommonCallback {}

    public record FinalSelectReqDTO(
            @JsonProperty("final_select") List<FinalSelectResult> selections
    ) {}

    public record FinalSelectResult(
            int id,
            String title,
            List<String> tags,
            @JsonProperty("related_swot") List<String> related_swot,
            @JsonProperty("final_reason") String final_reason
    ) {}

    // --- 4차 콜백: 상세 실행 계획 (ActionDetailCallbackDTO) ---
    public record ActionDetailCallbackDTO(
            boolean isSuccess,
            String code,
            String message,
            @JsonProperty("request_id") String requestId,
            String status,
            ActionDetailReqDTO result
    ) implements CommonCallback {}

    public record ActionDetailReqDTO(
            @JsonProperty("action_plan") ActionDetailResult actionPlan
    ) {}

    public record ActionDetailResult(
            List<ActionPlan> plans
    ) {}

    public record ActionPlan(
            int id,
            String title,
            @JsonProperty("action_detail") List<ActionDetail> actionDetail
    ) {}

    public record ActionDetail(
            int step,
            String title,
            String description,
            @JsonProperty("expected_outcome") String expectedOutcome
    ) {}
}
