package com.umc9th.bizscan.domain.aiAnalysis.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class FastApiActionPlanResponse {

    private List<ActionPlanItem> plans;

    @Getter
    public static class ActionPlanItem {
        private String title;
        private String category;
        private List<String> tags;
        private String reason;
        private List<ActionDetailItem> details;
    }

    @Getter
    public static class ActionDetailItem {
        private String content;
    }
}
