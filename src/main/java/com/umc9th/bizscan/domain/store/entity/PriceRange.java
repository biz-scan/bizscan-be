package com.umc9th.bizscan.domain.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "평균 결제 금액")
public enum PriceRange {

  @Schema(description = "1만원 미만")
  UNDER_10000,

  @Schema(description = "1만원 ~ 2만원")
  FROM_10000_TO_20000,

  @Schema(description = "2만원 ~ 4만원")
  FROM_20000_TO_40000,

  @Schema(description = "4만원 이상")
  OVER_40000
}