package com.umc9th.bizscan.domain.competitor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class SemasApiResponse {
  private Body body;

  @Data
  public static class Body {
    private List<Item> items;
    private int totalCount;
  }

  @Data
  public static class Item {
    @JsonProperty("bizesId")
    private String bizesId; // 상가업소번호

    @JsonProperty("bizesNm")
    private String bizesNm; // 상호명

    @JsonProperty("brchNm")
    private String brchNm; // 지점명

    @JsonProperty("indsLclsNm")
    private String indsLclsNm; // 대분류 (음식)

    @JsonProperty("indsMclsNm")
    private String indsMclsNm; // 중분류 (한식)

    @JsonProperty("indsSclsNm")
    private String indsSclsNm; // 소분류 (백반/한정식)

    @JsonProperty("rdnmAdr")
    private String rdnmAdr; // 도로명주소

    @JsonProperty("lon")
    private Double lon; // 경도

    @JsonProperty("lat")
    private Double lat; // 위도
  }
}
