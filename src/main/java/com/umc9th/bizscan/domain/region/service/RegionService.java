package com.umc9th.bizscan.domain.region.service;

import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import com.umc9th.bizscan.domain.region.infrastructure.KakaoApiClient;
import com.umc9th.bizscan.domain.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j // 로그 확인을 위해 추가 추천
@Service
@RequiredArgsConstructor
public class RegionService {

  private final RegionRepository regionRepository;
  private final KakaoApiClient kakaoApiClient;

  @Transactional
  public RegionMaster getOrFetchRegion(String address) {
    // 1. DB에 이미 저장된 주소인지 확인
    RegionMaster region = regionRepository.findByFullAddress(address).orElse(null);

    // 2. DB 미존재 시 카카오 API 호출 및 신규 등록
    if (region == null) {
      log.info("⚠️ 신규 주소 감지. 카카오 API 조회 시도: {}", address);

      var kakaoData = kakaoApiClient.searchAddress(address);
      if (kakaoData == null) {
        throw new IllegalArgumentException("존재하지 않는 주소입니다: " + address);
      }

      // (1) 좌표 파싱
      double lat = Double.parseDouble(kakaoData.getY());
      double lon = Double.parseDouble(kakaoData.getX());

      // (2) 구 이름 추출
      String[] addressParts = kakaoData.getAddressName().split(" ");
      String guName = (addressParts.length > 1) ? addressParts[1] : "";

      // (3) 행정동 코드 처리
      String rawCode = kakaoData.getHCode();
      String mappedCode =
          (rawCode != null && rawCode.length() > 8) ? rawCode.substring(0, 8) : rawCode;

      // (4) ✨ [핵심 변경] 데이터가 존재하는 가장 가까운 상권 매핑
      String borrowedTrdarCd = null;
      String borrowedTrdarName = null;

      // 🚨 변경점: findNearestCommercialRegion -> findNearestRegionWithData
      var nearestWithData = regionRepository.findNearestRegionWithData(lat, lon).orElse(null);

      if (nearestWithData != null) {
        borrowedTrdarCd = nearestWithData.getTrdarCd();
        borrowedTrdarName = nearestWithData.getTrdarCdNm();
        log.info("🔗 [매핑 성공] 입력주소 근처 데이터 보유 상권 발견: {} ({})", borrowedTrdarName, borrowedTrdarCd);
      } else {
        log.warn("❌ [매핑 실패] 반경 3km 내에 분석 데이터가 있는 상권이 없습니다. (기본값 처리 필요)");
        // 필요하다면 여기서 기본 상권(왕십리 등)을 강제로 넣을 수도 있음.
      }

      // 5. 엔티티 생성 및 저장
      region =
          RegionMaster.builder()
              .fullAddress(kakaoData.getAddressName())
              .guNm(guName)
              .adstrdCd(mappedCode)
              .adstrdNm(kakaoData.getRegion3DepthHName())
              .lat(lat)
              .lon(lon)
              .trdarCd(borrowedTrdarCd) // 찾아낸 '데이터 있는 상권' 코드 저장
              .trdarCdNm(borrowedTrdarName) // 상권 이름 저장
              .build();

      regionRepository.save(region);
      log.info("✅ 지역 정보 DB 적재 완료: {}", region.getFullAddress());
    }

    return region;
  }
}
