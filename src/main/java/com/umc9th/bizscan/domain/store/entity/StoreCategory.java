package com.umc9th.bizscan.domain.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "가게 업종(대분류)")
public enum StoreCategory {
  @Schema(description = "카페/베이커리")
  CAFE_BAKERY("카페/베이커리"),

  @Schema(description = "식당")
  RESTAURANT("식당"),

  @Schema(description = "주점")
  BAR("주점");

  private final String korean;

  StoreCategory(String korean) {
    this.korean = korean;
  }
}
