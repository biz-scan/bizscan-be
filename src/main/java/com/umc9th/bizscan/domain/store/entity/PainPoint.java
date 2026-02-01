package com.umc9th.bizscan.domain.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "현재 고민")
public enum PainPoint {
  @Schema(description = "고객 유입")
  CUSTOMER_ACQUISITION("고객 유입"),

  @Schema(description = "단골 관리")
  LOYALTY_MANAGEMENT("단골 관리"),

  @Schema(description = "경쟁력 확보")
  COMPETITIVE_EDGE("경쟁력 확보"),

  @Schema(description = "수익 개선")
  PROFIT_IMPROVEMENT("수익 개선"),

  @Schema(description = "상품 기획")
  PRODUCT_PLANNING("상품 기획");

  private final String korean;

  PainPoint(String korean) {
    this.korean = korean;
  }
}
