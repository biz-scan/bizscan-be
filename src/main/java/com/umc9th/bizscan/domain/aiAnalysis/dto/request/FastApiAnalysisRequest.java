package com.umc9th.bizscan.domain.aiAnalysis.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FastApiAnalysisRequest {

  // callback
  private String requestId;
  private String callbackUrl;

  // store info
  private Long storeId;
  private String name;
  private String address;

  private String category;
  private String categoryDetail;
  private String signature;

  private String price;
  private String target;
  private String painPoint;
}
