package com.umc9th.bizscan.domain.aiVector.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponseDto {

  // 1. 순위 (예: 1)
  private Integer rank;

  // 2. 제목 (예: "성수동 베이커리 카페")
  private String storeTitle;

  // 3. AI 분석 유사도 (예: 98 -> "98%"로 프론트에서 처리)
  private Integer similarityPercent;

  // 4. 매칭 해시태그 3개 (예: ["#업종유사", "#타겟유사", "#고민유사"])
  private List<String> hashTags;

  // 5. 캐치프레이즈 (예: "20대 여성이 줄 서는 힙한 카페")
  private String catchphrase;

  // 6. 실행 전략 한 줄 (예: "인스타그램 릴스 챌린지")
  private String actionPlanSummary;

  // 7. 상세페이지 이동용
  private Long storeId;
}
