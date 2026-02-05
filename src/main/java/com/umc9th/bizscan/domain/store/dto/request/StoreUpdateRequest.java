package com.umc9th.bizscan.domain.store.dto.request;

import com.umc9th.bizscan.domain.store.entity.PainPoint;
import com.umc9th.bizscan.domain.store.entity.PriceRange;
import com.umc9th.bizscan.domain.store.entity.StoreCategory;
import com.umc9th.bizscan.domain.store.entity.StoreCategoryDetail;
import com.umc9th.bizscan.domain.store.entity.Target;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "StoreUpdateRequest: 가게 수정 요청 DTO (부분 수정)")
public class StoreUpdateRequest {

  @Schema(description = "매장명", example = "문화제빵")
  @Size(max = 100)
  private String name;

  @Schema(description = "주소", example = "서울 종로구 돈화문로 65 1층")
  @Size(max = 255)
  private String address;

  @Schema(description = "업종", example = "CAFE_BAKERY")
  private StoreCategory category;

  @Schema(description = "업종 소분류", example = "BAKERY_DESSERT")
  private StoreCategoryDetail categoryDetail;

  @Schema(description = "대표 메뉴", example = "마늘빵")
  @Size(max = 255)
  private String signature;

  @Schema(description = "1인당 평균 결제 금액", example = "UNDER_10000")
  private PriceRange price;

  @Schema(description = "주 타겟", example = "LOCAL_RESIDENT")
  private Target target;

  @Schema(description = "사장님 고민", example = "CUSTOMER_ACQUISITION")
  private PainPoint painPoint;
}
