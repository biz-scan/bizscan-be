package com.umc9th.bizscan.domain.region.infrastructure;

import com.umc9th.bizscan.domain.region.dto.KakaoApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class KakaoApiClient {

  private final RestTemplate restTemplate; // Bean 등록 필요

  @Value("${kakao.api.key}")
  private String apiKey;

  @Value("${kakao.api.url}")
  private String apiUrl;

  public KakaoApiResponseDto.Document.Address searchAddress(String address) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "KakaoAK " + apiKey); // 헤더 설정 필수

    HttpEntity<String> entity = new HttpEntity<>(headers);
    String url = apiUrl + "?query=" + address;

    ResponseEntity<KakaoApiResponseDto> response =
        restTemplate.exchange(url, HttpMethod.GET, entity, KakaoApiResponseDto.class);

    // 결과 없으면 null 반환, 있으면 첫 번째 결과의 주소 정보 반환
    if (response.getBody() != null && !response.getBody().getDocuments().isEmpty()) {
      return response.getBody().getDocuments().get(0).getAddress();
    }
    return null;
  }
}
