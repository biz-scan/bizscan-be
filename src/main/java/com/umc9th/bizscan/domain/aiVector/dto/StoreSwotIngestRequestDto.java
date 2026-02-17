package com.umc9th.bizscan.domain.aiVector.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreSwotIngestRequestDto {

  private Long storeId;
  private String catchphrase;
  private List<SwotItemDto> items;

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SwotItemDto {
    private String type; // S, W, O, T
    private String keyword; // 키워드
    private String description; // 상세 설명
    private String diagnosis; // AI 진단
    private String rawText; // 원문 (벡터화 대상)
  }
}
