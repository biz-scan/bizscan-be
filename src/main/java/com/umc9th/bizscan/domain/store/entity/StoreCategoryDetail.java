package com.umc9th.bizscan.domain.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 업종(소분류)")
public enum StoreCategoryDetail {

  // CAFE_BAKERY
  @Schema(description = "일반 카페")
  GENERAL_CAFE,

  @Schema(description = "테이크아웃 전문")
  TAKEOUT_ONLY,

  @Schema(description = "베이커리/디저트")
  BAKERY_DESSERT,

  // RESTAURANT
  @Schema(description = "한식")
  KOREAN,

  @Schema(description = "구이")
  GRILL,

  @Schema(description = "양식/브런치")
  WESTERN_BRUNCH,

  @Schema(description = "아시안")
  ASIAN,

  @Schema(description = "패스트푸드")
  FAST_FOOD,

  // BAR
  @Schema(description = "포차")
  POCHA,

  @Schema(description = "이자카야")
  IZAKAYA,

  @Schema(description = "비어홀")
  BEER_HALL,

  @Schema(description = "와인바")
  WINE_BAR
}
