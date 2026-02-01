package com.umc9th.bizscan.domain.aiAnalysis.enums;

public enum AnalysisStatus {
  REQUEST, // 분석 요청 직후 (초반)
  SWOT_PROCESSING, // SWOT 생성 중
  ACTIONPLAN_PROCESSING, // ActionPlan 생성 중
  ACTIONDETAIL_PROCESSING, // ActionDetail 생성 중
  COMPLETED,
  FAILED
}
