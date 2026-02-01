package com.umc9th.bizscan.domain.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "평균 결제 금액")
public enum PriceRange {
  @Schema(description = "1만원 미만")
  UNDER_10000("1만원 미만"),

  @Schema(description = "1만원 ~ 2만원")
  FROM_10000_TO_20000("1만원 ~ 2만원"),

  @Schema(description = "2만원 ~ 4만원")
  FROM_20000_TO_40000("2만원 ~ 4만원"),

  @Schema(description = "4만원 이상")
  OVER_40000("4만원 이상");

  private final String korean;

  PriceRange(String korean) {
    this.korean = korean;
  }
}
