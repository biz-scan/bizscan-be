package com.umc9th.bizscan.domain.aiAnalysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc9th.bizscan.domain.aiAnalysis.dto.request.AnalysisReqDTO;
import com.umc9th.bizscan.domain.aiAnalysis.dto.request.FastApiAnalysisRequest;
import com.umc9th.bizscan.domain.aiAnalysis.dto.response.*;
import com.umc9th.bizscan.domain.aiAnalysis.entity.*;
import com.umc9th.bizscan.domain.aiAnalysis.enums.AnalysisStatus;
import com.umc9th.bizscan.domain.aiAnalysis.enums.RelatedSwotType;
import com.umc9th.bizscan.domain.aiAnalysis.enums.SwotType;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

  private final Environment env;

  @Value("${ai.callback.base-url}")
  private String callbackBaseUrl;

  /** AI 분석 요청 (프론트에서 최초 1회 호출) requestId 반환 → 프론트에서 폴링 */
  @Transactional
  public AnalysisRequestResponse analyzeStore(AnalysisReqDTO.AiAnalysisDTO dto, String email) {
    Long storeId = dto.storeId();
    Boolean retry = dto.retry();

    // 0. 매장 조회 (Spring에서만 DB 접근)
    Store store =
        storeRepository
            .findById(storeId)
            .orElseThrow(() -> new GeneralException(ErrorCode.STORE_NOT_FOUND));
    // 매장의 소유자 이메일과 현재 요청자의 이메일을 비교 (Store 엔티티에 Member 연관관계가 있다고 가정)
    if (!store.getMember().getEmail().equals(email)) {
      // 본인 매장이 아닌 경우 권한 에러 발생
      throw new GeneralException(ErrorCode.ANALYSIS_FORBIDDEN);
    }

    // 재시도 로직: 기존 분석 이력 체크 및 실패 시 삭제
    analysisRepository
        .findByStore(store)
        .ifPresent(
            existingAnalysis -> {
              // 해당 분석의 요청 상태 확인
              AnalysisRequest existingRequest =
                  analysisRequestRepository.findByAnalysis(existingAnalysis).orElse(null);

              if (existingRequest != null) {
                // 상태가 FAILED인 경우 || (완료 AND retry=True) 삭제 후 재시도
                if (existingRequest.getStatus() == AnalysisStatus.FAILED
                    || (existingRequest.getStatus() == AnalysisStatus.COMPLETED && retry)) {
                  // @OnDelete를 활용한 Bulk삭제
                  // JPA의 delete()는 @OnDelete와 상관없이 엔티티를 하나씩 조회 후 삭제하므로 N+1 발생
                  // @Query로 DB에 Delete 쿼리를 직접 날려 DB 레벨에서 단일 쿼리로 연쇄 삭제를 수행함
                  analysisRepository.deleteByIdBulk(existingAnalysis.getId());

                  store.deleteAnalysis();
                } else if (existingRequest.getStatus() == AnalysisStatus.COMPLETED) {
                  // 이미 분석이 완료된 경우
                  throw new GeneralException(ErrorCode.ANALYSIS_ALREADY_IN_COMPLETED);
                } else {
                  // 이미 분석이 진행 중인 경우
                  throw new GeneralException(ErrorCode.ANALYSIS_ALREADY_IN_PROGRESS);
                }
              }
            });

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
            .progressMessage(AnalysisStatus.REQUEST.getProgressMessage())
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
      request.updateStatus(AnalysisStatus.FAILED);
      throw new GeneralException(ErrorCode.ANALYSIS_SERVER_ERROR);
    }

    // 5. 즉시 requestId 반환
    return new AnalysisRequestResponse(requestId);
  }

  /** 분석 상태 조회 (폴링 API) */
  @Transactional
  public AnalysisStatusResponse getAnalysisStatus(String requestId) {
    AnalysisRequest request =
        analysisRequestRepository
            .findByRequestId(requestId)
            .orElseThrow(() -> new GeneralException(ErrorCode.ANALYSIS_REQUEST_NOT_FOUND));

    // 1. 진행 중 상태인지 확인
    boolean isInProgress =
        request.getStatus() != AnalysisStatus.COMPLETED
            && request.getStatus() != AnalysisStatus.FAILED;

    if (isInProgress) {
      // 2. 생성 시간 + 5분 초과 여부 확인
      LocalDateTime createdAt = request.getCreatedAt();
      LocalDateTime now = LocalDateTime.now();

      // 3. 5분 초과 시 FAILED로 강제 변경
      if (createdAt.plusMinutes(5).isBefore(now)) {
        request.updateStatus(AnalysisStatus.FAILED);
      }
    }

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

  public List<AnalysisResDTO.ActionPlanDTO> getActionPlans(Long storeId, SwotType swotType) {
    // BatchSize 적용 (N+1 및 MultipleBagFetchException 문제 방지)
    Analysis analysis =
        analysisRepository
            .findByStoreId(storeId)
            .orElseThrow(() -> new GeneralException(ErrorCode.ANALYSIS_NOT_FOUND));

    // 1. 해당 분석의 모든 ActionPlan을 스트림으로 변환
    Stream<ActionPlan> planStream = analysis.getActionPlans().stream();

    // 2. 필터 타입이 들어온 경우 필터링 수행
    if (swotType != null) {
      // S를 넣으면 [SO, ST] 리스트가 나옴
      List<RelatedSwotType> targetTypes = RelatedSwotType.findAllByComponent(swotType);

      // ActionPlan의 swotType이 targetTypes에 포함되는 것만 남김
      planStream = planStream.filter(plan -> targetTypes.contains(plan.getRelatedSwot()));
    }

    return planStream.map(AnalysisResDTO.ActionPlanDTO::of).toList();
  }

  public AnalysisResDTO.ActionPlanDetailDTO getActionPlanDetail(Long actionPlanId) {
    // BatchSize 적용
    ActionPlan actionPlan =
        actionPlanRepository
            .findById(actionPlanId)
            .orElseThrow(() -> new GeneralException(ErrorCode.ACTION_PLAN_NOT_FOUND));

    List<AnalysisResDTO.ActionDetailDTO> details =
        actionPlan.getDetails().stream().map(AnalysisResDTO.ActionDetailDTO::of).toList();

    return AnalysisResDTO.ActionPlanDetailDTO.of(actionPlan, details);
  }

  // Utils

  /** pollingTime 계산 로직 */
  private int calculatePoolingTime(AnalysisStatus status) {
    return switch (status) {
      case REQUEST -> 2000; // 데이터 정제
      case SWOT_PROCESSING -> 5000; // SWOT 분석
      case ACTION_PLAN_PROCESSING -> 8000; // ActionPlan 생성
      case ACTION_DETAIL_PROCESSING -> 1500; // ActionDetail 생성
      case COMPLETED, FAILED -> 0; // 폴링 종료
    };
  }

  private String buildCallbackUrl(String path) {
    return UriComponentsBuilder.fromHttpUrl(env.getProperty("ai.callback.base-url"))
        .path(env.getProperty(path))
        .toUriString();
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
        .swotCallbackUrl(callbackBaseUrl + "/api/analysis/callback/swots")
        .actionPlanCallbackUrl(callbackBaseUrl + "/api/analysis/callback/action-plans")
        .actionDetailCallbackUrl(callbackBaseUrl + "/api/analysis/callback/action-details")
        .failCallbackUrl(callbackBaseUrl + "/api/analysis/callback/fail")

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
