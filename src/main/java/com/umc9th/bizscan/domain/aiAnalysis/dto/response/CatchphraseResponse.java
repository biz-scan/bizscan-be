package com.umc9th.bizscan.domain.aiAnalysis.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CatchphraseResponse {
  @Schema(description = "AI 생성 캐치프레이즈 (최대 15자)", example = "성수동 직장인 회식 1타")
  private String catchphrase;
}
