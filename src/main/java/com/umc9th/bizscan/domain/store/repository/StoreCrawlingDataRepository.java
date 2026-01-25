package com.umc9th.bizscan.domain.store.repository;

import com.umc9th.bizscan.domain.store.entity.StoreCrawlingData;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreCrawlingDataRepository extends JpaRepository<StoreCrawlingData, Long> {

  // 네이버 플레이스 ID로 저장된 크롤링 데이터 조회
  Optional<StoreCrawlingData> findByPlaceId(String placeId);

  // 이미 수집된 데이터인지 확인용
  boolean existsByPlaceId(String placeId);
}
