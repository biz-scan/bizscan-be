package com.umc9th.bizscan.domain.hinterland.repository;

import com.umc9th.bizscan.domain.hinterland.entity.HousingStat;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HousingRepository extends JpaRepository<HousingStat, Long> {
  // 권역 이름으로 주거 통계 찾기
  List<HousingStat> findByZoneNm(String zoneNm);

  Optional<HousingStat> findFirstByZoneNmOrderByStdDateDesc(String zoneNm);
}
