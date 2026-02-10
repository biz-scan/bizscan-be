package com.umc9th.bizscan.domain.analysis.controller;

import com.umc9th.bizscan.domain.analysis.dto.AnalysisSummaryDto;
import com.umc9th.bizscan.domain.analysis.exception.SwotAnalysisErrorCode;
import com.umc9th.bizscan.domain.analysis.service.DataVerificationService;
import com.umc9th.bizscan.global.config.swagger.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Data Analysis", description = "상권 분석 데이터 추출 및 AI 프롬프트용 요약 정보 제공 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/swot")
public class AnalysisController {

  private final DataVerificationService dataVerificationService;

  @Operation(
      summary = "AI 분석용 요약 데이터 추출",
      description = "주소, 업종, 가게명, 키워드를 입력하면 AI 프롬프트에 사용할 핵심 요약 데이터(경쟁, 리뷰 포함)를 JSON으로 반환합니다.")
  @ApiErrorCodeExamples(
      dataAnalysis = {
        SwotAnalysisErrorCode.REGION_ADDRESS_NOT_FOUND,
        SwotAnalysisErrorCode.REGION_DATA_NOT_FOUND
      })
  @GetMapping("/summary")
  public AnalysisSummaryDto getAnalysisSummary(
      @Parameter(description = "매장 주소 (예: 서울 성동구 성수동1가 656-442)", required = true)
          @RequestParam("address")
          String address,
      @Parameter(description = "업종 키워드 (예: 카페, 한식, 주점)", required = true) @RequestParam("category")
          String category,
      @Parameter(description = "내 가게명 (예: 밀도)", required = false)
          @RequestParam(value = "storeName", required = false)
          String storeName,

      // 해시태그 추천용 키워드
      @Parameter(description = "해시태그 추천용 검색어 (예: 성수동 카페)", required = true) @RequestParam("keyword")
          String keyword) {

    return dataVerificationService.extractAnalysisSummary(address, category, storeName, keyword);
  }
}
