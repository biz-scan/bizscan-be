package com.umc9th.bizscan.domain.hinterland.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StrengthResponseDto {
  private String address; // 정제된 주소
  private String dongName; // 행정동 명
  private long avgMonthIncome; // 월 평균 소득 (IncomeStat에서 추출)
  private int myAvgPrice; // 사용자 입력 객단가
  private double burdenRatio; // 소득 대비 지출 부담률
  private String priceCompetitiveness; // 가격 경쟁력 결과 키워드
}
