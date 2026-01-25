package com.umc9th.bizscan.domain.analysis.service;

import com.umc9th.bizscan.domain.analysis.dto.AnalysisSummaryDto;
import com.umc9th.bizscan.domain.analysis.entity.AnalysisSummary;
import com.umc9th.bizscan.domain.analysis.repository.AnalysisSummaryRepository;
import com.umc9th.bizscan.domain.commercial.dto.OpportunityResponseDto;
import com.umc9th.bizscan.domain.commercial.entity.SalesEstimate;
import com.umc9th.bizscan.domain.commercial.repository.SalesRepository;
import com.umc9th.bizscan.domain.competitor.dto.ThreatResponseDto;
import com.umc9th.bizscan.domain.competitor.entity.CompetitorStore;
import com.umc9th.bizscan.domain.competitor.repository.CompetitorRepository;
import com.umc9th.bizscan.domain.hinterland.dto.StrengthResponseDto;
import com.umc9th.bizscan.domain.hinterland.entity.HousingStat;
import com.umc9th.bizscan.domain.hinterland.entity.IncomeStat;
import com.umc9th.bizscan.domain.hinterland.repository.HousingRepository;
import com.umc9th.bizscan.domain.hinterland.repository.IncomeRepository;
import com.umc9th.bizscan.domain.region.dto.HashtagDto;
import com.umc9th.bizscan.domain.region.dto.KakaoApiResponseDto;
import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import com.umc9th.bizscan.domain.region.infrastructure.KakaoApiClient;
import com.umc9th.bizscan.domain.region.repository.RegionRepository;
import com.umc9th.bizscan.domain.region.service.RegionTrendService;
import com.umc9th.bizscan.domain.store.dto.WeaknessResponseDto;
import com.umc9th.bizscan.domain.store.entity.StoreCrawlingData;
import com.umc9th.bizscan.domain.store.repository.StoreCrawlingDataRepository;
import com.umc9th.bizscan.domain.store.service.ReviewCrawlerService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataVerificationService {

  private final SalesRepository salesRepository;
  private final RegionRepository regionRepository;
  private final KakaoApiClient kakaoApiClient;
  private final CompetitorRepository competitorRepository;
  private final IncomeRepository incomeRepository;
  private final ReviewCrawlerService reviewCrawlerService;
  private final HousingRepository housingRepository;
  private final RegionTrendService regionTrendService;
  private final StoreCrawlingDataRepository storeCrawlingDataRepository;
  private final AnalysisSummaryRepository analysisSummaryRepository;

  // ================================================================
  //  Helper Methods
  // ================================================================

  // 주소 정제 (상세주소 제거 -> API 검색 정확도 향상)
  private String cleanAddress(String fullAddress) {
    if (fullAddress == null) return "";
    String cleaned =
        fullAddress
            .replaceAll("\\s\\d+층", "") // " 4층" 제거
            .replaceAll("\\s지하\\d+층", "") // " 지하1층" 제거
            .replaceAll("\\sB?\\d+호", "") // " 101호" 제거
            .replaceAll("\\s\\d+호", "") // " 101호" 제거
            .replaceAll("\\s[^\\s]+빌딩", "") // " 다미빌딩" 제거
            .trim();
    log.info("주소 정제: '{}' -> '{}'", fullAddress, cleaned);
    return cleaned;
  }

  private String extractDongFromAddress(String addr) {
    if (addr == null) return "";
    String[] parts = addr.split(" ");

    // 예: "서울 성동구 성수동1가 656-442" -> "성수동1가" (인덱스 2)
    // 예: "서울 강남구 역삼동" -> "역삼동" (인덱스 2)
    if (parts.length >= 3) {
      return parts[2];
    }
    // 예: "경기도 성남시" (동이 없는 경우 구/시 반환)
    if (parts.length >= 2) {
      return parts[1];
    }
    return addr; // 실패하면 원본 반환
  }

  private RegionMaster getRegionByAddress(String address) {
    KakaoApiResponseDto.Document.Address addressInfo = kakaoApiClient.searchAddress(address);

    if (addressInfo == null) {
      throw new IllegalArgumentException("입력하신 주소를 찾을 수 없습니다.");
    }

    double lat = Double.parseDouble(addressInfo.getY());
    double lon = Double.parseDouble(addressInfo.getX());

    return regionRepository
        .findNearestRegionWithData(lat, lon)
        .orElseGet(() -> regionRepository.findByTrdarCd("3110114").get());
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

  // ================================================================
  //  Main Logic
  // ================================================================

  // 1. [S] 강점 분석
  @Transactional(readOnly = true)
  public StrengthResponseDto analyzeStrength(String address, int myAvgPrice) {
    // 1. 주소 및 지역 정보 가져오기
    String cleanedAddr = cleanAddress(address);
    RegionMaster region = getRegionByAddress(cleanedAddr);
    String dongName = region.getAdstrdNm();

    // 2. 소득 데이터 조회 (IncomeStat 엔티티 활용)
    long regionIncome =
        incomeRepository
            .findFirstByAdstrdNmOrderByStdDateDesc(dongName)
            .map(IncomeStat::getAvgMonIncome)
            .orElseGet(
                () -> {
                  Double avgVal = incomeRepository.findAverageIncome();
                  return (avgVal != null) ? avgVal.longValue() : 3300000L;
                });

    // 3. 부담률 및 결과 산출
    double burdenRatio = (double) myAvgPrice / regionIncome * 100;
    String resultStatus =
        (burdenRatio < 0.5) ? "가격 경쟁력 우수" : (burdenRatio < 1.5) ? "가격 경쟁력 보통" : "가격 경쟁력 낮음";

    // 4. StrengthResponseDto 객체로 반환 (이게 JSON이 됨)
    return StrengthResponseDto.builder()
        .address(cleanedAddr)
        .dongName(dongName)
        .avgMonthIncome(regionIncome)
        .myAvgPrice(myAvgPrice)
        .burdenRatio(Math.round(burdenRatio * 100.0) / 100.0)
        .priceCompetitiveness(resultStatus) // 위에서 판별한 결과 문구
        .build();
  }

  // 2. [W] 약점 분석
  public WeaknessResponseDto analyzeWeakness(String address, String storeName) {
    try {
      String cleanedAddress = cleanAddress(address);
      String myPlaceId = reviewCrawlerService.findPlaceId(cleanedAddress, storeName);

      if (myPlaceId == null || myPlaceId.isEmpty()) {
        throw new RuntimeException("가게를 찾을 수 없습니다.");
      }

      // 1. 내 가게 데이터 가져오기
      StoreCrawlingData myData = reviewCrawlerService.getStoreCrawlingData(myPlaceId, storeName);

      // 2. 주변 경쟁사 데이터 수집
      RegionMaster region = getRegionByAddress(cleanedAddress);
      String searchKeyword = storeName.contains("카페") ? "카페" : "음식";
      List<CompetitorStore> competitors =
          competitorRepository.findNearbyCompetitors(
              region.getLat(), region.getLon(), searchKeyword, 0.5);

      double totalCompReviews = 0;
      double totalCompRating = 0;
      int count = 0;

      for (CompetitorStore comp : competitors) {
        if (count >= 3) break;
        if (comp.getStoreNm().contains(storeName) || storeName.contains(comp.getStoreNm()))
          continue;

        try {
          String compId =
              reviewCrawlerService.findPlaceId(
                  cleanAddress(comp.getAddress()), comp.getStoreNm().split(" ")[0]);
          if (compId != null) {
            StoreCrawlingData cData = reviewCrawlerService.getStoreCrawlingData(compId, "");

            if (cData.getReviewCount() > 0) {
              totalCompReviews += cData.getReviewCount();
              totalCompRating += cData.getRating(); // 경쟁사 평점들만 따로 합산
              count++;
            }
          }
        } catch (Exception ignored) {
        }
      }

      // 3. 경쟁사 평균 계산
      double avgCompReviews = (count > 0) ? (totalCompReviews / count) : 0;
      double avgCompRating = (count > 0) ? (totalCompRating / count) : 0;

      List<String> reviewList =
          (myData.getReviewContents() != null)
              ? List.of(myData.getReviewContents().split("\\|"))
              : List.of();

      // 4. 내 데이터와 경쟁사 데이터를 각각 필드에 매핑
      return WeaknessResponseDto.builder()
          .storeName(myData.getStoreName())
          .myReviewCount(myData.getReviewCount())
          .myRating(myData.getRating()) // 내 별점
          .avgCompReviewCount(Math.round(avgCompReviews * 10.0) / 10.0)
          .avgCompRating(Math.round(avgCompRating * 100.0) / 100.0) // 경쟁사 평균 별점
          .reviewList(reviewList)
          .build();

    } catch (Exception e) {
      log.error("약점 분석 중 오류: {}", e.getMessage());
      throw new RuntimeException("분석 실패: " + e.getMessage());
    }
  }

  // 3. [O] 기회 분석
  @Transactional(readOnly = true)
  public OpportunityResponseDto analyzeOpportunity(String address) {
    try {
      String cleanedAddr = cleanAddress(address);
      RegionMaster region = getRegionByAddress(cleanedAddr);
      String trdarCd = region.getTrdarCd();

      // 1. 상권 데이터 조회
      SalesEstimate s =
          salesRepository
              .findLatestByTrdarCd(trdarCd)
              .orElseThrow(() -> new RuntimeException("해당 상권의 매출/유동인구 데이터가 없습니다."));

      if (s.getTotalSaleCnt() == null || s.getTotalSaleCnt() == 0) {
        throw new RuntimeException("분석에 필요한 데이터가 부족합니다.");
      }

      // 2. 주 이용 연령대 추출
      String mainAge = "정보 없음";
      long maxAgeCnt = 0;
      if (s.getAge10Count() != null && s.getAge10Count() > maxAgeCnt) {
        maxAgeCnt = s.getAge10Count();
        mainAge = "10대";
      }
      if (s.getAge20Count() != null && s.getAge20Count() > maxAgeCnt) {
        maxAgeCnt = s.getAge20Count();
        mainAge = "20대";
      }
      if (s.getAge30Count() != null && s.getAge30Count() > maxAgeCnt) {
        maxAgeCnt = s.getAge30Count();
        mainAge = "30대";
      }
      if (s.getAge40Count() != null && s.getAge40Count() > maxAgeCnt) {
        maxAgeCnt = s.getAge40Count();
        mainAge = "40대";
      }
      if (s.getAge50Count() != null && s.getAge50Count() > maxAgeCnt) {
        maxAgeCnt = s.getAge50Count();
        mainAge = "50대";
      }
      if (s.getAge60Count() != null && s.getAge60Count() > maxAgeCnt) {
        maxAgeCnt = s.getAge60Count();
        mainAge = "60대 이상";
      }

      // 3. 주요 성별 추출
      String mainGender = (s.getMaleCount() >= s.getFemaleCount()) ? "남성" : "여성";

      // 4. 피크 시간대 추출
      String peakTime = "점심(11-14)";
      long maxTimeCnt = s.getTime1114();
      if (s.getTime1417() != null && s.getTime1417() > maxTimeCnt) {
        maxTimeCnt = s.getTime1417();
        peakTime = "오후(14-17)";
      }
      if (s.getTime1721() != null && s.getTime1721() > maxTimeCnt) {
        maxTimeCnt = s.getTime1721();
        peakTime = "저녁(17-21)";
      }
      if (s.getTime2124() != null && s.getTime2124() > maxTimeCnt) {
        maxTimeCnt = s.getTime2124();
        peakTime = "심야(21-24)";
      }

      // 5. DTO 조립
      return OpportunityResponseDto.builder()
          .address(cleanedAddr)
          .avgDailyPop(s.getTotalSaleCnt() / 30) // 월 매출건수를 일 단위로 환산
          .mainAgeGroup(mainAge)
          .mainGender(mainGender)
          .peakTime(peakTime)
          .build();

    } catch (Exception e) {
      log.error("기회 분석 에러: {}", e.getMessage());
      throw new RuntimeException("분석 실패: " + e.getMessage());
    }
  }

  // 4. [T] 위협 분석
  @Transactional(readOnly = true)
  public ThreatResponseDto analyzeThreat(String address, String bizType, String subCategory) {
    try {
      String cleanedAddr = cleanAddress(address);
      RegionMaster region = getRegionByAddress(cleanedAddr);

      // 반경 500m 내 경쟁업체 수 조회 (DB의 category_sm 등과 매핑됨)
      int count =
          competitorRepository.countCompetitors(region.getLat(), region.getLon(), bizType, 0.5);

      // 경쟁 상태 판별
      String status;
      if (count >= 10) status = "과포화";
      else if (count >= 5) status = "경쟁 치열";
      else status = "블루오션";

      return ThreatResponseDto.builder()
          .address(cleanedAddr)
          .bizType(bizType)
          .subCategory(subCategory)
          .competitorCount(count)
          .competitionStatus(status)
          .build();

    } catch (Exception e) {
      log.error("위협 분석 실패: {}", e.getMessage());
      throw new RuntimeException("위협 분석 중 오류 발생: " + e.getMessage());
    }
  }

  // ================================================================
  // 5. [AI 전용] 요약 데이터 추출 (최종)
  // ================================================================
  // @Transactional 제거! (500 에러 해결의 핵심)
  public AnalysisSummaryDto extractAnalysisSummary(
      String address, String bizCategory, String storeName, String keyword) {

    // 1. 주소 정제
    String cleanedAddress = cleanAddress(address);

    // 2. 공통 데이터 조회
    RegionMaster region = getRegionByAddress(cleanedAddress);
    String dongName = region.getAdstrdNm();

    SalesEstimate sales =
        salesRepository
            .findLatestByTrdarCd(region.getTrdarCd())
            .orElse(SalesEstimate.builder().build());

    int compCount =
        competitorRepository.countCompetitors(region.getLat(), region.getLon(), bizCategory, 0.5);

    long avgIncome =
        incomeRepository
            .findFirstByAdstrdNmOrderByStdDateDesc(dongName)
            .map(IncomeStat::getAvgMonIncome)
            .orElse(3300000L);

    String zoneNm = mapGuToZone(region.getGuNm());
    HousingStat housing =
        housingRepository.findFirstByZoneNmOrderByStdDateDesc(zoneNm).orElse(null);

    // --- 데이터 가공 ---
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

    String mainGender = "정보 없음";
    if (sales.getMaleCount() != null && sales.getFemaleCount() != null) {
      mainGender = (sales.getMaleCount() >= sales.getFemaleCount()) ? "남성" : "여성";
    }

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
        maxRatio = housing.getStudioRatio();
        mainHousing = "원룸";
      }
    }

    String hashtags = "정보 없음";
    try {
      String searchKeyword;

      // [전략 1] 사용자가 입력한 키워드가 있으면 최우선 (예: "커피")
      if (keyword != null && !keyword.isBlank()) {
        searchKeyword = keyword;
      }
      // [전략 2] 없으면 '행정동 + 업종' 조합 (예: "성수동 카페")
      else {
        // 주소에서 '구'나 '동'만 깔끔하게 추출
        // 예: "서울 성동구 성수동1가 656-442" -> "성동구 성수동1가"
        String simpleAddr = extractDongFromAddress(cleanedAddress);
        searchKeyword = simpleAddr + " " + bizCategory;
      }

      log.info("SNS API 요청 키워드(1차): {}", searchKeyword);
      List<HashtagDto> tags = regionTrendService.recommendHashtags(searchKeyword);

      // [전략 3] 1차 검색 결과가 없으면? -> 업종명(예: "카페")만으로 재검색 (범위를 넓힘)
      if (tags == null || tags.isEmpty()) {
        log.info("1차 검색 결과 없음. 업종명('{}')으로 재검색 시도...", bizCategory);
        tags = regionTrendService.recommendHashtags(bizCategory);
      }

      // 결과 매핑 (상위 5개)
      if (tags != null && !tags.isEmpty()) {
        hashtags =
            tags.stream().limit(5).map(HashtagDto::getHashtag).collect(Collectors.joining(", "));
      }
      // 끝까지 없으면 그냥 빈칸 ("정보 없음") 유지. 가짜 데이터 생성 X.

    } catch (Exception e) {
      log.warn("SNS 트렌드 조회 중 API 오류 발생: {}", e.getMessage());
      // 오류 나도 전체 로직 멈추지 않음. hashtags는 "정보 없음" 상태 유지.
    }

    // 리뷰 데이터 (크롤링)
    int myReviewCount = 0;
    double avgCompReviewCount = 0.0;
    double myRating = 0.0;
    String myReviewContents = "";

    if (storeName != null && !storeName.isEmpty()) {
      try {
        // 내 가게 크롤링
        String myPlaceId = reviewCrawlerService.findPlaceId(cleanedAddress, storeName);
        if (myPlaceId != null) {
          StoreCrawlingData myData =
              reviewCrawlerService.getStoreCrawlingData(myPlaceId, storeName);
          myReviewCount = myData.getReviewCount();
          myRating = myData.getRating();
          myReviewContents = myData.getReviewContents();
        }

        // 경쟁사 (3개 제한)
        List<CompetitorStore> competitors =
            competitorRepository.findNearbyCompetitors(
                region.getLat(), region.getLon(), bizCategory, 0.5);
        double totalCompReviews = 0;
        int count = 0;

        for (CompetitorStore comp : competitors) {
          // [속도 개선] 3개 채워지면 중단
          if (count >= 3) break;

          if (comp.getStoreNm().contains(storeName) || storeName.contains(comp.getStoreNm()))
            continue;

          try {
            String compCleanAddr = cleanAddress(comp.getAddress());
            String compId = reviewCrawlerService.findPlaceId(compCleanAddr, comp.getStoreNm());
            if (compId != null) {
              StoreCrawlingData cData =
                  reviewCrawlerService.getStoreCrawlingData(compId, comp.getStoreNm());
              totalCompReviews += cData.getReviewCount();
              count++;
            }
          } catch (Exception ignored) {
          }
        }
        avgCompReviewCount = (count > 0) ? (totalCompReviews / count) : 0.0;
      } catch (Exception ignored) {
      }
    }

    AnalysisSummary summaryEntity =
        AnalysisSummary.builder()
            .regionMaster(region)
            .stdDate(java.time.LocalDate.now()) // 분석 기준일
            // [O: 기회]
            .mainAgeGroup(mainAge)
            .mainGender(mainGender)
            .avgDailyPop(avgDailyPop)
            .peakTime(peakTime)
            // [T: 위협]
            .competitorCount((long) compCount)
            .competitionLevel(compLevel)
            // [S: 강점]
            .avgMonIncome(avgIncome)
            // [Trend]
            .housingType(mainHousing)
            .topHashtags(hashtags)
            // [W: 약점]
            .myReviewCount(myReviewCount)
            .myRating(myRating)
            .avgCompReviewCount(avgCompReviewCount)
            .myReviewContents(myReviewContents)
            .build();

    //  2. DB에 실제 저장
    analysisSummaryRepository.save(summaryEntity);
    log.info("분석 요약 데이터가 tb_analysis_summary에 저장되었습니다. ID: {}", summaryEntity.getId());

    // 3. 기존대로 DTO 반환
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
        .avgCompReviewCount(avgCompReviewCount)
        .myRating(myRating)
        .myReviewContents(myReviewContents)
        .build();
  }
}
