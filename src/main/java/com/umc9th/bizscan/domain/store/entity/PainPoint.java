package com.umc9th.bizscan.domain.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 고민")
public enum PainPoint {
  @Schema(description = "고객 유입")
  CUSTOMER_ACQUISITION,

  @Schema(description = "단골 관리")
  LOYALTY_MANAGEMENT,

  @Schema(description = "경쟁력 확보")
  COMPETITIVE_EDGE,

  @Schema(description = "수익 개선")
  PROFIT_IMPROVEMENT,

  @Schema(description = "상품 기획")
  PRODUCT_PLANNING
}
