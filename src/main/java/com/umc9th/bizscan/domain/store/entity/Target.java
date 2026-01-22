package com.umc9th.bizscan.domain.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "타겟 고객")
public enum Target {
  @Schema(description = "20대")
  TWENTIES,

  @Schema(description = "30~40대")
  THIRTIES_FORTIES,

  @Schema(description = "가족 단위")
  FAMILY,

  @Schema(description = "지역 주민")
  LOCAL_RESIDENT,

  @Schema(description = "관광객")
  TOURIST
}
