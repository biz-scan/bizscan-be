package com.umc9th.bizscan.domain.actionnote.service;

import com.umc9th.bizscan.domain.actionnote.converter.ActionNoteConverter;
import com.umc9th.bizscan.domain.actionnote.dto.req.ActionNoteReqDTO;
import com.umc9th.bizscan.domain.actionnote.dto.res.ActionNoteResDTO;
import com.umc9th.bizscan.domain.actionnote.entity.ActionNote;
import com.umc9th.bizscan.domain.actionnote.repository.ActionNoteRepository;
import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionDetail;
import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlan;
import com.umc9th.bizscan.domain.aiAnalysis.repository.ActionDetailRepository;
import com.umc9th.bizscan.domain.aiAnalysis.repository.ActionPlanRepository;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.exception.GeneralException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActionNoteService {
  private final ActionPlanRepository actionPlanRepository;
  private final ActionNoteRepository actionNoteRepository;
  private final ActionDetailRepository actionDetailRepository;

  @Transactional
  public ActionNoteResDTO.AddDTO addActionNote(ActionNoteReqDTO.AddDTO dto) {
    ActionPlan actionPlan =
        actionPlanRepository
            .findById(dto.actionPlanId())
            .orElseThrow(() -> new GeneralException(ErrorCode.ACTION_PLAN_NOT_FOUND));

    // 중복 체크
    if (actionNoteRepository.existsByActionPlanId(dto.actionPlanId())) {
      throw new GeneralException(ErrorCode.ACTION_NOTE_ALREADY_EXISTS);
    }

    ActionNote actionNote = ActionNoteConverter.toAddEntity(actionPlan);

    return ActionNoteResDTO.AddDTO.of(actionNoteRepository.save(actionNote));
  }

  @Transactional
  public ActionNoteResDTO.DeleteDTO deleteActionNote(Long actionPlanId) {
    ActionNote actionNote =
        actionNoteRepository
            .findByActionPlanId(actionPlanId)
            .orElseThrow(() -> new GeneralException(ErrorCode.ACTION_NOTE_NOT_FOUND));

    // 양방향 관계 해제
    ActionPlan actionPlan = actionNote.getActionPlan();
    if (actionPlan != null) {
      actionPlan.removeActionNote();
    }

    Long deletedNoteId = actionNote.getId();

    actionNoteRepository.delete(actionNote);

    return new ActionNoteResDTO.DeleteDTO(deletedNoteId);
  }

  @Transactional(readOnly = true)
  public List<ActionNoteResDTO.ActionNotesDTO> getActionNotes(Long storeId, Boolean isCompleted) {
    List<ActionPlan> actionPlans =
        actionPlanRepository.findAllByStoreIdAndCompletion(storeId, isCompleted);

    if (actionPlans.isEmpty()) {
      return Collections.emptyList();
    }

    // 모든 ActionDetail을 한 번에 조회
    List<ActionDetail> allActionDetails = actionDetailRepository.findAllByActionPlanIn(actionPlans);

    // ActionDetail들을 ActionPlan ID별로 그룹화 (Map<Long, List<ActionDetail>>)
    Map<Long, List<ActionDetail>> detailsMap =
        allActionDetails.stream().collect(Collectors.groupingBy(ad -> ad.getActionPlan().getId()));

    // ActionPlan 리스트를 DTO 리스트로 변환
    return actionPlans.stream()
        .map(
            plan -> {
              List<ActionDetail> details =
                  detailsMap.getOrDefault(plan.getId(), Collections.emptyList());

              // 진행도 계산
              int progress = calculateProgress(details);

              // 다음 할 일 타이틀 추출 (isCompleted가 false인 것 중 step이 가장 낮은 것)
              String nextActionTitle =
                  details.stream()
                      .filter(d -> !d.getIsCompleted())
                      .map(ActionDetail::getTitle)
                      .findFirst()
                      .orElse(null); // 모든 미션이 완료되었을 경우

              return ActionNoteResDTO.ActionNotesDTO.of(plan, progress, nextActionTitle);
            })
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public ActionNoteResDTO.ActionNoteDTO getActionNote(Long actionPlanId) {
    // N+1 및 MultipleBagFetchException 방지용 FetchJoin 쿼리 2번 (또는 BatchSize 사용해야함)
    // ActionPlan + Tag (영속성 컨텍스트에 저장)
    ActionPlan actionPlan =
        actionPlanRepository
            .findByIdWithTags(actionPlanId)
            .orElseThrow(() -> new GeneralException(ErrorCode.ACTION_PLAN_NOT_FOUND));

    // + ActionDetail (Hibernate가 1차 캐시에 있는 기존 actionPlan 객체에 details 리스트를 채움)
    actionPlanRepository.findByIdWithDetails(actionPlanId);
    List<ActionDetail> details = actionPlan.getDetails();

    // 진행도 계산
    int progress = calculateProgress(details);

    // 상세 미션 정렬 및 DTO 변환
    List<ActionNoteResDTO.ActionDetailDTO> sortedDetails =
        details.stream().map(ActionNoteResDTO.ActionDetailDTO::of).toList();

    return ActionNoteResDTO.ActionNoteDTO.of(actionPlan, progress, sortedDetails);
  }

  @Transactional
  public ActionNoteResDTO.UpdateActionDetailDTO updateActionDetail(
      Long actionDetailId, Boolean isCompleted) {
    // ActionDetail 조회
    ActionDetail actionDetail =
        actionDetailRepository
            .findByIdWithPlanAndNoteAndDetails(actionDetailId)
            .orElseThrow(() -> new GeneralException(ErrorCode.ACTION_DETAIL_NOT_FOUND));

    // 상태 업데이트
    actionDetail.updateIsCompleted(isCompleted);

    // 연관된 ActionPlan 및 모든 Details 조회
    ActionPlan actionPlan = actionDetail.getActionPlan();
    List<ActionDetail> allDetails = actionPlan.getDetails();

    // 진행도 재계산
    int updatedProgress = calculateProgress(allDetails);

    // ActionNote 완료 상태 동기화 (모든 Detail이 완료되었는지 확인)
    boolean allCompleted = allDetails.stream().allMatch(ActionDetail::getIsCompleted);

    ActionNote actionNote = actionPlan.getActionNote();
    if (actionNote != null) {
      actionNote.updateIsCompleted(allCompleted); // 모든 미션 완료 시 Note도 완료로 변경
    }

    return ActionNoteResDTO.UpdateActionDetailDTO.of(actionDetail, updatedProgress, allCompleted);
  }

  /** 진행도 계산 */
  private int calculateProgress(List<ActionDetail> details) {
    if (details == null || details.isEmpty()) {
      return 0;
    }

    long totalCount = details.size();
    long completedCount = details.stream().filter(ActionDetail::getIsCompleted).count();

    return (int) ((double) completedCount / totalCount * 100);
  }
}
