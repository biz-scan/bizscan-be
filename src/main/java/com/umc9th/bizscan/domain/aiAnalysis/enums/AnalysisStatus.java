package com.umc9th.bizscan.domain.aiAnalysis.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnalysisStatus {
  REQUEST("매장 데이터를 분석 중입니다."), // 분석 요청 직후 (초반)
  SWOT_PROCESSING("분석을 진행 중입니다."), // SWOT 생성 중
  ACTION_PLAN_PROCESSING("실행 전략을 생성 중입니다."), // ActionPlan 생성 중
  ACTION_DETAIL_PROCESSING("마무리 작업을 진행 중입니다."), // ActionDetail 생성 중
  COMPLETED("완료되었습니다."),
  FAILED("분석에 실패했습니다.");

  private final String progressMessage;
}
