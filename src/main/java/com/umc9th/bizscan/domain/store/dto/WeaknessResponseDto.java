package com.umc9th.bizscan.domain.store.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeaknessResponseDto {
  private String storeName;
  private int myReviewCount;
  private double myRating;
  private double avgCompReviewCount; // 경쟁사 평균 리뷰 수
}
