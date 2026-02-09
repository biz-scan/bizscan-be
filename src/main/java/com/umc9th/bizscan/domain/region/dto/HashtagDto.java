package com.umc9th.bizscan.domain.region.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HashtagDto {

  @com.fasterxml.jackson.annotation.JsonValue private String hashtag;
  private Long searchVolume;
}
