package com.umc9th.bizscan.domain.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "가게 업종(소분류)")
public enum StoreCategoryDetail {

  // CAFE_BAKERY
  @Schema(description = "일반 카페")
  GENERAL_CAFE("일반 카페"),

  @Schema(description = "테이크아웃 전문")
  TAKEOUT_ONLY("테이크아웃 전문"),

  @Schema(description = "베이커리/디저트")
  BAKERY_DESSERT("베이커리/디저트"),

  // RESTAURANT
  @Schema(description = "한식")
  KOREAN("한식"),

  @Schema(description = "구이")
  GRILL("구이"),

  @Schema(description = "양식/브런치")
  WESTERN_BRUNCH("양식/브런치"),

  @Schema(description = "아시안")
  ASIAN("아시안"),

  @Schema(description = "패스트푸드")
  FAST_FOOD("패스트푸드"),

  // BAR
  @Schema(description = "포차")
  POCHA("포차"),

  @Schema(description = "이자카야")
  IZAKAYA("이자카야"),

  @Schema(description = "비어홀")
  BEER_HALL("비어홀"),

  @Schema(description = "와인바")
  WINE_BAR("와인바");

  private final String korean;

  StoreCategoryDetail(String korean) {
    this.korean = korean;
  }
}
