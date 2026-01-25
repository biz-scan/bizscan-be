package com.umc9th.bizscan.domain.store.client;

import com.umc9th.bizscan.domain.store.client.dto.KakaoGeoResponse;
import com.umc9th.bizscan.domain.store.client.dto.KakaoGeoResponse.Document;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class KakaoGeoClient {

  private final WebClient webClient = WebClient.builder().baseUrl("https://dapi.kakao.com").build();

  @Value("${kakao.rest-api-key}")
  private String apiKey;

  /** 주소 → 위도/경도 변환 */
  public GeoPoint getCoordinates(String address) {
    KakaoGeoResponse response =
        webClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/v2/local/search/address.json")
                        .queryParam("query", address)
                        .build())
            .header("Authorization", "KakaoAK " + apiKey)
            .retrieve()
            .bodyToMono(KakaoGeoResponse.class)
            .block();

    if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
      return null;
    }

    Document doc = response.getDocuments().get(0);

    if (doc.getAddress() == null) {
      return null;
    }

    return new GeoPoint(
        new BigDecimal(doc.getAddress().getY()), // 위도
        new BigDecimal(doc.getAddress().getX()) // 경도
        );
  }

  /** Service에서 쓰는 좌표 객체 */
  public record GeoPoint(BigDecimal lat, BigDecimal lon) {}
}
