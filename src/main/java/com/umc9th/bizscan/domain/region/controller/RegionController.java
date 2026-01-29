package com.umc9th.bizscan.domain.region.controller;

import com.umc9th.bizscan.domain.analysis.dto.AnalysisSummaryDto;
import com.umc9th.bizscan.domain.analysis.service.DataVerificationService;
import com.umc9th.bizscan.domain.commercial.dto.OpportunityResponseDto;
import com.umc9th.bizscan.domain.competitor.dto.ThreatResponseDto;
import com.umc9th.bizscan.domain.hinterland.dto.StrengthResponseDto;
import com.umc9th.bizscan.domain.region.dto.HashtagDto;
import com.umc9th.bizscan.domain.region.service.RegionTrendService;
import com.umc9th.bizscan.domain.store.dto.WeaknessResponseDto;
import com.umc9th.bizscan.domain.store.service.ReviewCrawlerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/swot")
public class RegionController {

  private final RegionTrendService regionTrendService;
  private final ReviewCrawlerService reviewCrawlerService;
  private final DataVerificationService dataVerificationService;

  // http://localhost:8080/api/trend/hashtag?keyword=성수동디저트
  @Operation(
      summary = "SNS 해시태그 추천 (실시간)",
      description = "입력한 키워드와 연관된 검색어들을 조회하여, 검색량이 높은 순서대로 SNS 해시태그 리스트를 반환합니다. (DB에 저장하지 않음)")
  @GetMapping("/trend/hashtag")
  public List<String> getHashtagRecommendation(
      @Parameter(description = "분석할 메뉴명 또는 지역+업종 (예: 성수동카페, 서울숲카페)", example = "성수동디저트")
          @RequestParam("keyword")
          String keyword) {
    List<HashtagDto> tags = regionTrendService.recommendHashtags(keyword);

    return tags.stream().map(HashtagDto::getHashtag).collect(Collectors.toList());
  }

  // 1. [S] 강점 : 내부 요인 / 긍정적
  @Operation(
      summary = "[S] 강점 분석 (가격 경쟁력)",
      description = "주소와 예상 객단가를 입력하면 지역 소득 대비 가격 경쟁력을 분석합니다.")
  @GetMapping("/strength")
  public StrengthResponseDto testStrength(
      @Parameter(description = "주소 (예: 서울시 성동구 용답동 57-13)", required = true)
          @RequestParam(name = "address")
          String address,
      @Parameter(description = "예상 객단가 (단위: 원)", required = true) @RequestParam(name = "avgPrice")
          int avgPrice) {
    return dataVerificationService.analyzeStrength(address, avgPrice);
  }

  // 2. [W] 약점 : 내부 요인 / 부정적
  @Operation(
      summary = "[W] 약점 분석 (Weakness)",
      description = "주소와 상호명을 기반으로 내 가게의 리뷰 수와 주변 경쟁사(반경 500m)의 리뷰 수를 비교 분석합니다.")
  @GetMapping("/weakness")
  public WeaknessResponseDto analyzeWeakness(
      @Parameter(
              description = "매장 상세 주소 (예: 서울 성동구 성수동1가 656-442)",
              required = true,
              example = "서울 성동구 성수동1가 656-442")
          @RequestParam("address")
          String address,
      @Parameter(description = "매장 이름 (예: 밀도)", required = true, example = "밀도")
          @RequestParam("name")
          String name) {
    return dataVerificationService.analyzeWeakness(address, name);
  }

  @Operation(
      summary = "[O] 기회 분석 (Address Based)",
      description = "주소(예: 서울시 성동구 왕십리로 123)를 입력하면 -> 자동으로 상권을 찾고 -> 기회 요인을 분석합니다.")
  @GetMapping("/opportunity")
  public OpportunityResponseDto testOpportunity(
      @Parameter(description = "주소 입력 (예: 서울시 성동구 행당동 168-1)", required = true)
          @RequestParam(name = "address")
          String address) {

    // 사용자가 입력한 '주소'를 서비스로 넘김
    return dataVerificationService.analyzeOpportunity(address);
  }

  // 4. [T] 위협 : 외부 요인 / 부정적
  @Operation(
      summary = "[T] 위협 분석 (UI 시뮬레이션)",
      description = "사용자가 선택한 대분류와 소분류를 기반으로 경쟁 강도를 분석합니다.")
  @GetMapping("/threat")
  public ThreatResponseDto testThreat(
      @Parameter(description = "주소 (예: 서울시 성동구 용답동 57-13)", required = true)
          @RequestParam(name = "address")
          String address,
      @Parameter(description = "대분류 (카페/베이커리, 식당, 술집/주점)", required = true)
          @RequestParam(name = "mainCategory")
          String mainCategory,
      @Parameter(description = "소분류 (예: 일반 카페, 한식/백반/국밥, 고기/구이 등)", required = true)
          @RequestParam(name = "subCategory")
          String subCategory) {
    return dataVerificationService.analyzeThreat(address, mainCategory, subCategory);
  }

  @Operation(
      summary = "AI 분석용 요약 데이터 추출",
      description = "주소, 업종, 가게명, 키워드를 입력하면 AI 프롬프트에 사용할 핵심 요약 데이터(경쟁, 리뷰 포함)를 JSON으로 반환합니다.")
  @GetMapping("/summary")
  public AnalysisSummaryDto getAnalysisSummary(
      @Parameter(description = "매장 주소 (예: 서울 성동구 성수동1가 656-442)", required = true)
          @RequestParam("address")
          String address,
      @Parameter(description = "업종 키워드 (예: 카페, 한식)", required = true) @RequestParam("category")
          String category,
      @Parameter(description = "내 가게명 (예: 성수카페)", required = false)
          @RequestParam(value = "storeName", required = false)
          String storeName,

      // 해시태그 추천용 키워드
      @Parameter(description = "해시태그 추천용 검색어 (예: 성수동 데이트)", required = true)
          @RequestParam("keyword")
          String keyword) {
    // keyword 파라미터까지 전달
    return dataVerificationService.extractAnalysisSummary(address, category, storeName, keyword);
  }
}
