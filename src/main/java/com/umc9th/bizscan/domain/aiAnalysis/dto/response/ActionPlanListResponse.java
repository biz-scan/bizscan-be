package com.umc9th.bizscan.domain.aiAnalysis.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ActionPlanListResponse {

  private List<ActionPlanItem> solutions;

  @Getter
  @AllArgsConstructor
  public static class ActionPlanItem {
    private Long solutionId;
    private String title;
    private List<String> tags;
  }
}
