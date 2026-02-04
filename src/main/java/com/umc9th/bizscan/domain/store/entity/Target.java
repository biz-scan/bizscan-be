package com.umc9th.bizscan.domain.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "타겟 고객")
public enum Target {
  @Schema(description = "20대")
  TWENTIES("20대"),

  @Schema(description = "30~40대")
  THIRTIES_FORTIES("30~40대"),

  @Schema(description = "가족 단위")
  FAMILY("가족 단위"),

  @Schema(description = "지역 주민")
  LOCAL_RESIDENT("지역 주민"),

  @Schema(description = "관광객")
  TOURIST("관광객");

  private final String korean;

  Target(String korean) {
    this.korean = korean;
  }
}
