package com.umc9th.bizscan.domain.aiAnalysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc9th.bizscan.domain.aiAnalysis.dto.request.FastApiAnalysisRequest;
import com.umc9th.bizscan.domain.aiAnalysis.dto.response.*;
import com.umc9th.bizscan.domain.aiAnalysis.entity.Analysis;
import com.umc9th.bizscan.domain.aiAnalysis.entity.AnalysisRequest;
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
    FastApiAnalysisRequest fastApiRequest = toFastApiRequest(store, requestId);

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
    Analysis analysis =
        analysisRepository
            .findByStoreId(storeId)
            .orElseThrow(() -> new GeneralException(ErrorCode.STORE_NOT_FOUND));

    //    AnalysisRequest request =
    //        analysisRequestRepository
    //            .findTopByStoreIdOrderByCreatedAtDesc(storeId)
    //            .orElseThrow(() -> new GeneralException(ErrorCode.ANALYSIS_REQUEST_NOT_FOUND));

    return new CatchphraseResponse(analysis.getCatchphrase());
  }

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
