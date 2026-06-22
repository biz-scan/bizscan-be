package com.umc9th.bizscan.domain.analysis.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class AnalysisSummaryDto {
  // [O: 기회] 유동인구 & 매출
  private String mainAgeGroup; // 주 이용 연령대
  private String mainGender; // 주 이용 성별
  private String peakTime; // 피크 시간대
  private Long avgDailyPop; // 일 평균 유동인구

  // [T: 위협] 경쟁 정보 개수
  private int competitorCount; // 경쟁 점포 수
  private String competitionLevel; // 경쟁 강도 (HIGH/MID/LOW)

  // [S: 강점] 배후지 소득
  private Long avgMonthIncome; // 월 평균 소득

  // [W: 약점] 리뷰 및 평점
  private int myReviewCount; // 내 가게 리뷰 수
  private double avgCompReviewCount; // 경쟁사 평균 리뷰 수
  private double myRating; // 내 별점

  // 주거 형태
  private String mainHousingType; // 주된 주거 형태

  // 트렌드 (SNS)
  private String topHashtags; // 인기 해시태그 모음
  private String evidenceText;
}
