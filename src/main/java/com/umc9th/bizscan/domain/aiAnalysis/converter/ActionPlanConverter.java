package com.umc9th.bizscan.domain.aiAnalysis.converter;

import com.umc9th.bizscan.domain.aiAnalysis.dto.request.ActionPlanCallbackReqDTO;
import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionDetail;
import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlan;
import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlanTag;
import com.umc9th.bizscan.domain.aiAnalysis.entity.Analysis;
import com.umc9th.bizscan.domain.aiAnalysis.enums.RelatedSwotType;
import com.umc9th.bizscan.domain.aiAnalysis.enums.TagType;

public class ActionPlanConverter {
    // 전략(ActionPlan) 엔티티 변환
    public static ActionPlan toActionPlan(ActionPlanCallbackReqDTO.FinalSelectResult dto, Analysis analysis, RelatedSwotType swotType) {
        return ActionPlan.builder()
                .analysis(analysis)
                .aiRefId(dto.id())
                .title(dto.title())
                .reason(dto.final_reason())
                .relatedSwot(swotType)
                .build();
    }

    // 전략 태그(ActionPlanTag) 엔티티 변환
    public static ActionPlanTag toActionPlanTag(String tagContent, ActionPlan actionPlan, TagType type) {
        return ActionPlanTag.builder()
                .actionPlan(actionPlan)
                .content(tagContent)
                .type(type)
                .build();
    }

    // 세부 실행 계획(ActionDetail) 엔티티 변환
    public static ActionDetail toActionDetail(ActionPlanCallbackReqDTO.ActionDetail dto, ActionPlan actionPlan) {
        return com.umc9th.bizscan.domain.aiAnalysis.entity.ActionDetail.builder()
                .actionPlan(actionPlan)
                .step(dto.step())
                .title(dto.title())
                .description(dto.description())
                .expectedOutcome(dto.expectedOutcome())
                .isCompleted(false)
                .build();
    }
}
