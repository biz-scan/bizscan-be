package com.umc9th.bizscan.domain.region.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HashtagDto {

  @com.fasterxml.jackson.annotation.JsonValue private String hashtag; // 예: #성수동크로플
  private Long searchVolume; // 검색량 (높은 순 정렬용)
}
