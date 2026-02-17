package com.umc9th.bizscan.domain.aiVector.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SwotSummaryDto {
  private String strength; // 대표 강점
  private String weakness; // 대표 약점
}
