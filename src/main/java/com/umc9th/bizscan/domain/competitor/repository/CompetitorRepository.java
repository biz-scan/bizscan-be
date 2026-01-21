package com.umc9th.bizscan.domain.competitor.repository;

import com.umc9th.bizscan.domain.competitor.entity.CompetitorStore;
import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitorRepository extends JpaRepository<CompetitorStore, Long> {
  List<CompetitorStore> findByRegionMaster(RegionMaster regionMaster);
}
