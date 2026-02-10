package com.umc9th.bizscan.domain.member.dto.response;

import com.umc9th.bizscan.domain.aiAnalysis.entity.Analysis;
import com.umc9th.bizscan.domain.aiAnalysis.entity.AnalysisRequest;
import com.umc9th.bizscan.domain.aiAnalysis.enums.AnalysisStatus;
import com.umc9th.bizscan.domain.member.entity.Member;
import com.umc9th.bizscan.domain.store.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberResponseDto {

  private Long id;
  private String email;
  private String nickname;
  private Long storeId;
  private String requestId;
  private AnalysisStatus status;

  public static MemberResponseDto from(Member member) {
    Long storeId = null;
    String requestId = null;
    AnalysisStatus status = null;

    // Store가 존재하는지 확인
    Store store = member.getStore();
    if (store != null) {
      storeId = store.getId();

      // Analysis 및 AnalysisRequest가 순차적으로 존재하는지 확인
      Analysis analysis = store.getAnalysis();
      if (analysis != null) {
        AnalysisRequest request = analysis.getAnalysisRequest();
        if (request != null) {
          requestId = request.getRequestId();
          status = request.getStatus();
        }
      }
    }

    return new MemberResponseDto(
        member.getId(), member.getEmail(), member.getNickname(), storeId, requestId, status);
  }
}
