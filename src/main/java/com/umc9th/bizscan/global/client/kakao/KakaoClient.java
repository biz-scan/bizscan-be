package com.umc9th.bizscan.global.client.kakao;

import com.umc9th.bizscan.global.client.kakao.dto.KakaoApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class KakaoClient {

  private final WebClient webClient = WebClient.builder().baseUrl("https://dapi.kakao.com").build();

  @Value("${kakao.api.key}")
  private String apiKey;

  public KakaoApiResponse searchAddress(String address) {
    return webClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/v2/local/search/address.json")
                    .queryParam("query", address)
                    .build())
        .header("Authorization", "KakaoAK " + apiKey)
        .retrieve()
        .bodyToMono(KakaoApiResponse.class)
        .block();
  }
}
