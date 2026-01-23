package com.umc9th.bizscan.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(title = "StoreResponse: 가게 생성/조회 응답 DTO")
public class StoreResponse {

  @Schema(description = "가게 ID", example = "1")
  private Long storeId;

  @Schema(description = "매장명", example = "문화제빵")
  private String name;

  @Schema(description = "주소", example = "서울 종로구 돈화문로 65 1층")
  private String address;

  @Schema(description = "위도", example = "37.5723")
  private BigDecimal lat;

  @Schema(description = "경도", example = "126.9876")
  private BigDecimal lon;

  @Schema(description = "업종", example = "CAFE_BAKERY")
  private String category;

  @Schema(description = "업종 소분류", example = "BAKERY_DESSERT")
  private String categoryDetail;

  @Schema(description = "대표 메뉴", example = "마늘빵")
  private String signature;

  @Schema(description = "가격대", example = "UNDER_10000")
  private String price;

  @Schema(description = "주 타겟", example = "LOCAL_RESIDENT")
  private String target;

  @Schema(description = "사장님 고민", example = "CUSTOMER_ACQUISITION")
  private String painPoint;

  @Schema(description = "저장된 태그 목록")
  private List<TagInfo> tags;

  @Getter
  @Builder
  @AllArgsConstructor
  public static class TagInfo {

    @Schema(description = "태그 ID", example = "10")
    private Long id;

    @Schema(description = "태그 타입", example = "MOOD")
    private String type;

    @Schema(description = "태그 이름", example = "VIEW")
    private String name;
  }

  public static StoreResponse of(
      Long storeId,
      String name,
      String address,
      BigDecimal lat,
      BigDecimal lon,
      String category,
      String categoryDetail,
      String signature,
      String price,
      String target,
      String painPoint,
      List<TagInfo> tags) {

    return StoreResponse.builder()
        .storeId(storeId)
        .name(name)
        .address(address)
        .lat(lat)
        .lon(lon)
        .category(category)
        .categoryDetail(categoryDetail)
        .signature(signature)
        .price(price)
        .target(target)
        .painPoint(painPoint)
        .tags(tags)
        .build();
  }
}
