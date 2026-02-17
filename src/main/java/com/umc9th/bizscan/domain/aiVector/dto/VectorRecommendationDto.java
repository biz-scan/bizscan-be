package com.umc9th.bizscan.domain.aiVector.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class VectorRecommendationDto {

  @JsonAlias({"store_id", "id", "storeId"})
  private Long storeId;

  @JsonAlias({"catchphrase", "catch_phrase"})
  private String catchphrase;

  @JsonProperty("score")
  private double score;

  @JsonProperty("raw_text")
  private String rawText;
}
