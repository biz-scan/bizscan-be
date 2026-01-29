package com.umc9th.bizscan.domain.region.service;

import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import com.umc9th.bizscan.domain.region.repository.RegionRepository;
import com.umc9th.bizscan.global.client.kakao.KakaoClient;
import com.umc9th.bizscan.global.client.kakao.dto.KakaoApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionService {

  private final RegionRepository regionRepository;
  private final KakaoClient kakaoClient;

  @Transactional
  public RegionMaster getOrFetchRegion(String address) {
    // 1. DB 조회
    RegionMaster region = regionRepository.findByFullAddress(address).orElse(null);

    // 2. DB 미존재 시 카카오 API 호출
    if (region == null) {
      log.info("⚠️ 신규 주소 감지. 카카오 API 조회 시도: {}", address);

      KakaoApiResponse response = kakaoClient.searchAddress(address);

      if (response == null
          || response.getDocuments() == null
          || response.getDocuments().isEmpty()) {
        throw new IllegalArgumentException("존재하지 않는 주소입니다: " + address);
      }

      // DTO 구조에 맞춰 데이터 추출 (Document -> Address)
      KakaoApiResponse.Address addressInfo = response.getDocuments().get(0).getAddress();

      // (1) 좌표 파싱
      double lat = Double.parseDouble(addressInfo.getY());
      double lon = Double.parseDouble(addressInfo.getX());

      // (2) 구 이름 추출
      String[] addressParts = addressInfo.getAddressName().split(" ");
      String guName = (addressParts.length > 1) ? addressParts[1] : "";

      // (3) 행정동 코드 처리
      String rawCode = addressInfo.getHCode();
      String mappedCode =
          (rawCode != null && rawCode.length() > 8) ? rawCode.substring(0, 8) : rawCode;

      // (4) 상권 매핑
      String borrowedTrdarCd = null;
      String borrowedTrdarName = null;

      var nearestWithData = regionRepository.findNearestRegionWithData(lat, lon).orElse(null);

      if (nearestWithData != null) {
        borrowedTrdarCd = nearestWithData.getTrdarCd();
        borrowedTrdarName = nearestWithData.getTrdarCdNm();
        log.info("🔗 [매핑 성공] 근처 데이터 보유 상권: {} ({})", borrowedTrdarName, borrowedTrdarCd);
      } else {
        log.warn("❌ [매핑 실패] 반경 내 데이터 보유 상권 없음.");
      }

      // 5. 엔티티 생성 및 저장
      region =
          RegionMaster.builder()
              .fullAddress(addressInfo.getAddressName())
              .guNm(guName)
              .adstrdCd(mappedCode)
              .adstrdNm(addressInfo.getRegion3DepthHName())
              .lat(lat)
              .lon(lon)
              .trdarCd(borrowedTrdarCd)
              .trdarCdNm(borrowedTrdarName)
              .build();

      regionRepository.save(region);
      log.info("✅ 지역 정보 DB 적재 완료: {}", region.getFullAddress());
    }

    return region;
  }
}
