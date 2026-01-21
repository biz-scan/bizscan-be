package com.umc9th.bizscan.domain.region.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class NaverKeywordResponse {

  private List<KeywordResult> keywordList;

  @Getter
  @NoArgsConstructor
  @ToString
  public static class KeywordResult {
    @JsonProperty("relKeyword")
    private String relKeyword; // 연관 키워드

    @JsonProperty("monthlyPcQcCnt")
    private String monthlyPcQcCnt; // 월간 PC 검색수 (String인 이유: "< 10" 같은 값 때문)

    @JsonProperty("monthlyMobileQcCnt")
    private String monthlyMobileQcCnt; // 월간 모바일 검색수
  }
}
