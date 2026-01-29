package com.umc9th.bizscan.domain.analysis.service;

import com.umc9th.bizscan.domain.analysis.dto.AnalysisSummaryDto;
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
import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import com.umc9th.bizscan.domain.region.repository.RegionRepository;
import com.umc9th.bizscan.domain.region.service.RegionTrendService;
import com.umc9th.bizscan.domain.store.dto.WeaknessResponseDto;
import com.umc9th.bizscan.domain.store.entity.StoreCrawlingData;
import com.umc9th.bizscan.domain.store.repository.StoreCrawlingDataRepository;
import com.umc9th.bizscan.domain.store.service.ReviewCrawlerService;
import com.umc9th.bizscan.global.client.kakao.KakaoClient;
import com.umc9th.bizscan.global.client.kakao.dto.KakaoApiResponse;
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
  private final KakaoClient kakaoClient;
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
    KakaoApiResponse response = kakaoClient.searchAddress(address);

    if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
      throw new IllegalArgumentException("입력하신 주소를 찾을 수 없습니다.");
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
                    .orElseThrow(() -> new IllegalArgumentException("기본 상권 데이터를 찾을 수 없습니다.")));
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
      log.info("약점 분석 시작: 가게명={}, 주소={}", storeName, cleanedAddress);

      // 1. 내 가게 ID 찾기
      String myPlaceId = reviewCrawlerService.findPlaceId(cleanedAddress, storeName);
      if (myPlaceId == null || myPlaceId.isEmpty()) {
        throw new RuntimeException("내 가게의 위치 정보를 찾을 수 없습니다."); // 메시지 구체화
      }

      // 2. 내 가게 크롤링
      StoreCrawlingData myData = reviewCrawlerService.getStoreCrawlingData(myPlaceId, storeName);

      // 내 데이터가 제대로 왔는지 확인
      log.info("내 가게 크롤링 결과 - 리뷰: {}, 별점: {}", myData.getReviewCount(), myData.getRating());

      // 3. 주변 경쟁사 데이터 수집
      RegionMaster region = getRegionByAddress(cleanedAddress);

      String searchKeyword = "음식";
      if (storeName != null) {
        String nameUpper = storeName.toUpperCase(); // 대소문자 무시
        if (nameUpper.contains("카페")
            || nameUpper.contains("커피")
            || nameUpper.contains("COFFEE")
            || nameUpper.contains("베이커리")
            || nameUpper.contains("빵")
            || nameUpper.contains("디저트")) {
          searchKeyword = "카페";
        }
      }
      log.info("경쟁사 검색 키워드 결정: '{}' (원본 이름: {})", searchKeyword, storeName);

      List<CompetitorStore> competitors =
          competitorRepository.findNearbyCompetitors(
              region.getLat(), region.getLon(), searchKeyword, 0.5);

      log.info("DB에서 조회된 경쟁사 수: {}개", competitors.size());

      double totalCompReviews = 0;
      double totalCompRatingSum = 0; // 변수명 명확하게 변경
      int count = 0;

      for (CompetitorStore comp : competitors) {
        if (count >= 3) break;

        // 내 가게와 이름이 비슷하면 제외
        if (comp.getStoreNm().contains(storeName) || storeName.contains(comp.getStoreNm()))
          continue;

        try {
          String compCleanAddr = cleanAddress(comp.getAddress());
          String compSimpleName = comp.getStoreNm().split(" ")[0]; // "스타벅스 성수점" -> "스타벅스"

          String compId = reviewCrawlerService.findPlaceId(compCleanAddr, compSimpleName);
          if (compId != null) {
            StoreCrawlingData cData =
                reviewCrawlerService.getStoreCrawlingData(compId, comp.getStoreNm());

            if (cData.getReviewCount() > 0) {
              totalCompReviews += cData.getReviewCount();
              totalCompRatingSum += cData.getRating();
              count++;
              log.info(
                  " - 경쟁사({}) 확보: 리뷰 {}, 별점 {}",
                  comp.getStoreNm(),
                  cData.getReviewCount(),
                  cData.getRating());
            }
          } else {
            log.warn(" - 경쟁사({}) Place ID 찾기 실패", comp.getStoreNm());
          }
        } catch (Exception e) {
          log.warn(" - 경쟁사({}) 크롤링 중 에러: {}", comp.getStoreNm(), e.getMessage());
        }
      }

      // 4. 경쟁사 평균 계산
      double avgCompReviews = (count > 0) ? (totalCompReviews / count) : 0;
      double avgCompRating = (count > 0) ? (totalCompRatingSum / count) : 0;

      log.info("최종 경쟁사 평균: 리뷰 {:.1f}, 별점 {:.1f} (표본 {}개)", avgCompReviews, avgCompRating, count);

      List<String> reviewList =
          (myData.getReviewContents() != null && !myData.getReviewContents().isEmpty())
              ? List.of(myData.getReviewContents().split("\\|"))
              : List.of();

      return WeaknessResponseDto.builder()
          .storeName(myData.getStoreName())
          .myReviewCount(myData.getReviewCount())
          .myRating(myData.getRating())
          .avgCompReviewCount(Math.round(avgCompReviews * 10.0) / 10.0)
          .avgCompRating(Math.round(avgCompRating * 100.0) / 100.0)
          .reviewList(reviewList)
          .build();

    } catch (Exception e) {
      log.error("약점 분석 중 치명적 오류: ", e); // 스택트레이스 전체 출력
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

      String searchKeyword = "";

      // 1. 소분류가 있으면 우선적으로 키워드 할당
      if (subCategory != null && !subCategory.isEmpty()) {
        String sub = subCategory.replace(" ", "");

        if (sub.contains("호프") || sub.contains("맥주")) {
          searchKeyword = "호프";
        } else if (sub.contains("치킨") || sub.contains("통닭")) {
          searchKeyword = "치킨";
        } else if (sub.contains("이자카야") || sub.contains("꼬치") || sub.contains("선술집")) {
          searchKeyword = "일식";
        } else if (sub.contains("와인") || sub.contains("바") || sub.contains("칵테일")) {
          searchKeyword = "칵테일";
        } else if (sub.contains("포차") || sub.contains("요리주점")) {
          searchKeyword = "포차";
        }

        // [식당]
        else if (sub.contains("고기") || sub.contains("구이")) {
          searchKeyword = "육류";
        } else if (sub.contains("한식") || sub.contains("백반")) {
          searchKeyword = "한식";
        } else if (sub.contains("양식") || sub.contains("파스타")) {
          searchKeyword = "양식";
        }
      }

      // 2. 소분류에서 못 잡았으면 대분류로 포괄적 매핑
      if (searchKeyword.isEmpty() && bizType != null) {
        String upperCat = bizType.toUpperCase();
        if (upperCat.contains("CAFE") || upperCat.contains("BAKERY") || upperCat.contains("카페")) {
          searchKeyword = "카페";
        } else if (upperCat.contains("RESTAURANT") || upperCat.contains("식당")) {
          searchKeyword = "음식";
        } else if (upperCat.contains("BAR")
            || upperCat.contains("PUB")
            || upperCat.contains("술집")
            || upperCat.contains("주점")) {
          searchKeyword = "호프";
        }
      }

      if (searchKeyword.isEmpty()) searchKeyword = "음식";

      log.info("위협 분석 키워드 변환: '{}'/'{}' -> '{}'", bizType, subCategory, searchKeyword);
      int count =
          competitorRepository.countCompetitors(
              region.getLat(), region.getLon(), searchKeyword, 0.5);

      // 경쟁 상태 판별
      String status;
      if (count >= 15) status = "과포화";
      else if (count >= 10) status = "경쟁 치열";
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
  // 5. [AI 전용] 요약 데이터 추출
  // ================================================================
  public AnalysisSummaryDto extractAnalysisSummary(
      String address, String bizCategory, String storeName, String keyword) {

    // 1. 주소 정제
    String cleanedAddress = cleanAddress(address);

    // 2. 공통 데이터 조회
    RegionMaster region = getRegionByAddress(cleanedAddress);
    String dongName = region.getAdstrdNm();

    // 카테고리 매핑 (Enum -> 한글 키워드)
    String searchKeyword = "음식";
    if (bizCategory != null) {
      String upperCat = bizCategory.toUpperCase();
      if (upperCat.contains("CAFE") || upperCat.contains("BAKERY") || upperCat.contains("카페")) {
        searchKeyword = "카페";
      } else if (upperCat.contains("RESTAURANT") || upperCat.contains("식당")) {
        searchKeyword = "음식";
      } else if (upperCat.contains("BAR")
          || upperCat.contains("PUB")
          || upperCat.contains("술집")
          || upperCat.contains("주점")) {
        searchKeyword = "술집";
      }
    }
    log.info("실시간 분석 키워드: '{}' -> '{}'", bizCategory, searchKeyword);

    // 상권 데이터 조회 (없으면 빈 객체)
    SalesEstimate sales =
        salesRepository
            .findLatestByTrdarCd(region.getTrdarCd())
            .orElse(SalesEstimate.builder().build());

    // 경쟁 업체 수 실시간 카운트
    int compCount =
        competitorRepository.countCompetitors(region.getLat(), region.getLon(), searchKeyword, 0.5);

    // 소득 데이터
    long avgIncome =
        incomeRepository
            .findFirstByAdstrdNmOrderByStdDateDesc(dongName)
            .map(IncomeStat::getAvgMonIncome)
            .orElse(3300000L);

    // 주거 형태
    String zoneNm = mapGuToZone(region.getGuNm());
    HousingStat housing =
        housingRepository.findFirstByZoneNmOrderByStdDateDesc(zoneNm).orElse(null);

    // --- 데이터 가공 (연령/성별 등) ---
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
      // 1. "1가", "2동" 같은 군더더기 제거 ("성수동1가" -> "성수동")
      String simpleDong = dongName.replaceAll("[0-9]+(가|동|로)$", "").trim();

      if (simpleDong.isEmpty()) simpleDong = dongName;

      // 2. 가운데 공백(" ") 없이 바로 이어 붙임 (+)
      String derivedKeyword = simpleDong + searchKeyword;

      // 우선순위: 사용자 입력 > 가공된 키워드(성수동카페)
      String snsKeyword = (keyword != null && !keyword.isBlank()) ? keyword : derivedKeyword;

      log.info("SNS 키워드 결정: 사용자입력='{}', 자동생성='{}' (원래동: {})", keyword, derivedKeyword, dongName);

      // 1차 검색 시도
      List<HashtagDto> tags = regionTrendService.recommendHashtags(snsKeyword);

      // 2차 검색 시도 (실패 시 업종명만, 예: "카페")
      if (tags == null || tags.isEmpty()) {
        log.info("1차 실패. 업종명('{}')으로 재시도", searchKeyword);
        tags = regionTrendService.recommendHashtags(searchKeyword);
      }

      // 3차 검색 시도 (실패 시 동이름만, 예: "성수동")
      if (tags == null || tags.isEmpty()) {
        log.info("2차 실패. 지역명('{}')으로 재시도", simpleDong);
        tags = regionTrendService.recommendHashtags(simpleDong);
      }

      if (tags != null && !tags.isEmpty()) {
        hashtags =
            tags.stream().limit(5).map(HashtagDto::getHashtag).collect(Collectors.joining(", "));
        log.info("해시태그 확보: {}", hashtags);
      } else {
        hashtags = String.format("#%s%s, #%s맛집, #데이트, #핫플", simpleDong, searchKeyword, simpleDong);
      }

    } catch (Exception e) {
      log.warn("SNS 트렌드 조회 실패: {}", e.getMessage());
      String simpleDong = dongName.replaceAll("[0-9]+(가|동|로)$", "");
      hashtags = String.format("#%s%s, #%s핫플", simpleDong, searchKeyword, simpleDong);
    }

    // ================================================================
    // 리뷰 데이터 크롤링
    // ================================================================
    int myReviewCount = 0;
    double avgCompReviewCount = 0.0;
    double myRating = 0.0;
    String myReviewContents = "";

    if (storeName != null && !storeName.isEmpty()) {
      try {
        // 1. 내 가게 정보
        String myPlaceId = reviewCrawlerService.findPlaceId(cleanedAddress, storeName);
        if (myPlaceId != null) {
          StoreCrawlingData myData =
              reviewCrawlerService.getStoreCrawlingData(myPlaceId, storeName);
          myReviewCount = myData.getReviewCount();
          myRating = myData.getRating();
          myReviewContents = myData.getReviewContents();
        }

        // 2. 경쟁사 정보 (실시간 주변 검색)
        List<CompetitorStore> competitors =
            competitorRepository.findNearbyCompetitors(
                region.getLat(), region.getLon(), searchKeyword, 0.5);

        log.info("'{}' 주변 경쟁사(실시간) {}개 발견", cleanedAddress, competitors.size());

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

        // 평균 계산 (누적 없이 이번 요청에 대한 평균만 계산)
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
        .avgCompReviewCount(avgCompReviewCount)
        .myRating(myRating)
        .myReviewContents(myReviewContents)
        .build();
  }
}
