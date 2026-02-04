package com.umc9th.bizscan.domain.aiAnalysis.dto.request;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FastApiAnalysisRequest {

  // store info
  private Long storeId;
  private String name;
  private String address;

  private String category;
  private String categoryDetail;
  private String price;
  private String target;
  private String painPoint;
  private String signature;
  private List<TagInfoRequest> tags;

  // callback
  private String requestId;
  private String swotCallbackUrl;
  private String actionPlanCallbackUrl;
  private String actionDetailCallbackUrl;

  @Getter
  @Builder
  public static class TagInfoRequest {
    private String type;
    private String name;
  }
}
