package com.umc9th.bizscan.domain.analysis.service;

import com.umc9th.bizscan.domain.analysis.dto.AnalysisSummaryDto;
import com.umc9th.bizscan.domain.analysis.exception.SwotAnalysisErrorCode;
import com.umc9th.bizscan.domain.commercial.entity.SalesEstimate;
import com.umc9th.bizscan.domain.commercial.repository.SalesRepository;
import com.umc9th.bizscan.domain.competitor.entity.CompetitorStore;
import com.umc9th.bizscan.domain.competitor.repository.CompetitorRepository;
import com.umc9th.bizscan.domain.hinterland.entity.HousingStat;
import com.umc9th.bizscan.domain.hinterland.entity.IncomeStat;
import com.umc9th.bizscan.domain.hinterland.repository.HousingRepository;
import com.umc9th.bizscan.domain.hinterland.repository.IncomeRepository;
import com.umc9th.bizscan.domain.region.dto.HashtagDto;
import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import com.umc9th.bizscan.domain.region.repository.RegionRepository;
import com.umc9th.bizscan.domain.region.service.RegionTrendService;
import com.umc9th.bizscan.domain.store.entity.StoreCrawlingData;
import com.umc9th.bizscan.domain.store.service.ReviewCrawlerService;
import com.umc9th.bizscan.global.apiPayload.exception.GeneralException;
import com.umc9th.bizscan.global.client.kakao.KakaoClient;
import com.umc9th.bizscan.global.client.kakao.dto.KakaoApiResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataVerificationService {

  private final SalesRepository salesRepository;
  private final RegionRepository regionRepository;
  private final KakaoClient kakaoClient;
  private final CompetitorRepository competitorRepository;
  private final IncomeRepository incomeRepository;
  private final ReviewCrawlerService reviewCrawlerService;
  private final HousingRepository housingRepository;
  private final RegionTrendService regionTrendService;

  private String cleanAddress(String fullAddress) {
    if (fullAddress == null) return "";
    return fullAddress
        .replaceAll("\\s\\d+층", "")
        .replaceAll("\\s지하\\d+층", "")
        .replaceAll("\\sB?\\d+호", "")
        .replaceAll("\\s\\d+호", "")
        .replaceAll("\\s[^\\s]+빌딩", "")
        .trim();
  }

  private RegionMaster getRegionByAddress(String address) {
    KakaoApiResponse response = kakaoClient.searchAddress(address);

    if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
      throw new GeneralException(SwotAnalysisErrorCode.REGION_ADDRESS_NOT_FOUND);
    }

    KakaoApiResponse.Address addressInfo = response.getDocuments().get(0).getAddress();
    double lat = Double.parseDouble(addressInfo.getY());
    double lon = Double.parseDouble(addressInfo.getX());

    return regionRepository
        .findNearestRegionWithData(lat, lon)
        .orElseGet(
            () ->
                regionRepository
                    .findByTrdarCd("3110114")
                    .orElseThrow(
                        () -> new GeneralException(SwotAnalysisErrorCode.REGION_DATA_NOT_FOUND)));
  }

  private String mapGuToZone(String guName) {
    if (guName == null) return "동북권";
    if (guName.matches(".*(종로|중구|용산).*")) return "도심권";
    if (guName.matches(".*(성동|광진|동대문|중랑|성북|강북|도봉|노원).*")) return "동북권";
    if (guName.matches(".*(은평|서대문|마포).*")) return "서북권";
    if (guName.matches(".*(양천|강서|구로|금천|영등포|동작|관악).*")) return "서남권";
    if (guName.matches(".*(서초|강남|송파|강동).*")) return "동남권";
    return "동북권";
  }

  // [AI 전용] 요약 데이터
  public AnalysisSummaryDto extractAnalysisSummary(
      String address, String bizCategory, String storeName, String keyword) {

    String cleanedAddress = cleanAddress(address);
    RegionMaster region = getRegionByAddress(cleanedAddress);
    String dongName = region.getAdstrdNm();

    String searchKeyword = "음식";
    if (bizCategory != null) {
      String upperCat = bizCategory.toUpperCase();
      if (upperCat.matches(".*(CAFE|BAKERY|카페).*")) searchKeyword = "카페";
      else if (upperCat.matches(".*(RESTAURANT|식당).*")) searchKeyword = "음식";
      else if (upperCat.matches(".*(BAR|PUB|술집|주점).*")) searchKeyword = "술집";
    }

    SalesEstimate sales =
        salesRepository
            .findLatestByTrdarCd(region.getTrdarCd())
            .orElse(SalesEstimate.builder().build());
    int compCount =
        competitorRepository.countCompetitors(region.getLat(), region.getLon(), searchKeyword, 0.5);
    long avgIncome =
        incomeRepository
            .findFirstByAdstrdNmOrderByStdDateDesc(dongName)
            .map(IncomeStat::getAvgMonIncome)
            .orElse(3300000L);
    String zoneNm = mapGuToZone(region.getGuNm());
    HousingStat housing =
        housingRepository.findFirstByZoneNmOrderByStdDateDesc(zoneNm).orElse(null);

    // 연령/성별/피크타임
    String mainAge = "정보 없음";
    long maxAgeCnt = 0;
    if (sales.getAge10Count() != null && sales.getAge10Count() > maxAgeCnt) {
      maxAgeCnt = sales.getAge10Count();
      mainAge = "10대";
    }
    if (sales.getAge20Count() != null && sales.getAge20Count() > maxAgeCnt) {
      maxAgeCnt = sales.getAge20Count();
      mainAge = "20대";
    }
    if (sales.getAge30Count() != null && sales.getAge30Count() > maxAgeCnt) {
      maxAgeCnt = sales.getAge30Count();
      mainAge = "30대";
    }
    if (sales.getAge40Count() != null && sales.getAge40Count() > maxAgeCnt) {
      maxAgeCnt = sales.getAge40Count();
      mainAge = "40대";
    }
    if (sales.getAge50Count() != null && sales.getAge50Count() > maxAgeCnt) {
      maxAgeCnt = sales.getAge50Count();
      mainAge = "50대";
    }
    if (sales.getAge60Count() != null && sales.getAge60Count() > maxAgeCnt) {
      mainAge = "60대 이상";
    }
    String mainGender =
        (sales.getMaleCount() != null && sales.getFemaleCount() != null)
            ? ((sales.getMaleCount() >= sales.getFemaleCount()) ? "남성" : "여성")
            : "정보 없음";
    String peakTime = "정보 없음";
    long maxTimeCnt = 0;
    if (sales.getTime1114() != null && sales.getTime1114() > maxTimeCnt) {
      maxTimeCnt = sales.getTime1114();
      peakTime = "점심(11-14)";
    }
    if (sales.getTime1417() != null && sales.getTime1417() > maxTimeCnt) {
      maxTimeCnt = sales.getTime1417();
      peakTime = "오후(14-17)";
    }
    if (sales.getTime1721() != null && sales.getTime1721() > maxTimeCnt) {
      maxTimeCnt = sales.getTime1721();
      peakTime = "저녁(17-21)";
    }

    long avgDailyPop =
        (sales.getTotalSaleCnt() != null && sales.getTotalSaleCnt() > 0)
            ? (sales.getTotalSaleCnt() / 30)
            : 0;
    String compLevel = (compCount >= 20) ? "HIGH" : (compCount >= 5) ? "MID" : "LOW";

    String mainHousing = "정보 없음";
    if (housing != null) {
      double maxRatio = 0;
      if (housing.getAptRatio() != null && housing.getAptRatio() > maxRatio) {
        maxRatio = housing.getAptRatio();
        mainHousing = "아파트";
      }
      if (housing.getSingleFamRatio() != null && housing.getSingleFamRatio() > maxRatio) {
        maxRatio = housing.getSingleFamRatio();
        mainHousing = "단독주택";
      }
      if (housing.getStudioRatio() != null && housing.getStudioRatio() > maxRatio) {
        mainHousing = "원룸";
      }
    }

    String hashtags;
    try {
      String simpleDong = dongName.replaceAll("[0-9]+(가|동|로)$", "").trim();
      if (simpleDong.isEmpty()) simpleDong = dongName;
      String derivedKeyword = simpleDong + searchKeyword;
      String snsKeyword = (keyword != null && !keyword.isBlank()) ? keyword : derivedKeyword;

      List<HashtagDto> tags = regionTrendService.recommendHashtags(snsKeyword);
      if (tags == null || tags.isEmpty())
        tags = regionTrendService.recommendHashtags(searchKeyword);
      if (tags == null || tags.isEmpty()) tags = regionTrendService.recommendHashtags(simpleDong);

      if (tags != null && !tags.isEmpty()) {
        hashtags =
            tags.stream().limit(5).map(HashtagDto::getHashtag).collect(Collectors.joining(", "));
      } else {
        hashtags = String.format("#%s%s, #%s맛집, #데이트, #핫플", simpleDong, searchKeyword, simpleDong);
      }
    } catch (Exception e) {
      String simpleDong = dongName.replaceAll("[0-9]+(가|동|로)$", "");
      hashtags = String.format("#%s%s, #%s핫플", simpleDong, searchKeyword, simpleDong);
    }

    // 리뷰 데이터 크롤링
    int myReviewCount = 0;
    double myRating = 0.0;
    double avgCompReviewCount = 0.0;

    if (storeName != null && !storeName.isEmpty()) {
      try {
        // 1. 내 가게 정보
        String myPlaceId = reviewCrawlerService.findPlaceId(cleanedAddress, storeName);
        if (myPlaceId != null) {
          StoreCrawlingData myData =
              reviewCrawlerService.getStoreCrawlingData(myPlaceId, storeName);
          myReviewCount = myData.getReviewCount();
          myRating = myData.getRating();
        }

        // 2. 경쟁사 정보
        List<CompetitorStore> competitors =
            competitorRepository.findNearbyCompetitors(
                region.getLat(), region.getLon(), searchKeyword, 0.5);
        double totalCompReviews = 0;
        int count = 0;

        for (CompetitorStore comp : competitors) {
          if (count >= 3) break;
          if (comp.getStoreNm().contains(storeName) || storeName.contains(comp.getStoreNm()))
            continue;
          try {
            String compCleanAddr = cleanAddress(comp.getAddress());
            String compSimpleName = comp.getStoreNm().split(" ")[0];
            String compId = reviewCrawlerService.findPlaceId(compCleanAddr, compSimpleName);
            if (compId != null) {
              StoreCrawlingData cData =
                  reviewCrawlerService.getStoreCrawlingData(compId, comp.getStoreNm());
              if (cData.getReviewCount() > 0) {
                totalCompReviews += cData.getReviewCount();
                count++;
              }
            }
          } catch (Exception ignored) {
          }
        }
        avgCompReviewCount = (count > 0) ? (totalCompReviews / count) : 0.0;
      } catch (Exception e) {
        log.error("리뷰 분석 실패: {}", e.getMessage());
      }
    }

    return AnalysisSummaryDto.builder()
        .mainAgeGroup(mainAge)
        .mainGender(mainGender)
        .peakTime(peakTime)
        .avgDailyPop(avgDailyPop)
        .competitorCount(compCount)
        .competitionLevel(compLevel)
        .avgMonthIncome(avgIncome)
        .mainHousingType(mainHousing)
        .topHashtags(hashtags)
        .myReviewCount(myReviewCount)
        .myRating(myRating)
        .avgCompReviewCount(avgCompReviewCount)
        .build();
  }
}
