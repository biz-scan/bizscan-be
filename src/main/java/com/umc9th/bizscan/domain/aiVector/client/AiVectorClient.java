package com.umc9th.bizscan.domain.aiVector.client;

import com.umc9th.bizscan.domain.aiVector.dto.StoreSwotIngestRequestDto;
import com.umc9th.bizscan.domain.aiVector.dto.VectorRecommendationDto;
import com.umc9th.bizscan.global.config.FastApiProperties;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiVectorClient {

  private final FastApiProperties fastApiProperties;
  private final RestTemplate restTemplate = new RestTemplate();

  /** [GET] 유사 매장 검색 요청 */
  public List<VectorRecommendationDto> getSimilarStores(Long storeId, String query) {
    try {
      String url =
          UriComponentsBuilder.fromHttpUrl(fastApiProperties.getBaseUrl())
              .path("/api/vector/recommend")
              .queryParam("storeId", storeId)
              .queryParam("queryText", query)
              .queryParam("topK", 20)
              .encode()
              .toUriString();

      log.info("Sending Request to Python: {}", url);

      ResponseEntity<VectorResponseWrapper> response =
          restTemplate.exchange(url, HttpMethod.GET, null, VectorResponseWrapper.class);

      if (response.getBody() != null && response.getBody().getResults() != null) {
        return response.getBody().getResults();
      }

      return Collections.emptyList();

    } catch (Exception e) {
      log.error("Vector Search Error: ", e);
      return Collections.emptyList();
    }
  }

  public String checkStoreDataInVectorDb(Long storeId) {
    try {
      String url =
          UriComponentsBuilder.fromHttpUrl(fastApiProperties.getBaseUrl())
              .path("/api/vector/check/" + storeId)
              .toUriString();

      ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
      return response.getBody();
    } catch (Exception e) {
      return "확인 실패: " + e.getMessage();
    }
  }

  /** [POST] SWOT 분석 데이터 적재 요청 (마이그레이션용) */
  public void ingestSwotData(StoreSwotIngestRequestDto requestDto) {
    try {
      String url =
          UriComponentsBuilder.fromHttpUrl(fastApiProperties.getBaseUrl())
              .path("/api/vector/ingest")
              .toUriString();

      // 헤더 설정 (JSON 명시)
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      // 요청 엔티티 생성
      HttpEntity<StoreSwotIngestRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);

      log.info("Sending Ingest Request to Python for StoreId: {}", requestDto.getStoreId());

      restTemplate.postForEntity(url, requestEntity, String.class);

    } catch (Exception e) {
      log.error("Vector Ingest Error for StoreId {}: ", requestDto.getStoreId(), e);
    }
  }

  @Getter
  @NoArgsConstructor
  static class VectorResponseWrapper {
    private List<VectorRecommendationDto> results;
  }
}
