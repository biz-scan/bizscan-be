package com.umc9th.bizscan.domain.aiAnalysis.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc9th.bizscan.domain.aiAnalysis.dto.request.AiAnalysisCallbackRequest;
import com.umc9th.bizscan.domain.aiAnalysis.dto.request.DiagnosisRequest;
import com.umc9th.bizscan.domain.aiAnalysis.dto.request.FastApiAnalysisRequest;
import com.umc9th.bizscan.domain.aiAnalysis.dto.response.*;
import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlan;
import com.umc9th.bizscan.domain.aiAnalysis.entity.Analysis;
import com.umc9th.bizscan.domain.aiAnalysis.entity.AnalysisRequest;
import com.umc9th.bizscan.domain.aiAnalysis.entity.Swot;
import com.umc9th.bizscan.domain.aiAnalysis.enums.ActionCategory;
import com.umc9th.bizscan.domain.aiAnalysis.enums.AnalysisStatus;
import com.umc9th.bizscan.domain.aiAnalysis.repository.ActionPlanRepository;
import com.umc9th.bizscan.domain.aiAnalysis.repository.AnalysisRepository;
import com.umc9th.bizscan.domain.aiAnalysis.repository.AnalysisRequestRepository;
import com.umc9th.bizscan.domain.aiAnalysis.repository.SwotRepository;
import com.umc9th.bizscan.domain.store.entity.Store;
import com.umc9th.bizscan.domain.store.repository.StoreRepository;
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
  private final ActionPlanRepository actionPlanRepository;
  private final AnalysisRequestRepository analysisRequestRepository;
  private final AnalysisRepository analysisRepository;
  private final ObjectMapper objectMapper;

  private final FastApiProperties fastApiProperties;
  private final RestTemplate restTemplate = new RestTemplate();

  /** AI 분석 요청 (프론트에서 최초 1회 호출) requestId 반환 → 프론트에서 폴링 */
  @Transactional
  public String analyzeStore(Long storeId) {

    // 0. 매장 조회 (Spring에서만 DB 접근)
    Store store =
        storeRepository
            .findById(storeId)
            .orElseThrow(() -> new GeneralException(ErrorCode.STORE_NOT_FOUND));

    // 1. requestId 생성
    String requestId = UUID.randomUUID().toString();

    // 추가: 분석 Entity 저장
      Analysis analysis = Analysis.builder()
              .store(store)
              .build();

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
    FastApiAnalysisRequest fastApiRequest = toFastApiRequest(store, requestId);

    // 4. FastAPI 호출 (응답 기다리지 않음)
//    try {
//      restTemplate.postForObject(
//          fastApiProperties.getBaseUrl() + fastApiProperties.getAnalysisPath(),
//          fastApiRequest,
//          Void.class);
//    } catch (Exception e) {
//      request.fail("AI 분석 요청 실패");
//      throw e;
//    }

    // 5. 즉시 requestId 반환
    return requestId;
  }

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
      // 추가: analysis에 catchphrase 존재
      Analysis analysis = analysisRepository.findByStoreId(storeId)
              .orElseThrow(() -> new GeneralException(ErrorCode.STORE_NOT_FOUND));

//    AnalysisRequest request =
//        analysisRequestRepository
//            .findTopByStoreIdOrderByCreatedAtDesc(storeId)
//            .orElseThrow(() -> new GeneralException(ErrorCode.ANALYSIS_REQUEST_NOT_FOUND));

    return new CatchphraseResponse(analysis.getCatchphrase());
  }

  /** 최신 SWOT 조회 (대시보드용) */
//  public Swot getLatestSwotEntity(Long storeId) {
//    return swotRepository
//        .findTopByStoreIdOrderByCreatedAtDesc(storeId)
//        .orElseThrow(() -> new GeneralException(ErrorCode.SWOT_NOT_FOUND));
//  }

  /** 정밀 진단 조회 -> url 수정 */
//  public DiagnosisResponse generateDiagnosis(DiagnosisRequest request) {
//    String url = fastApiProperties.getBaseUrl() + fastApiProperties.getDiagnosisPath();
//
//    return restTemplate.postForObject(url, request, DiagnosisResponse.class);
//  }

  /** 실행 전략 목록 조회 */
