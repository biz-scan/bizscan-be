package com.umc9th.bizscan.domain.aiVector.controller;

import com.umc9th.bizscan.domain.aiVector.dto.RecommendationResponseDto;
import com.umc9th.bizscan.domain.aiVector.service.AiVectorService;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI Vector API", description = "AI 벡터 기반 유사 매장 추천 API")
@RestController
@RequestMapping("/api/ai-vector")
@RequiredArgsConstructor
public class AiVectorController {

  private final AiVectorService aiVectorService;

  @Operation(summary = "유사 매장 추천 조회", description = "벡터 유사도 및 매장 정보를 기반으로 상위 4개의 추천 매장을 반환합니다.")
  @GetMapping("/recommend/{storeId}")
  public ResponseEntity<ApiResponse<List<RecommendationResponseDto>>> getRecommendedStores(
      @Parameter(description = "매장 ID", required = true) @PathVariable("storeId") Long storeId) {
    List<RecommendationResponseDto> result = aiVectorService.recommendSimilarStores(storeId);

    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessCode.OK, result));
  }
}
