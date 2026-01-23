package com.umc9th.bizscan.domain.aiAnalysis.service;

import com.umc9th.bizscan.domain.aiAnalysis.dto.request.SwotAnalyzeRequest;
import com.umc9th.bizscan.domain.aiAnalysis.dto.response.FastApiSwotResponse;
import com.umc9th.bizscan.domain.aiAnalysis.dto.response.SwotResponse;
import com.umc9th.bizscan.domain.aiAnalysis.entity.AnalysisRequest;
import com.umc9th.bizscan.domain.aiAnalysis.entity.Swot;
import com.umc9th.bizscan.domain.aiAnalysis.enums.AnalysisStatus;
import com.umc9th.bizscan.domain.aiAnalysis.repository.AnalysisRequestRepository;
import com.umc9th.bizscan.domain.aiAnalysis.repository.SwotRepository;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SwotService {

    private final SwotRepository swotRepository;
    private final AnalysisRequestRepository analysisRequestRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public SwotResponse getLatestSwot(Long storeId) {
        Swot swot = swotRepository
                .findTopByStoreIdOrderByCreatedAtDesc(storeId)
                .orElseThrow(() ->
                        new GeneralException(ErrorCode.SWOT_NOT_FOUND)
                );
        return SwotResponse.from(swot);
    }

    @Transactional
    public SwotResponse generateSwot(Long storeId) {

        // 1. 분석 요청 생성 (상태 관리용)
        AnalysisRequest request = AnalysisRequest.builder()
                .storeId(storeId)
                .status(AnalysisStatus.REQUEST)
                .build();
        analysisRequestRepository.save(request);

        // 2. FastAPI 요청 DTO 생성
        SwotAnalyzeRequest aiRequest = SwotAnalyzeRequest.builder()
                .storeId(storeId)
                .build();

        // 3. FastAPI 호출
        FastApiSwotResponse aiResponse = callFastApi(aiRequest);

        // 4. SWOT 저장 (ERD 기준)
        Swot swot = Swot.builder()
                .storeId(storeId)
                .badge(aiResponse.getBadge())
                .sTitle(aiResponse.getSTitle())
                .sDetail(aiResponse.getSDetail())
                .wTitle(aiResponse.getWTitle())
                .wDetail(aiResponse.getWDetail())
                .oTitle(aiResponse.getOTitle())
                .oDetail(aiResponse.getODetail())
                .tTitle(aiResponse.getTTitle())
                .tDetail(aiResponse.getTDetail())
                .build();

        swotRepository.save(swot);

        // 5. 요청 상태 완료
        request.toDone();
        analysisRequestRepository.save(request);

        return SwotResponse.from(swot);
    }

    // FastAPI 호출 메서드
    public FastApiSwotResponse callFastApi(SwotAnalyzeRequest request) {
        String url = "http://localhost:8000/swot";

        return restTemplate.postForObject(
                url,
                request,
                FastApiSwotResponse.class
        );
    }
}
