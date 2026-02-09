package com.umc9th.bizscan.domain.region.service;

import com.umc9th.bizscan.domain.region.dto.HashtagDto;
import com.umc9th.bizscan.domain.region.dto.NaverKeywordResponse;
import com.umc9th.bizscan.domain.region.repository.RegionRepository;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RegionTrendService {

  private final RegionRepository regionRepository;

  @Value("${naver.ad.base-url}")
  private String baseUrl;

  @Value("${naver.ad.api-key}")
  private String apiKey;

  @Value("${naver.ad.secret-key}")
  private String secretKey;

  @Value("${naver.ad.customer-id}")
  private String customerId;

  public List<HashtagDto> recommendHashtags(String keyword) {

    // 1. API 호출
    String timestamp = String.valueOf(System.currentTimeMillis());
    String signature = generateSignature(timestamp, "GET", "/keywordstool", secretKey);

    WebClient webClient =
        WebClient.builder()
            .baseUrl(baseUrl)
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
            .build();

    NaverKeywordResponse response =
        webClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/keywordstool")
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

    // 2. 검색량 순으로 정렬
    return response.getKeywordList().stream()
        .map(
            result -> {
              long totalCount =
                  parseCount(result.getMonthlyPcQcCnt())
                      + parseCount(result.getMonthlyMobileQcCnt());
              // 결과에 '#' 붙여주기
              String tag = "#" + result.getRelKeyword().replaceAll(" ", "");
              return new HashtagDto(tag, totalCount);
            })
        .sorted((a, b) -> Long.compare(b.getSearchVolume(), a.getSearchVolume()))
        .limit(20)
        .collect(Collectors.toList());
  }

  private long parseCount(String count) {
    if (count == null || count.contains("<")) {
      return 0L;
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
