package com.umc9th.bizscan.domain.region.controller;

import com.umc9th.bizscan.domain.region.dto.HashtagDto;
import com.umc9th.bizscan.domain.region.service.RegionTrendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Trend Test API", description = "네이버 트렌드 수집 및 해시태그 추천 테스트용 API")
@RestController
@RequiredArgsConstructor
public class TestController {

  private final RegionTrendService regionTrendService;

  // http://localhost:8080/api/test/trend?regionId=1&keyword=성수동맛집
  @Operation(
      summary = "네이버 검색 트렌드 수집 및 DB 저장",
      description =
          "특정 지역(regionId)과 키워드를 입력받아 네이버 검색광고 API 데이터를 수집하고, 결과를 DB(tb_region_trend)에 저장합니다.")
  @GetMapping("/api/test/trend")
  public String testTrend(
      @Parameter(description = "지역 ID (DB tb_region_master의 PK)", example = "1")
          @RequestParam("regionId")
          Long regionId,
      @Parameter(description = "수집할 중심 키워드", example = "성수동맛집") @RequestParam("keyword")
          String keyword) {
    regionTrendService.collectSearchTrend(regionId, keyword);
    return "수집 완료! DB tb_region_trend 테이블 확인해보세요.";
  }

  // http://localhost:8080/api/trend/hashtag?keyword=성수동디저트
  @Operation(
      summary = "SNS 해시태그 추천 (실시간)",
      description = "입력한 키워드와 연관된 검색어들을 조회하여, 검색량이 높은 순서대로 SNS 해시태그 리스트를 반환합니다. (DB에 저장하지 않음)")
  @GetMapping("/api/trend/hashtag")
  public List<HashtagDto> getHashtagRecommendation(
      @Parameter(description = "분석할 메뉴명 또는 지역+업종 (예: 성수동카페, 서울숲카페)", example = "성수동디저트")
          @RequestParam("keyword")
          String keyword) {
    return regionTrendService.recommendHashtags(keyword);
  }
}
