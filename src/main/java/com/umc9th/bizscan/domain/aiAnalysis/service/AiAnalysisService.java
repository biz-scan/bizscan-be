package com.umc9th.bizscan.domain.aiAnalysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc9th.bizscan.domain.aiAnalysis.dto.request.FastApiAnalysisRequest;
import com.umc9th.bizscan.domain.aiAnalysis.dto.response.*;
import com.umc9th.bizscan.domain.aiAnalysis.entity.*;
import com.umc9th.bizscan.domain.aiAnalysis.enums.AnalysisStatus;
import com.umc9th.bizscan.domain.aiAnalysis.repository.ActionPlanRepository;
import com.umc9th.bizscan.domain.aiAnalysis.repository.AnalysisRepository;
import com.umc9th.bizscan.domain.aiAnalysis.repository.AnalysisRequestRepository;
import com.umc9th.bizscan.domain.aiAnalysis.repository.SwotRepository;
import com.umc9th.bizscan.domain.store.entity.Store;
import com.umc9th.bizscan.domain.store.entity.StoreTag;
import com.umc9th.bizscan.domain.store.repository.StoreRepository;
import com.umc9th.bizscan.domain.store.repository.StoreTagRepository;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.exception.GeneralException;
import com.umc9th.bizscan.global.config.FastApiProperties;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiAnalysisService {

  private final SwotRepository swotRepository;
  private final StoreRepository storeRepository;
  private final StoreTagRepository storeTagRepository;
  private final ActionPlanRepository actionPlanRepository;
  private final AnalysisRequestRepository analysisRequestRepository;
  private final AnalysisRepository analysisRepository;
  private final ObjectMapper objectMapper;

  private final FastApiProperties fastApiProperties;
  private final RestTemplate restTemplate = new RestTemplate();

  /** AI 분석 요청 (프론트에서 최초 1회 호출) requestId 반환 → 프론트에서 폴링 */
  @Transactional
  public AnalysisRequestResponse analyzeStore(Long storeId) {

    // 0. 매장 조회 (Spring에서만 DB 접근)
    Store store =
        storeRepository
            .findById(storeId)
            .orElseThrow(() -> new GeneralException(ErrorCode.STORE_NOT_FOUND));

    List<StoreTag> storeTags = storeTagRepository.findAllByStoreFetchTag(store);

    // 1. requestId 생성
    String requestId = UUID.randomUUID().toString();

    // 추가: 분석 Entity 저장
    Analysis analysis = Analysis.builder().store(store).build();

    analysisRepository.save(analysis);

    // 2. 분석 요청 상태 저장
    AnalysisRequest request =
        AnalysisRequest.builder()
            .requestId(requestId)
            .analysis(analysis) // 추가: store -> analysis 변경
            .status(AnalysisStatus.REQUEST)
            .progressMessage("AI 분석 요청 중입니다.")
            .build();

    analysisRequestRepository.save(request);

    // 3. FastAPI 요청 DTO 생성
    FastApiAnalysisRequest fastApiRequest = toFastApiRequest(store, storeTags, requestId);

    // 4. FastAPI 호출 (응답 기다리지 않음)
    try {
      restTemplate.postForObject(
          fastApiProperties.getBaseUrl() + fastApiProperties.getAnalysisPath(),
          fastApiRequest,
          Void.class);
    } catch (Exception e) {
      request.fail("AI 분석 요청 실패");
      throw e;
    }

    // 5. 즉시 requestId 반환
    return new AnalysisRequestResponse(requestId);
  }

  /** 분석 상태 조회 (폴링 API) */
  public AnalysisStatusResponse getAnalysisStatus(String requestId) {
    AnalysisRequest request =
        analysisRequestRepository
            .findByRequestId(requestId)
            .orElseThrow(() -> new GeneralException(ErrorCode.ANALYSIS_REQUEST_NOT_FOUND));

    int poolingTime = calculatePoolingTime(request.getStatus());
    return new AnalysisStatusResponse(
        request.getStatus(), request.getProgressMessage(), poolingTime);
  }

  /** ai 캐치프레이즈 badge */
  public CatchphraseResponse getLatestCatchphrase(Long storeId) {
    Analysis analysis =
        analysisRepository
            .findByStoreId(storeId)
            .orElseThrow(() -> new GeneralException(ErrorCode.ANALYSIS_NOT_FOUND));

    return new CatchphraseResponse(analysis.getCatchphrase());
  }

  public List<AnalysisResDTO.SwotDTO> getSwots(Long storeId) {
    Analysis analysis =
        analysisRepository
            .findByStoreIdWithSwot(storeId)
            .orElseThrow(() -> new GeneralException(ErrorCode.ANALYSIS_NOT_FOUND));

    return analysis.getSwots().stream().map(AnalysisResDTO.SwotDTO::of).toList();
  }

  public DiagnosisResponse getDiagnosis(Long swotId) {
    Swot swot =
        swotRepository
            .findById(swotId)
            .orElseThrow(() -> new GeneralException(ErrorCode.SWOT_NOT_FOUND));

    return new DiagnosisResponse(swot.getDiagnosis());
  }

  public List<AnalysisResDTO.ActionPlanDTO> getActionPlans(Long storeId) {
    Analysis analysis =
        analysisRepository
            .findByStoreIdWithActionPlan(storeId)
            .orElseThrow(() -> new GeneralException(ErrorCode.ANALYSIS_NOT_FOUND));

    return analysis.getActionPlans().stream().map(AnalysisResDTO.ActionPlanDTO::of).toList();
  }

  public AnalysisResDTO.ActionPlanDetailDTO getActionPlanDetail(Long actionPlanId) {
    // N+1 및 MultipleBagFetchException 방지용 FetchJoin 쿼리 2번 (또는 BatchSize 사용해야함)
    // ActionPlan + Tag (영속성 컨텍스트에 저장)
    ActionPlan actionPlan =
        actionPlanRepository
            .findByIdWithTags(actionPlanId)
            .orElseThrow(() -> new GeneralException(ErrorCode.ACTION_PLAN_NOT_FOUND));

    // + ActionDetail (Hibernate가 1차 캐시에 있는 기존 actionPlan 객체에 details 리스트를 채움)
    actionPlanRepository.findByIdWithDetails(actionPlanId);

    List<AnalysisResDTO.ActionDetailDTO> details =
        actionPlan.getDetails().stream().map(AnalysisResDTO.ActionDetailDTO::of).toList();

    return AnalysisResDTO.ActionPlanDetailDTO.of(actionPlan, details);
  }

  // Utils

  /** pollingTime 계산 로직 */
  private int calculatePoolingTime(AnalysisStatus status) {
    return switch (status) {
      case REQUEST -> 30000; // 초반
      case SWOT_PROCESSING -> 10000; // 중반
      case ACTION_PLAN_PROCESSING -> 10000; // 중반
      case ACTION_DETAIL_PROCESSING -> 2000; // 후반
      case COMPLETED, FAILED -> 0; // 폴링 종료
    };
  }

  private FastApiAnalysisRequest toFastApiRequest(
      Store store, List<StoreTag> storeTags, String requestId) {
    // 1. Tag 엔티티 리스트를 FastAPI용 TagInfoRequest 리스트로 변환
    List<FastApiAnalysisRequest.TagInfoRequest> tagInfos =
        storeTags.stream()
            .map(
                st ->
                    FastApiAnalysisRequest.TagInfoRequest.builder()
                        .type(st.getTag().getType().getKorean()) // "분위기"
                        .name(st.getTag().getName().getKorean()) // "#뷰맛집"
                        .build())
            .toList();

    return FastApiAnalysisRequest.builder()
        // callback 식별용
        .requestId(requestId)
        .swotCallbackUrl(fastApiProperties.getBaseUrl() + "/api/analysis/callback/swots")
        .actionPlanCallbackUrl(
            fastApiProperties.getBaseUrl() + "/api/analysis/callback/action-plans")
        .actionDetailCallbackUrl(
            fastApiProperties.getBaseUrl() + "/api/analysis/callback/action-details")

        // store 기본 정보
        .storeId(store.getId())
        .name(store.getName())
        .address(store.getAddress())

        // Enum → 한글 변환
        .category(store.getCategory().getKorean())
        .categoryDetail(store.getCategoryDetail().getKorean())
        .price(store.getPrice().getKorean())
        .target(store.getTarget().getKorean())
        .painPoint(store.getPainPoint().getKorean())

        // 기타
        .signature(store.getSignature())
        .tags(tagInfos)
        .build();
  }
}
