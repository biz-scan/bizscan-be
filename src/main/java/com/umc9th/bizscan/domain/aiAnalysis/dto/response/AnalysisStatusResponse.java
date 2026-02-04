package com.umc9th.bizscan.domain.aiAnalysis.dto.response;

import com.umc9th.bizscan.domain.aiAnalysis.enums.AnalysisStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnalysisStatusResponse {

  private AnalysisStatus status;
  private String progressMessage;
  private int pollingTime;
}
