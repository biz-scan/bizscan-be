package com.umc9th.bizscan.domain.region.service;


import com.umc9th.bizscan.domain.region.dto.HashtagDto;
import com.umc9th.bizscan.domain.region.dto.NaverKeywordResponse;
import com.umc9th.bizscan.domain.region.dto.NaverKeywordResponse.KeywordResult;
import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import com.umc9th.bizscan.domain.region.entity.RegionTrend;
import com.umc9th.bizscan.domain.region.repository.RegionRepository;
import com.umc9th.bizscan.domain.region.repository.RegionTrendRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RegionTrendService {

    private final RegionRepository regionRepository;
    private final RegionTrendRepository regionTrendRepository;

    @Value("${naver.ad.base-url}")
    private String baseUrl;

    @Value("${naver.ad.api-key}")
    private String apiKey;

    @Value("${naver.ad.secret-key}")
    private String secretKey;

    @Value("${naver.ad.customer-id}")
    private String customerId;

    // 특정 지역(Region)에 대한 키워드 검색량 수집
    public void collectSearchTrend(Long regionId, String searchKeyword) {

        // 1. 부모 지역 찾기
        RegionMaster region = regionRepository.findById(regionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 지역이 없습니다."));

        // 2. API 호출을 위한 서명 및 헤더 준비
        String timestamp = String.valueOf(System.currentTimeMillis());
        String method = "GET";
        String resource = "/keywordstool";
        String signature = generateSignature(timestamp, method, resource, secretKey);

        // 3. WebClient로 네이버 API 호출
        // WebClient 생성 시 메모리 한도 늘리기 (10MB)
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();

        NaverKeywordResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder.path(resource)
                        .queryParam("hintKeywords", searchKeyword) // 예: "성수동 맛집"
                        .queryParam("showDetail", "1")
                        .build())
                .header("X-Timestamp", timestamp)
                .header("X-API-KEY", apiKey)
                .header("X-Customer", customerId)
                .header("X-Signature", signature)
                .retrieve()
                .bodyToMono(NaverKeywordResponse.class)
                .block(); // 동기 처리 (데이터 수집이니까 기다림)

        if (response == null || response.getKeywordList() == null) {
            log.warn("네이버 API 응답이 비어있습니다.");
            return;
        }

        // 4. DB 저장 (상위 5개만 예시로 저장)
        List<KeywordResult> results = response.getKeywordList();
        int rank = 1;

        for (KeywordResult result : results) {
            if (rank > 10) break; // 상위 10개만 저장

            long pcCount = parseCount(result.getMonthlyPcQcCnt());
            long mobileCount = parseCount(result.getMonthlyMobileQcCnt());
            long totalCount = pcCount + mobileCount;

            RegionTrend trend = RegionTrend.builder()
                    .regionMaster(region)
                    .stdDate(LocalDate.now())
                    .keyword(result.getRelKeyword())
                    .searchVol(totalCount)
                    .rank((long) rank++)
                    .build();

            regionTrendRepository.save(trend);
        }

        log.info("키워드 '{}' 관련 트렌드 데이터 {}건 저장 완료", searchKeyword, rank - 1);
    }

    // SNS 해시태그 추천용 (DB 저장 X, 즉시 리턴)
    public List<HashtagDto> recommendHashtags(String keyword) {

        // 1. API 호출 (기존 로직 동일)
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = generateSignature(timestamp, "GET", "/keywordstool", secretKey);

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();

        NaverKeywordResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/keywordstool")
                        .queryParam("hintKeywords", keyword)
                        .queryParam("showDetail", "1")
                        .build())
                .header("X-Timestamp", timestamp)
                .header("X-API-KEY", apiKey)
                .header("X-Customer", customerId)
                .header("X-Signature", signature)
                .retrieve()
                .bodyToMono(NaverKeywordResponse.class)
                .block();

        if (response == null || response.getKeywordList() == null) {
            return Collections.emptyList();
        }

        // 2. 검색량 순으로 정렬 + # 붙여서 리턴
        return response.getKeywordList().stream()
                .map(result -> {
                    long totalCount = parseCount(result.getMonthlyPcQcCnt())
                            + parseCount(result.getMonthlyMobileQcCnt());
                    // 결과에 '#' 붙여주기
                    String tag = "#" + result.getRelKeyword().replaceAll(" ", "");
                    return new HashtagDto(tag, totalCount);
                })
                .sorted((a, b) -> Long.compare(b.getSearchVolume(), a.getSearchVolume())) // 검색량 내림차순 정렬
                .limit(20) // 상위 20개만 추천
                .collect(Collectors.toList());
    }




    // [유틸] "< 10" 같은 문자열을 숫자로 변환
    private long parseCount(String count) {
        if (count == null || count.contains("<")) {
            return 0L; // 10 미만은 0으로 처리 (또는 5로 처리해도 됨)
        }
        try {
            return Long.parseLong(count);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    // [보안] 네이버 API 서명 생성 (HMAC-SHA256)
    private String generateSignature(String timestamp, String method, String resource, String key) {
        try {
            String message = timestamp + "." + method + "." + resource;
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            return Base64.getEncoder().encodeToString(mac.doFinal(message.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new RuntimeException("API 서명 생성 실패", e);
        }
    }
}
