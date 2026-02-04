package com.umc9th.bizscan.domain.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Schema(description = "가게 태그 코드(최대 3개 선택)")
public enum TagCode {

  // =====================
  // MOOD
  // =====================
  @Schema(description = "분위기: 뷰맛집")
  MOOD_VIEW(Type.MOOD, Name.VIEW),

  @Schema(description = "분위기: 힙한")
  MOOD_HIP(Type.MOOD, Name.HIP),

  @Schema(description = "분위기: 조용한")
  MOOD_QUIET(Type.MOOD, Name.QUIET),

  @Schema(description = "분위기: 레트로")
  MOOD_RETRO(Type.MOOD, Name.RETRO),

  @Schema(description = "분위기: 고급진")
  MOOD_LUXURY(Type.MOOD, Name.LUXURY),

  @Schema(description = "분위기: 활기찬")
  MOOD_LIVELY(Type.MOOD, Name.LIVELY),

  // =====================
  // FEATURE
  // =====================
  @Schema(description = "특징: 가성비")
  FEATURE_GOOD_VALUE(Type.FEATURE, Name.GOOD_VALUE),

  @Schema(description = "특징: 혼밥 환영")
  FEATURE_SOLO_FRIENDLY(Type.FEATURE, Name.SOLO_FRIENDLY),

  @Schema(description = "특징: 단체석")
  FEATURE_GROUP_SEAT(Type.FEATURE, Name.GROUP_SEAT),

  @Schema(description = "특징: 반려동물")
  FEATURE_PET_FRIENDLY(Type.FEATURE, Name.PET_FRIENDLY),

  @Schema(description = "특징: 사진 스팟")
  FEATURE_PHOTO_SPOT(Type.FEATURE, Name.PHOTO_SPOT),

  // =====================
  // OPERATION
  // =====================
  @Schema(description = "운영: 홀 영업")
  OPERATION_HALL_SERVICE(Type.OPERATION, Name.HALL_SERVICE),

  @Schema(description = "운영: 배달 가능")
  OPERATION_DELIVERY_AVAILABLE(Type.OPERATION, Name.DELIVERY_AVAILABLE),

  @Schema(description = "운영: 포장 전문")
  OPERATION_TAKEOUT_ONLY(Type.OPERATION, Name.TAKEOUT_ONLY);

  private final Type type;
  private final Name name;

  TagCode(Type type, Name name) {
    this.type = type;
    this.name = name;
  }

  @Getter
  @AllArgsConstructor
  public enum Type {
    MOOD("분위기"),
    FEATURE("특징"),
    OPERATION("운영");

    private final String korean;
  }

  @Getter
  @AllArgsConstructor
  public enum Name {
    // mood
    VIEW("#뷰맛집"),
    HIP("#힙한"),
    QUIET("#조용한"),
    RETRO("#레트로"),
    LUXURY("#고급진"),
    LIVELY("#활기찬"),
    // feature
    GOOD_VALUE("#가성비"),
    SOLO_FRIENDLY("#혼밥환영"),
    GROUP_SEAT("#단체석"),
    PET_FRIENDLY("#반려동물"),
    PHOTO_SPOT("#사진스팟"),
    // operation
    HALL_SERVICE("#홀영업"),
    DELIVERY_AVAILABLE("#배달가능"),
    TAKEOUT_ONLY("#포장전문");

    private final String korean;
  }
}
