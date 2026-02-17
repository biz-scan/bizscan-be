package com.umc9th.bizscan.domain.aiVector.client;

import com.umc9th.bizscan.domain.aiVector.dto.VectorRecommendationDto;
import com.umc9th.bizscan.global.config.FastApiProperties;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
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

  public List<VectorRecommendationDto> getSimilarStores(Long storeId, String query) {
    try {
      String url =
          UriComponentsBuilder.fromHttpUrl(fastApiProperties.getBaseUrl())
              .path("/api/vector/recommend")
              .queryParam("storeId", storeId)
              .queryParam("queryText", query)
              .queryParam("topK", 4)
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

  @Getter
  @NoArgsConstructor
  static class VectorResponseWrapper {
    private List<VectorRecommendationDto> results;
  }
}
