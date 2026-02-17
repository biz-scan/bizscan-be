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
@RequestMapping("/api/ai-vector") // 기본 경로
@RequiredArgsConstructor
public class AiVectorController {

  private final AiVectorService aiVectorService;

  @Operation(summary = "유사 매장 추천 조회", description = "벡터 유사도 및 매장 정보를 기반으로 상위 3개의 추천 매장을 반환합니다.")
  @GetMapping("/recommend/{storeId}")
  public ResponseEntity<ApiResponse<List<RecommendationResponseDto>>> getRecommendedStores(
      @Parameter(description = "매장 ID", required = true) @PathVariable("storeId") Long storeId) {
    List<RecommendationResponseDto> result = aiVectorService.recommendSimilarStores(storeId);

    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessCode.OK, result));
  }

  //  @Operation(
  //      summary = "전체 데이터 적재 (마이그레이션)",
  //      description = "MySQL의 모든 Store 데이터를 Vector DB로 업로드합니다. (배포 후 1회 실행용)")
  //  @PostMapping("/migrate-all")
  //  public ResponseEntity<ApiResponse<String>> migrateAllData() {
  //    int count = aiVectorService.migrateAllStoresToVectorDb();
  //
  //    return ResponseEntity.ok(
  //        ApiResponse.onSuccess(SuccessCode.OK, count + "개의 가게 데이터가 성공적으로 적재되었습니다."));
  //  }

  @Operation(
      summary = "Vector DB 데이터 조회 확인",
      description = "특정 가게의 데이터가 Vector DB에 잘 들어갔는지 내용을 확인합니다.")
  @GetMapping("/check/{storeId}")
  public ResponseEntity<ApiResponse<String>> checkVectorData(@PathVariable Long storeId) {
    String result = aiVectorService.checkStoreData(storeId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessCode.OK, result));
  }
}
