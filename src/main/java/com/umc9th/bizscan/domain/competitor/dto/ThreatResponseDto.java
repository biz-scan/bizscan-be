package com.umc9th.bizscan.domain.competitor.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreatResponseDto {
  private String address; // 분석 주소
  private String bizType; // 분석 대상 업종 (대분류)
  private String subCategory; // 상세 업종 (소분류)
  private int competitorCount; // 주변 경쟁 업체 수
  private String competitionStatus; // 경쟁 상태 (과포화, 치열 등)
}
