package com.umc9th.bizscan.domain.actionnote.dto.res;

import com.umc9th.bizscan.domain.actionnote.entity.ActionNote;
import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionDetail;
import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlan;
import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlanTag;
import com.umc9th.bizscan.domain.aiAnalysis.enums.TagType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

public class ActionNoteResDTO {
  @Builder
  @Schema(description = "실행 노트 등록 응답 정보")
  public record AddDTO(
      @Schema(description = "생성된 실행 노트의 고유 ID", example = "10") Long actionNoteId,
      @Schema(description = "실행 노트 등록 일시", example = "2024-05-20T14:30:00")
          LocalDateTime createdAt) {
    public static AddDTO of(ActionNote actionNote) {
      return AddDTO.builder()
          .actionNoteId(actionNote.getId())
          .createdAt(actionNote.getCreatedAt())
          .build();
    }
  }

  @Builder
  public record DeleteDTO(@Schema(description = "삭제된 실행 노트 ID", example = "5") Long actionNoteId) {}

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
  public record ActionNotesDTO(
      @Schema(description = "실행 전략 ID", example = "1") Long actionPlanId,
      @Schema(description = "완료 여부", example = "false") Boolean isCompleted,
      @Schema(description = "생성 일자", example = "2024-02-04T12:00:00") LocalDateTime createdAt,
      @Schema(description = "실행 전략 제목", example = "오후 5시 '직장인 퇴근길' 예약 프로모션") String title,
      @Schema(description = "진행도 (%)", example = "34") Integer progress,
      @Schema(description = "다음 할 일 제목", example = "가게 앞 입간판 문구 수정하기") String nextActionDetailTitle,
      @Schema(description = "관련 태그 리스트") List<TagDTO> tags) {
    public static ActionNotesDTO of(
        ActionPlan actionPlan, Integer progress, String nextActionDetailTitle) {
      return ActionNotesDTO.builder()
          .actionPlanId(actionPlan.getId())
          .isCompleted(actionPlan.getActionNote().getIsCompleted())
          .createdAt(actionPlan.getActionNote().getCreatedAt())
          .title(actionPlan.getTitle())
          .progress(progress)
          .nextActionDetailTitle(nextActionDetailTitle)
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
          String expectedOutcome,
      @Schema(description = "완료 여부", example = "true") Boolean isCompleted) {
    public static ActionDetailDTO of(ActionDetail actionDetail) {
      return ActionDetailDTO.builder()
          .actionDetailId(actionDetail.getId())
          .step(actionDetail.getStep())
          .title(actionDetail.getTitle())
          .description(actionDetail.getDescription())
          .expectedOutcome(actionDetail.getExpectedOutcome())
          .isCompleted(actionDetail.getIsCompleted())
          .build();
    }
  }

  @Builder
  public record ActionNoteDTO(
      @Schema(description = "실행 전략 ID", example = "1") Long actionPlanId,
      @Schema(description = "실행 전략 제목", example = "리뷰 작성 시 '저녁 시간 전용' 할인권 증정")
          String actionPlanTitle,
      @Schema(description = "관련 태그 리스트") List<TagDTO> tags,
      @Schema(
              description = "작성 사유",
              example =
                  "우수한 가성비라는 확실한 강점이 있음에도 불구하고, 현재 온라인 리뷰 수가 경쟁사의 20% 수준(W)이라는 점은 잠재 고객을 이탈시키는 가장 큰 병목 구간입니다. \\n저녁 시간대 방문객(O)들에게 실질적인 혜택인 할인권을 제공하여 자발적인 리뷰 작성을 유도함으로써, 디지털 신뢰도의 핵심인 '사회적 증거'를 단기간에 확보해야 합니다. \\n이 전략은 가장 적은 비용(Low Effort)으로 온라인 검색 시의 전환율을 획기적으로 개선하여 장기적인 매장 인지도를 탄탄하게 구축해 줄 것입니다.")
          String reason,
      @Schema(description = "전체 진행도 (%)", example = "34") Integer progress,
      @Schema(description = "상세 실행 전략 리스트") List<ActionDetailDTO> actionDetails) {
    public static ActionNoteDTO of(
        ActionPlan actionPlan, Integer progress, List<ActionDetailDTO> sortedActionDetails) {
      return ActionNoteDTO.builder()
          .actionPlanId(actionPlan.getId())
          .actionPlanTitle(actionPlan.getTitle())
          .tags(actionPlan.getTags().stream().map(TagDTO::of).toList())
          .reason(actionPlan.getReason())
          .progress(progress)
          .actionDetails(sortedActionDetails)
          .build();
    }
  }

  @Builder
  public record UpdateActionDetailDTO(
      @Schema(description = "수정된 상세 실행 전략 ID", example = "10") Long actionDetailId,
      @Schema(description = "해당 상세 실행 전략의 완료 여부", example = "true") Boolean isDetailCompleted,
      @Schema(description = "업데이트된 전체 진행도 (%)", example = "67") Integer progress,
      @Schema(description = "실행 노트 전체 완료 여부 (모든 미션 완료 시 true)", example = "false")
          Boolean isNoteCompleted) {
    public static UpdateActionDetailDTO of(
        ActionDetail detail, Integer progress, Boolean isNoteCompleted) {
      return UpdateActionDetailDTO.builder()
          .actionDetailId(detail.getId())
          .isDetailCompleted(detail.getIsCompleted())
          .progress(progress)
          .isNoteCompleted(isNoteCompleted)
          .build();
    }
  }
}
