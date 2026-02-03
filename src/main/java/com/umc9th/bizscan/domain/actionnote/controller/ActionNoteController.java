package com.umc9th.bizscan.domain.actionnote.controller;

import com.umc9th.bizscan.domain.actionnote.dto.req.ActionNoteReqDTO;
import com.umc9th.bizscan.domain.actionnote.dto.res.ActionNoteResDTO;
import com.umc9th.bizscan.domain.actionnote.service.ActionNoteService;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.code.SuccessCode;
import com.umc9th.bizscan.global.config.swagger.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Action Note", description = "실행 노트 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/action-notes")
public class ActionNoteController {
  private final ActionNoteService actionNoteService;

  @Operation(summary = "실행 노트 등록 API", description = "특정 실행 전략(ActionPlan)을 바탕으로 실행 노트에 등록합니다.")
  @ApiErrorCodeExamples({ErrorCode.ACTION_PLAN_NOT_FOUND, ErrorCode.ACTION_NOTE_ALREADY_EXISTS})
  @PostMapping()
  public ApiResponse<ActionNoteResDTO.AddDTO> addActionNote(
      @RequestBody ActionNoteReqDTO.AddDTO addDTO) {
    return ApiResponse.onSuccess(SuccessCode.OK, actionNoteService.addActionNote(addDTO));
  }

  @Operation(
      summary = "실행 노트 삭제 API",
      description = "특정 실행 전략(ActionPlan)을 연관된 실행 노트(ActionNote)에서 삭제합니다.")
  @ApiErrorCodeExamples({ErrorCode.ACTION_NOTE_NOT_FOUND})
  @DeleteMapping("/{actionPlanId}")
  public ApiResponse<ActionNoteResDTO.DeleteDTO> deleteActionNote(
      @Parameter(description = "실행 전략 ID", example = "1") @PathVariable Long actionPlanId) {
    return ApiResponse.onSuccess(SuccessCode.OK, actionNoteService.deleteActionNote(actionPlanId));
  }

  @Operation(
      summary = "실행 노트 목록 조회 API",
      description = "특정 매장(Store)의 실행 노트 목록을 조회합니다. 완료 여부(isCompleted)에 따라 필터링이 가능합니다.")
  @GetMapping()
  public ApiResponse<List<ActionNoteResDTO.ActionNotesDTO>> getActionNotes(
      @Parameter(description = "매장 ID", example = "1") @RequestParam Long storeId,
      @Parameter(description = "완료 여부 (true: 완료된 노트, false: 진행 중인 노트)", example = "false")
          @RequestParam
          Boolean isCompleted) {
    return ApiResponse.onSuccess(
        SuccessCode.OK, actionNoteService.getActionNotes(storeId, isCompleted));
  }

  @Operation(
      summary = "실행 노트 상세 조회 API",
      description =
          "특정 실행 전략(ActionPlan)의 내용과 포함된 상세 실행 전략(ActionDetail) 리스트, 진행도를 조회합니다. "
              + "포함된 상세 실행 전략 리스트는 단계(step) 순으로 정렬되어 반환됩니다.")
  @ApiErrorCodeExamples({ErrorCode.ACTION_PLAN_NOT_FOUND})
  @GetMapping("/{actionPlanId}")
  public ApiResponse<ActionNoteResDTO.ActionNoteDTO> getActionNote(
      @Parameter(description = "조회할 실행 전략 ID", example = "1") @PathVariable Long actionPlanId) {
    return ApiResponse.onSuccess(SuccessCode.OK, actionNoteService.getActionNote(actionPlanId));
  }

  @Operation(
      summary = "상세 미션 완료 상태 수정 API",
      description = "특정 상세 미션의 완료 여부를 수정합니다. 수정 시 해당 전략의 전체 진행도와 노트의 완료 여부가 자동 갱신됩니다.")
  @ApiErrorCodeExamples({ErrorCode.ACTION_DETAIL_NOT_FOUND})
  @PatchMapping("/{actionDetailId}")
  public ApiResponse<ActionNoteResDTO.UpdateActionDetailDTO> updateActionDetail(
      @Parameter(description = "상세 미션 ID", example = "10") @PathVariable Long actionDetailId,
      @Parameter(description = "완료 여부", example = "true") @RequestParam Boolean isCompleted) {
    return ApiResponse.onSuccess(
        SuccessCode.OK, actionNoteService.updateActionDetail(actionDetailId, isCompleted));
  }
}
