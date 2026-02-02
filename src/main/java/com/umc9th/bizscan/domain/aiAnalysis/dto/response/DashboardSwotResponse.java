package com.umc9th.bizscan.domain.aiAnalysis.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardSwotResponse {

  private SwotGroup strengths;
  private SwotGroup weaknesses;
  private SwotGroup opportunities;
  private SwotGroup threats;

  @Getter
  @AllArgsConstructor
  public static class SwotGroup {
    private List<SwotItem> items;
  }

  @Getter
  @AllArgsConstructor
  public static class SwotItem {
    private String keyword;
    private String description;
  }

  // SWOT 엔티티 -> 대시보드 DTO 변환
  //  public static DashboardSwotResponse from(Swot swot) {
  //    return new DashboardSwotResponse(
  //        wrap(swot.getSTitle(), swot.getSDetail()),
  //        wrap(swot.getWTitle(), swot.getWDetail()),
  //        wrap(swot.getOTitle(), swot.getODetail()),
  //        wrap(swot.getTTitle(), swot.getTDetail()));
  //  }

  private static SwotGroup wrap(String title, String detail) {
    if (title == null || detail == null) {
      return new SwotGroup(List.of());
    }

    return new SwotGroup(List.of(new SwotItem(title, detail)));
  }
}
