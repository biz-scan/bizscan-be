package com.umc9th.bizscan.domain.commercial.repository;

import com.umc9th.bizscan.domain.commercial.entity.SalesEstimate;
import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesRepository extends JpaRepository<SalesEstimate, Long> {
  Optional<SalesEstimate> findTopByRegionMasterOrderByStdQuarterDesc(RegionMaster regionMaster);
}
