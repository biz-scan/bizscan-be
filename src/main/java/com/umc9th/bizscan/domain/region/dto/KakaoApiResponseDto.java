package com.umc9th.bizscan.domain.region.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class KakaoApiResponseDto {
  private List<Document> documents;

  @Data
  public static class Document {
    private Address address;

    @Data
    public static class Address {
      @JsonProperty("address_name")
      private String addressName;

      @JsonProperty("h_code")
      private String hCode; // 행정동 코드 (핵심)

      @JsonProperty("region_3depth_h_name")
      private String region3DepthHName; // 행정동 이름 (예: 성수1가1동)

      private String x; // 경도 (Longitude)
      private String y; // 위도 (Latitude)
    }
  }
}
