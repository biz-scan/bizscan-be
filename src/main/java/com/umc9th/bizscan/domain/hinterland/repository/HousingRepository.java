package com.umc9th.bizscan.domain.hinterland.repository;

import com.umc9th.bizscan.domain.hinterland.entity.HousingStat;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HousingRepository extends JpaRepository<HousingStat, Long> {
  List<HousingStat> findByZoneNm(String zoneNm);

  Optional<HousingStat> findFirstByZoneNmOrderByStdDateDesc(String zoneNm);
}
