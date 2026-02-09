package com.umc9th.bizscan.domain.aiAnalysis.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public class AnalysisReqDTO {
    @Schema(description = "AI 분석 요청 정보")
    public record AiAnalysisDTO(
            @Schema(description = "분석할 매장의 ID", example = "1")
            Long storeId,
            @Schema(description = "기존 분석 결과가 있을 경우 재분석 여부 (true 시 기존 데이터 삭제 후 재진행)", example = "false")
            Boolean retry
    ){}
}
