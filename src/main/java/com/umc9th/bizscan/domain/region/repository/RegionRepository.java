package com.umc9th.bizscan.domain.region.repository;

import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<RegionMaster, Long> {
    // CSV의 상권코드로 부모 엔티티를 찾기 위해 필수
    Optional<RegionMaster> findByTrdarCd(String trdarCd);
}
