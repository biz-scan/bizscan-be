package com.umc9th.bizscan.domain.aiVector.service;

import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlan;
import com.umc9th.bizscan.domain.aiAnalysis.entity.Analysis;
import com.umc9th.bizscan.domain.aiAnalysis.repository.AnalysisRepository;
import com.umc9th.bizscan.domain.aiVector.client.AiVectorClient;
import com.umc9th.bizscan.domain.aiVector.dto.RecommendationResponseDto;
import com.umc9th.bizscan.domain.aiVector.dto.StoreSwotIngestRequestDto;
import com.umc9th.bizscan.domain.aiVector.dto.VectorRecommendationDto;
import com.umc9th.bizscan.domain.store.entity.Store;
import com.umc9th.bizscan.domain.store.entity.StoreTag;
import com.umc9th.bizscan.domain.store.repository.StoreRepository;
import com.umc9th.bizscan.domain.store.repository.StoreTagRepository;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.exception.GeneralException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiVectorService {

  private final AiVectorClient aiVectorClient;
  private final StoreRepository storeRepository;
  private final StoreTagRepository storeTagRepository;
  private final AnalysisRepository analysisRepository;

  private static final double WEIGHT_VECTOR = 50.0;
  private static final double WEIGHT_CATEGORY = 20.0;
  private static final double WEIGHT_PAIN_POINT = 20.0;
  private static final double WEIGHT_TARGET = 10.0;

  @Transactional(readOnly = true)
  public List<RecommendationResponseDto> recommendSimilarStores(Long currentStoreId) {

    // 1. 내 가게 정보 조회
    Store currentStore =
        storeRepository
            .findById(currentStoreId)
            .orElseThrow(() -> new GeneralException(ErrorCode.STORE_NOT_FOUND));

    // 2. Python 검색 쿼리 생성
    List<StoreTag> myTags = storeTagRepository.findAllByStoreFetchTag(currentStore);
    String tagString =
        myTags.stream().map(st -> "#" + st.getTag().getName()).collect(Collectors.joining(" "));
    String queryText =
        String.format(
            "%s %s %s", currentStore.getCategory(), currentStore.getPainPoint(), tagString);

    // 3. Python 호출 (Vector DB 검색)
    List<VectorRecommendationDto> candidates =
        aiVectorClient.getSimilarStores(currentStoreId, queryText);

    List<RecommendationResponseDto> finalResults = new ArrayList<>();

    // 4. 정밀 채점 및 데이터 가공
    for (VectorRecommendationDto candidate : candidates) {
      if (candidate.getStoreId() == null || candidate.getStoreId().equals(currentStoreId)) continue;

      Optional<Store> targetOpt = storeRepository.findById(candidate.getStoreId());
      if (targetOpt.isEmpty()) continue;
      Store targetStore = targetOpt.get();

      // --- 점수 및 해시태그 계산 ---
      double totalScore = candidate.getScore() * WEIGHT_VECTOR;
      List<String> hashTags = new ArrayList<>();

      // #SWOT 분석 유사
      hashTags.add("#SWOT 분석 유사");

      // #업종 유사
      if (currentStore.getCategory() == targetStore.getCategory()) {
        totalScore += WEIGHT_CATEGORY;
        hashTags.add("#업종 유사");
      }
      // #고민 유사
      if (currentStore.getPainPoint() == targetStore.getPainPoint()) {
        totalScore += WEIGHT_PAIN_POINT;
        hashTags.add("#고민 유사");
      }
      // #타겟 유사
      if (currentStore.getTarget() == targetStore.getTarget()) {
        totalScore += WEIGHT_TARGET;
        hashTags.add("#타겟 고객 유사");
      }

      // 태그가 3개 미만이면 거리 태그 추가
      if (hashTags.size() < 3) {
        double dist =
            calculateDistance(
                currentStore.getLat(),
                currentStore.getLon(),
                targetStore.getLat(),
                targetStore.getLon());
        if (dist <= 3.0) hashTags.add("#가까운 거리");
      }

      // 최대 3개까지만 유지
      if (hashTags.size() > 3) hashTags = hashTags.subList(0, 3);

      // 점수 % 변환 (최대 99%로 제한)
      int percentScore = (int) Math.min(99, Math.round(totalScore));

      // 제목 생성
      String title =
          makeStoreTitle(targetStore.getAddress(), targetStore.getCategoryDetail().getKorean());

      finalResults.add(
          RecommendationResponseDto.builder()
              .rank(0) // 추후 설정
              .storeId(targetStore.getId()) // 상세 페이지 이동용
              .storeTitle(title) // 2. 제목
              .similarityPercent(percentScore) // 3. 유사도 %
              .hashTags(hashTags) // 4. 해시태그 리스트
              .catchphrase(candidate.getCatchphrase()) // 5. 캐치프레이즈
              .build());
    }

    // 5. 정렬 및 상위 3개 선정
    List<RecommendationResponseDto> sortedResults =
        finalResults.stream()
            .sorted(
                Comparator.comparingInt(RecommendationResponseDto::getSimilarityPercent).reversed())
            .limit(4)
            .collect(Collectors.toList());

    // 6. 상세 정보
    int rank = 1;
    for (RecommendationResponseDto dto : sortedResults) {
      dto.setRank(rank++); // 1. 순위

      Optional<Analysis> analysisOpt =
          analysisRepository.findByStoreIdWithActionPlan(dto.getStoreId());

      if (analysisOpt.isPresent()) {
        Analysis analysis = analysisOpt.get();

        // 실행 전략 한 줄 요약
        List<ActionPlan> plans = analysis.getActionPlans();
        if (plans != null && !plans.isEmpty()) {
          dto.setActionPlanSummary(plans.get(0).getTitle());
        } else {
          dto.setActionPlanSummary("준비된 실행 전략이 없습니다.");
        }
      } else {
        dto.setActionPlanSummary("분석 결과 대기 중");
      }
    }

    return sortedResults;
  }

  // 주소에서 "동" 추출해서 제목 만들기 (예: "성수동 베이커리")
  private String makeStoreTitle(String address, String categoryDetailKorean) {
    String region = "서울";
    if (address != null) {
      String[] parts = address.split(" ");
      if (parts.length >= 2) {
        region = parts[1]; // "구" 또는 "동"
      }
    }
    return region + " " + categoryDetailKorean;
  }

  // 거리 계산 함수
  private double calculateDistance(
      BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
    if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return 9999.0;

    double R = 6371; // 지구 반지름 (km)
    double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
    double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1.doubleValue()))
                * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);

    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  @Transactional(readOnly = true)
  public int migrateAllStoresToVectorDb() {
    List<Store> allStores = storeRepository.findAll();
    int successCount = 0;
    int totalCount = allStores.size();

    log.info(">>> 데이터 마이그레이션 시작. 총 대상 가게 수: {}", totalCount);

    for (Store store : allStores) {
      try {
        // 1. 해당 가게의 최신 분석 결과 조회 (SWOT Fetch Join)
        Optional<Analysis> analysisOpt =
            analysisRepository.findTopByStoreIdOrderByCreatedAtDesc(store.getId());

        if (analysisOpt.isPresent()) {
          Analysis analysis = analysisOpt.get();

          // 2. FastAPI 전송용 DTO 생성
          StoreSwotIngestRequestDto requestDto = createIngestDto(store, analysis);

          // 3. FastAPI 호출 (Ingest)
          aiVectorClient.ingestSwotData(requestDto);

          successCount++;
          if (successCount % 10 == 0) {
            log.info(">>> 진행 중: {}/{} 완료", successCount, totalCount);
          }
        } else {
          log.debug(">>> Skipping Store ID: {} (분석 데이터 없음)", store.getId());
        }
      } catch (Exception e) {
        log.error(">>> 마이그레이션 실패 - Store ID: {}", store.getId(), e);
      }
    }

    log.info(">>> 마이그레이션 완료. 성공: {}/{}", successCount, totalCount);
    return successCount;
  }

  // DTO 변환 헬퍼 메서드
  private StoreSwotIngestRequestDto createIngestDto(Store store, Analysis analysis) {

    List<StoreSwotIngestRequestDto.SwotItemDto> items = new ArrayList<>();

    if (analysis.getSwots() != null) {
      items =
          analysis.getSwots().stream()
              .map(
                  swot ->
                      StoreSwotIngestRequestDto.SwotItemDto.builder()
                          .type(swot.getType() != null ? swot.getType().toString() : "")
                          .keyword(swot.getKeyword())
                          .description(swot.getDescription())
                          .diagnosis(swot.getDiagnosis())
                          .rawText(
                              String.format(
                                  "[%s] %s: %s",
                                  swot.getType(), swot.getKeyword(), swot.getDescription()))
                          .build())
              .collect(Collectors.toList());
    }

    return StoreSwotIngestRequestDto.builder()
        .storeId(store.getId())
        .catchphrase(analysis.getCatchphrase() != null ? analysis.getCatchphrase() : "캐치프레이즈 없음")
        .items(items)
        .build();
  }

  public String checkStoreData(Long storeId) {
    return aiVectorClient.checkStoreDataInVectorDb(storeId);
  }
}
