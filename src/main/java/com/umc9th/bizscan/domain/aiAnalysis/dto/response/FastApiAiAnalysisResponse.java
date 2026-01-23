package com.umc9th.bizscan.domain.aiAnalysis.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class FastApiAiAnalysisResponse {

    private SwotPart swot;
    private List<ActionPlanPart> actionPlans;

    // SWOT
    @Getter
    public static class SwotPart {
        private String badge;

        private String sTitle;
        private String sDetail;

        private String wTitle;
        private String wDetail;

        private String oTitle;
        private String oDetail;

        private String tTitle;
        private String tDetail;
    }

    // ActionPlan
    @Getter
    public static class ActionPlanPart {
        private String title;
        private String category;
        private List<String> tags;
        private String reason;

        private List<ActionDetailPart> details;
    }

    // ActionDetail
    @Getter
    public static class ActionDetailPart {
        private String content;
    }
}