//  public ActionPlanListResponse getActionPlans(Long storeId) {
//
//    List<ActionPlan> plans = actionPlanRepository.findAllBySwot_StoreId(storeId);
//
//    if (plans.isEmpty()) {
//      return new ActionPlanListResponse(List.of());
//    }
//
//    List<ActionPlanListResponse.ActionPlanItem> items =
//        plans.stream()
//            .map(
//                plan -> {
//                  List<String> tags;
//                  try {
//                    tags =
//                        objectMapper.readValue(
//                            plan.getTags(), new TypeReference<List<String>>() {});
//                  } catch (Exception e) {
//                    tags = List.of();
//                  }
//
//                  return new ActionPlanListResponse.ActionPlanItem(
//                      plan.getId(), plan.getTitle(), tags);
//                })
//            .toList();
//
//    return new ActionPlanListResponse(items);
//  }

  /** 실행 전략 상세 조회 */
//  public ActionPlanDetailResponse getActionPlanDetail(Long solutionId) {
//
//    ActionPlan plan =
//        actionPlanRepository
//            .findById(solutionId)
//            .orElseThrow(() -> new GeneralException(ErrorCode.ACTION_PLAN_NOT_FOUND));
//
//    List<String> steps;
//    try {
//      steps = objectMapper.readValue(plan.getSteps(), new TypeReference<List<String>>() {});
//    } catch (Exception e) {
//      steps = List.of();
//    }
//
//    return new ActionPlanDetailResponse(
//        plan.getId(), plan.getTitle(), plan.getReason(), steps, false // 노트 추가 여부 연동 필요
//        );
//  }

  /** FastAPI 콜백 처리 */
//  @Transactional
//  public void completeAnalysis(AiAnalysisCallbackRequest callback) {
//
//    AnalysisRequest request =
//        analysisRequestRepository
//            .findByRequestId(callback.getRequestId())
//            .orElseThrow(() -> new GeneralException(ErrorCode.ANALYSIS_REQUEST_NOT_FOUND));
//
//    try {
//      // 1. SWOT 처리 시작
//      request.updateProgress("SWOT 분석 중입니다.");
//      request.updateStatus(AnalysisStatus.SWOT_PROCESSING);
//
//      FastApiAiAnalysisResponse result = callback.getResult();
//
//      Swot swot =
//          new Swot(
//              request.getStoreId(),
//              result.getSwot().getSTitle(),
//              result.getSwot().getSDetail(),
//              result.getSwot().getWTitle(),
//              result.getSwot().getWDetail(),
//              result.getSwot().getOTitle(),
//              result.getSwot().getODetail(),
//              result.getSwot().getTTitle(),
//              result.getSwot().getTDetail());
//      swotRepository.save(swot);
//
//      // 2. ActionPlan 처리 시작
//      request.updateProgress("실행 전략을 생성 중입니다.");
//      request.updateStatus(AnalysisStatus.ACTION_PLAN_PROCESSING);
//
//      for (FastApiAiAnalysisResponse.ActionPlanPart planDto : result.getActionPlans()) {
//
//        String tagsJson = objectMapper.writeValueAsString(planDto.getTags());
//
//        ActionPlan plan =
//            ActionPlan.builder()
//                .swot(swot)
//                .title(planDto.getTitle())
//                .category(ActionCategory.valueOf(planDto.getCategory()))
//                .tags(tagsJson)
//                .reason(planDto.getReason())
//                .build();
//
//        actionPlanRepository.save(plan);
//      }
//
//      // 3. ActionDetail 처리 시작
//      request.updateProgress("실행 전략 상세를 생성 중입니다.");
//      request.updateStatus(AnalysisStatus.ACTION_DETAIL_PROCESSING);
//
//      // 4. 완료
//      request.complete(result.getCatchphrase());
//
//    } catch (Exception e) {
//      request.fail("분석 중 오류가 발생했습니다.");
//      throw new RuntimeException(e);
//    }
//  }

  private FastApiAnalysisRequest toFastApiRequest(Store store, String requestId) {

    return FastApiAnalysisRequest.builder()
        // callback 식별용
        .requestId(requestId)

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
        .build();
  }
}
