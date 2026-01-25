package com.umc9th.bizscan.domain.store.dto.request;

import com.umc9th.bizscan.domain.store.entity.PainPoint;
import com.umc9th.bizscan.domain.store.entity.PriceRange;
import com.umc9th.bizscan.domain.store.entity.StoreCategory;
import com.umc9th.bizscan.domain.store.entity.StoreCategoryDetail;
import com.umc9th.bizscan.domain.store.entity.TagCode;
import com.umc9th.bizscan.domain.store.entity.Target;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "StoreRequest: 가게 생성 요청 DTO")
public class StoreRequest {

  @NotNull
  @Schema(description = "가게 등록 사용자 ID", example = "1")
  private Long memberId;

  @NotBlank
  @Size(max = 100)
  @Schema(description = "매장명", example = "문화제빵")
  private String name;

  @NotBlank
  @Size(max = 255)
  @Schema(description = "주소", example = "서울 종로구 돈화문로 65 1층")
  private String address;

  @NotNull
  @Schema(description = "업종", example = "CAFE_BAKERY")
  private StoreCategory category;

  @NotNull
  @Schema(description = "업종 소분류", example = "BAKERY_DESSERT")
  private StoreCategoryDetail categoryDetail;

  @NotBlank
  @Size(max = 255)
  @Schema(description = "대표 메뉴(필수)", example = "마늘빵")
  private String signature;

  @NotNull
  @Schema(description = "1인당 평균 결제 금액", example = "UNDER_10000")
  private PriceRange price;

  @NotNull
  @Schema(description = "주 타겟", example = "LOCAL_RESIDENT")
  private Target target;

  @NotNull
  @Schema(description = "사장님 고민", example = "CUSTOMER_ACQUISITION")
  private PainPoint painPoint;

  @NotNull
  @Size(min = 1, max = 3)
  @Schema(
      description = "태그(필수, 1개 이상, 최대 3개)",
      example = "[\"MOOD_VIEW\", \"FEATURE_GOOD_VALUE\", \"OPERATION_TAKEOUT_ONLY\"]")
  private List<TagCode> tags;
}
