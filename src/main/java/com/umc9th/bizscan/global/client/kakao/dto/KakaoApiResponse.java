package com.umc9th.bizscan.global.client.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoApiResponse {

  private List<Document> documents;

  @Getter
  @NoArgsConstructor
  public static class Document {
    private Address address;
  }

  @Getter
  @NoArgsConstructor
  public static class Address {
    @JsonProperty("address_name")
    private String addressName;

    @JsonProperty("h_code")
    private String hCode;

    @JsonProperty("region_3depth_h_name")
    private String region3DepthHName;

    private String x; // 경도 (Longitude)
    private String y; // 위도 (Latitude)
  }
}
