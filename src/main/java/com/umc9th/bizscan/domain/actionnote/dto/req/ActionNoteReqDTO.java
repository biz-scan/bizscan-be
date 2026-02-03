package com.umc9th.bizscan.domain.actionnote.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

public class ActionNoteReqDTO {
  @Schema(description = "실행 노트 등록 요청 정보")
  public record AddDTO(
      @Schema(description = "연관된 실행 계획(ActionPlan)의 ID", example = "1") Long actionPlanId) {}
}
