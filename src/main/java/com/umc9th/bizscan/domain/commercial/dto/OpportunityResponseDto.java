package com.umc9th.bizscan.domain.commercial.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpportunityResponseDto {
  private String address; // 분석 주소
  private long avgDailyPop; // 일 평균 유동인구
  private String mainAgeGroup; // 주 이용 연령대
  private String mainGender; // 주요 이용 성별
  private String peakTime; // 가장 붐비는 시간대
}
