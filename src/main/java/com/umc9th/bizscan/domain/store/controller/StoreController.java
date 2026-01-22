package com.umc9th.bizscan.domain.store.controller;

import com.umc9th.bizscan.domain.store.dto.request.StoreRequest;
import com.umc9th.bizscan.domain.store.dto.response.StoreDeleteResponse;
import com.umc9th.bizscan.domain.store.dto.response.StoreResponse;
import com.umc9th.bizscan.domain.store.entity.PainPoint;
import com.umc9th.bizscan.domain.store.entity.PriceRange;
import com.umc9th.bizscan.domain.store.entity.StoreCategory;
import com.umc9th.bizscan.domain.store.entity.StoreCategoryDetail;
import com.umc9th.bizscan.domain.store.entity.TagCode;
import com.umc9th.bizscan.domain.store.entity.Target;
import com.umc9th.bizscan.domain.store.service.StoreService;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {

  private final StoreService storeService;

  @Operation(
      summary = "가게 등록",
      description =
          """
              사용자가 매장을 등록합니다.
              
              입력 항목(필수):
              - 매장명, 주소, 업종, 업종 소분류, 대표 메뉴
              - 1인당 평균 결제 금액, 주 타겟, 사장님 고민
              - 태그(1개 이상, 최대 3개)
              
              참고:
              - enum은 영문 코드로 선택되며, Description에 한글 설명이 표시됩니다.
              - 위도/경도는 주소 기반으로 서버에서 자동 변환됩니다. (Kakao Geocoding)
              """)
  @PostMapping
  public ResponseEntity<ApiResponse<StoreResponse>> createStore(
      @Parameter(description = "가게 등록 사용자 ID", example = "1", required = true)
      @RequestParam Long memberId,

      @Parameter(description = "매장명", example = "문화제빵", required = true)
      @RequestParam String name,

      @Parameter(description = "주소", example = "서울 종로구 돈화문로 65 1층", required = true)
      @RequestParam String address,

      @Parameter(description = "업종 (카페/베이커리, 식당, 술집/주점)", example = "CAFE_BAKERY", required = true)
      @RequestParam StoreCategory category,

      @Parameter(
          description =
              """
                  업종 소분류
                  - 카페/베이커리: GENERAL_CAFE(일반 카페), TAKEOUT_ONLY(테이크아웃 전문), BAKERY_DESSERT(베이커리/디저트)
                  - 식당: KOREAN(한식/백반/국밥), GRILL(고기/구이), WESTERN_BRUNCH(양식/브런치), ASIAN(일식/중식/아시안), FAST_FOOD(분식/패스트푸드)
                  - 주점: POCHA(요리주점/포차), IZAKAYA(이자카야/꼬치), BEER_HALL(호프/맥주), WINE_BAR(와인/바/칵테일)
                  """,
          example = "BAKERY_DESSERT",
          required = true)
      @RequestParam StoreCategoryDetail categoryDetail,

      @Parameter(description = "대표 메뉴(필수)", example = "마늘빵", required = true)
      @RequestParam String signature,

      @Parameter(
          description = "1인당 평균 결제 금액 (UNDER_10000=1만원 미만 / FROM_10000_TO_20000=1~2만원대 / FROM_20000_TO_40000=2~4만원대 / OVER_40000=4만원 이상)",
          example = "UNDER_10000",
          required = true)
      @RequestParam PriceRange price,

      @Parameter(
          description = "주 타겟 (TWENTIES=20대 / THIRTIES_FORTIES=30~40대 직장인 / FAMILY=가족 단위 / LOCAL_RESIDENT=동네 주민 / TOURIST=관광객)",
          example = "LOCAL_RESIDENT",
          required = true)
      @RequestParam Target target,

      @Parameter(
          description = "사장님 고민 (CUSTOMER_ACQUISITION=모객 / LOYALTY_MANAGEMENT=단골 관리 / COMPETITIVE_EDGE=경쟁 우위 / PROFIT_IMPROVEMENT=수익성 개선 / PRODUCT_PLANNING=상품 기획)",
          example = "CUSTOMER_ACQUISITION",
          required = true)
      @RequestParam PainPoint painPoint,

      @Parameter(
          description =
              """
                  태그(필수, 1~3개)
                  - 분위기: MOOD_VIEW(뷰맛집), MOOD_HIP(힙한), MOOD_QUIET(조용한), MOOD_RETRO(레트로), MOOD_LUXURY(고급진), MOOD_LIVELY(활기찬)
                  - 특징: FEATURE_GOOD_VALUE(가성비), FEATURE_SOLO_FRIENDLY(혼밥환영), FEATURE_GROUP_SEAT(단체석), FEATURE_PET_FRIENDLY(반려동물), FEATURE_PHOTO_SPOT(사진맛집)
                  - 운영: OPERATION_HALL_SERVICE(홀영업), OPERATION_DELIVERY_AVAILABLE(배달가능), OPERATION_TAKEOUT_ONLY(포장전문)
                  
                  전달 예시:
                  tags=MOOD_VIEW&tags=FEATURE_GOOD_VALUE&tags=OPERATION_TAKEOUT_ONLY
                  """,
          required = true)
      @RequestParam List<TagCode> tags
  ) {

    StoreRequest request =
        StoreRequest.builder()
            .memberId(memberId)
            .name(name)
            .address(address)
            .category(category)
            .categoryDetail(categoryDetail)
            .signature(signature)
            .price(price)
            .target(target)
            .painPoint(painPoint)
            .tags(tags)
            .build();

    StoreResponse result = storeService.createStore(request);

    return ResponseEntity.status(SuccessCode.OK.getStatus())
        .body(ApiResponse.onSuccess(SuccessCode.OK, result));
  }

  @Operation(summary = "가게 전체 조회", description = "등록된 모든 가게를 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<List<StoreResponse>>> getStores() {

    return ResponseEntity.ok(
        ApiResponse.onSuccess(
            SuccessCode.OK,
            storeService.getStores()
        )
    );
  }

  @Operation(summary = "가게 단건 조회", description = "가게 ID로 단일 가게 정보를 조회합니다.")
  @GetMapping("/{storeId}")
  public ResponseEntity<ApiResponse<StoreResponse>> getStore(
      @Parameter(description = "가게 ID", example = "1", required = true)
      @PathVariable Long storeId
  ) {

    return ResponseEntity.ok(
        ApiResponse.onSuccess(
            SuccessCode.OK,
            storeService.getStore(storeId)
        )
    );
  }

  @Operation(summary = "가게 삭제", description = "가게를 삭제합니다. (연결된 태그 매핑도 함께 삭제)")
  @DeleteMapping("/{storeId}")
  public ResponseEntity<ApiResponse<StoreDeleteResponse>> deleteStore(@PathVariable Long storeId) {
    StoreDeleteResponse result = storeService.deleteStore(storeId);

    return ResponseEntity.status(SuccessCode.OK.getStatus())
        .body(ApiResponse.onSuccess(SuccessCode.OK, result));
  }
}