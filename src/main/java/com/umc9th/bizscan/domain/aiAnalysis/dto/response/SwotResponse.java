package com.umc9th.bizscan.domain.aiAnalysis.dto.response;

import com.umc9th.bizscan.domain.aiAnalysis.entity.Swot;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SwotResponse {

  private String sTitle;
  private String sDetail;

  private String wTitle;
  private String wDetail;

  private String oTitle;
  private String oDetail;

  private String tTitle;
  private String tDetail;

  public static SwotResponse from(Swot swot) {
    return SwotResponse.builder()
        .sTitle(swot.getSTitle())
        .sDetail(swot.getSDetail())
        .wTitle(swot.getWTitle())
        .wDetail(swot.getWDetail())
        .oTitle(swot.getOTitle())
        .oDetail(swot.getODetail())
        .tTitle(swot.getTTitle())
        .tDetail(swot.getTDetail())
        .build();
  }
}
