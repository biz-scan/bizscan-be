package com.umc9th.bizscan.domain.aiAnalysis.dto.response;

import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionDetail;
import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlan;
import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlanTag;
import com.umc9th.bizscan.domain.aiAnalysis.entity.Swot;
import com.umc9th.bizscan.domain.aiAnalysis.enums.SwotType;
import com.umc9th.bizscan.domain.aiAnalysis.enums.TagType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

public class AnalysisResDTO {
  @Builder
  public record SwotDTO(
      @Schema(description = "SWOT 아이템 ID", example = "10") Long swotId,
      @Schema(description = "SWOT 타입 (S, W, O, T)", example = "S") SwotType type,
      @Schema(description = "SWOT 키워드", example = "높은 재방문율") String keyword,
      @Schema(description = "상세 설명", example = "단골 고객 비중이 타 매장 대비 20% 높습니다.") String description) {
    public static SwotDTO of(Swot swot) {
      return SwotDTO.builder()
          .swotId(swot.getId())
          .type(swot.getType())
          .keyword(swot.getKeyword())
          .description(swot.getDescription())
          .build();
    }
  }

  @Builder
  public record TagDTO(
      @Schema(description = "태그 ID", example = "1") Long tagId,
      @Schema(description = "태그 타입", example = "GOAL") TagType tagType,
      @Schema(description = "태그 내용", example = "#객단가 UP") String content) {
    public static TagDTO of(ActionPlanTag actionPlanTag) {
      return TagDTO.builder()
          .tagId(actionPlanTag.getId())
          .tagType(actionPlanTag.getType())
          .content(actionPlanTag.getContent())
          .build();
    }
  }

  @Builder
  public record ActionPlanDTO(
      @Schema(description = "실행 전략 ID", example = "1") Long actionPlanId,
      @Schema(description = "실행 전략 제목", example = "오후 5시 '직장인 퇴근길' 예약 프로모션") String title,
      @Schema(description = "관련 태그 리스트") List<TagDTO> tags) {
    public static ActionPlanDTO of(ActionPlan actionPlan) {
      return ActionPlanDTO.builder()
          .actionPlanId(actionPlan.getId())
          .title(actionPlan.getTitle())
          .tags(actionPlan.getTags().stream().map(TagDTO::of).toList())
          .build();
    }
  }

  @Builder
  public record ActionDetailDTO(
      @Schema(description = "상세 실행 전략 ID", example = "10") Long actionDetailId,
      @Schema(description = "단계", example = "1") Integer step,
      @Schema(description = "제목", example = "리뷰 이벤트 안내물 및 디지털 할인 쿠폰 제작") String title,
      @Schema(
              description = "설명",
              example =
                  "테이블마다 네이버 플레이스/SNS 리뷰 참여 방법을 담은 스탠드를 비치하고, 재방문 시 사용 가능한 '저녁 전용 20% 할인권'을 디자인합니다.")
          String description,
      @Schema(description = "기대 효과", example = "고객이 리뷰 이벤트에 참여할 수 있는 시각적 창구와 보상 체계가 마련됩니다.")
          String expectedOutcome) {
    public static ActionDetailDTO of(ActionDetail actionDetail) {
      return ActionDetailDTO.builder()
          .actionDetailId(actionDetail.getId())
          .step(actionDetail.getStep())
          .title(actionDetail.getTitle())
          .description(actionDetail.getDescription())
          .expectedOutcome(actionDetail.getExpectedOutcome())
          .build();
    }
  }

  @Builder
  public record ActionPlanDetailDTO(
      @Schema(description = "실행 전략 ID", example = "1") Long actionPlanId,
      @Schema(description = "실행 전략 제목", example = "리뷰 작성 시 '저녁 시간 전용' 할인권 증정")
          String actionPlanTitle,
      @Schema(description = "관련 태그 리스트") List<TagDTO> tags,
      @Schema(
              description = "작성 사유",
              example =
                  "우수한 가성비라는 확실한 강점이 있음에도 불구하고, 현재 온라인 리뷰 수가 경쟁사의 20% 수준(W)이라는 점은 잠재 고객을 이탈시키는 가장 큰 병목 구간입니다. \\n저녁 시간대 방문객(O)들에게 실질적인 혜택인 할인권을 제공하여 자발적인 리뷰 작성을 유도함으로써, 디지털 신뢰도의 핵심인 '사회적 증거'를 단기간에 확보해야 합니다. \\n이 전략은 가장 적은 비용(Low Effort)으로 온라인 검색 시의 전환율을 획기적으로 개선하여 장기적인 매장 인지도를 탄탄하게 구축해 줄 것입니다.")
          String reason,
      @Schema(description = "상세 실행 전략 리스트") List<ActionDetailDTO> actionDetails) {
    public static ActionPlanDetailDTO of(
        ActionPlan actionPlan, List<ActionDetailDTO> sortedActionDetails) {
      return ActionPlanDetailDTO.builder()
          .actionPlanId(actionPlan.getId())
          .actionPlanTitle(actionPlan.getTitle())
          .tags(actionPlan.getTags().stream().map(TagDTO::of).toList())
          .reason(actionPlan.getReason())
          .actionDetails(sortedActionDetails)
          .build();
    }
  }
}
