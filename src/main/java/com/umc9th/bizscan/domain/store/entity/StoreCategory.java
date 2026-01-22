package com.umc9th.bizscan.domain.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 업종(대분류)")
public enum StoreCategory {
  @Schema(description = "카페/베이커리")
  CAFE_BAKERY,

  @Schema(description = "식당")
  RESTAURANT,

  @Schema(description = "주점")
  BAR
}
