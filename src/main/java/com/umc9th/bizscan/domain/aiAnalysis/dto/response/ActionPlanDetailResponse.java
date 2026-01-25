package com.umc9th.bizscan.domain.aiAnalysis.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ActionPlanDetailResponse {

  private Long solutionId;
  private String title;
  private String reason;
  private List<String> steps;
  private boolean isAdded;
}
